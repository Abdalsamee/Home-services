package com.example.homeserv.ui.admin.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentAdminCategoriesBinding
import com.example.homeserv.ui.customer.home.CategoryAdapter
import com.example.homeserv.utils.hide
import com.example.homeserv.utils.show

class AdminCategoriesFragment : BaseFragment<FragmentAdminCategoriesBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminCategoriesBinding.inflate(inflater, container, false)

    private val vm: AdminCategoriesViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun setup() {
        adapter = CategoryAdapter { category ->
            // Navigate to edit screen with category ID
            val action = AdminCategoriesFragmentDirections
                .actionCategoriesToAddCategory(categoryId = category.id)
            findNavController().navigate(action)
        }
        binding.rvCategories.apply {
            this.adapter  = this@AdminCategoriesFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        // FAB → Add new category (empty ID = add mode)
        binding.fabAdd.setOnClickListener {
            val action = AdminCategoriesFragmentDirections
                .actionCategoriesToAddCategory(categoryId = "")
            findNavController().navigate(action)
        }

        vm.loadCategories()
    }

    override fun observeData() {
        vm.categories.observe(viewLifecycleOwner) { result ->
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