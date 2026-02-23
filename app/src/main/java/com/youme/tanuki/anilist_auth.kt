package com.youme.tanuki

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException

class AniListTokenManager(private val context: Context) {
    private val encryptedPrefs by lazy { createEncryptedSharedPrefs() }

    @Throws(GeneralSecurityException::class)
    private fun createEncryptedSharedPrefs(): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "ANILIST_AUTH_PREFS",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
    fun saveToken(accessToken: String) {
        encryptedPrefs.edit().apply {
            putString("access_token", accessToken)
            putLong("token_created_at", System.currentTimeMillis())
        }.apply()
    }
    fun getAccessToken(): String? {
        return encryptedPrefs.getString("access_token", null)
    }
    fun getTokenCreationTime(): Long {
        return encryptedPrefs.getLong("token_created_at", 0)
    }
    fun isTokenExpired(expirationTimeInMillis: Long = TOKEN_EXPIRATION_TIME): Boolean {
        val creationTime = getTokenCreationTime()
        return System.currentTimeMillis() - creationTime > expirationTimeInMillis
    }
    fun clearToken() {
        encryptedPrefs.edit().clear().apply()
    }
    companion object {
        private const val TOKEN_EXPIRATION_TIME = 365L * 24 * 60 * 60 * 1000
    }
}

class AniListAuthActivity : AppCompatActivity() {
    private lateinit var tokenManager: AniListTokenManager

    companion object {
        private const val CLIENT_ID = "23298"
        fun createIntent(context: Context): Intent {
            return Intent(context, AniListAuthActivity::class.java)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = AniListTokenManager(this)
        if (intent?.data != null) {
            handleAuthCallback(intent)
        } else {
            startAuthFlow()
        }
    }
    private fun startAuthFlow() {
        val authUrl = Uri.parse("https://anilist.co/api/v2/oauth/authorize").buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "token")
            .build()

        val browserIntent = Intent(Intent.ACTION_VIEW, authUrl)
        startActivity(browserIntent)
        finish()
    }
    private fun handleAuthCallback(intent: Intent) {
        val uri = intent.data ?: return
        val fragment = uri.fragment
        if (fragment != null) {
            try {
                val params = fragment.split("&")
                    .map { it.split("=") }
                    .associate { it[0] to it[1] }
                val accessToken = params["access_token"]
                if (accessToken != null) {
                    tokenManager.saveToken(accessToken)
                    Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                } else {
                    handleAuthError("No access token found in response")
                }
            } catch (e: Exception) {
                handleAuthError("Failed to parse authentication response: ${e.localizedMessage}")
            }
        } else {
            handleAuthError("No fragment found in redirect URL")
        }
        finish()
    }
    private fun handleAuthError(error: String) {
        Log.e("AniListAuth", "Authentication Error: $error")
        Toast.makeText(
            this,
            "Authentication Failed: $error",
            Toast.LENGTH_LONG
        ).show()
        setResult(RESULT_CANCELED)
    }
}