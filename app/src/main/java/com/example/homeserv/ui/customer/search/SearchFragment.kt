package com.example.homeserv.ui.customer.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.SortOption
import com.homeserv.databinding.FragmentSearchBinding
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show

class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSearchBinding.inflate(inflater, container, false)

    private val vm: SearchViewModel by viewModels()
    private lateinit var providerAdapter: ProviderAdapter

    override fun setup() {
        setupRecycler()
        setupSearch()
        setupChips()

        // Read category args safely without Safe Args
        val categoryId   = arguments?.getString("categoryId")   ?: ""
        val categoryName = arguments?.getString("categoryName") ?: ""

        if (categoryName.isNotEmpty())
            binding.tvTitle.text = categoryName

        if (categoryId.isNotEmpty())
            vm.loadByCategory(categoryId)
        else
            vm.loadAll()
    }

    override fun observeData() {
        vm.providers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showShimmer(true)
                is Resource.Success -> {
                    showShimmer(false)
                    val list = result.data
                    binding.tvResultCount.text = "${list.size} results found"
                    if (list.isEmpty()) {
                        binding.layoutEmpty.show()
                        binding.rvProviders.hide()
                    } else {
                        binding.layoutEmpty.hide()
                        binding.rvProviders.show()
                        providerAdapter.submitList(list)
                    }
                }
                is Resource.Error -> {
                    showShimmer(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun setupRecycler() {
        providerAdapter = ProviderAdapter { provider ->
            // Navigate using Bundle — avoids Safe Args issues
            val bundle = Bundle().apply {
                putString("providerId", provider.id)
            }
            findNavController().navigate(
                R.id.providerDetailFragment, bundle
            )
        }
        binding.rvProviders.apply {
            adapter       = providerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val q = s.toString().trim()
                if (q.isNotEmpty()) binding.ivClear.show() else binding.ivClear.hide()
                vm.onQueryChanged(q)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.ivClear.setOnClickListener {
            binding.etSearch.setText("")
            binding.ivClear.hide()
        }
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(binding.chipAvailable.id) -> {
                    vm.onAvailableOnly(true)
                    vm.onSortChanged(SortOption.RATING)
                }
                checkedIds.contains(binding.chipTopRated.id) -> {
                    vm.onAvailableOnly(false)
                    vm.onSortChanged(SortOption.RATING)
                }
                checkedIds.contains(binding.chipPriceLow.id) -> {
                    vm.onAvailableOnly(false)
                    vm.onSortChanged(SortOption.PRICE_LOW)
                }
                checkedIds.contains(binding.chipPriceHigh.id) -> {
                    vm.onAvailableOnly(false)
                    vm.onSortChanged(SortOption.PRICE_HIGH)
                }
                else -> vm.resetFilters()
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerProviders.show()
            binding.shimmerProviders.startShimmer()
            binding.rvProviders.hide()
            binding.layoutEmpty.hide()
        } else {
            binding.shimmerProviders.stopShimmer()
            binding.shimmerProviders.hide()
        }
    }
}