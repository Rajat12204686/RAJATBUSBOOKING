package com.examples.rentors.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.examples.rentors.R
import com.examples.rentors.databinding.FragmentProfileBinding
import com.examples.rentors.viewmodels.ProfileViewModel


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, ProfileViewModel.Factory(activity.application)
        )[ProfileViewModel::class.java]
    }

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.profileAppBarBackButton.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_profileFragment_to_mainFragment)
        }
        binding.profileAppBarLogout.setOnClickListener {
            viewModel.logout()
            (activity as MainActivity).navController.navigate(R.id.action_profileFragment_to_loginFragment)
        }
        binding.profileContactMail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(viewModel.email.value))
            }
            startActivity(intent)
        }
        binding.profileContactPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${viewModel.contactPhone.value}")
            }
            startActivity(intent)
        }
        return binding.root
    }

}