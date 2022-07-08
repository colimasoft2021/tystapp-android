package com.app.tyst.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.BuildConfig
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.hb.Settings
import com.app.tyst.databinding.FragmentSettingsBinding
import com.app.tyst.ui.core.BaseFragment
import com.app.tyst.ui.settings.changepassword.ChangePasswordActivity
import com.app.tyst.ui.settings.changephonenumber.ChangePhoneNumberActivity
import com.app.tyst.ui.settings.editprofile.EditProfileActivity
import com.app.tyst.ui.settings.feedback.SendFeedbackActivity
import com.app.tyst.ui.settings.staticpages.StaticPagesActivity
import com.app.tyst.ui.settings.subscription.SubscriptionActivity
import com.app.tyst.ui.tutorial.TutorialActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.sharedPreference
import com.app.tyst.utility.extension.showSnackBar
import com.crashlytics.android.Crashlytics
import com.hb.logger.Logger

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel
        get() = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
//    private var addFreeSKU: SkuDetails? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentFragment(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun iniViews() {
        binding = getViewDataBinding()
        binding.rlDebug.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        binding.lineDebug.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        binding.viewSetting = viewModel.settingConfig
        binding.user = sharedPreference.userDetail
        addObserver()
    }

    override fun initListener() {
        binding.apply {
            rlAdFree.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Purchase Subscription Click")
//                mIdlingResource?.setIdleState(false)
//                viewModel.makePurchase(mBaseActivity!!, skuDetails = addFreeSKU)
                startActivity(Intent(mBaseActivity!!, SubscriptionActivity::class.java))
            }
            rlChangePassword.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Change Password Click")
                startActivity(Intent(mBaseActivity, ChangePasswordActivity::class.java))
            }
            rlChangePhoneNumber.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Change Phone Number Click")
                startActivity(Intent(mBaseActivity, ChangePhoneNumberActivity::class.java))
            }
            rlAboutUs.clickWithDebounce { startActivity(StaticPagesActivity.getStartIntent(mBaseActivity!!, IConstants.STATIC_PAGE_ABOUT_US, getString(R.string.about_us))) }
            rlPrivacyPolicy.clickWithDebounce { startActivity(StaticPagesActivity.getStartIntent(mBaseActivity!!, IConstants.STATIC_PAGE_PRIVACY_POLICY, getString(R.string.privacy_policy))) }
            rlTermsConditions.clickWithDebounce { startActivity(StaticPagesActivity.getStartIntent(mBaseActivity!!, IConstants.STATIC_PAGE_TERMS_CONDITION, getString(R.string.terms_n_conditions))) }
            rlEula.clickWithDebounce { startActivity(StaticPagesActivity.getStartIntent(mBaseActivity!!, IConstants.STATIC_PAGE_EULA, getString(R.string.end_user_licence_agreement))) }
            rlReportProblem.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Report Problem Click")
                startActivity(Intent(mBaseActivity, SendFeedbackActivity::class.java))
            }
            rlShareApp.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Share App Click")
                openShareIntent()
            }
            rlDeleteAccount.clickWithDebounce {
                performDeleteAccount()
            }
            btnLogOut.makeBounceable()
            btnLogOut.clickWithDebounce {
                performLogOut()
            }
            rlEditProfile.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Profile Click")
                startActivity(Intent(mBaseActivity, EditProfileActivity::class.java))
            }

            tbPushNotification.clickWithDebounce {
                showHideProgressDialog(true)
                mIdlingResource?.setIdleState(false)
                viewModel.callManageNotification(disableNotification = if (binding.tbPushNotification.isChecked) "Yes" else "No")
            }

            rlTutorial.clickWithDebounce {
                startActivity(TutorialActivity.getStartIntent(mBaseActivity!!, true))
            }

            rlDebug.clickWithDebounce {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Debug Button Click")
                Logger.launchActivity()
            }
        }
    }

    private fun addObserver() {
        viewModel.notificationSettingData.observe(this, Observer {
            showHideProgressDialog(false)
            when {
                it.settings?.success == "1" -> {
                    viewModel.updateNotificationSetting(if (sharedPreference.userDetail?.notification?.equals("Yes") == true) "No" else "Yes")
                    it.settings?.message?.showSnackBar(mBaseActivity, IConstants.SNAKBAR_TYPE_SUCCESS, duration = IConstants.SNAKE_BAR_SHOW_TIME)
                    mIdlingResource?.setIdleState(true)
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    binding.user = sharedPreference.userDetail
                    binding.executePendingBindings()
                    if (it?.settings?.success?.equals(Settings.NETWORK_ERROR) == false)
                        it.settings?.message?.showSnackBar(mBaseActivity, IConstants.SNAKBAR_TYPE_ERROR, duration = IConstants.SNAKE_BAR_SHOW_TIME)
                    mIdlingResource?.setIdleState(true)
                }
                else -> {
                    binding.user = sharedPreference.userDetail
                    binding.executePendingBindings()
                    mIdlingResource?.setIdleState(true)
                }
            }

        })

        viewModel.logOutLiveData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                mBaseActivity?.navigateToLoginScreen(true)
            } else {
                it.settings?.message?.showSnackBar(mBaseActivity)
            }
            mIdlingResource?.setIdleState(true)
        })

        viewModel.deleteAccountData.observe(this, Observer {
            showHideProgressDialog(false)
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                mBaseActivity?.navigateToLoginScreen(true)
            } else {
                it.settings?.message?.showSnackBar(mBaseActivity)
            }
            mIdlingResource?.setIdleState(true)
        })

//        viewModel.addFreeSKU.observe(mBaseActivity!!, Observer {
//            addFreeSKU = it
//        })
//
//        viewModel.purchaseData.observe(mBaseActivity!!, Observer {
//            if (it != null) {
//                sharedPreference.isAdRemoved = true
//                viewModel.settingConfig.showRemoveAdd = false
//                binding.viewSetting = viewModel.settingConfig
//                binding.executePendingBindings()
//                viewModel.callSubscriptionPurchase(it)
//                (mBaseActivity?.application as MainApplication).isAdRemoved.value = true
//            }
//            mIdlingResource?.setIdleState(true)
//        })

        // Listen user has purchased ad free and remove add from this screen
        (mBaseActivity?.application as MainApplication).isAdRemoved.observe(this@SettingsFragment, Observer {
            if (it) {
                viewModel.settingConfig.showRemoveAdd = false
                binding.viewSetting = viewModel.settingConfig
                binding.executePendingBindings()
                val user = sharedPreference.userDetail
                user?.purchaseStatus = "Yes"
                sharedPreference.userDetail = user
            } else {
                viewModel.settingConfig.showRemoveAdd = true
                binding.viewSetting = viewModel.settingConfig
                binding.executePendingBindings()
            }
        })

    }

    /**
     * Show confirmation message to log out
     */
    private fun performLogOut() {
        mIdlingResource?.setIdleState(false)
        DialogUtil.alert(context = mBaseActivity!!, msg = getString(R.string.logout_alert),
                positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Log Out Click")
                        showHideProgressDialog(true)
                        viewModel.callLogOut()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        mIdlingResource?.setIdleState(true)
                    }
                }, isCancelable = false)
    }

    /**
     * Share application
     */
    private fun openShareIntent() {
        val shareBody = "Try TYST App on Appstore or Playstore:\nhttps://itunes.apple.com/us/app/apple-store/\nhttps://play.google.com/store/apps/details?id=com.app.tyst"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Download The TYST")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.share_using)))
    }

    /**
     * Show confirmation message to delete account
     */
    private fun performDeleteAccount() {
        mIdlingResource?.setIdleState(false)
        DialogUtil.alert(context = mBaseActivity!!, msg = getString(R.string.delete_account_alert),
                positiveBtnText = getString(R.string.yes), negativeBtnText = getString(R.string.no),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Delete Account Click")
                        showHideProgressDialog(true)
                        viewModel.callDeleteAccount()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                        mIdlingResource?.setIdleState(true)
                    }
                }, isCancelable = false)
    }

    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }
        return mIdlingResource as RemoteIdlingResource
    }
}