package com.tut.firebasechat.views.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentVerifyOtpBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility
import com.tut.firebasechat.viewmodels.AuthViewModel
import com.tut.firebasechat.viewmodels.ProfileViewModel
import com.tut.firebasechat.views.activities.HomeActivity
import com.tut.firebasechat.views.activities.RegistrationActivity


class VerifyOtpFragment : BaseFragment() {
    private lateinit var binding: FragmentVerifyOtpBinding
    private lateinit var viewModel: AuthViewModel
    private val destinationHome = 37
    private val destinationRegistration = 45

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonSubmit.setOnClickListener {
            validateAndRequest()
        }
        binding.otpField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.otpContainer.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAndRequest() {
        val code = binding.otpField.text.toString()
        if (code.length<6) {
            binding.otpContainer.error = getString(R.string.text_invalid_phone)
            return
        }
        viewModel.otpCode = code
        verifyOtp()
    }

    private fun verifyOtp() {
        ViewUtility.hideKeyboard(requireActivity())
        showLoadingUI()
        binding.buttonSubmit.isEnabled = false
        viewModel.verifyOTP().observe(viewLifecycleOwner) { response ->
            hideLoadingUI()
            binding.buttonSubmit.isEnabled = true
            when (response) {
                FirebaseResponse.SUCCESS -> isProfileExists()
                FirebaseResponse.INVALID_CREDENTIALS -> {
                    ViewUtility.showSnack(requireActivity(), getString(R.string.text_invalid_otp))
                }
                else -> handleError(response){verifyOtp()}
            }
        }
    }

    private fun intentTo(destination: Int) {
        Intent(requireActivity(),
            if (destination == destinationRegistration) RegistrationActivity::class.java
            else HomeActivity::class.java)
            .apply {
                startActivity(this)
                requireActivity().finish()
        }
    }

    private fun isProfileExists() {
        showLoadingUI()
        val profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.isProfileExists()
            .observe(requireActivity()) { responseWrapper ->
                hideLoadingUI()
                if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                    if (profileViewModel.isProfileCompleted()) intentTo(destinationHome)
                    else intentTo(destinationRegistration)
                } else handleError(responseWrapper.response){isProfileExists()}
            }
    }

    override fun showLoadingUI() {
        super.showLoadingUI()
        binding.progressbar.show()
        binding.buttonSubmit.isEnabled = false
        binding.otpField.isEnabled = false
    }

    override fun hideLoadingUI(): Boolean {
        val shouldHide = super.hideLoadingUI()
        if (shouldHide) {
            binding.progressbar.hide()
            binding.buttonSubmit.isEnabled = true
            binding.otpField.isEnabled = true
        }
        return shouldHide
    }
}