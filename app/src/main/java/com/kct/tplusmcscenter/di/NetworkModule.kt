package com.kct.tplusmcscenter.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kct.tplusmcscenter.R
import com.kct.tplusmcscenter.utils.API
import com.kct.tplusmcscenter.utils.FCM
import com.kct.tplusmcscenter.utils.network.NetworkInterceptor
import com.kct.tplusmcscenter.utils.network.NullOnEmptyConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @API
    fun provideBaseUrl(): String = "http://kctapp.m-n.kr/common/"

    @Provides
    @FCM
    fun provideBaseFcmUrl(): String = "https://fcm.googleapis.com/"

    @Provides
    fun provideInterceptor(@ApplicationContext context: Context): NetworkInterceptor = NetworkInterceptor(context)

    @Provides
    @Singleton
    @API
    fun provideOkHttpClient(networkInterceptor: NetworkInterceptor): OkHttpClient
    = OkHttpClient.Builder()
        .readTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(networkInterceptor)
        .build()

    @Provides
    @Singleton
    @API
    fun provideRetrofit(@API okHttpClient: OkHttpClient, @API baseUrl: String): Retrofit
    = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .addConverterFactory(NullOnEmptyConverterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext applicationContext: Context): GoogleSignInClient
    = GoogleSignIn.getClient(
        applicationContext,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()
    )

}