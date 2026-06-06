package com.example.homeserv.ui.auth

import androidx.activity.viewModels
import com.example.homeserv.base.BaseActivity
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.ActivityLoginBinding
import com.example.homeserv.ui.admin.AdminActivity
import com.example.homeserv.ui.customer.MainActivity
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.isValidEmail
import com.example.homeserv.utils.isValidPassword
import com.example.homeserv.utils.navigate
import com.example.homeserv.utils.navigateAndClearStack
import com.example.homeserv.utils.show

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun inflateBinding() = ActivityLoginBinding.inflate(layoutInflater)

    private val vm: AuthViewModel by viewModels()

    override fun setup() {
        binding.tvRegister.setOnClickListener {
            navigate<RegisterActivity>()
        }

        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            attemptLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            showSnack("Password reset coming soon")
        }

        vm.loginResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    session.saveUser(result.data)
                    if (session.isAdmin) navigateAndClearStack<AdminActivity>()
                    else navigateAndClearStack<MainActivity>()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showErrorBanner(result.message)
                }
            }
        }
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!email.isValidEmail()) {
            binding.tilEmail.error = "Enter a valid email"
            return
        } else binding.tilEmail.error = null

        if (!password.isValidPassword()) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return
        } else binding.tilPassword.error = null

        vm.login(email, password)
    }

    private fun showLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        if (loading) {
            binding.progressBar.show()
            binding.layoutError.hide()
        } else {
            binding.progressBar.hide()
        }
    }

    private fun showErrorBanner(message: String) {
        binding.tvError.text = message
        binding.layoutError.show()
    }
}