package com.tut.firebasechat.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentRequestOtpBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility
import com.tut.firebasechat.viewmodels.AuthViewModel


class RequestOtpFragment : BaseFragment() {
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
        binding.phoneField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.phoneContainer.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAndRequest() {
        var phone = binding.phoneField.text.toString()
        if (phone.length<10) {
            binding.phoneContainer.error = getString(R.string.text_invalid_phone)
            return
        }
        if (!phone.startsWith("+91")) phone = "+91$phone"
        viewModel.phoneNumber = phone
        ViewUtility.hideKeyboard(requireActivity())
        requestOtp()
    }

    private fun requestOtp() {
        showLoadingUI()
        viewModel.requestOTP(requireActivity()).observe(viewLifecycleOwner) { response ->
            hideLoadingUI()
            when (response) {
                FirebaseResponse.CODE_SENT -> {
                    NavHostFragment.findNavController(this).navigate(R.id.requestToVerify)
                }
                FirebaseResponse.INVALID_CREDENTIALS -> {
                    ViewUtility.showSnack(requireActivity(), "Invalid Phone number")
                }
                else -> handleError(response){requestOtp()}
            }
        }
    }

    override fun showLoadingUI() {
        super.showLoadingUI()
        binding.progressbar.show()
        binding.buttonSubmit.isEnabled = false
    }

    override fun hideLoadingUI(): Boolean {
        val shouldHide = super.hideLoadingUI()
        if (shouldHide) {
            binding.progressbar.hide()
            binding.buttonSubmit.isEnabled = true
        }
        return shouldHide
    }
}