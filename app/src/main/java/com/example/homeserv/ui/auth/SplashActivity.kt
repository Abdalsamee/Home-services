package com.example.homeserv.ui.auth

import android.os.Handler
import android.os.Looper
import com.example.homeserv.base.BaseActivity
import com.homeserv.databinding.ActivitySplashBinding
import com.example.homeserv.ui.admin.AdminActivity
import com.example.homeserv.ui.customer.MainActivity
import com.example.homeserv.utils.Constants
import com.example.homeserv.utils.navigateAndClearStack

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun inflateBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun setup() {
        animateContent()

        // Auto-navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            decideNavigation()
        }, Constants.SPLASH_DELAY)
    }

    private fun animateContent() {
        // Animate center logo
        binding.layoutCenter.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(Constants.ANIM_DURATION_LONG)
            .setStartDelay(200)
            .start()

        // Animate bottom loading
        binding.layoutBottom.animate()
            .alpha(1f)
            .setDuration(Constants.ANIM_DURATION_LONG)
            .setStartDelay(500)
            .start()
    }

    private fun decideNavigation() {
        when {
            !session.isLoggedIn -> navigateAndClearStack<LoginActivity>()
            session.isAdmin     -> navigateAndClearStack<AdminActivity>()
            else                -> navigateAndClearStack<MainActivity>()
        }
    }
}