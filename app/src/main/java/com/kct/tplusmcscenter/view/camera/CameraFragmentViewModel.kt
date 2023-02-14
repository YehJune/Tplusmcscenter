package com.kct.tplusmcscenter.view.camera
//
//import android.content.SharedPreferences
//import android.os.Handler
//import android.os.HandlerThread
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageCapture
//import androidx.lifecycle.ViewModel
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_FLASH
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_GRID
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_HDR
//import com.kct.tplusmcscenter.utils.camera.LuminosityAnalyzer
//import com.kct.tplusmcscenter.utils.camera.ThreadExecutor
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class CameraFragmentViewModel
//@Inject
//constructor(
//    private val sharedPreferences: SharedPreferences
//) : ViewModel() {
//
//    val flashMode: Int
//    get() = sharedPreferences.getInt(KEY_FLASH, ImageCapture.FLASH_MODE_OFF)
//
//    // Selector showing is grid enabled or not
//    val hasGrid: Boolean
//    get() = sharedPreferences.getBoolean(KEY_GRID, false)
//
//    // Selector showing is hdr enabled or not (will work, only if device's camera supports hdr on hardware level)
//    val hasHdr: Boolean
//    get() = sharedPreferences.getBoolean(KEY_HDR, false)
//
//    fun <D: Any> savesPref(key: String, data: D) = when (key) {
//        KEY_FLASH -> { sharedPreferences.edit().putInt(key, data as Int).apply() }
//        KEY_GRID -> { sharedPreferences.edit().putBoolean(key, data as Boolean).apply() }
//        KEY_HDR -> { sharedPreferences.edit().putBoolean(key, data as Boolean).apply() }
//        else -> {  }
//    }
//
//    fun setLuminosityAnalyzer(imageAnalysis: ImageAnalysis) {
//        // Use a worker thread for image analysis to prevent glitches
//        val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
//        imageAnalysis.setAnalyzer(
//            ThreadExecutor(Handler(analyzerThread.looper)),
//            LuminosityAnalyzer()
//        )
//    }
//}