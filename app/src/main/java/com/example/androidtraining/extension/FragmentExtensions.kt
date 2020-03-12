package com.example.androidtraining.extension

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.androidtraining.R


fun Fragment.getErrorDialog(errorMessage: String, context: Context): AlertDialog.Builder{
    return AlertDialog.Builder(context).apply {
        setTitle(getString(R.string.error))
        setMessage(errorMessage)
        setPositiveButton(getString(R.string.Ok)) { dialog : DialogInterface, _: Int ->
            dialog.dismiss()
        }
    }
}

fun Fragment.updateToolBarTitle(title: String){
    (activity as AppCompatActivity?)!!.supportActionBar!!.title = title
}