package com.example.homeserv.ui.customer

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.homeserv.R
import com.example.homeserv.base.BaseActivity
import com.homeserv.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding() = ActivityMainBinding.inflate(layoutInflater)

    private lateinit var navController: NavController

    override fun setup() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations — no back arrow shown for these
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.ordersFragment,
                R.id.profileFragment
            )
        )

        // Wire bottom nav to nav controller
        binding.bottomNav.setupWithNavController(navController)

        // Prevent re-navigation when tapping the current tab
        binding.bottomNav.setOnItemReselectedListener { /* do nothing */ }
    }

    // Allow fragments to navigate back with system back button
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}