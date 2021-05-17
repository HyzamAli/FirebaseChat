package com.tut.firebasechat.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentRequestOtpBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility.showSnack
import com.tut.firebasechat.viewmodels.AuthViewModel


class RequestOtpFragment : Fragment() {
    private lateinit var binding: FragmentRequestOtpBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequestOtpBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonSubmit.setOnClickListener {
            validateAndRequest()
        }
    }

    private fun validateAndRequest() {
        var phone = binding.phoneField.text.toString()
        if (phone.length<10) {
            showSnack(requireActivity(), getString(R.string.text_invalid_phone))
            return
        }
        if (!phone.startsWith("+91"))
            phone = "+91$phone"
        requestOtp(phone)
    }

    private fun requestOtp(phone: String) {
        binding.buttonSubmit.isEnabled = false
        viewModel.requestOTP(phone, requireActivity()).observe(viewLifecycleOwner) { response ->
            binding.buttonSubmit.isEnabled = true
            when(response) {
                FirebaseResponse.SUCCESS -> showSnack(requireActivity(), "SUCCESS")
                FirebaseResponse.NO_INTERNET -> showSnack(requireActivity(), "NO INTERNET")
                FirebaseResponse.INVALID_CREDENTIALS -> showSnack(requireActivity(), "Invalid Phone number")
                FirebaseResponse.CODE_SENT -> NavHostFragment.findNavController(this).navigate(R.id.requestToVerify)
                FirebaseResponse.QUOTA_EXCEED -> showSnack(requireActivity(), "Quota Exceeded")
                else -> showSnack(requireActivity(), "Try Later")
            }
        }
    }
}