package com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail

import android.app.Application
import com.app.tyst.utility.extension.isValidEmail
import com.app.tyst.R
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.validation.EMAIL_EMPTY
import com.app.tyst.utility.validation.EMAIL_INVALID
import com.app.tyst.utility.validation.createValidationResult

/**
 * This class is used for perform forgot password with email related operations
 */
class ForgotPasswordEmailViewModel( app: Application) : BaseViewModel(app) {


    /**
     * Api call send reset password link on user's registered email id
     */

    fun callForgotPassword(email: String) {
        val map = HashMap<String, String>()
        map["email"] = email
        ForgotPasswordEmailRepository(this@ForgotPasswordEmailViewModel).callForgotPassword(map = map)
    }

    /**
     * Validate inputs
     */
    fun isValid(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                validationObserver.value = createValidationResult(EMAIL_EMPTY, R.id.inputEmail)
                return false
            }

            !email.isValidEmail() -> {
                validationObserver.value = createValidationResult(EMAIL_INVALID, R.id.inputEmail)
                return false
            }

            else -> true
        }
    }
}