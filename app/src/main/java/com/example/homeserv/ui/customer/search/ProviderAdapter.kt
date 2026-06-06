package com.example.homeserv.ui.customer.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeserv.R
import com.example.homeserv.data.model.Provider
import com.homeserv.databinding.ItemProviderBinding
import com.example.homeserv.utils.loadCircle

class ProviderAdapter(
    private val onViewClick: (Provider) -> Unit
) : ListAdapter<Provider, ProviderAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val b: ItemProviderBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(p: Provider) {
            b.tvProviderName.text = p.name
            b.tvCategoryName.text = p.categoryName
            b.tvPrice.text        = p.formattedPrice()
            b.tvDistance.text     = "— km"
            b.ivProviderAvatar.loadCircle(p.imageUrl)

            // ── Rating display ────────────────────────────────────
            val rateFloat = p.rateAsFloat()
            b.ratingBar.rating    = rateFloat
            b.tvRating.text       = p.formattedRate()
            b.tvReviewCount.text  = if (p.reviewCount > 0)
                "(${p.reviewCount})" else "(No reviews)"

            // ── Availability badge ────────────────────────────────
            val ctx = b.root.context
            if (p.isAvailable) {
                b.tvAvailability.text = "Available"
                b.tvAvailability.setTextColor(ctx.getColor(R.color.status_completed))
                b.tvAvailability.backgroundTintList =
                    ColorStateList.valueOf(ctx.getColor(R.color.status_completed_bg))
            } else {
                b.tvAvailability.text = "Unavailable"
                b.tvAvailability.setTextColor(ctx.getColor(R.color.status_cancelled))
                b.tvAvailability.backgroundTintList =
                    ColorStateList.valueOf(ctx.getColor(R.color.status_cancelled_bg))
            }

            b.btnViewProvider.setOnClickListener { onViewClick(p) }
            b.root.setOnClickListener { onViewClick(p) }

            b.root.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN ->
                        v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(120).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemProviderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Provider>() {
        override fun areItemsTheSame(a: Provider, b: Provider) = a.id == b.id
        override fun areContentsTheSame(a: Provider, b: Provider) = a == b
    }
}