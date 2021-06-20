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
import com.tut.firebasechat.views.activities.RegistrationActivity


class VerifyOtpFragment : BaseFragment() {
    private lateinit var binding: FragmentVerifyOtpBinding
    private lateinit var viewModel: AuthViewModel

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
        showLoadingUI()
        binding.buttonSubmit.isEnabled = false
        viewModel.verifyOTP().observe(viewLifecycleOwner) { response ->
            hideLoadingUI()
            binding.buttonSubmit.isEnabled = true
            when (response) {
                FirebaseResponse.SUCCESS -> intentRegistration()
                FirebaseResponse.INVALID_CREDENTIALS -> {
                    ViewUtility.showSnack(requireActivity(), getString(R.string.text_invalid_otp))
                }
                else -> handleError(response){verifyOtp()}
            }
        }
    }

    private fun intentRegistration() {
        Intent(requireActivity(), RegistrationActivity::class.java).apply {
            startActivity(this)
            requireActivity().finish()
        }
    }

    override fun showLoadingUI() {
        super.showLoadingUI()
        binding.progressbar.show()
    }

    override fun hideLoadingUI(): Boolean {
        val shouldHide = super.hideLoadingUI()
        if (shouldHide) binding.progressbar.hide()
        return shouldHide
    }
}