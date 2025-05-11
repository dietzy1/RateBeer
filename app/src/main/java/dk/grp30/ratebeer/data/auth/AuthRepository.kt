package dk.grp30.ratebeer.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
        
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null
    
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Login failed: unknown error")
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }
    
    suspend fun register(email: String, password: String, username: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                
                it.updateProfile(profileUpdates).await()
                AuthResult.Success(it)
            } ?: AuthResult.Error("Registration failed: unknown error")
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }
    
    fun logout() {
        auth.signOut()
    }
} 