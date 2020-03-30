package com.example.androidtraining

import com.example.androidtraining.database.GitHubDataBase
import com.example.androidtraining.service.Service
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TellerIssueCommentOnlineRepositoryTests {

    @Mock
    lateinit var mService: Service

    @Mock
    lateinit var mDb: GitHubDataBase

    lateinit var
}