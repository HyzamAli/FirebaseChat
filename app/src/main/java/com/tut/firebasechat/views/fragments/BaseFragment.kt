package com.tut.firebasechat.views.fragments

import androidx.fragment.app.Fragment
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility

open class BaseFragment: Fragment() {
    fun handleError(response: FirebaseResponse) {
        when(response) {
            FirebaseResponse.NO_INTERNET ->
                ViewUtility.showSnack(requireActivity(), "NO INTERNET")
            FirebaseResponse.INVALID_CREDENTIALS ->
                ViewUtility.showSnack(requireActivity(), "Invalid Phone number")
            FirebaseResponse.QUOTA_EXCEED ->
                ViewUtility.showSnack(requireActivity(), "Quota Exceeded")
            else ->
                ViewUtility.showSnack(requireActivity(), "Try Later")
        }
    }
}