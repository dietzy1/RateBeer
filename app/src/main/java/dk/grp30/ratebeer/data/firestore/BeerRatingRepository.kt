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

data class BeerRating(
    val id: String = "",
    val beerId: String = "",
    val groupId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val timestamp: Long = 0
)

data class GroupBeerRating(
    val id: String = "",
    val beerId: String = "",
    val groupId: String = "",
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val userRatings: List<BeerRating> = emptyList(),
    val timestamp: Long = 0
)

sealed class RatingResult {
    data class Success(val rating: GroupBeerRating) : RatingResult()
    data class Error(val message: String) : RatingResult()
}

@Singleton
class BeerRatingRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val ratingsCollection = db.collection("beer_ratings")
    
    suspend fun submitRating(groupId: String, beerId: String, rating: Int): RatingResult {
        val currentUser = auth.currentUser ?: return RatingResult.Error("User not logged in")
        
        try {
            val querySnapshot = ratingsCollection
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("beerId", beerId)
                .get()
                .await()
            
            val userRating = BeerRating(
                beerId = beerId,
                groupId = groupId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "User",
                rating = rating,
                timestamp = System.currentTimeMillis()
            )
            
            if (querySnapshot.isEmpty) {
                val groupRating = GroupBeerRating(
                    beerId = beerId,
                    groupId = groupId,
                    averageRating = rating.toDouble(),
                    ratingCount = 1,
                    userRatings = listOf(userRating),
                    timestamp = System.currentTimeMillis()
                )
                
                val documentRef = ratingsCollection.document()
                val groupRatingWithId = groupRating.copy(id = documentRef.id)
                
                documentRef.set(groupRatingWithId).await()
                
                return RatingResult.Success(groupRatingWithId)
            } else {
                val groupRatingDoc = querySnapshot.documents.first()
                val groupRating = groupRatingDoc.toObject<GroupBeerRating>() 
                    ?: return RatingResult.Error("Invalid group rating data")
                
                val existingRatingIndex = groupRating.userRatings.indexOfFirst { it.userId == currentUser.uid }
                
                if (existingRatingIndex >= 0) {
                    val updatedUserRatings = groupRating.userRatings.toMutableList()
                    updatedUserRatings[existingRatingIndex] = userRating
                    
                    val totalRating = updatedUserRatings.sumOf { it.rating }
                    val newAverage = totalRating.toDouble() / updatedUserRatings.size
                    
                    groupRatingDoc.reference.update(
                        mapOf(
                            "userRatings" to updatedUserRatings,
                            "averageRating" to newAverage
                        )
                    ).await()
                } else {
                    val newRatingCount = groupRating.ratingCount + 1
                    val totalRating = (groupRating.averageRating * groupRating.ratingCount) + rating
                    val newAverage = totalRating / newRatingCount
                    
                    groupRatingDoc.reference.update(
                        mapOf(
                            "userRatings" to FieldValue.arrayUnion(userRating),
                            "averageRating" to newAverage,
                            "ratingCount" to newRatingCount
                        )
                    ).await()
                }
                
                val updatedDoc = groupRatingDoc.reference.get().await()
                val updatedGroupRating = updatedDoc.toObject<GroupBeerRating>()
                    ?: return RatingResult.Error("Failed to get updated group rating")
                
                return RatingResult.Success(updatedGroupRating)
            }
        } catch (e: Exception) {
            return RatingResult.Error("Failed to submit rating: ${e.message}")
        }
    }
    
    suspend fun getGroupRating(groupId: String, beerId: String): RatingResult {
        try {
            val querySnapshot = ratingsCollection
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("beerId", beerId)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                return RatingResult.Error("Rating not found")
            }
            
            val groupRatingDoc = querySnapshot.documents.first()
            val groupRating = groupRatingDoc.toObject<GroupBeerRating>()
                ?: return RatingResult.Error("Invalid group rating data")
            
            return RatingResult.Success(groupRating)
        } catch (e: Exception) {
            return RatingResult.Error("Failed to get group rating: ${e.message}")
        }
    }
    
    fun observeGroupRating(groupId: String, beerId: String): Flow<GroupBeerRating?> = callbackFlow {
        val query = ratingsCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("beerId", beerId)
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            
            if (snapshot != null && !snapshot.isEmpty) {
                val groupRating = snapshot.documents.first().toObject<GroupBeerRating>()
                trySend(groupRating)
            } else {
                trySend(null)
            }
        }
        
        awaitClose { subscription.remove() }
    }
    
    suspend fun getGlobalAverageForBeer(beerId: String): Double? {
        return try {
            val ratingsSnapshot = ratingsCollection
                .whereEqualTo("beerId", beerId)
                .get()
                .await()
            val allRatings = ratingsSnapshot.documents.mapNotNull { it.toObject(GroupBeerRating::class.java) }
            val allUserRatings = allRatings.flatMap { it.userRatings }
            if (allUserRatings.isNotEmpty()) allUserRatings.map { it.rating }.average() else null
        } catch (e: Exception) {
            null
        }
    }
} 