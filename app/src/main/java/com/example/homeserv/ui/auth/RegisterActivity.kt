package com.example.homeserv.ui.auth



import androidx.activity.viewModels
import com.example.homeserv.base.BaseActivity
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.ActivityRegisterBinding
import com.example.homeserv.ui.customer.MainActivity
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.isValidEmail
import com.example.homeserv.utils.isValidPassword
import com.example.homeserv.utils.isValidPhone
import com.example.homeserv.utils.navigateAndClearStack
import com.example.homeserv.utils.show

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    override fun inflateBinding() = ActivityRegisterBinding.inflate(layoutInflater)

    private val vm: AuthViewModel by viewModels()

    override fun setup() {
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            attemptRegister()
        }

        vm.registerResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    session.saveUser(result.data)
                    navigateAndClearStack<MainActivity>()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showErrorBanner(result.message)
                }
            }
        }
    }

    private fun attemptRegister() {
        val name     = binding.etFullName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val phone    = binding.etPhone.text.toString().trim()
        val pass     = binding.etPassword.text.toString()
        val confirm  = binding.etConfirmPassword.text.toString()

        // Clear previous errors
        listOf(
            binding.tilFullName, binding.tilEmail,
            binding.tilPhone, binding.tilPassword, binding.tilConfirmPassword
        ).forEach { it.error = null }

        var valid = true

        if (name.length < 2) {
            binding.tilFullName.error = "Enter your full name"
            valid = false
        }
        if (!email.isValidEmail()) {
            binding.tilEmail.error = "Enter a valid email"
            valid = false
        }
        if (!phone.isValidPhone()) {
            binding.tilPhone.error = "Enter a valid phone number"
            valid = false
        }
        if (!pass.isValidPassword()) {
            binding.tilPassword.error = "At least 6 characters required"
            valid = false
        }
        if (pass != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        }

        if (!valid) return

        vm.register(name, email, phone, pass)
    }

    private fun showLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
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