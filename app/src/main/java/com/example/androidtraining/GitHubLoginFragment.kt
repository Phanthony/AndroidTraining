package com.example.androidtraining

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.androidtraining.extension.getErrorDialog
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class GitHubLoginFragment:Fragment() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("github", Context.MODE_PRIVATE)
        if(sharedPreferences.getString("access_token",null) != null){
            val nav = activity!!.findNavController(R.id.nav_host_fragment)
            nav.navigate(R.id.issues_dest)
            onDestroy()
        }

    }


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

            gitHubViewModel.service.loginToGithub(checkPassword,checkUsername)
                .subscribeOn(Schedulers.io())
                .subscribeBy{
                    if(it.isFailure){
                        Log.i("fail",it.exceptionOrNull()!!.message)
                        this@GitHubLoginFragment.activity!!.runOnUiThread {
                            getErrorDialog(it.exceptionOrNull()!!.message!!,this@GitHubLoginFragment.context!!).show()
                        }
                    }
                    else{
                        Log.i("succ",it.getOrNull()?.toString())
                        sharedPreferences.edit().putString("access_token",it.getOrNull()!!.response.access_token).apply()
                        sharedPreferences.edit().putString("auth_url",it.getOrNull()!!.response.auth_url).apply()
                        val nav = activity!!.findNavController(R.id.nav_host_fragment)
                        nav.navigate(R.id.issues_dest)
                        onDestroy()
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