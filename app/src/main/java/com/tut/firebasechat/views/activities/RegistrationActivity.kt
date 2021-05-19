package com.tut.firebasechat.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.tut.firebasechat.databinding.ActivityRegistrationBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility
import com.tut.firebasechat.viewmodels.ProfileViewModel

class RegistrationActivity : AppCompatActivity() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        if (viewModel.isProfileCompleted()) intentHome()
        binding.buttonSubmit.setOnClickListener{putProfileDetails()}
    }

    private fun putProfileDetails() {
        val name = binding.nameField.text.toString()
        if (name.length<3){
            ViewUtility.showSnack(this, "Enter Valid Name")
            return
        }
        binding.buttonSubmit.isEnabled = false
        viewModel.putProfileDetails(name).observe(this) { response ->
            binding.buttonSubmit.isEnabled = true
            when(response) {
                FirebaseResponse.SUCCESS -> intentHome()
                FirebaseResponse.INVALID_CREDENTIALS -> ViewUtility.showSnack(this, "Error, try later") // TODO: login again
                else -> ViewUtility.showSnack(this, "Error, try later")
            }
        }
    }

    private fun intentHome() {
        Intent(this, HomeActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }
}