package torille.fi.lurkforreddit.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to get stuff from sharedpreferences
 */
@Singleton
class Store @Inject
constructor(context: Context) {

    private val mSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    var token: String
        get() = mSharedPreferences.getString(TOKEN, "")
        set(token) = mSharedPreferences.edit().putString(TOKEN, token).apply()

    var refreshToken: String
        get() = mSharedPreferences.getString(REFRESH_TOKEN, "")
        set(token) = mSharedPreferences.edit().putString(REFRESH_TOKEN, token).apply()

    val isLoggedIn: Boolean
        get() = mSharedPreferences.getBoolean(LOGGED_IN, false)

    fun loggedIn(loggedIn: Boolean?) {
        mSharedPreferences.edit().putBoolean(LOGGED_IN, loggedIn!!).apply()
    }

    companion object {

        private val TOKEN = "token"
        private val REFRESH_TOKEN = "refreshtoken"
        private val LOGGED_IN = "logged_in"
    }
}
