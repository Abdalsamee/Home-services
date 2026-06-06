package com.example.homeserv.ui.admin.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentAdminAddCategoryBinding
import com.example.homeserv.utils.ImageHelper
import com.example.homeserv.utils.disable
import com.example.homeserv.utils.enable
import com.example.homeserv.utils.loadUri
import com.example.homeserv.utils.loadUrl
import com.example.homeserv.utils.show

class AdminAddCategoryFragment : BaseFragment<FragmentAdminAddCategoryBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminAddCategoryBinding.inflate(inflater, container, false)

    private val vm: AdminCategoriesViewModel by viewModels()
    private val args: AdminAddCategoryFragmentArgs by navArgs()

    private var selectedImagePath  = ""
    private var existingCategory: Category? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            binding.ivCategoryImage.loadUri(it)
            selectedImagePath = ImageHelper.saveImage(
                requireContext(), it,
                "categories",
                ImageHelper.generateFileName("category")
            ) ?: ""
        }
    }

    override fun setup() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPickImage.setOnClickListener { imagePicker.launch("image/*") }
        binding.btnSave.setOnClickListener { saveCategory() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        // If categoryId is not empty → edit mode
        if (args.categoryId.isNotEmpty()) {
            binding.tvTitle.text = "Edit Category"
            binding.btnDelete.show()
            vm.loadCategory(args.categoryId)
        }
    }

    override fun observeData() {
        vm.category.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) prefillForm(result.data)
        }

        vm.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnSave.disable()
                    binding.btnSave.text = "Saving…"
                }
                is Resource.Success -> {
                    binding.btnSave.enable()
                    binding.btnSave.text = "Save Category"
                    showSuccess("Category saved")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnSave.enable()
                    binding.btnSave.text = "Save Category"
                    showError(result.message)
                }
            }
        }

        vm.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                showSuccess("Category deleted")
                findNavController().navigateUp()
            }
        }
    }

    private fun prefillForm(c: Category) {
        existingCategory  = c
        selectedImagePath = c.imageUrl
        binding.etName.setText(c.name)
        binding.etDescription.setText(c.description)
        binding.ivCategoryImage.loadUrl(c.imageUrl)
    }

    private fun saveCategory() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        } else binding.tilName.error = null

        val category = Category(
            id          = existingCategory?.id ?: "",
            name        = name,
            description = binding.etDescription.text.toString().trim(),
            imageUrl    = existingCategory?.imageUrl ?: ""
        )

        if (existingCategory != null)
            vm.updateCategory(category, selectedImagePath)
        else
            vm.addCategory(category, selectedImagePath)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                existingCategory?.let { vm.deleteCategory(it.id) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}