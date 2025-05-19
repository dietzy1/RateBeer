package dk.grp30.ratebeer.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class GroupMember(
    val id: String = "",
    val name: String = "",
    val isHost: Boolean = false
)

data class Group(
    val id: String = "",
    val groupCode: String = "",
    val hostId: String = "",
    val members: List<GroupMember> = emptyList(),
    val createdAt: Long = 0,
    val isActive: Boolean = true
)

sealed class GroupResult {
    data class Success(val group: Group) : GroupResult()
    data class Error(val message: String) : GroupResult()
}

@Singleton
class GroupRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val groupsCollection = db.collection("groups")
    
    suspend fun createGroup(groupCode: String): GroupResult {
        val currentUser = auth.currentUser ?: return GroupResult.Error("User not logged in")
        
        try {
            val group = Group(
                groupCode = groupCode,
                hostId = currentUser.uid,
                createdAt = System.currentTimeMillis(),
                members = listOf(
                    GroupMember(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "Host",
                        isHost = true
                    )
                )
            )
            
            val documentRef = groupsCollection.document()
            val groupWithId = group.copy(id = documentRef.id)
            
            documentRef.set(groupWithId).await()
            
            return GroupResult.Success(groupWithId)
        } catch (e: Exception) {
            return GroupResult.Error("Failed to create group: ${e.message}")
        }
    }
    
    suspend fun joinGroup(groupCode: String): GroupResult {
        val currentUser = auth.currentUser ?: return GroupResult.Error("User not logged in")
        
        try {
            val querySnapshot = groupsCollection
                .whereEqualTo("groupCode", groupCode)
                .whereEqualTo("active", true)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                return GroupResult.Error("Group not found")
            }
            
            val groupDoc = querySnapshot.documents.first()
            val group = groupDoc.toObject<Group>() ?: return GroupResult.Error("Invalid group data")
            
            if (group.members.any { it.id == currentUser.uid }) {
                return GroupResult.Success(group)
            }
            
            val newMember = GroupMember(
                id = currentUser.uid,
                name = currentUser.displayName ?: "User",
                isHost = false
            )
            
            groupDoc.reference.update("members", FieldValue.arrayUnion(newMember)).await()
            
            val updatedGroupDoc = groupDoc.reference.get().await()
            val updatedGroup = updatedGroupDoc.toObject<Group>() 
                ?: return GroupResult.Error("Failed to get updated group")
            
            return GroupResult.Success(updatedGroup)
        } catch (e: Exception) {
            return GroupResult.Error("Failed to join group: ${e.message}")
        }
    }
    
    fun observeGroup(groupId: String): Flow<Group?> = callbackFlow {
        val subscription = groupsCollection.document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                val group = snapshot?.toObject<Group>()
                trySend(group)
            }
        
        awaitClose { subscription.remove() }
    }
} 