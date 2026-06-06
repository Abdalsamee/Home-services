package com.example.homeserv.ui.customer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentHomeBinding
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show
import java.util.Calendar

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    private val vm: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun setup() {
        setupGreeting()
        setupRecycler()
        setupClickListeners()
    }

    // Reload every time fragment is visible
    override fun onResume() {
        super.onResume()
        vm.loadCategories()
    }

    override fun observeData() {
        vm.categories.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> showShimmer(true)
                is Resource.Success -> {
                    showShimmer(false)
                    if (result.data.isEmpty()) {
                        binding.layoutEmpty.show()
                        binding.rvCategories.hide()
                    } else {
                        binding.layoutEmpty.hide()
                        binding.rvCategories.show()
                        categoryAdapter.submitList(result.data)
                    }
                }
                is Resource.Error -> {
                    showShimmer(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else      -> "Good Evening 👋"
        }
        binding.tvUserName.text = session.userName.ifEmpty { "Welcome!" }
    }

    private fun setupRecycler() {
        categoryAdapter = CategoryAdapter { category ->
            // Navigate to search with category filter using Bundle
            val bundle = Bundle().apply {
                putString("categoryId",   category.id)
                putString("categoryName", category.name)
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }
        binding.rvCategories.apply {
            adapter       = categoryAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupClickListeners() {
        binding.layoutSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerCategories.show()
            binding.shimmerCategories.startShimmer()
            binding.rvCategories.hide()
        } else {
            binding.shimmerCategories.stopShimmer()
            binding.shimmerCategories.hide()
        }
    }
}