package com.tut.firebasechat.views.fragments

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.tut.firebasechat.models.FirebaseResponse
import com.tut.firebasechat.utilities.ViewUtility

open class BaseFragment: Fragment() {
    private var requestsInProgress = 0

    fun handleError(response: FirebaseResponse, runnable: (() -> Unit)? = null) {
        when(response) {
            FirebaseResponse.NO_INTERNET -> {
                ViewUtility.showSnack(requireActivity(), "NO INTERNET"){runnable}
            }
            FirebaseResponse.INVALID_CREDENTIALS ->
                ViewUtility.showSnack(requireActivity(), "Invalid Phone number")
            FirebaseResponse.QUOTA_EXCEED ->
                ViewUtility.showSnack(requireActivity(), "Quota Exceeded")
            else ->
                ViewUtility.showSnack(requireActivity(), "Try Later")
        }
    }

    @CallSuper
    protected open fun showLoadingUI() {
        requestsInProgress += 1
    }

    @CallSuper
    protected open fun hideLoadingUI(): Boolean {
        requestsInProgress -= 1
        return requestsInProgress == 0
    }
}