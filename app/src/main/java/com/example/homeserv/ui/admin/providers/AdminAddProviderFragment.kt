package com.example.homeserv.ui.admin.providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentAdminAddProviderBinding
import com.example.homeserv.utils.ImageHelper
import com.example.homeserv.utils.disable
import com.example.homeserv.utils.enable
import com.example.homeserv.utils.loadCircle
import com.example.homeserv.utils.loadUri
import com.example.homeserv.utils.show

class AdminAddProviderFragment : BaseFragment<FragmentAdminAddProviderBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAdminAddProviderBinding.inflate(inflater, container, false)

    private val vm: AdminProvidersViewModel by viewModels()
    private val args: AdminAddProviderFragmentArgs by navArgs()

    private var selectedImagePath    = ""
    private var selectedCategoryId   = ""
    private var selectedCategoryName = ""
    private var selectedLat          = 0.0
    private var selectedLng          = 0.0
    private var categories: List<Category> = emptyList()
    private var existingProvider: Provider? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            binding.ivProviderImage.loadUri(it)
            selectedImagePath = ImageHelper.saveImage(
                requireContext(), it, "providers",
                ImageHelper.generateFileName("provider")
            ) ?: ""
        }
    }

    override fun setup() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPickImage.setOnClickListener { imagePicker.launch("image/*") }
        binding.btnPickLocation.setOnClickListener { openMapPicker() }
        binding.btnSave.setOnClickListener { saveProvider() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        vm.loadCategories()

        if (args.providerId.isNotEmpty()) {
            binding.tvTitle.text = "Edit Provider"
            binding.btnDelete.show()
            vm.loadProvider(args.providerId)
        }

        // Listen for location result from MapPickerFragment
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Double>("picked_lat")
            ?.observe(viewLifecycleOwner) { lat ->
                val lng = findNavController()
                    .currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Double>("picked_lng") ?: 0.0
                if (lat != 0.0) {
                    selectedLat = lat
                    selectedLng = lng
                    binding.tvSelectedLocation.text =
                        "📍 %.5f, %.5f".format(selectedLat, selectedLng)
                }
            }
    }

    override fun observeData() {
        vm.categories.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                categories = result.data
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories.map { it.name }
                )
                binding.actvCategory.setAdapter(adapter)
                binding.actvCategory.setOnItemClickListener { _, _, pos, _ ->
                    selectedCategoryId   = categories[pos].id
                    selectedCategoryName = categories[pos].name
                }
                existingProvider?.let { p ->
                    binding.actvCategory.setText(p.categoryName, false)
                }
            }
        }

        vm.provider.observe(viewLifecycleOwner) { result ->
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
                    binding.btnSave.text = "Save Provider"
                    showSuccess("Provider saved successfully")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.btnSave.enable()
                    binding.btnSave.text = "Save Provider"
                    showError(result.message)
                }
            }
        }

        vm.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                showSuccess("Provider deleted")
                findNavController().navigateUp()
            }
        }
    }

    private fun openMapPicker() {
        val bundle = Bundle().apply {
            putFloat("initLat", selectedLat.toFloat())
            putFloat("initLng", selectedLng.toFloat())
        }
        findNavController().navigate(R.id.action_addProvider_to_mapPicker, bundle)
    }

    private fun prefillForm(p: Provider) {
        existingProvider     = p
        selectedCategoryId   = p.categoryId
        selectedCategoryName = p.categoryName
        selectedImagePath    = p.imageUrl
        selectedLat          = p.latitude
        selectedLng          = p.longitude

        with(binding) {
            etName.setText(p.name)
            etPhone.setText(p.phone)
            etAddress.setText(p.address)
            etPrice.setText(p.pricePerHour.toString())
            etExperience.setText(p.experienceYears.toString())
            etDescription.setText(p.description)
            actvCategory.setText(p.categoryName, false)
            switchAvailable.isChecked = p.isAvailable
            ivProviderImage.loadCircle(p.imageUrl)
            if (p.latitude != 0.0 || p.longitude != 0.0)
                tvSelectedLocation.text =
                    "📍 %.5f, %.5f".format(p.latitude, p.longitude)
        }
    }

    private fun saveProvider() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"; return
        } else binding.tilName.error = null

        if (selectedCategoryId.isEmpty()) {
            binding.tilCategory.error = "Select a category"; return
        } else binding.tilCategory.error = null

        if (selectedLat == 0.0 && selectedLng == 0.0) {
            showError("Please pick a location on the map"); return
        }

        val provider = Provider(
            id              = existingProvider?.id ?: "",
            name            = name,
            phone           = binding.etPhone.text.toString().trim(),
            address         = binding.etAddress.text.toString().trim(),
            latitude        = selectedLat,
            longitude       = selectedLng,
            pricePerHour    = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0,
            experienceYears = binding.etExperience.text.toString().toIntOrNull() ?: 0,
            description     = binding.etDescription.text.toString().trim(),
            categoryId      = selectedCategoryId,
            categoryName    = selectedCategoryName,
            isAvailable     = binding.switchAvailable.isChecked,
            imageUrl        = existingProvider?.imageUrl ?: ""
        )

        if (existingProvider != null)
            vm.updateProvider(provider, selectedImagePath)
        else
            vm.addProvider(provider, selectedImagePath)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Provider")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                existingProvider?.let { vm.deleteProvider(it.id) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}