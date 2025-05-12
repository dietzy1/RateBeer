package dk.grp30.ratebeer.di

import dk.grp30.ratebeer.data.auth.AuthRepository

/**
 * Simple manual dependency injection for authentication related dependencies
 */
object AuthModule {
    private var instance: AuthRepository? = null
    
    /**
     * Get the AuthRepository instance, creating it if necessary
     */
    fun provideAuthRepository(): AuthRepository {
        return instance ?: synchronized(this) {
            instance ?: AuthRepository().also { instance = it }
        }
    }
}