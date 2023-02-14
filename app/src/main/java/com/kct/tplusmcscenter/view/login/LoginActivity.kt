package com.kct.tplusmcscenter.view.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.OAuthProvider
import com.kct.tplusmcscenter.R
import com.kct.tplusmcscenter.base.BaseActivity
import com.kct.tplusmcscenter.databinding.ActivityLoginBinding
import com.kct.tplusmcscenter.model.objects.AppleSignInStr
import com.kct.tplusmcscenter.view.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>(R.layout.activity_login) {
    companion object {
        private const val TAG = "[LoginActivity]"
    }

    //region private val field
    private val viewModel: LoginActivityViewModel by viewModels()

    private val getResult: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when(it.resultCode) {
            RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                handleSignInResult(task)
            }
            else -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("네트워크를 확인해주세요")
                    .setPositiveButton("확인") { dialog, which -> dialog.dismiss() }
                builder.show()


            }
        }
    }

    private val permissionList = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
    //endregion

    //region Activity LifeCycle fun
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstIntro()
        checkPermission()
        init()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    //endregion

    //region override function
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
    //endregion

    //region private function
    private fun init() = binding.run {
        btnGoogleSignIn.setOnClickListener {
            getResult.launch(viewModel.getGSC().signInIntent)
        }
        btnAppleSignIn.setOnClickListener {
            when (viewModel.pendingIsNull()) {
                true -> { appleSignIn() }
                false -> {
                    AlertDialog.Builder(this@LoginActivity)
                        .setMessage("다른 로그인을 선택해주시기 바랍니다.")
                        .setPositiveButton("확인") { dialog, which -> dialog.dismiss() }
                        .show()
                }
            }
        }
        btnDefaultSignIn.setOnClickListener {
            viewModel.setUserId("defaultlogin")
            viewModel.auth.signOut()
            Log.d("btnDefaultSignIn", "btnDefaultSignIn")

            startActivity(Intent(this@LoginActivity, MainActivity::class.java))

        }
    }

    // Handled Apple Sign in
    private fun appleSignIn() = viewModel.auth.startActivityForSignInWithProvider(
        this,
        OAuthProvider.newBuilder("apple.com").apply {
            scopes = listOf("email", "name")
            addCustomParameter("locale", AppleSignInStr.KO_KR)
        }.build()
    ).addOnSuccessListener { authResult ->
        // Success Sign in
        Log.d(TAG, "activitySignIn:onSuccess user:${authResult.user}")
        Log.d(TAG, "activitySignIn:onSuccess email:${authResult.user?.email}")
        Log.d(TAG, "activitySignIn:onSuccess uid:${authResult.user?.uid}")
        //viewModel.setUserId(authResult.user?.email.toString())
        viewModel.setUserId(authResult.user?.uid.toString())

        viewModel.auth.signOut()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }
    .addOnFailureListener { e ->
        // Failure Sign in
        Log.d(TAG, "activitySignIn:onFailure", e)
        AlertDialog.Builder(this)
            .setMessage("애플 로그인을 실패하셨습니다")
            .setPositiveButton("확인") { dialog, which -> dialog.dismiss() }
            .show()
    }

    // Handled Google Sign in
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, account.email.toString())
            Log.d(TAG, account.id.toString())

            viewModel.setUserId(account.id.toString())
            viewModel.getGSC().signOut()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        } catch (e: ApiException){
            Log.e(TAG, "signInResult:failed code=" + e.statusCode)
            AlertDialog.Builder(this)
                .setMessage("네트워크를 확인해주세요")
                .setPositiveButton("확인") { dialog, which -> dialog.dismiss() }
                .show()
        }
    }

    private fun firstIntro() {
        when (viewModel.isFirst) {
            true -> {
                Log.d(TAG, "First time running the app for the App")

                val text = "[필수적 접근권한] \n" +
                        "* 저장 공간 권한: 자동 로그인 기능을 위한 임시파일 저장을 위해 저장 공간 접근권한 동의가 필요합니다. \n\n" +
                        "앱 설치 후 최초 접근 시에만 동의 절차가 진행됩니다. \n" +
                        "[TPLUS] 사용을 위해 권한 사용에 동의하시겠습니까?"

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

    private fun checkPermission() = permissionList.forEach { permission ->
        when (checkCallingOrSelfPermission(permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permission: $permission granted")
            }
            PackageManager.PERMISSION_DENIED -> {
                Log.d(TAG, "Permission: $permission denied")
                ActivityCompat.requestPermissions(this, permissionList, 0)
            }
        }
    }
    //endregion

}