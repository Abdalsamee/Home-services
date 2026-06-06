package com.example.homeserv.ui.customer.provider

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentProviderDetailBinding
import com.example.homeserv.utils.loadCircle

class ProviderDetailFragment : BaseFragment<FragmentProviderDetailBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProviderDetailBinding.inflate(inflater, container, false)

    private val vm: ProviderDetailViewModel by viewModels()
    private var providerId = ""

    override fun setup() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        providerId = arguments?.getString("providerId") ?: ""
        if (providerId.isNotEmpty()) {
            vm.loadProvider(providerId)
        } else {
            showError("Provider not found")
            findNavController().navigateUp()
        }
    }

    // Reload every time screen becomes visible (updated rating etc.)
    override fun onResume() {
        super.onResume()
        if (providerId.isNotEmpty()) vm.loadProvider(providerId)
    }

    override fun observeData() {
        vm.provider.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> { /* optionally show skeleton */ }
                is Resource.Success -> bindProvider(result.data)
                is Resource.Error   -> showError(result.message)
            }
        }
    }

    private fun bindProvider(p: Provider) {
        with(binding) {
            ivAvatar.loadCircle(p.imageUrl)
            tvName.text             = p.name
            tvCategory.text         = p.categoryName
            tvRating.text           = p.formattedRate()
            tvReviewCount.text      = "(${p.reviewCount} reviews)"
            ratingBarDetail.rating  = p.rateAsFloat()
            tvPrice.text            = "${p.pricePerHour.toInt()}"
            tvJobsDone.text         = p.jobsDone.toString()
            tvExperience.text       = "${p.experienceYears} yrs"
            tvDescription.text      = p.description
            tvAddress.text          = p.address
            tvPhone.text            = p.phone

            // Availability badge
            if (p.isAvailable) {
                tvAvailability.text = "Available"
                tvAvailability.setTextColor(
                    requireContext().getColor(R.color.status_completed)
                )
                tvAvailability.backgroundTintList = ColorStateList.valueOf(
                    requireContext().getColor(R.color.status_completed_bg)
                )
            } else {
                tvAvailability.text = "Unavailable"
                tvAvailability.setTextColor(
                    requireContext().getColor(R.color.status_cancelled)
                )
                tvAvailability.backgroundTintList = ColorStateList.valueOf(
                    requireContext().getColor(R.color.status_cancelled_bg)
                )
            }

            // Request Service
            btnRequestService.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("providerId", p.id)
                }
                findNavController().navigate(R.id.requestServiceFragment, bundle)
            }

            // View Map
            btnViewMap.setOnClickListener {
                val bundle = Bundle().apply {
                    putFloat("providerLat",  p.latitude.toFloat())
                    putFloat("providerLng",  p.longitude.toFloat())
                    putString("providerName", p.name)
                }
                findNavController().navigate(R.id.mapFragment, bundle)
            }
        }
    }
}