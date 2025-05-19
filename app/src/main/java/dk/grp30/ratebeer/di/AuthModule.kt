package dk.grp30.ratebeer.di

import dk.grp30.ratebeer.data.auth.AuthRepository

object AuthModule {
    private var instance: AuthRepository? = null

    fun provideAuthRepository(): AuthRepository {
        return instance ?: synchronized(this) {
            instance ?: AuthRepository().also { instance = it }
        }
    }
}