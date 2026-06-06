package com.example.homeserv.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.homeserv.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── View ─────────────────────────────────────────────────────────

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.enable()  { isEnabled = true;  alpha = 1f }
fun View.disable() { isEnabled = false; alpha = 0.5f }

fun View.snack(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionLabel: String? = null,
    action: (() -> Unit)? = null
) {
    val sb = Snackbar.make(this, message, duration)
    if (actionLabel != null && action != null)
        sb.setAction(actionLabel) { action() }
    sb.show()
}

// ── Activity ──────────────────────────────────────────────────────

fun Activity.toast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
}

inline fun <reified T : Activity> Activity.navigate(
    finishCurrent: Boolean = false,
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply(block)
    startActivity(intent)
    if (finishCurrent) finish()
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

inline fun <reified T : Activity> Activity.navigateAndClearStack() {
    val intent = Intent(this, T::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

// ── Fragment ──────────────────────────────────────────────────────

fun Fragment.toast(msg: String) =
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

fun Fragment.hideKeyboard() {
    val imm = requireContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
}

// ── ImageView ─────────────────────────────────────────────────────

fun ImageView.loadUrl(
    path: String?,
    placeholder: Int = R.drawable.ic_logo
) {
    val source: Any = when {
        path.isNullOrEmpty()    -> placeholder
        path.startsWith("http") -> path                  // remote URL
        else                    -> java.io.File(path)    // local file path
    }
    Glide.with(context)
        .load(source)
        .placeholder(placeholder)
        .error(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

fun ImageView.loadCircle(
    path: String?,
    placeholder: Int = R.drawable.ic_logo
) {
    val source: Any = when {
        path.isNullOrEmpty()    -> placeholder
        path.startsWith("http") -> path
        else                    -> java.io.File(path)
    }
    Glide.with(context)
        .load(source)
        .placeholder(placeholder)
        .error(placeholder)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

// Load directly from a URI (for preview before saving)
fun ImageView.loadUri(
    uri: android.net.Uri?,
    placeholder: Int = R.drawable.ic_logo
) {
    Glide.with(context)
        .load(uri)
        .placeholder(placeholder)
        .error(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

// ── String ────────────────────────────────────────────────────────

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPhone(): Boolean =
    length in 7..15 && all { it.isDigit() || it == '+' || it == '-' }

fun String.isValidPassword(): Boolean = length >= 6

// ── Long (timestamp) ─────────────────────────────────────────────

fun Long.toDateString(pattern: String = "MMM d, yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000      -> "Just now"
        diff < 3_600_000   -> "${diff / 60_000}m ago"
        diff < 86_400_000  -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else               -> this.toDateString()
    }
}

// ── Double ────────────────────────────────────────────────────────

fun Double.toCurrency(): String = "$%.2f".format(this)

fun Double.toDistanceString(): String =
    if (this < 1.0) "${(this * 1000).toInt()} m"
    else "%.1f km".format(this)