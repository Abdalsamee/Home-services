package com.example.homeserv.base


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.example.homeserv.utils.SessionManager

/**
 * All Fragments extend this.
 * Provides: safe ViewBinding lifecycle, snackbar helpers,
 * keyboard dismissal, and session access.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var session: SessionManager

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        session  = SessionManager(requireContext())
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        observeData()
    }

    // Subclasses override these
    open fun setup() {}
    open fun observeData() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null          // Prevent memory leaks
    }

    // ── Snackbar helpers ──────────────────────────────────────────

    fun showSnack(message: String, isError: Boolean = false) {
        val sb = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        if (isError) sb.setBackgroundTint(
            requireContext().getColor(com.homeserv.R.color.error)
        )
        sb.show()
    }

    fun showError(message: String)   = showSnack(message, isError = true)
    fun showSuccess(message: String) = showSnack(message, isError = false)

    // ── Keyboard ──────────────────────────────────────────────────

    fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}