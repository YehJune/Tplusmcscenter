package com.kct.tplusmcscenter.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.io.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class KeyManager {
    companion object {
        private const val TAG = "[KeyManager]"

        private const val KEYFILE_DIR = "keyfile"
        private const val KEYFILE_NAME = "keyfile.txt"
        private const val APPTOKEN_NAME = "apptoken.txt"

        // macaddress -> android 31부터 제한
        // uuid -> 16바이트로 이루어져 동일한 uuid가 나오게 될 확률은 매우 낮음
        // e.g. 550e8400-e29b-41d4-a716-446655440000
        // 총 340,282,366,920,938,463,463,374,607,431,768,211,456 개
        private val uuid: String = UUID.randomUUID().toString()

        var APPTOKEN = ""

        //region AES 256 Algorithm
        // key value 32 byte: AES256 (24: AES192, 16: AES128)
        private const val SECRET_KEY = "95212316197839737114631013598768"
        private val SECRET_IV = SECRET_KEY.substring(0, 16).toByteArray()

        /**
         * CBC encode
         */
        private fun String.encryptCBC(): String{
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
            val iv = IvParameterSpec(SECRET_IV)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            val crypted = cipher.doFinal(this.toByteArray(Charsets.UTF_8))

            return Base64.encodeToString(crypted, Base64.DEFAULT)
        }

        /**
         * CBC decode
         */
        private fun String.decryptCBC(): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
            val iv = IvParameterSpec(SECRET_IV)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)

            val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
            val output = cipher.doFinal(decodedByte)

            return String(output, Charsets.UTF_8)
        }
        //endregion

        fun getKey(context: Context): String {


            var key = ""
            val path = "${context.filesDir}/$KEYFILE_DIR/$KEYFILE_NAME"
            val file = File(path)

            if (!file.exists()) {

                File("${context.filesDir}/$KEYFILE_DIR").mkdirs()

                try {
                    file.createNewFile()
                    val fos = FileOutputStream(file)
                    val uniqueId: String = uuid
                    val uniqueIdToAES = uniqueId.encryptCBC()
                    Log.d(TAG, "macAddress: $uniqueId")
                    Log.d(TAG, "AES mac: $uniqueIdToAES")
                    Log.d(TAG, "AES decode ${uniqueIdToAES.decryptCBC()}")
                    fos.write(uniqueIdToAES.toByteArray())
                    fos.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Fail create keyfile", e)
                    Toast.makeText(context, "키파일 생성 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    if (file.exists()) file.delete()
                }
            }

            try {
                val fis = FileInputStream(file)
                val buffer = ByteArray(fis.available())
                fis.read(buffer)
                key = String(buffer)
            } catch (e: Exception) {
                Log.e(TAG, "Fail read file", e)
                Toast.makeText(context, "키파일 로드 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }

            return key
        }

        fun getAppToken(context: Context): String {
            var appToken = ""
            val path = "${context.filesDir}/$KEYFILE_DIR/$APPTOKEN_NAME"
            val file = File(path)

            when (file.exists()) {
                true -> {
                    Log.d(TAG, "Exist AppToken File")

                    try{
                        val fis = FileInputStream(file)
                        val buffer = ByteArray(fis.available())
                        fis.read(buffer)

                        appToken = String(buffer)
                        Log.d(TAG, "------------------getAppToken 키파일 있는 경우 : 키파일 읽기------------------")
                        Log.d(TAG, "AppToken: $appToken")
                    } catch (e: Exception) {
                        Log.d(TAG, "------------------getAppToken 키파일 있는 경우 : 키파일 읽기 실패------------------")
                        Log.e(TAG, "Fail read AppToken", e)
                        Toast.makeText(context, "APPTOKEN 파일 로드 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }

                false -> {
                    Log.d(TAG, "Non AppToken File")
                    setAppToken(context)
                    try {
                        val fis = FileInputStream(file)
                        val buffer = ByteArray(fis.available())
                        fis.read(buffer)

                        appToken = String(buffer)
                        Log.d(TAG, "------------------getAppToken 키파일 없는 경우 : 키파일 읽기------------------")
                        Log.d(TAG, "AppToken: $appToken")
                    } catch (e: Exception) {
                        Log.d(TAG, "------------------getAppToken 키파일 없는 경우 : 키파일 읽기 실패------------------")
                        Log.e(TAG, "Fail read AppToken", e)
                        Toast.makeText(context, "APPTOKEN 파일 로드 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            return appToken
        }

        fun setAppToken(context: Context) {
            val path = "${context.filesDir}/$KEYFILE_DIR/$APPTOKEN_NAME"
            val file = File(path)

            when (file.exists()) {
                true -> {
                    try {
                        file.delete()
                        file.createNewFile()
                        val writer = FileWriter(file)
                        writer.write(APPTOKEN_NAME)
                        Log.d(TAG, "------------------setAppToken 키파일 있는 경우 : 키파일 쓰기------------------")
                        Log.d(TAG, "AppToken: $APPTOKEN_NAME")
                        writer.flush()
                        writer.close()
                    } catch (e: Exception) {
                        Log.d(TAG, "------------------setAppToken 키파일 있는 경우 : 키파일 쓰기 실패------------------")
                        Log.e(TAG, "Fail write KeyFile", e)
                        Toast.makeText(context, "APPTOKEN 파일 생성 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                        if (file.exists()) file.delete()
                    }
                }

                false -> {
                    File("${context.filesDir}/$KEYFILE_DIR").mkdir()
                    try {
                        file.createNewFile()
                        val writer = FileWriter(file)
                        writer.write(APPTOKEN_NAME)
                        Log.d(TAG, "------------------setAppToken 키파일 없는 경우 : 키파일 쓰기------------------")
                        Log.d(TAG, "AppToken: $APPTOKEN_NAME")
                        writer.flush()
                        writer.close()
                    } catch (e: IOException) {
                        Log.d(TAG, "------------------setAppToken 키파일 없는 경우 : 키파일 쓰기 실패------------------")
                        Log.e(TAG, "Fail write KeyFile", e)
                        Toast.makeText(context, "APPTOKEN 파일 생성 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                        if (file.exists()) file.delete()
                    }
                }
            }
        }

    }
}