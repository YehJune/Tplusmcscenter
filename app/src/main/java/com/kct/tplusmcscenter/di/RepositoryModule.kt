package com.kct.tplusmcscenter.di

import com.kct.tplusmcscenter.network.api.WidgetAPI
import com.kct.tplusmcscenter.network.remote.WidgetRemoteSource
import com.kct.tplusmcscenter.network.repository.WidgetRepository
import com.kct.tplusmcscenter.network.repository.WidgetRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWidgetRemoteSource(widgetAPI: WidgetAPI) : WidgetRemoteSource
    = WidgetRemoteSource(widgetAPI)

    @Provides
    @Singleton
    fun provideWidgetRepository(widgetRemoteSource: WidgetRemoteSource) : WidgetRepository
    = WidgetRepositoryImpl(widgetRemoteSource)
}