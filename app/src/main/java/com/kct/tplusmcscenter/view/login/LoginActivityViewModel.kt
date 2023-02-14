package com.kct.tplusmcscenter.view.login

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel
@Inject
constructor(
    private val sharedPreferences: SharedPreferences,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {
    companion object {
        private const val TAG = "[LoginActVM]"
    }

    val auth: FirebaseAuth
    get() = Firebase.auth

    val isFirst: Boolean
    get() = sharedPreferences.getBoolean(SharedPreferenceKey.KEY_ISFIRST, true)

    private val pending: Task<AuthResult>?
    get() = auth.pendingAuthResult

    //region sharedPreferences setter
    fun setUserId(userId: String) = sharedPreferences.edit().putString(SharedPreferenceKey.KEY_USER_ID, userId).apply()

    fun setIsFirst(boolean: Boolean) = sharedPreferences.edit().putBoolean(SharedPreferenceKey.KEY_ISFIRST, boolean).apply()
    //endregion

    fun getGSC() = googleSignInClient

    fun pendingIsNull(): Boolean = if (pending != null) {
        pending!!.addOnSuccessListener { authResult ->
            Log.d(TAG, "checkPending:onSuccess: $authResult")
            authResult.user?.email
        }.addOnFailureListener { e ->
            Log.w(TAG, "checkPending:OnFailure", e)
        }
        false
    } else {
        Log.d(TAG, "pending: null")
        true
    }

}