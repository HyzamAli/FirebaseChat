package com.tut.firebasechat.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentVerifyOtpBinding


class VerifyOtpFragment : Fragment() {
    private lateinit var binding: FragmentVerifyOtpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return binding.root
    }
}