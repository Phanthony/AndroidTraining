package com.example.androidtraining

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.androidtraining.extension.onCreateDiGraph
import com.example.androidtraining.extension.updateToolBarText
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateDiGraph()
        setContentView(R.layout.activity_main)

        val gitHubViewModel by viewModels<GitHubViewModel> { viewModelFactory }
        //set up ViewModel

        //Set up Toolbar
        ToolBar.title = getString(R.string.trending)
        ToolBar.setTitleTextColor(android.graphics.Color.WHITE)
        setSupportActionBar(ToolBar)

        //Set up Navigation Host and Controller
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        val navController = host.navController

        //set up bottom navigation
        setupBottomNav(navController)
    }

    private fun setupBottomNav(controller : NavController){
        val sharedPreferences = getSharedPreferences("github", Context.MODE_PRIVATE)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener {
            item -> when(item.itemId){
                 R.id.login_dest -> {
                     if(sharedPreferences.contains("access_token")){
                         controller.navigate(R.id.issues_dest)
                         updateToolBarText(getString(R.string.Issues))
                     }
                     else{
                         controller.navigate(R.id.login_dest)
                         updateToolBarText(getString(R.string.Login))
                     }
                     true
                 }
                else -> {
                    controller.navigate(item.itemId)
                    updateToolBarText(getString(R.string.trending))
                    true
                }
            }
        }
    }
}
