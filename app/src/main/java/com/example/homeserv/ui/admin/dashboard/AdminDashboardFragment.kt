package com.example.homeserv.ui.admin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.repository.AuthRepository
import com.homeserv.databinding.FragmentAdminDashboardBinding
import com.example.homeserv.ui.auth.LoginActivity
import com.example.homeserv.ui.customer.search.ProviderAdapter
import com.example.homeserv.utils.navigateAndClearStack

class AdminDashboardFragment : BaseFragment<FragmentAdminDashboardBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminDashboardBinding.inflate(inflater, container, false)

    private val vm: AdminDashboardViewModel by viewModels()
    private val authRepo = AuthRepository()
    private lateinit var topProvidersAdapter: ProviderAdapter

    override fun setup() {
        setupRecyclers()

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    authRepo.logout()
                    session.clear()
                    requireActivity().navigateAndClearStack<LoginActivity>()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Refresh every time the dashboard becomes visible
    override fun onResume() {
        super.onResume()
        vm.loadStats()
        vm.loadTopProviders()
        vm.fixProviderRatingTypes() // one-time migration
    }

    override fun observeData() {
        vm.stats.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val data = result.data
                binding.tvTotalRequests.text =
                    (data["total"] as? Int ?: 0).toString()
                binding.tvCompleted.text =
                    (data["completed"] as? Int ?: 0).toString()
                binding.tvPending.text =
                    (data["pending"] as? Int ?: 0).toString()

                @Suppress("UNCHECKED_CAST")
                val byCategory = data["byCategory"] as? Map<String, Int> ?: emptyMap()
                if (byCategory.isNotEmpty())
                    binding.rvMostRequested.adapter =
                        MostRequestedAdapter(byCategory.entries.toList())
            }
        }

        vm.topProviders.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success)
                topProvidersAdapter.submitList(result.data)
        }

        vm.totalProviders.observe(viewLifecycleOwner) { count ->
            binding.tvTotalProviders.text = count.toString()
        }
    }

    private fun setupRecyclers() {
        topProvidersAdapter = ProviderAdapter { /* view only */ }
        binding.rvTopProviders.apply {
            adapter       = topProvidersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.rvMostRequested.layoutManager = LinearLayoutManager(requireContext())
    }
}