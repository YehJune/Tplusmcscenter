package com.kct.tplusmcscenter.view.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class MainActivityViewModel
@Inject
constructor(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    companion object {
        private const val TAG = "[MainActivityVM]"
    }

    //region public variable
    val isFirst: Boolean
    get() = sharedPreferences.getBoolean(SharedPreferenceKey.KEY_ISFIRST, true)

    val phoneNumber: String
    get() = sharedPreferences.getString(SharedPreferenceKey.KEY_PHONENUMBER, "") ?: ""

    val firebaseToken: String
    get() = sharedPreferences.getString(SharedPreferenceKey.KEY_FIREBASETOKEN, "") ?: ""



    val userId: String
    get() = sharedPreferences.getString(SharedPreferenceKey.KEY_USER_ID, "") ?: ""

    val params: HashMap<String, String>
    get() = HashMap()
    //endregion

    //region private variable
    private val maxCount = 3

    private var tryCount = 0

    private var currentPhotoPath = ""
    //endregion

    //region sharedPreferences Setter
    fun setIsFirst(boolean: Boolean) = sharedPreferences.edit().putBoolean(SharedPreferenceKey.KEY_ISFIRST, boolean).apply()

    fun setPhone(number: String) = sharedPreferences.edit().putString(SharedPreferenceKey.KEY_PHONENUMBER, number).apply()
    //endregion

    //region public function
    fun updateToken() {
        Log.d("start222", "token")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            when(task.isSuccessful) {
                true -> {
                    val token = task.result
                    Log.d(TAG, "Token: $token")
                    sharedPreferences.edit().putString(SharedPreferenceKey.KEY_FIREBASETOKEN, token).apply()
                }
                false -> {
                    Log.d(TAG, "Fetching FCM registration token failed: ${task.exception}")
                    return@addOnCompleteListener
                }
            }
        }
    }
    //endregion

    //region private function
    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(context: Context): File {
        Log.d("test", "createImageFile 입장")
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath

        return image
    }
    //endregion

}