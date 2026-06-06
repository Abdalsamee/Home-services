package com.example.homeserv.ui.customer.home



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.homeserv.data.model.Category
import com.homeserv.databinding.ItemCategoryBinding
import com.example.homeserv.utils.loadUrl

class CategoryAdapter(
    private val onClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            binding.tvCategoryName.text  = item.name
            binding.tvProviderCount.text = "${item.providerCount} providers"
            binding.ivCategoryIcon.loadUrl(item.imageUrl)
            binding.root.setOnClickListener { onClick(item) }

            // Press scale animation
            binding.root.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN ->
                        v.animate().scaleX(0.95f).scaleY(0.95f)
                            .setDuration(150).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL ->
                        v.animate().scaleX(1f).scaleY(1f)
                            .setDuration(150).start()
                }
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(a: Category, b: Category) = a.id == b.id
        override fun areContentsTheSame(a: Category, b: Category) = a == b
    }
}