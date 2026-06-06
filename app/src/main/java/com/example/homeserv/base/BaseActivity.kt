package com.example.homeserv.base



import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.example.homeserv.utils.SessionManager

/**
 * All Activities extend this.
 * Provides: ViewBinding inflation, snackbar helpers,
 * keyboard dismissal, and session access.
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var session: SessionManager

    // Subclasses implement this to inflate their binding
    abstract fun inflateBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session  = SessionManager(this)
        _binding = inflateBinding()
        setContentView(binding.root)
        setup()
    }

    // Subclasses override this instead of onCreate
    open fun setup() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // ── Snackbar helpers ──────────────────────────────────────────

    fun showSnack(message: String, isError: Boolean = false) {
        val root = findViewById<View>(android.R.id.content)
        val sb   = Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
        if (isError) sb.setBackgroundTint(getColor(com.homeserv.R.color.error))
        sb.show()
    }

    fun showError(message: String)   = showSnack(message, isError = true)
    fun showSuccess(message: String) = showSnack(message, isError = false)

    // ── Keyboard ──────────────────────────────────────────────────

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}