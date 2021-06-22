package com.tut.firebasechat.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
                    viewModel.imageUri = cropResult.uri
                    setDp(viewModel.imageUri)
            }
        }

        binding.nameField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               binding.nameContainer.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.usernameField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.usernameContainer.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.buttonSubmit.setOnClickListener{putProfileDetails()}
        binding.imageContainer.setOnClickListener{intentForImage()}

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

    private fun putProfileDetails() {
        ViewUtility.hideKeyboard(this)
        val name = binding.nameField.text.toString()
        val username = binding.usernameField.text.toString()
        if (name.length<3){
            binding.nameContainer.error = getString(R.string.error_invalid_name)
            return
        }
        if (username.length<3){
            binding.usernameContainer.error = getString(R.string.error_invalid_username)
            return
        }

        showLoadingUi()
        viewModel.username = username
        viewModel.name = name
        viewModel.putNewUserDetails()
            .observe(this) { response ->
                hideLoadingUi()
                when(response) {
                    FirebaseResponse.SUCCESS -> intentHome()
                    FirebaseResponse.DUPLICATE_USERNAME -> {
                        binding.usernameContainer.error = getString(R.string.errror_duplicate_username)
                    }
                    FirebaseResponse.NO_INTERNET -> {
                        ViewUtility.showSnack(this, getString(R.string.error_no_internet)){putProfileDetails()}
                    }
                    else ->ViewUtility.showSnack(this, getString(R.string.error_try_again))
                }
            }
    }

    private fun intentHome() {
        Intent(this, HomeActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun showLoadingUi() {
        binding.progressbar.show()
        binding.buttonSubmit.isEnabled = false
        binding.nameField.isEnabled = false
        binding.usernameField.isEnabled = false
        binding.imageContainer.isEnabled = false
    }

    private fun hideLoadingUi() {
        binding.progressbar.hide()
        binding.buttonSubmit.isEnabled = true
        binding.nameField.isEnabled = true
        binding.usernameField.isEnabled = true
        binding.imageContainer.isEnabled = true
    }
}