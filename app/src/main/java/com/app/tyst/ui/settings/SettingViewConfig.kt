package com.app.tyst.ui.settings

class SettingViewConfig {
    var showNotification: Boolean = false
    var showRemoveAdd: Boolean = true
    var showEditProfile: Boolean = true
    var showChangePhone: Boolean = true
    var showChangePassword: Boolean = true
    var showDeleteAccount: Boolean = true

    var showSendFeedback: Boolean = true
    var showLogOut: Boolean = true

    var appVersion:String = ""

    /**
     * If there is no any visible setting for Account setting, then hide Account setting Option
     */
    fun showAccountHeaderShow():Boolean{
        return (!showNotification &&
                !showRemoveAdd &&
                !showChangePassword &&
                !showChangePhone &&
                !showDeleteAccount &&
                !showEditProfile)
    }

}