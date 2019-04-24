package com.example.androidtraining

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun displayName(view: View){
        var firstName = ""
        if (UserNameEditText.text != null){
            firstName = UserNameEditText.text.toString()
        }
        if (firstName != ""){
            nameDisplayTextView.text = "It's nice to have you."
            WelcomeTextView.text = "Welcome, $firstName!"
        }
        else{
            nameDisplayTextView.text = "What is your name?"
            WelcomeTextView.text = "Welcome!"
        }
    }

}
