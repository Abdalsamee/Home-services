package com.example.homeserv.ui.admin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.homeserv.databinding.ItemMostRequestedBinding

class MostRequestedAdapter(
    private val items: List<Map.Entry<String, Int>>
) : RecyclerView.Adapter<MostRequestedAdapter.ViewHolder>() {

    inner class ViewHolder(private val b: ItemMostRequestedBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: Map.Entry<String, Int>, rank: Int) {
            b.tvRank.text     = "#$rank"
            b.tvCategory.text = item.key
            b.tvCount.text    = "${item.value} requests"

            // Progress bar width relative to max
            val max = items.maxOfOrNull { it.value } ?: 1
            b.progressBar.max      = max
            b.progressBar.progress = item.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemMostRequestedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(h: ViewHolder, pos: Int) =
        h.bind(items[pos], pos + 1)

    override fun getItemCount() = items.size
}