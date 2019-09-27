package com.example.androidtraining

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class GitHubLoginFragment:Fragment() {

    lateinit var retrofit: GitHubLoginApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrofit = Retrofit.Builder()
            .baseUrl("https://devclassserver.foundersclub.software/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(GitHubLoginApi::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.github_login_fragment_layout,container,false)
        val loginButton = view.findViewById<Button>(R.id.GitHubLoginButton)

        loginButton.setOnClickListener {

            val username = view.findViewById<EditText>(R.id.GitHubLoginUsernameText)
            val checkUsername = checkEditText(username)
            if(checkUsername == null){
                username.requestFocus()
                username.error = "Empty Username"
                return@setOnClickListener
            }
            val password = view.findViewById<EditText>(R.id.GitHubLoginPasswordText)
            val checkPassword = checkEditText(password)
            if(checkPassword == null){
                password.requestFocus()
                password.error = "Empty Password"
                return@setOnClickListener
            }

            retrofit.loginGithub(checkPassword,checkUsername)
                .subscribeOn(Schedulers.io())
                .subscribeBy { result ->
                    if(!result.isError){
                        Log.i("test",result.response()!!.body()!!.response.access_token)
                    }
                    else{
                        Log.i("test",result.error()!!.message)
                    }
                }

        }


        return view
    }

    fun checkEditText(editText: EditText): String? {
        val input = editText.text.trim()
        return if(input.isEmpty()){
            null
        } else{
            input.toString()
        }
    }

}