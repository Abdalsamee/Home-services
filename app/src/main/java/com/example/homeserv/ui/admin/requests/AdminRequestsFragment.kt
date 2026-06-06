package com.example.homeserv.ui.admin.requests

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import com.homeserv.databinding.FragmentAdminRequestsBinding
import com.homeserv.databinding.ItemAdminRequestBinding
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show

class AdminRequestsFragment : BaseFragment<FragmentAdminRequestsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminRequestsBinding.inflate(inflater, container, false)

    private val vm: AdminRequestsViewModel by viewModels()
    private lateinit var adapter: AdminRequestAdapter

    override fun setup() {
        adapter = AdminRequestAdapter(
            onInProgress = { req ->
                vm.updateStatus(req.id, ServiceRequest.STATUS_IN_PROGRESS)
            },
            onComplete = { req ->
                vm.updateStatus(req.id, ServiceRequest.STATUS_COMPLETED)
            }
        )
        binding.rvRequests.apply {
            this.adapter  = this@AdminRequestsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        setupChips()
        vm.loadRequests()
    }

    override fun observeData() {
        // Requests list
        vm.requests.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.show()
                    binding.layoutEmpty.hide()
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    if (result.data.isEmpty()) {
                        binding.layoutEmpty.show()
                        binding.rvRequests.hide()
                    } else {
                        binding.layoutEmpty.hide()
                        binding.rvRequests.show()
                        adapter.submitList(result.data.toList())
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showError(result.message)
                }
            }
        }

        // Show overlay while status is being updated
        vm.updating.observe(viewLifecycleOwner) { updating ->
            if (updating) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            val status = when {
                ids.contains(binding.chipPending.id)    -> ServiceRequest.STATUS_PENDING
                ids.contains(binding.chipInProgress.id) -> ServiceRequest.STATUS_IN_PROGRESS
                ids.contains(binding.chipCompleted.id)  -> ServiceRequest.STATUS_COMPLETED
                ids.contains(binding.chipCancelled.id)  -> ServiceRequest.STATUS_CANCELLED
                else -> null
            }
            vm.filterByStatus(status)
        }
    }
}

// ── Adapter ───────────────────────────────────────────────────────

class AdminRequestAdapter(
    private val onInProgress: (ServiceRequest) -> Unit,
    private val onComplete:   (ServiceRequest) -> Unit
) : ListAdapter<ServiceRequest, AdminRequestAdapter.VH>(Diff) {

    inner class VH(private val b: ItemAdminRequestBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(r: ServiceRequest) {
            b.tvCustomerName.text = r.customerName
            b.tvProviderName.text = r.providerName
            b.tvDate.text         = "${r.scheduledDate} · ${r.scheduledTime}"
            b.tvTotal.text        = r.formattedTotal()
            applyStatus(r.status)

            // Show rating if completed and rated
            if (r.status == ServiceRequest.STATUS_COMPLETED && r.rating > 0f) {
                b.layoutRating.visibility  = android.view.View.VISIBLE
                b.ratingBar.rating         = r.rating
                b.tvRatingValue.text       = "%.1f / 5".format(r.rating)
            } else {
                b.layoutRating.visibility  = android.view.View.GONE
            }

            // Show/hide buttons based on current status
            when (r.status) {
                ServiceRequest.STATUS_COMPLETED,
                ServiceRequest.STATUS_CANCELLED -> {
                    b.btnInProgress.hide()
                    b.btnComplete.hide()
                }
                ServiceRequest.STATUS_IN_PROGRESS -> {
                    b.btnInProgress.hide()
                    b.btnComplete.show()
                }
                else -> {
                    b.btnInProgress.show()
                    b.btnComplete.show()
                }
            }

            b.btnInProgress.setOnClickListener { onInProgress(r) }
            b.btnComplete.setOnClickListener   { onComplete(r) }
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
                else -> Triple(
                    "Cancelled", R.color.status_cancelled, R.color.status_cancelled_bg
                )
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

        private fun android.view.View.show() { visibility = android.view.View.VISIBLE }
        private fun android.view.View.hide() { visibility = android.view.View.GONE }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemAdminRequestBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    object Diff : DiffUtil.ItemCallback<ServiceRequest>() {
        override fun areItemsTheSame(a: ServiceRequest, b: ServiceRequest) = a.id == b.id
        override fun areContentsTheSame(a: ServiceRequest, b: ServiceRequest) = a == b
    }
}