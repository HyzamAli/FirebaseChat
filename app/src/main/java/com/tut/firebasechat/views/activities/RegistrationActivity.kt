package com.tut.firebasechat.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.tut.firebasechat.R
import com.tut.firebasechat.databinding.ActivityRegistrationBinding
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility
import com.tut.firebasechat.viewmodels.ProfileViewModel

class RegistrationActivity : AppCompatActivity() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var filePicker: ActivityResultLauncher<String>
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>
    private var croppedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePicker = registerForActivityResult(GetContent()) { uri: Uri? ->
            uri?.let {
                intentToCrop(it)
            }
        }

        cropLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
                val data = result.data
                if (result.resultCode == RESULT_OK) {
                    val cropResult: CropImage.ActivityResult = CropImage.getActivityResult(data)
                    croppedImageUri = cropResult.uri
                    setDp(croppedImageUri)
            }
        }

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.buttonSubmit.setOnClickListener{putProfileDetails()}
        binding.imageContainer.setOnClickListener{intentForImage()}

        isProfileExists()
        setDp()
    }

    private fun intentForImage() {
        filePicker.launch("image/*")
    }

    private fun intentToCrop(uri: Uri) {
        val intent = CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .getIntent(this)
        cropLauncher.launch(intent)
    }

    private fun setDp(uri: Uri? = null) {
        Glide.with(this)
            .load(uri ?: ContextCompat.getDrawable(this, R.drawable.ic_avatar))
            .into(binding.imageContainer)
    }

    private fun isProfileExists() {
        viewModel.isProfileExists().observe(this) { responseWrapper ->
            if (responseWrapper.response == FirebaseResponse.SUCCESS) {
                if (viewModel.isProfileCompleted()) intentHome()
            } else ViewUtility.showSnack(this, "Error, try later")
        }
    }

    private fun putProfileDetails() {
        val name = binding.nameField.text.toString()
        val username = binding.usernameField.text.toString()
        if (name.length<3){
            ViewUtility.showSnack(this, "Enter Valid Name")
            return
        }
        if (username.length<3){
            ViewUtility.showSnack(this, "Enter Valid Name")
            return
        }
        binding.buttonSubmit.isEnabled = false
        viewModel.putProfileDetails(name, username, croppedImageUri)
            .observe(this) { response ->
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