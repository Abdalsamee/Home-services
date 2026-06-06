package com.example.homeserv.ui.customer.request

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.homeserv.R
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.DialogRequestSuccessBinding
import com.homeserv.databinding.FragmentRequestServiceBinding
import com.example.homeserv.utils.disable
import com.example.homeserv.utils.enable
import com.example.homeserv.utils.loadCircle
import java.util.Calendar

class RequestServiceFragment : BaseFragment<FragmentRequestServiceBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRequestServiceBinding.inflate(inflater, container, false)

    private val vm: RequestServiceViewModel by viewModels()
    private var currentProvider: Provider? = null

    override fun setup() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Read providerId from Bundle safely
        val providerId = arguments?.getString("providerId") ?: ""
        if (providerId.isNotEmpty()) {
            vm.loadProvider(providerId)
        } else {
            showError("Provider not found")
            findNavController().navigateUp()
        }

        setupDateTimePickers()
        setupHoursWatcher()
        setupConfirmButton()
    }

    override fun observeData() {
        vm.provider.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    currentProvider = result.data
                    bindProviderSummary(result.data)
                    updateEstimate()
                }
                is Resource.Error -> showError(result.message)
                else -> {}
            }
        }
        vm.submitResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnConfirm.disable()
                    binding.btnConfirm.text = "Submitting…"
                }
                is Resource.Success -> {
                    binding.btnConfirm.enable()
                    binding.btnConfirm.text = "Confirm Request"
                    showSuccessDialog()
                }
                is Resource.Error -> {
                    binding.btnConfirm.enable()
                    binding.btnConfirm.text = "Confirm Request"
                    showError(result.message)
                }
            }
        }
    }

    private fun bindProviderSummary(p: Provider) {
        binding.ivProviderAvatar.loadCircle(p.imageUrl)
        binding.tvProviderName.text = p.name
        binding.tvCategoryName.text = p.categoryName
        binding.tvProviderRate.text = p.formattedPrice()
    }

    private fun setupDateTimePickers() {
        val cal = Calendar.getInstance()
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    binding.etDate.setText("%04d-%02d-%02d".format(y, m + 1, d))
                    updateEstimate()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).also { it.datePicker.minDate = System.currentTimeMillis() }.show()
        }
        binding.tilDate.setEndIconOnClickListener { binding.etDate.performClick() }

        binding.etTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, min ->
                    val amPm = if (h < 12) "AM" else "PM"
                    val h12  = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
                    binding.etTime.setText("%02d:%02d %s".format(h12, min, amPm))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }
        binding.tilTime.setEndIconOnClickListener { binding.etTime.performClick() }
    }

    private fun setupHoursWatcher() {
        binding.etHours.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = updateEstimate()
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateEstimate() {
        val p     = currentProvider ?: return
        val hours = binding.etHours.text.toString().toIntOrNull() ?: 1
        binding.tvEstimatedPrice.text = "$%.2f".format(p.pricePerHour * hours)
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            hideKeyboard()
            if (!validateForm()) return@setOnClickListener
            val p = currentProvider ?: return@setOnClickListener
            vm.submitRequest(
                customerId    = session.userId,
                customerName  = session.userName,
                customerPhone = "",
                provider      = p,
                date          = binding.etDate.text.toString(),
                time          = binding.etTime.text.toString(),
                address       = binding.etAddress.text.toString().trim(),
                hours         = binding.etHours.text.toString().toIntOrNull() ?: 1,
                notes         = binding.etNotes.text.toString().trim()
            )
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        if (binding.etDate.text.isNullOrEmpty()) {
            binding.tilDate.error = "Please select a date"; valid = false
        } else binding.tilDate.error = null
        if (binding.etTime.text.isNullOrEmpty()) {
            binding.tilTime.error = "Please select a time"; valid = false
        } else binding.tilTime.error = null
        if (binding.etAddress.text.isNullOrBlank()) {
            binding.tilAddress.error = "Please enter your address"; valid = false
        } else binding.tilAddress.error = null
        val hours = binding.etHours.text.toString().toIntOrNull()
        if (hours == null || hours < 1) {
            binding.tilHours.error = "Enter at least 1 hour"; valid = false
        } else binding.tilHours.error = null
        return valid
    }

    private fun showSuccessDialog() {
        val dialog   = Dialog(requireContext())
        val dBinding = DialogRequestSuccessBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dBinding.btnTrackOrder.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.ordersFragment)
        }
        dBinding.btnBackHome.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.homeFragment)
        }
        dialog.show()
    }
}