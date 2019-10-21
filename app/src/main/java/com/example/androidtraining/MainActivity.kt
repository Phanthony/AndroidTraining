package com.example.androidtraining

import android.content.Context
import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var gitHubViewModel: GitHubViewModelDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up ViewModel
        gitHubViewModel = ViewModelProviders.of(this).get(GitHubViewModelDependencies::class.java)

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
                         ToolBar.title = getString(R.string.Issues)
                     }
                     else{
                         controller.navigate(R.id.login_dest)
                         ToolBar.title = getString(R.string.Login)
                     }
                     true
                 }
                else -> {
                    controller.navigate(item.itemId)
                    ToolBar.title = getString(R.string.trending)
                    true
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        gitHubViewModel.getComposite().clear()
    }

}
