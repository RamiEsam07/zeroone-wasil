package com.example.data.supabase

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Shared role-aware Google -> Supabase login flow used by each WASIL role app.
 * Authorization is based on the server-owned public.profiles row, never Google metadata.
 */
class SupabaseRoleAuthenticator(
    context: Context,
    private val requiredRole: String,
) {
    private val appContext = context.applicationContext
    private val sessionStore = SecureSessionStore(appContext)
    private val authRepository = SupabaseAuthRepository(sessionStore)

    init {
        SupabaseSessionProvider.sessionRefresher = { candidate ->
            authRepository.ensureValidSession(candidate).getOrThrow()
        }
    }

    fun isConfigured(): Boolean =
        authRepository.isConfigured() && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    suspend fun restore(): Result<SupabaseSession?> = runCatching {
        val session = authRepository.restoreSession().getOrThrow() ?: return@runCatching null
        validateRole(session)
    }

    suspend fun signIn(activity: Activity): Result<SupabaseSession> = runCatching {
        check(isConfigured()) { "أكمل إعداد Supabase وGoogle OAuth في local.properties أولاً." }
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        val idToken = if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw IllegalStateException("لم يتم استلام بيانات تسجيل دخول Google المتوقعة.")
        }
        validateRole(authRepository.signInWithGoogleIdToken(idToken).getOrThrow())
    }

    suspend fun signOut(session: SupabaseSession?): Result<Unit> = authRepository.signOut(session)

    private suspend fun validateRole(session: SupabaseSession): SupabaseSession {
        val access = authRepository.getAccountAccess(session).getOrThrow()
        if (!access.isActive || access.role != requiredRole) {
            authRepository.signOut(session)
            val message = if (!access.isActive) {
                "هذا الحساب غير مفعّل حالياً. تواصل مع الدعم."
            } else {
                "هذا التطبيق مخصص لدور " + requiredRole + " فقط."
            }
            throw IllegalStateException(message)
        }
        return session
    }
}
