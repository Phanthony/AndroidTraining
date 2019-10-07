package com.example.androidtraining

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class GitHubLoginFragment:Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.github_login_fragment_layout,container,false)
        val loginButton = view.findViewById<Button>(R.id.GitHubLoginButton)

        val gitHubViewModel = activity!!.run {
            ViewModelProviders.of(this)[GitHubViewModelDependencies::class.java]
        }

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

            //testuser7891
            //goldcatchadmit72
            gitHubViewModel.logIntoGitHub(checkPassword,checkUsername)
                .subscribeOn(Schedulers.io())
                .subscribeBy{
                    if(it.isFailure()){
                        Log.i("fail",it.failure!!.message)
                    }
                    else{
                        Log.i("succ",it.response.toString())
                    }
                }

        }
        return view
    }

    private fun checkEditText(editText: EditText): String? {
        val input = editText.text.trim()
        return if(input.isEmpty()){
            null
        } else{
            input.toString()
        }
    }

}