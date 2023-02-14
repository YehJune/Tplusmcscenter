package com.kct.tplusmcscenter.view.main

import InAppUpdate
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.kct.tplusmcscenter.R
import com.kct.tplusmcscenter.base.BaseActivity
import com.kct.tplusmcscenter.databinding.ActivityMainBinding
import com.kct.tplusmcscenter.model.objects.BaseWebUrl
import com.kct.tplusmcscenter.utils.KeyManager
import com.kct.tplusmcscenter.utils.loadBlankUrl
import com.kct.tplusmcscenter.utils.loadEntryUrl
import com.kct.tplusmcscenter.utils.web.CustomWebClient
import com.kct.tplusmcscenter.view.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
public class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
    companion object {
        private const val TAG = "[MainActivity]"
    }

    //region private val Field
    private val viewModel : MainActivityViewModel by viewModels()

    private val permissionList = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
    //endregion

    //region private var Field
    private var lastTimeBackPressed: Long = 0L
    //endregion

    //region lateinit Field
    private lateinit var appPauseTime: String
    private lateinit var appRestartTime: String
    //endregion


    var cameraPath = ""
    var mWebViewImageUpload: ValueCallback<Array<Uri>>? = null


    //public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    //public static final String USER_AGENT = "537712621652-ut55rbpl692i0v1nde74mb870af47rct.apps.googleusercontent.com";


    private lateinit var inAppUpdate: InAppUpdate

    private val MY_REQUEST_CODE = 100
    private var mAppUpdateManager: AppUpdateManager? = null


    //region Activity Life Cycle func
    override fun onCreate(savedInstanceState: Bundle?) {
        try {

        super.onCreate(savedInstanceState)
        viewModel.updateToken()
            inAppUpdate = InAppUpdate(this)

        checkLogin()
        firstIntro()
        loadMainWebView()
        settingWebView()


        binding.webView.addJavascriptInterface(this, "MyApp")






        //html a태그 파일다운로드
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(this@MainActivity,
                Environment.DIRECTORY_DOWNLOADS,
                ".pdf")
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
        }


            //binding.webView.setWebViewClient(WebClient())

        binding.webView.webChromeClient = object : WebChromeClient(){


            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                try{
                    mWebViewImageUpload = filePathCallback!!
                    var takePictureIntent : Intent?
                    takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if(takePictureIntent.resolveActivity(packageManager) != null){
                        var photoFile : File?

                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath",cameraPath)

                        if(photoFile != null){
                            cameraPath = "file:${photoFile.absolutePath}"
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile))
                        }
                        else takePictureIntent = null
                    }
                    val contentSelectionIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    contentSelectionIntent.type = "image/*"

                    var intentArray: Array<Intent?>

                    if(takePictureIntent != null) intentArray = arrayOf(takePictureIntent)
                    else intentArray = takePictureIntent?.get(0)!!

                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE,"사용할 앱을 선택해주세요.")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                    launcher.launch(chooserIntent)
                }
                catch (e : Exception){ }
                return true
            }



            }



        }
        catch (e : java.lang.Exception)
        {
            Log.d("start222", "oncreate error : " + e.message.toString())
        }


    }

    fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data

            if(intent == null){ //바로 사진을 찍어서 올리는 경우
                val results = arrayOf(Uri.parse(cameraPath))
                mWebViewImageUpload!!.onReceiveValue(results!!)
            }
            else{ //사진 앱을 통해 사진을 가져온 경우
                val results = intent!!.data!!
                mWebViewImageUpload!!.onReceiveValue(arrayOf(results!!))
            }
        }
        else{ //취소 한 경우 초기화
            mWebViewImageUpload!!.onReceiveValue(null)
            mWebViewImageUpload = null
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onPause() {
        super.onPause()
        val format = SimpleDateFormat("yyyyMMddHHmmss")
        appPauseTime = format.format(Date())
        if (this::appPauseTime.isInitialized) {
            Log.d("TEST", "APP ON PAUSE() TIME : $appPauseTime")
        }
    }

    override fun onStop() {
        super.onStop()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onRestart() {
        super.onRestart()

        val format = SimpleDateFormat("yyyyMMddHHmmss")
        appRestartTime = format.format(Date())
        if (this::appPauseTime.isInitialized && this::appRestartTime.isInitialized) {
            Log.d("TEST", "[APP ON PAUSE() TIME : $appPauseTime] [APP ON RESTART() TIME : $appRestartTime] ")
        }

        try {
            //잠시 주석....
//            val pauseTime = format.parse(appPauseTime)!!.time
//            val restartTime = format.parse(appRestartTime)!!.time
//
//            if ((restartTime - pauseTime) / 60000 > 0) {
//                Log.d(TAG, "Since it's been over a minute, move the URL")
//                loadMainWebView()
//            } else {
//                Log.d(TAG, "Stay")
//            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.printStackTrace()}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    //endregion

    //region override function
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "Entry onRequestPermissionsResult, requestCode: $requestCode")

        when (requestCode) {
            0 -> {
                grantResults.forEach {
                    when (it) {
                        PackageManager.PERMISSION_GRANTED -> { Log.d(TAG, "Permission granted") }
                        PackageManager.PERMISSION_DENIED -> {
                            Toast.makeText(applicationContext, "앱 권한을 설정해주세요", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
            else -> { Log.d(TAG, "처리되지 않은 requestCode: $requestCode") }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "Entry onKeyDown")

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (binding.webView.canGoBack()) {
                    binding.webView.url?.let {
                        if (it.contains("URL")) {
                            if (System.currentTimeMillis() - lastTimeBackPressed < 1500) finish()
                            lastTimeBackPressed = System.currentTimeMillis()
                            Toast.makeText(applicationContext, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    }

                    binding.webView.goBack()
                    return true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        Log.d("TAG", "Entry onBackPressed")

        AlertDialog.Builder(this)
            .setTitle("종료")
            .setMessage("이 어플을 종료 하시겠습니까?")
            .setNegativeButton("아니요") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("예") { _, _ ->
                moveTaskToBack(true)
                finishAndRemoveTask()
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("TAG", "Entry onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
        binding.webView.restoreState(savedInstanceState)
    }
    //endregion

    //region private function
    private fun firstIntro() {
        Log.d("start222", "firstIntro")
        when (viewModel.isFirst) {
            true -> {
                Log.d(TAG, "First time running the app for the App")

                val text = "[필수적 접근권한] \n" +
                        "* 저장 공간 권한: 자동 로그인 기능을 위한 임시파일 저장을 위해 저장 공간 접근권한 동의가 필요합니다. \n\n" +
                        "앱 설치 후 최초 접근 시에만 동의 절차가 진행됩니다. \n" +
                        "[TPLUS 고객센터앱] 사용을 위해 권한 사용에 동의하시겠습니까?"

                AlertDialog.Builder(this)
                    .setTitle("권한 동의 안내")
                    .setMessage(text)
                    .setCancelable(false)
                    .setPositiveButton("동의") { dialog, which ->
                        Log.d(TAG, "개인정보 수집 동의")
                        Toast.makeText(applicationContext, "개인정보 수집에 동의하셨습니다.", Toast.LENGTH_SHORT).show()
                        viewModel.setIsFirst(false)
                        dialog.dismiss()
                        checkPermission()
                    }
                    .setNegativeButton("거부") { dialog, which ->
                        Log.d(TAG, "개인정보 수집 거부")
                        Toast.makeText(applicationContext, "개인정보 수집을 거부하셨습니다. 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }

            false -> {
                Log.d(TAG, "Not the first time running the app")
                checkPermission()
            }
        }
    }

    private fun checkPermission() {
        permissionList.forEach { permission ->
            when (checkCallingOrSelfPermission(permission)) {
                PackageManager.PERMISSION_GRANTED -> { Log.d(TAG, "Permission: $permission granted") }
                PackageManager.PERMISSION_DENIED -> {
                    Log.d(TAG, "Permission: $permission denied")
                    ActivityCompat.requestPermissions(this, permissionList, 0)
//                    requestPermissions(arrayOf(permission), 0)
                }
            }
        }
    }


    private fun checkLogin() {
        Log.d("start222", "checkLogin")
        if (viewModel.userId.isEmpty()) {
            Log.d("start222", "viewModel.userId.isEmpty()")
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
    }

    private fun loadMainWebView() {
        Log.d("start222", "loadMainWebView")
        try{
            val key = KeyManager.getKey(this).replace("+","%2B").replace("&","%26").replace("=","%3D")
            val appToken = KeyManager.getAppToken(this)
            var appversion = packageManager.getPackageInfo(packageName, 0).versionName;

            Log.d("Load Url222", "Load Url222 version : " + appversion)


            if (key.isNotEmpty() && appToken.isNotEmpty()) {

                Log.d(TAG, "Load Url222: ${BaseWebUrl.BASE_WEB_URL}${BaseWebUrl.ENTRY_URL}/?KEYVALUE=$key&APPTOKEN=$appToken&DEVICE=A&FIREBASETOKEN=${viewModel.firebaseToken}&EMAIL=${viewModel.userId}&APPVERSION=$appversion")
                //Log.d(TAG, viewModel.firebaseToken)

                //binding.webView.loadUrl(BaseWebUrl.BASE_WEB_URL+"/"+BaseWebUrl.ENTRY_URL+"?KEYVALUE="+key+"&APPTOKEN="+appToken+"&DEVICE=A&FIREBASETOKEN="+viewModel.firebaseToken+"&EMAIL="+viewModel.userId)

                binding.webView.loadEntryUrl(
                    key = key,
                    appToken = appToken,
                    firebaseToken = viewModel.firebaseToken,
                    userId = viewModel.userId,
                    appversion = appversion
                )

            } else {
                Log.d("start222", "key is Empty")
                Log.d(TAG, "key is Empty: ${key.isEmpty()}, AppToken is Empty: ${appToken.isEmpty()}")
                binding.webView.loadBlankUrl()
            }
        } catch (e: SecurityException) {
            Log.d("start222 error ", "Non phoneNumber && Non Permission")
            val key = KeyManager.getKey(this).replace("+","%2B").replace("&","%26").replace("=","%3D")
            val appToken = KeyManager.getAppToken(this)

            var appversion = packageManager.getPackageInfo(packageName, 0).versionName;

            if (key.isNotEmpty() && appToken.isNotEmpty()) {
                Log.d(TAG, "Load Url: ${BaseWebUrl.BASE_WEB_URL}${BaseWebUrl.ENTRY_URL}/?KEYVALUE=$key&APPTOKEN=$appToken&DEVICE=A&FIREBASETOKEN=${viewModel.firebaseToken}&EMAIL=${viewModel.userId}&APPVERSION=$appversion")
                binding.webView.loadEntryUrl(
                    key = key,
                    appToken = appToken,
                    firebaseToken = viewModel.firebaseToken,
                    userId = viewModel.userId,
                    appversion = appversion
                )
            } else {
                Log.d("start222", "key is Empty2")
                Log.d(TAG, "key is Empty: ${key.isEmpty()}, AppToken is Empty: ${appToken.isEmpty()}")
                binding.webView.loadBlankUrl()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun settingWebView() = binding.run {
        try {

            webView.webViewClient = CustomWebClient(webView, this@MainActivity)
            webView.settings.run {
                useWideViewPort = false
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                javaScriptEnabled = true
                textZoom = 100
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                userAgentString = userAgentString



            }
            CookieManager.getInstance().run {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(webView, true)
            }

            webView.webViewClient = WebClient()

            webView.setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    var url = url
                    Log.d("start222 333", "로드 url: $url")

                    if(url.contains("/view/member/othLogin.aspx"))
                    {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))

                    }
                    else if(url.contains("https://www.kcttel.com") ||url.contains("https://www.tplring.com") || url.contains("http://www.musicbellring.com") || url.contains("https://m.tplusdirectmall.com") || url.contains("https://inside.kt.com")
                        || url.contains("https://www.lguplus.com") || url.contains("https://www.tworld.co.kr")
                    )
                    {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            intent?.let { startActivity(it) }
                            return true
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }

                    }
                    else if(url.contains("/view/common/getSession.aspx"))
                    {
                        startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    }
                    return false

                }

            })
        }
        catch (e :java.lang.Exception)
        {

            Log.d("start222", "error : " + e.message.toString())
        }
    }
    //endregion

    //region public function
    @JavascriptInterface
    fun resize(height: Float) = runOnUiThread {
        Log.d("TEST", binding.webView.url + " :: Resize.....")
        binding.webView.layoutParams = FrameLayout.LayoutParams(
            resources.displayMetrics.widthPixels,
            (height * resources.displayMetrics.density).toInt()
        )
    }
    //endregion


    // 웹뷰 클라이언트 클래스

    public class WebClient : WebViewClient() {
        // URL 로드 요청시 처리 함수(키파일 쓰기 및 읽기)

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            var url = url
            Log.d("start222", "로드 url: $url")

            if(url.contains("/view/member/othLogin.aspx"))
            {
//                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//
//                val intent = Intent(this@MainActivity, LoginActivity::class.java)
//                startActivity(intent)

                //finish()
            }
           return false
        }


    }



}
