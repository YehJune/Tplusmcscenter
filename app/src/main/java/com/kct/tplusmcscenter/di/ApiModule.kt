package com.kct.tplusmcscenter.di

import com.kct.tplusmcscenter.network.api.WidgetAPI
import com.kct.tplusmcscenter.utils.API
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideWidgetApi(@API retrofit: Retrofit): WidgetAPI = retrofit.create(WidgetAPI::class.java)

}