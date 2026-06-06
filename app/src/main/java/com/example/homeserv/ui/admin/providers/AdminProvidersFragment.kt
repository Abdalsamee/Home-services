package com.example.homeserv.ui.admin.providers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentAdminProvidersBinding
import com.example.homeserv.ui.customer.search.ProviderAdapter
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show

class AdminProvidersFragment : BaseFragment<FragmentAdminProvidersBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminProvidersBinding.inflate(inflater, container, false)

    private val vm: AdminProvidersViewModel by viewModels()
    private lateinit var adapter: ProviderAdapter

    override fun setup() {
        adapter = ProviderAdapter { provider ->
            val action = AdminProvidersFragmentDirections
                .actionProvidersToAddProvider(providerId = provider.id)
            findNavController().navigate(action)
        }
        binding.rvProviders.apply {
            this.adapter  = this@AdminProvidersFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.fabAdd.setOnClickListener {
            val action = AdminProvidersFragmentDirections
                .actionProvidersToAddProvider(providerId = "")
            findNavController().navigate(action)
        }
        vm.loadProviders()
    }

    override fun observeData() {
        vm.providers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> binding.progressBar.show()
                is Resource.Success -> {
                    binding.progressBar.hide()
                    adapter.submitList(result.data)
                }
                is Resource.Error -> {
                    binding.progressBar.hide()
                    showError(result.message)
                }
            }
        }
    }
}