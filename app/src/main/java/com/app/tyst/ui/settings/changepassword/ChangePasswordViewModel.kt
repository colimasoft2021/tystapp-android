package com.app.tyst.ui.settings.changepassword

import android.app.Application
import com.app.tyst.R
import com.app.tyst.ui.core.BaseViewModel
import com.app.tyst.utility.PasswordStrength
import com.app.tyst.utility.extension.toEncrypt
import com.app.tyst.utility.validation.*

/**
 * View model call for performing all business logic of [com.app.tyst.ui.changepassword.ChangePasswordActivity]
 */
class ChangePasswordViewModel(app: Application) : BaseViewModel(app) {

    /**
     * Api call for change password
     */

    fun callChangePassword( oldPassword:String, newPassword: String,isEncrypted: Boolean = false) {
        val map = HashMap<String, String>()
        map["old_password"] = if (isEncrypted) oldPassword.toEncrypt() else oldPassword
        map["new_password"] = if (isEncrypted) newPassword.toEncrypt() else newPassword
        if (isEncrypted)
            map["api_version"] = "1.1"
        ChangePasswordRepository(this@ChangePasswordViewModel).callChangePassword(map = map)
    }

    /**
     * Validate Sign Up inputs
     */
    fun isValid(oldPassword:String, newPassword: String, confirmPassword: String): Boolean {
        return when {

            oldPassword.isEmpty() -> {
                validationObserver.value = createValidationResult(OLD_PASSWORD_EMPTY, R.id.inputNewPassword)
                false
            }

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