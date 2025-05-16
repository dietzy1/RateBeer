package dk.grp30.ratebeer.data.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class Participant(
    val userId: String = "",
    val userName: String = "",
)

data class Group(
    @DocumentId val id: String = "",
    val pinCode: String = "",
    val hostId: String = "",
    var status: String = "lobby",
    var currentBeerId: String? = null,
    var currentBeerName: String? = null,
    val participants: List<Participant> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null,
    val isActive: Boolean = true
)

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}


@Singleton
class GroupRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val GROUPS_COLLECTION = "groups"
    }
    private val groupsCollection = db.collection(GROUPS_COLLECTION)

    suspend fun createGroup(hostNameFromUI: String?): DataResult<Group> {
        val currentUser = auth.currentUser ?: return DataResult.Error("User not logged in")
        val pin = (100000..999999).random().toString()

        try {
            val group = Group(
                pinCode = pin,
                hostId = currentUser.uid,
                status = "lobby",
                participants = listOf(
                    Participant(
                        userId = currentUser.uid,
                        userName = hostNameFromUI ?: currentUser.displayName ?: "Host"
                    )
                ),
                isActive = true
            )

            val documentRef = groupsCollection.document()
            val groupWithId = group.copy(id = documentRef.id) // Set the auto-generated ID

            documentRef.set(groupWithId).await()

            // For the client to have the createdAt immediately after creation without another read,
            // it's tricky. @ServerTimestamp is populated by the server.
            // The groupWithId returned here will have createdAt = null.
            // The observer (observeGroup) will pick up the server-populated value shortly after.
            // If you absolutely need it immediately, you'd have to do a get() after set(),
            // or approximate with client time and accept potential small discrepancies.
            // For most cases, relying on the observer is fine.
            return DataResult.Success(groupWithId)
        } catch (e: Exception) {
            return DataResult.Error("Failed to create group: ${e.message}")
        }
    }

    suspend fun joinGroup(pinCodeToJoin: String, userNameFromUI: String?): DataResult<Group> {
        val currentUser = auth.currentUser ?: return DataResult.Error("User not logged in")

        try {
            val querySnapshot = groupsCollection
                .whereEqualTo("pinCode", pinCodeToJoin)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return DataResult.Error("Group with PIN $pinCodeToJoin not found or is inactive.")
            }

            val groupDocRef = querySnapshot.documents.first().reference
            lateinit var finalGroupState: Group

            db.runTransaction { transaction ->
                val snapshot = transaction.get(groupDocRef)
                val group = snapshot.toObject(Group::class.java)
                    ?: throw FirebaseFirestoreException("Failed to parse group data during join.", FirebaseFirestoreException.Code.DATA_LOSS)

                if (group.participants.any { it.userId == currentUser.uid }) {
                    finalGroupState = group
                    return@runTransaction
                }

                val newParticipant = Participant(
                    userId = currentUser.uid,
                    userName = userNameFromUI ?: currentUser.displayName ?: "User"
                )

                transaction.update(groupDocRef, "participants", FieldValue.arrayUnion(newParticipant))
                finalGroupState = group.copy(participants = group.participants + newParticipant)

            }.await()

            val updatedDocSnapshot = groupDocRef.get().await()
            val fullyUpdatedGroup = updatedDocSnapshot.toObject(Group::class.java)
                ?: return DataResult.Error("Failed to get updated group after join.")

            return DataResult.Success(fullyUpdatedGroup)

        } catch (e: Exception) {
            return DataResult.Error("Failed to join group: ${e.message}")
        }
    }

    fun observeGroup(groupId: String): Flow<Group?> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(null).isSuccess
            close(IllegalArgumentException("Group ID cannot be blank for observation."))
            return@callbackFlow
        }
        val docRef = groupsCollection.document(groupId)
        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ObserveGroup", "Listen failed.", error)
                trySend(null).isSuccess
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(Group::class.java)).isSuccess
            } else {
                trySend(null).isSuccess
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun setTastingBeer(groupId: String, beerId: String, beerName: String): DataResult<Unit> {
        return try {
            groupsCollection.document(groupId).update(
                mapOf(
                    "currentBeerId" to beerId,
                    "currentBeerName" to beerName,
                    "status" to "tasting"
                )
            ).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error("Failed to set tasting beer: ${e.message}")
        }
    }

    suspend fun moveToResults(groupId: String): DataResult<Unit> {
        return try {
            groupsCollection.document(groupId).update("status", "results").await()
            DataResult.Success(Unit)
        } catch (e:Exception) {
            DataResult.Error("Failed to move to results: ${e.message}")
        }
    }

    suspend fun leaveGroup(groupId: String, userIdToLeave: String): DataResult<Unit> {
        if (userIdToLeave.isBlank()) return DataResult.Error("User ID not provided for leaving group.")
        try {
            val groupDocRef = groupsCollection.document(groupId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(groupDocRef)
                val group = snapshot.toObject(Group::class.java)
                    ?: throw FirebaseFirestoreException("Group not found during leave operation.", FirebaseFirestoreException.Code.NOT_FOUND)

                val updatedParticipants = group.participants.filterNot { it.userId == userIdToLeave }

                if (updatedParticipants.isEmpty()) {
                    transaction.delete(groupDocRef)
                } else if (group.hostId == userIdToLeave) {
                    val newHostId = updatedParticipants.first().userId
                    transaction.update(groupDocRef, mapOf(
                        "participants" to updatedParticipants,
                        "hostId" to newHostId
                    ))
                } else {
                    transaction.update(groupDocRef, "participants", updatedParticipants)
                }
                null
            }.await()
            return DataResult.Success(Unit)
        } catch (e: Exception) {
            return DataResult.Error("Failed to leave group: ${e.message}")
        }
    }
}