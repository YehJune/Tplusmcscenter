package com.kct.tplusmcscenter.utils.web

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kct.tplusmcscenter.model.objects.BaseWebUrl
import com.kct.tplusmcscenter.utils.KeyManager
import com.kct.tplusmcscenter.utils.loadBlankUrl
import com.kct.tplusmcscenter.view.login.LoginActivity

class CustomWebClient
constructor(
    private val webView: WebView,
    private val activity: AppCompatActivity,
) : WebViewClient() {
    companion object {
        private const val TAG = "[CustomWebClient]"
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        Log.d(TAG, "Entry onReceivedError")
        error?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "ErrorCode: ${it.errorCode}, Description: ${it.description}")
            } else {
                Log.d(TAG, "Error")
            }
        }
        super.onReceivedError(view, request, error)

        // 빈 페이지로 이동

        // 빈 페이지로 이동
        webView.loadBlankUrl()

        // 앱 종료 다이얼로그 생성

        // 앱 종료 다이얼로그 생성
        AlertDialog.Builder(webView.context)
            .setMessage("네트워크 문제로 앱을 종료합니다\n")
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                activity.moveTaskToBack(true)
                activity.finishAndRemoveTask()
                android.os.Process.killProcess(android.os.Process.myPid())
            } // 앱 종료
            .setCancelable(false)
            .create()
            .show()
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        var url = url
        Log.d("test", "로드 url*****************: $url")
        val webUrl = url.split("\\?").toTypedArray()[0]
        if (url.startsWith("intent:")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                activity.startActivity(intent)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (webUrl == BaseWebUrl.BASE_WEB_URL + BaseWebUrl.GET_SESSION_URL) //getSession.aspx
            {
                /*
                       1) 세션이 끊겼을 때 getSession.aspx로 이동
                       2) 키파일 읽어서 읽기(ID)
                       3) load.aspx로 이동(ID 파라미터로 보내기)
                    */
                Log.d("test", "------------------getSession.aspx 이동 시 키파일 읽기------------------")
                val sBid: String = KeyManager.getKey(activity.baseContext).replace("+","%2B").replace("&","%26").replace("=","%3D")
                Log.d("test", "id: $sBid")
                url = BaseWebUrl.BASE_WEB_URL + BaseWebUrl.LOAD_WEB_URL + "?BID=" + sBid
            } else if (webUrl == BaseWebUrl.BASE_WEB_URL + BaseWebUrl.REGIT_URL) {
                /*
                       1) 로그인 성공 시 regit.aspx로 이동
                       2) 키파일에 쓰기(ID)
                       3) getSession.aspx로 이동
                    */
                val uri = Uri.parse(url)
                val sBid = uri.getQueryParameter("BID")
                Log.d("test", "------------------regit.aspx 이동 시 키파일 쓰기------------------")
                Log.d("test", "BID: $sBid")
                KeyManager.APPTOKEN = sBid!!
                KeyManager.setAppToken(activity.applicationContext)
                url = BaseWebUrl.BASE_WEB_URL + BaseWebUrl.GET_SESSION_URL
            } else if (webUrl == BaseWebUrl.BASE_WEB_URL + BaseWebUrl.OTH_LOGIN_URL) {
                Log.d("test", "------------------othLogin.aspx 이동------------------")

                //snsLogin 엑티비티 호출
                val nation = Uri.parse(url).getQueryParameter("NATION")
                val bid = Uri.parse(url).getQueryParameter("BID")
                Log.d("test", "nation: $nation")
                Log.d("test", "bid: $bid")
                val intent = Intent(activity, LoginActivity::class.java)
                intent.putExtra("NATION", nation)
                intent.putExtra("BID", bid)
                activity.startActivity(intent)
                activity.finish()
            }
            Log.d("test", "이동 URL : $url")
            view.loadUrl(url)
        }
        return true
    }

    override fun onPageFinished(view: WebView, url: String) {
        var url = url
        Log.d("test", "Finished loading URL: $url")
        if (url == BaseWebUrl.BASE_WEB_URL + BaseWebUrl.GET_SESSION_URL) //getSession.aspx
        {
            /*
                   1) 세션이 끊겼을 때 getSession.aspx로 이동
                   2) 키파일 읽어서 읽기(ID)
                   3) load.aspx로 이동(ID 파라미터로 보내기)
                */
            Log.d("test", "------------------getSession.aspx 이동 시 키파일 읽기------------------")
            val sBid: String = KeyManager.getKey(activity.applicationContext).replace("+","%2B").replace("&","%26").replace("=","%3D")
            Log.d("test", "id: $sBid")
            url = BaseWebUrl.BASE_WEB_URL + BaseWebUrl.LOAD_WEB_URL + "?BID=" + sBid
            view.loadUrl(url)
        }
    }

}