package com.example.homeserv.ui.customer.orders

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeserv.R
import com.example.homeserv.data.model.ServiceRequest
import com.homeserv.databinding.DialogRatingBinding
import com.homeserv.databinding.ItemOrderBinding
import com.example.homeserv.utils.loadCircle

class OrdersAdapter(
    private val onRatingSubmit: (requestId: String, providerId: String, rating: Float) -> Unit
) : ListAdapter<ServiceRequest, OrdersAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val b: ItemOrderBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(order: ServiceRequest) {
            b.tvProviderName.text = order.providerName
            b.tvCategoryName.text = order.categoryName
            b.tvDate.text         = "${order.scheduledDate} · ${order.scheduledTime}"
            b.tvTotal.text        = order.formattedTotal()
            b.tvAddress.text      = order.address
            b.ivProviderAvatar.loadCircle(order.providerImageUrl)
            applyStatus(order.status)
            handleRating(order)
        }

        private fun handleRating(order: ServiceRequest) {
            when {
                // Completed + not yet rated
                order.status == ServiceRequest.STATUS_COMPLETED && !order.isRated -> {
                    b.btnRate.visibility     = android.view.View.VISIBLE
                    b.layoutRated.visibility = android.view.View.GONE
                    b.btnRate.setOnClickListener { showRatingDialog(order) }
                }
                // Completed + already rated
                order.status == ServiceRequest.STATUS_COMPLETED && order.isRated -> {
                    b.btnRate.visibility     = android.view.View.GONE
                    b.layoutRated.visibility = android.view.View.VISIBLE
                    val stars = order.rating.toInt().coerceIn(1, 5)
                    b.tvRatingGiven.text = "You rated: ${"★".repeat(stars)} ($stars/5)"
                }
                // Any other status
                else -> {
                    b.btnRate.visibility     = android.view.View.GONE
                    b.layoutRated.visibility = android.view.View.GONE
                }
            }
        }

        private fun showRatingDialog(order: ServiceRequest) {
            val ctx     = b.root.context
            val dialog  = Dialog(ctx)
            val dBind   = DialogRatingBinding.inflate(LayoutInflater.from(ctx))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(dBind.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(
                (ctx.resources.displayMetrics.widthPixels * 0.88).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            dBind.tvProviderName.text = order.providerName

            val labels = listOf("", "Poor", "Fair", "Good", "Very Good", "Excellent")

            dBind.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                val stars = rating.toInt()
                if (stars > 0) {
                    dBind.tvRatingLabel.text      = "${"★".repeat(stars)} ${labels[stars]}"
                    dBind.btnSubmitRating.isEnabled = true
                } else {
                    dBind.tvRatingLabel.text        = "Tap a star to rate"
                    dBind.btnSubmitRating.isEnabled = false
                }
            }

            dBind.btnSubmitRating.setOnClickListener {
                val rating = dBind.ratingBar.rating
                if (rating > 0f) {
                    onRatingSubmit(order.id, order.providerId, rating)
                    dialog.dismiss()
                }
            }

            dBind.btnSkip.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        private fun applyStatus(status: String) {
            val ctx = b.root.context
            val (label, textColorRes, bgColorRes) = when (status) {
                ServiceRequest.STATUS_PENDING -> Triple(
                    "Pending", R.color.status_pending, R.color.status_pending_bg
                )
                ServiceRequest.STATUS_IN_PROGRESS -> Triple(
                    "In Progress", R.color.status_in_progress, R.color.status_in_progress_bg
                )
                ServiceRequest.STATUS_COMPLETED -> Triple(
                    "Completed", R.color.status_completed, R.color.status_completed_bg
                )
                ServiceRequest.STATUS_CANCELLED -> Triple(
                    "Cancelled", R.color.status_cancelled, R.color.status_cancelled_bg
                )
                else -> Triple("Unknown", R.color.text_secondary, R.color.divider)
            }
            b.tvStatus.text = label
            b.tvStatus.setTextColor(ContextCompat.getColor(ctx, textColorRes))
            val drawable = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = 20f * ctx.resources.displayMetrics.density
                setColor(ContextCompat.getColor(ctx, bgColorRes))
            }
            b.tvStatus.background = drawable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<ServiceRequest>() {
        override fun areItemsTheSame(a: ServiceRequest, b: ServiceRequest) = a.id == b.id
        override fun areContentsTheSame(a: ServiceRequest, b: ServiceRequest) = a == b
    }
}