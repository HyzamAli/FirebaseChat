package com.tut.firebasechat.views.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentVerifyOtpBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility.showSnack
import com.tut.firebasechat.viewmodels.AuthViewModel
import com.tut.firebasechat.views.activities.RegistrationActivity


class VerifyOtpFragment : Fragment() {
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
    }

    private fun validateAndRequest() {
        val code = binding.otpField.text.toString()
        if (code.length<6) {
            showSnack(requireActivity(), getString(R.string.text_invalid_phone))
            return
        }
        requestOtp(code)
    }

    private fun requestOtp(code: String) {
        binding.buttonSubmit.isEnabled = false
        viewModel.verifyOTP(code).observe(viewLifecycleOwner) { response ->
            binding.buttonSubmit.isEnabled = true
            when(response) {
                FirebaseResponse.SUCCESS -> intentHome()
                FirebaseResponse.NO_INTERNET -> showSnack(
                    requireActivity(),
                    "NO INTERNET"
                )
                FirebaseResponse.INVALID_CREDENTIALS -> showSnack(
                    requireActivity(),
                    "Invalid OTP Code"
                )
                else -> showSnack(requireActivity(), "Try Later")
            }
        }
    }

    private fun intentHome() {
        Intent(requireActivity(), RegistrationActivity::class.java).apply {
            startActivity(this)
            requireActivity().finish()
        }
    }
}