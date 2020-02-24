package com.example.androidtraining.extension

import android.app.Activity
import android.app.Service
import androidx.fragment.app.Fragment
import com.example.androidtraining.di.AppGraph
import com.example.androidtraining.ui.MainApplication

fun Activity.onCreateDiGraph(): AppGraph {
    return (application as MainApplication).appComponent
}

fun Fragment.onAttachDiGraph(): AppGraph {
    return (activity!!.application as MainApplication).appComponent
}

fun Service.onCreateDiGraph(): AppGraph {
    return (application as MainApplication).appComponent
}