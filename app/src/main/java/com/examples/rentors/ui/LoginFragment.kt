package com.examples.rentors.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.examples.rentors.viewmodels.LoginViewModel
import inc.fabudi.vulpecula.R
import inc.fabudi.vulpecula.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, LoginViewModel.Factory(activity.application)
        )[LoginViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (viewModel.loggedIn.get()) {
            (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        val binding: FragmentLoginBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_login, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner



        binding.loginNext.setOnClickListener {
            if (!viewModel.codeSent.get()) {
                val phoneNumber = binding.loginPhoneInput.getText()
                if (phoneNumber == "") {
                    binding.loginPhoneInput.setError("Empty field")
                    return@setOnClickListener
                }
                if (phoneNumber.length != 13) {
                    binding.loginPhoneInput.setError("Wrong number")
                    return@setOnClickListener
                }
                viewModel.login(phoneNumber)
                return@setOnClickListener
            }
            if (viewModel.codeSent.get() && !viewModel.newUser.get()) {
                viewModel.completeLogin(binding.loginCodeInput.getText())
                if (viewModel.loggedIn.get() && !viewModel.newUser.get()) {
                    (activity as MainActivity).navController.navigate(
                        R.id.action_loginFragment_to_mainFragment
                    )
                }
                return@setOnClickListener
            }
            if (viewModel.codeSent.get() && viewModel.newUser.get()) {
                val name = binding.loginNameInput.getText()
                val lastName = binding.loginLastnameInput.getText()
                if (name.isEmpty()) {
                    binding.loginNameInput.setError("Wrong number")
                    return@setOnClickListener
                }
                if (lastName.isEmpty()) {
                    binding.loginLastnameInput.setError("Wrong number")
                    return@setOnClickListener
                }
                viewModel.writeUserToDatabase(name, lastName)
                (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
                return@setOnClickListener
            }
        }

        binding.sendPhoneButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString()
            viewModel.login(phone)
        }
        binding.sendCodeButton.setOnClickListener {
            val code = binding.codeEditText.text.toString()
            viewModel.completeLogin(code)
            if (!viewModel.newUser.get()) (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        binding.infoButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val lastName = binding.lastnameEditText.text.toString()
            viewModel.writeUserToDatabase(name, lastName)
            (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        return binding.root
    }

}