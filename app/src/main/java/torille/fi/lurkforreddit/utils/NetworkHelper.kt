package torille.fi.lurkforreddit.utils

import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditService
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom
import java.util.UUID.randomUUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for creating auth calls
 */
@Singleton
class NetworkHelper @Inject
constructor(
    private val store: Store,
    private val authApi: RedditService.Auth
) {

    @Throws(IOException::class)
    fun authenticateApp(): String {

        return if (store.isLoggedIn) {
            Timber.d("Was logged in as user, refreshing token")
            Timber.d("Using refreshtoken: " + store.refreshToken)
            authApi.refreshUserToken("refresh_token", store.refreshToken).map { (accessToken) ->
                Timber.d("New token: $accessToken")
                store.token = accessToken
                accessToken
            }.doOnError { Timber.e(it) }.blockingSingle()

        } else {
            Timber.d("User was not logged in")
            getToken()
        }
    }

    @Throws(IOException::class)
    fun getToken(): String {
        val UUID = createUUID()
        val grant_type = "https://oauth.reddit.com/grants/installed_client"
        Timber.d("Getting token")
        return authApi.getAuthToken(grant_type, UUID).map { (access_token) ->
            Timber.d("Got new token $access_token")
            store.token = access_token
            access_token
        }.doOnError { Timber.e(it) }.blockingSingle()
    }

    companion object {

        private fun createUUID(): String {
            return randomUUID().toString()
        }

        fun nextStateId(): String {
            val random = SecureRandom()
            return BigInteger(130, random).toString(32)
        }
    }


}
