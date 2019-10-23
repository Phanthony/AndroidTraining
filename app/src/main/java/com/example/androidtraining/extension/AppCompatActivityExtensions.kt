package com.example.androidtraining.extension

import android.app.Activity
import kotlinx.android.synthetic.main.activity_main.*

fun Activity.updateToolBarText(string: String){
    ToolBar?.title = string
}