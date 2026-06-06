package com.example.homeserv.ui.admin

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.homeserv.R
import com.example.homeserv.base.BaseActivity
import com.homeserv.databinding.ActivityAdminBinding

class AdminActivity : BaseActivity<ActivityAdminBinding>() {

    override fun inflateBinding() = ActivityAdminBinding.inflate(layoutInflater)

    private lateinit var navController: NavController

    override fun setup() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.adminNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        AppBarConfiguration(
            setOf(
                R.id.adminDashboardFragment,
                R.id.adminRequestsFragment,
                R.id.adminProvidersFragment,
                R.id.adminCategoriesFragment
            )
        )

        binding.adminBottomNav.setupWithNavController(navController)
        binding.adminBottomNav.setOnItemReselectedListener { /* do nothing */ }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}