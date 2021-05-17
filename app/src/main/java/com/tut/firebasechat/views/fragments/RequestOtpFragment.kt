package com.tut.firebasechat.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.FragmentRequestOtpBinding


class RequestOtpFragment : Fragment() {
    private lateinit var binding: FragmentRequestOtpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequestOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

}