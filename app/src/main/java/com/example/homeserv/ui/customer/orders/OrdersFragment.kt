package com.example.homeserv.ui.customer.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import com.homeserv.databinding.FragmentOrdersBinding
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show

class OrdersFragment : BaseFragment<FragmentOrdersBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentOrdersBinding.inflate(inflater, container, false)

    private val vm: OrdersViewModel by viewModels()
    private lateinit var ordersAdapter: OrdersAdapter

    override fun setup() {
        setupRecycler()
        setupChips()
        binding.btnExplore.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        vm.loadOrders(session.userId)
    }

    override fun observeData() {
        vm.orders.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showShimmer(true)
                is Resource.Success -> {
                    showShimmer(false)
                    if (result.data.isEmpty()) {
                        binding.layoutEmpty.show()
                        binding.rvOrders.hide()
                    } else {
                        binding.layoutEmpty.hide()
                        binding.rvOrders.show()
                        ordersAdapter.submitList(result.data.toList())
                    }
                }
                is Resource.Error -> {
                    showShimmer(false)
                    showError(result.message)
                }
            }
        }

        vm.ratingResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> showSuccess("Rating submitted! Thank you.")
                is Resource.Error   -> showError(result.message)
                else -> {}
            }
        }
    }

    private fun setupRecycler() {
        ordersAdapter = OrdersAdapter { requestId, providerId, rating ->
            vm.submitRating(requestId, providerId, rating)
        }
        binding.rvOrders.apply {
            adapter       = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when {
                checkedIds.contains(binding.chipPending.id)    ->
                    ServiceRequest.STATUS_PENDING
                checkedIds.contains(binding.chipInProgress.id) ->
                    ServiceRequest.STATUS_IN_PROGRESS
                checkedIds.contains(binding.chipCompleted.id)  ->
                    ServiceRequest.STATUS_COMPLETED
                checkedIds.contains(binding.chipCancelled.id)  ->
                    ServiceRequest.STATUS_CANCELLED
                else -> null
            }
            vm.filterByStatus(status)
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerOrders.show()
            binding.shimmerOrders.startShimmer()
            binding.rvOrders.hide()
            binding.layoutEmpty.hide()
        } else {
            binding.shimmerOrders.stopShimmer()
            binding.shimmerOrders.hide()
        }
    }
}