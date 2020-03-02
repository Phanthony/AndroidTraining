package com.example.androidtraining.ui_test.di_test

import com.example.androidtraining.JsonAdapter
import com.example.androidtraining.service.*
import com.example.androidtraining.service.logger.ActivityLogger
import com.example.androidtraining.service.logger.AppActivityLogger
import com.example.androidtraining.ui_test.MockWebServer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestNetworkModules {

    @Provides
    @Singleton
    fun provideMockWebserver(jsonAdapter: JsonAdapter): MockWebServer {
        return MockWebServer(
            okhttp3.mockwebserver.MockWebServer(),
            jsonAdapter
        )
    }

    @Provides
    fun provideService(
        devApi: DevApi,
        gitHubApi: GitHubApi,
        responseProcessor: ResponseProcessor
    ): Service {
        return RetrofitService(gitHubApi, devApi, responseProcessor)
    }

    @Provides
    fun provideActivityLogger(): ActivityLogger {
        return AppActivityLogger()
    }

    @Provides
    @Singleton
    fun provideGithubService(
        serviceProvider: ServiceProvider,
        mockWebServer: MockWebServer
    ): GitHubApi {
        return serviceProvider.get(mockWebServer.url, GitHubApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDevService(serviceProvider: ServiceProvider, mockWebServer: MockWebServer): DevApi {
        return serviceProvider.get(mockWebServer.url, DevApi::class.java)
    }


}