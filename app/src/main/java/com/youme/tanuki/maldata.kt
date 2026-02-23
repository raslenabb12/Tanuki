package com.youme.tanuki

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.IOException
data class MalTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val refresh_token: String
)
interface MalAuthService {
    @POST("v1/oauth2/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): MalTokenResponse

    @POST("v1/oauth2/token")
    @FormUrlEncoded
    suspend fun refreshAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): MalTokenResponse
}
class MalTokenManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("MAL_AUTH_PREFS", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putLong("expires_at", System.currentTimeMillis() + (expiresIn * 1000))
        }.apply()
    }
    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }
    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }
    fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong("expires_at", 0)
        return System.currentTimeMillis() >= expiresAt
    }
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
