package com.example.homeserv.ui.customer.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.example.homeserv.base.BaseFragment
import com.example.homeserv.data.model.Resource
import com.homeserv.databinding.FragmentProfileBinding
import com.example.homeserv.ui.auth.LoginActivity
import com.example.homeserv.utils.navigateAndClearStack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileBinding.inflate(inflater, container, false)

    private val vm: ProfileViewModel by viewModels()

    override fun setup() {
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    override fun onResume() {
        super.onResume()
        vm.loadProfile(session.userId)
        vm.loadOrderCount(session.userId)
    }

    override fun observeData() {
        vm.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> bindUser(result.data)
                is Resource.Error   -> showError(result.message)
                else -> {}
            }
        }

        vm.orderCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalOrders.text = count.toString()
        }
    }

    private fun bindUser(user: com.example.homeserv.data.model.User) {
        binding.tvName.text      = user.fullName
        binding.tvEmail.text     = user.email
        binding.tvEmailInfo.text = user.email
        binding.tvPhone.text     = user.phone.ifEmpty { "—" }

        // Member since year
        val year = SimpleDateFormat("yyyy", Locale.getDefault())
            .format(Date(user.createdAt))
        binding.tvMemberSince.text = year
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                vm.logout()
                session.clear()
                requireActivity().navigateAndClearStack<LoginActivity>()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}