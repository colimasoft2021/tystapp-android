package com.app.tyst.ui.authentication.resetpassword

import android.app.Application
import com.app.tyst.R
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.PasswordStrength
import com.app.tyst.utility.validation.*

/**
 * View model call for performing all business logic of [com.app.tyst.ui.authentication.resetpassword.ResetPasswordActivity]
 */
class ResetPasswordViewModel(app: Application) : BaseViewModel(app) {


    /**
     * Api call for reset password
     */

    fun callResetPassword(newPassword: String, phoneNumber: String, resetKey: String) {
        val map = HashMap<String, String>()
        map["new_password"] = newPassword
        map["mobile_number"] = phoneNumber
        map["reset_key"] = resetKey
        ResetPasswordRepository(this@ResetPasswordViewModel).callResetPassword(map = map)
    }

    /**
     * Validate Sign Up inputs
     */
    fun isValid(newPassword: String, confirmPassword: String): Boolean {
        return when {

            newPassword.isEmpty() -> {
                validationObserver.value = createValidationResult(PASSWORD_EMPTY, R.id.inputNewPassword)
                false
            }
            PasswordStrength.calculateStrength(newPassword).value < PasswordStrength.STRONG.value -> {
                validationObserver.value = createValidationResult(PASSWORD_INVALID, R.id.inputNewPassword)
                false
            }
            confirmPassword.isEmpty() -> {
                validationObserver.value = createValidationResult(CONFORM_PASSWORD_EMPTY, R.id.inputConfirmPassword)
                false
            }
            newPassword != confirmPassword -> {
                validationObserver.value = createValidationResult(PASSWORD_NOT_MATCH, R.id.inputConfirmPassword)
                false
            }

            else -> true
        }
    }
}