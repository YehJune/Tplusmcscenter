package com.kct.tplusmcscenter.utils.web

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import com.kct.tplusmcscenter.model.objects.BaseWebUrl
import com.kct.tplusmcscenter.utils.KeyManager
import java.net.URISyntaxException

class CustomWebChromeClient
constructor(
    private val context: Context,
    private val mWebView: WebView
) : WebChromeClient() {

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                result?.confirm()
            }
            .create()
            .show()

        return true
//            return super.onJsAlert(view, url, message, result)
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        Log.d("[CustomWebChromeClient]", "Entry onCreateWindow")

        val newWebView = WebView(context)

        newWebView.settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        val dialog = Dialog(context)
        dialog.run {
            setContentView(newWebView)
            dialog.window?.let {
                val params: ViewGroup.LayoutParams = it.attributes
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.attributes = params as WindowManager.LayoutParams
            }
        }
        dialog.show()

        dialog.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
            Log.d("[CustomWebChromeClient]", "Entry dialog onKey")

            return@setOnKeyListener when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    dialog.dismiss()
                    newWebView.run {
                        removeView(this)
                        destroy()
                    }
                    true
                }
                else -> { false }
            }
        }

        newWebView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView?) {
                Log.d("[CustomWebChromeClient]", "Entry onCloseWindow")
                dialog.dismiss()
                newWebView.removeView(window)
                window?.destroy()
            }
        }

        newWebView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { dialog, which -> result.confirm() }
                    .setCancelable(false)
                    .create()
                    .show()
                return true
            }

            override fun onCloseWindow(window: WebView) {
                Log.d("TEST", "newWebView onCloseWindow 입장")
                // finish(); //추가
                dialog.dismiss()

                //20201208
                newWebView.removeView(window)
                window.destroy()
            }
        }

        newWebView.webViewClient = object : WebViewClient() {
            //링크 클릭에 대한 반응
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d("TEST", "newWebView shouldOverrideUrlLoading 입장 URL: $url")
                Log.d("TEST", "BASE_WEB_URL + REGIT_URL: ${BaseWebUrl.BASE_WEB_URL}${BaseWebUrl.REGIT_URL}")
                //BASE_WEB_URL + REGIT_URL
                when {
                    url.startsWith("intent") -> { return checkAppInstalled(view, url, "intent") }

                    url.startsWith("market://") -> {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            intent?.let { context.startActivity(it) }
                            return true
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                    }

                    url.contains("${BaseWebUrl.BASE_WEB_URL}/view/merge/selfopen/card_auth/card_auth_success.aspx") -> {
                        Log.d("TEST", "본인인증 성공 페이지 입장")
                        val sParameter = url.split("\\?").toTypedArray()[1] //본인인증 성공 파라미터
                        val goUrl = "${BaseWebUrl.BASE_WEB_URL}/view/merge/selfopen/card_auth/card_auth_android.aspx" //새로 이동할 URL
                        Log.d("TEST", "target url: $goUrl?$sParameter")
                        newWebView.loadUrl("javascript:self.close();") //팝업 닫고
                        mWebView.loadUrl("$goUrl?$sParameter") //새로운 url로 이동
                    }

                    url == BaseWebUrl.BASE_WEB_URL + BaseWebUrl.GET_SESSION_URL -> {
                        Log.d("TEST", "GET_SESSION_URL 입장")
                        /*
                           1) 세션이 끊겼을 때 getSession.aspx로 이동
                           2) 키파일 읽어서 읽기(ID)
                           3) load.aspx로 이동(ID 파라미터로 보내기)
                        */
                        Log.d(
                            "test",
                            "------------------getSession.aspx 이동 시 키파일 읽기------------------"
                        )
                        val sKey: String = KeyManager.getKey(context).replace("+","%2B").replace("&","%26").replace("=","%3D")
                        val sApptoken: String = KeyManager.getAppToken(context)
                        Log.d("test", "KEY: $sKey, APPTOKEN: $sApptoken")
                        view.loadUrl(
                            "${BaseWebUrl.BASE_WEB_URL}${BaseWebUrl.LOAD_WEB_URL}?key=${
                                KeyManager.getKey(
                                    context
                                )
                            }&div=android&APPTOKEN=$sApptoken"
                        )
                    }

                    url.contains(BaseWebUrl.BASE_WEB_URL + BaseWebUrl.REGIT_URL) -> {
                        Log.d("TEST", "REGIT_URL 입장")
                        /*
                           1) 로그인 성공 시 regit.aspx로 이동
                           2) 키파일에 쓰기(ID)
                           3) main.aspx로 이동
                        */
                        val uri = Uri.parse(url)
                        //String sKey = KeyManager.getKey(MainActivity.this);
                        val sApptoken = uri.getQueryParameter("APPTOKEN")
                        KeyManager.APPTOKEN = sApptoken ?: ""
                        KeyManager.setAppToken(context)
                        Log.d("test", "APPTOKEN: $sApptoken")
                        view.loadUrl(BaseWebUrl.BASE_WEB_URL + BaseWebUrl.MAIN_WEB_URL)
                    }

                    url.startsWith("http://") || url.startsWith("https://") -> {
                        view.loadUrl(url)
                    }

                    else -> {
                        Log.d("TEST", "ELSE 입장")
                        return checkAppInstalled(view, url, "customLink")
                    }
                }

                return true
            }
        }

        resultMsg?.let {
            (it.obj as WebView.WebViewTransport).webView = newWebView
            it.sendToTarget()
        }
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        when (newProgress) {
            100 -> {
                mWebView.settings.run {
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    domStorageEnabled = true
                    mixedContentMode = 0
                }
            }

            else -> {  }
        }
    }

    private fun checkAppInstalled(view: WebView, url: String, type: String)
    = when (type) {
        "intent" -> { intentSchemeParser(view, url) }
        "customLink" -> { customSchemeParser(view, url) }
        else -> false
    }

    private fun intentSchemeParser(view: WebView, url: String): Boolean {
        try {
            var intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            if (context.packageManager.resolveActivity(intent, 0) == null) {
                val pakagename = intent.getPackage()
                if (pakagename != null) {
                    val uri = Uri.parse("market://details?id=$pakagename")
                    intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                    return true
                }
            }
            val uri = Uri.parse(intent.dataString)
            intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            return true
        } catch (e: URISyntaxException) {
            Log.d("TEST", "intentSchemeParser ERR: $e")
        }
        return false
    }

    private fun customSchemeParser(view: WebView, url: String): Boolean {
        val packageName: String = when {
            url.startsWith("shinhan-sr-ansimclick://") -> {
                "com.shcard.smartpay" // 신한 앱카드
            }
            url.startsWith("mpocket.online.ansimclick://") -> {
                "kr.co.samsungcard.mpocket" // 삼성 앱카드
            }
            url.startsWith("vguardstart://") || url.startsWith("vguardend://") -> {
                "kr.co.shiftworks.vguardweb" // vguard 백신 (VG 기업용 Web SDK)
            }
            url.startsWith("ahnlabv3mobileplus") -> {
                "com.ahnlab.v3mobileplus" // V3 Mobile Plus
            }
            url.startsWith("lotteappcard") -> {
                "com.lcacApp" // 롯데 앱카드
            }
            url.startsWith("cloudpay") -> {
                "com.hanaskcard.paycla" // 하나 앱카드
            }
            url.startsWith("nhallonepayansimclick") -> {
                "nh.smart.nhallonepay" // 농협 앱카드
            }
            url.startsWith("hyundaicardappcardid") -> {
                "com.hyundaicard.appcard" // 현대 앱카드
            }
            url.startsWith("kb-auth://pay") -> {
                "com.kbcard.cxh.appcard" // 국민 앱카드
            }
            else -> { return false }
        }

        var intent: Intent
        //하드코딩된 패키지명으로 앱 설치여부를 판단하여 해당 앱 실행 또는 마켓 이동
        if (chkAppInstalled(view, packageName)) {
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val uri = Uri.parse(intent.dataString)
                intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
                return true
            } catch (e: URISyntaxException) {
                Log.d("TEST", "chkAppInstalled 이동 ERR: $e")
            }
        } else { //마켓이동
            val uri = Uri.parse("market://details?id=$packageName")
            intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            return true
        }

        return false
    }

    private fun chkAppInstalled(view: WebView, packagePath: String): Boolean
    = try {
        context.packageManager.getPackageInfo(packagePath, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        Log.d("TEST", "chkAppInstalled 체크 ERR: $e")
        false
    }
}