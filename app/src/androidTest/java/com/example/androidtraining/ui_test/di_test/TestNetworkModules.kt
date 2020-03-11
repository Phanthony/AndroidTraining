package com.example.androidtraining.ui_test.di_test

import com.example.androidtraining.JsonAdapter
import com.example.androidtraining.service.*
import com.example.androidtraining.service.logger.ActivityLogger
import com.example.androidtraining.service.logger.AppActivityLogger
import com.example.androidtraining.ui_test.MockWebServer
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
class TestNetworkModules {

    @Provides
    @Singleton
    fun provideMockWebserver(jsonAdapter: JsonAdapter): MockWebServer {
        return MockWebServer(
            okhttp3.mockwebserver.MockWebServer(),
            jsonAdapter
        ).apply {
            startServer()
        }
    }

    @Singleton
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

    @Singleton
    @Provides
    fun provideGithubService(
        serviceProvider: ServiceProvider,
        mockWebServer: MockWebServer
    ): GitHubApi {
        var temp = ""
        runBlocking(Dispatchers.IO) {
            temp = mockWebServer.url
        }
        return serviceProvider.get(temp, GitHubApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDevService(serviceProvider: ServiceProvider, mockWebServer: MockWebServer): DevApi {
        var temp = ""
        runBlocking(Dispatchers.IO) {
            temp = mockWebServer.url
        }
        return serviceProvider.get(temp, DevApi::class.java)
    }


}