package com.example.androidtraining.ui

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.androidtraining.GitHubViewModel
import com.example.androidtraining.R
import com.example.androidtraining.extension.getErrorDialog
import com.example.androidtraining.extension.onAttachDiGraph
import com.example.androidtraining.extension.updateToolBarText
import com.example.androidtraining.extension.updateToolBarTitle
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GitHubLoginFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onAttachDiGraph().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        updateToolBarTitle("Login into GitHub")
        val view = inflater.inflate(R.layout.github_login_fragment_layout, container, false)
        val loginButton = view.findViewById<Button>(R.id.GitHubLoginButton)

        val gitHubViewModel by viewModels<GitHubViewModel> { viewModelFactory }

        sharedPreferences = requireActivity().getSharedPreferences("github", Context.MODE_PRIVATE)
        val username = view.findViewById<EditText>(R.id.GitHubLoginUsernameText)
        val password = view.findViewById<EditText>(R.id.GitHubLoginPasswordText)

        loginButton.setOnClickListener {
            val checkUsername = checkEditText(username)
            if (checkUsername == null) {
                username.requestFocus()
                username.error = "Empty Username"
                return@setOnClickListener
            }
            val checkPassword = checkEditText(password)
            if (checkPassword == null) {
                password.requestFocus()
                password.error = "Empty Password"
                return@setOnClickListener
            }

            gitHubViewModel.loginToGithub(checkPassword, checkUsername)
                .subscribeOn(Schedulers.io())
                .subscribeBy {
                    if (it.isFailure) {

                        Log.i("fail", it.exceptionOrNull()!!.message)
                        this@GitHubLoginFragment.requireActivity().runOnUiThread {
                            getErrorDialog(
                                it.exceptionOrNull()!!.message!!,
                                this@GitHubLoginFragment.requireContext()
                            ).show()
                        }
                    } else {
                        Log.i("succ", it.getOrNull()?.toString())
                        val accessToken = it.getOrNull()!!.response.access_token
                        sharedPreferences.edit().putString("user", checkUsername).apply()
                        sharedPreferences.edit().putString("access_token", accessToken).apply()
                        sharedPreferences.edit()
                            .putString("auth_url", it.getOrNull()!!.response.auth_url).apply()
                        gitHubViewModel.changeIssueUser(checkUsername)
                        val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                        requireActivity().runOnUiThread {
                            requireActivity().updateToolBarText(
                                requireContext().getString(
                                    R.string.Issues
                                )
                            )
                        }
                        nav.navigate(R.id.issues_dest)
                    }
                }

        }
        return view
    }

    private fun checkEditText(editText: EditText): String? {
        val input = editText.text.trim()
        return if (input.isEmpty()) {
            null
        } else {
            input.toString()
        }
    }

}