package torille.fi.lurkforreddit.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to get values from SharedPreferences
 */
@Singleton
class Store @Inject
constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    var token: String
        get() = sharedPreferences.getString(TOKEN, "")
        set(token) = sharedPreferences.edit().putString(TOKEN, token).apply()

    var refreshToken: String
        get() = sharedPreferences.getString(REFRESH_TOKEN, "")
        set(token) = sharedPreferences.edit().putString(REFRESH_TOKEN, token).apply()

    val isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(LOGGED_IN, false)

    fun loggedIn(loggedIn: Boolean?) {
        sharedPreferences.edit().putBoolean(LOGGED_IN, loggedIn!!).apply()
    }

    companion object {

        private val TOKEN = "token"
        private val REFRESH_TOKEN = "refreshtoken"
        private val LOGGED_IN = "logged_in"
    }
}
