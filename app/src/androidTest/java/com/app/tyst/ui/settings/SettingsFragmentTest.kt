package com.app.tyst.ui.settings

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.ui.settings.staticpages.StaticPagesActivity
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.*
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest : BaseTest() {
    private val TIMER_SLEEP = 1

    companion object {
        const val TAG = "SettingsFragmentTest"
    }

    @get:Rule
    var activityRule = ActivityTestRule<HomeActivity>(HomeActivity::class.java)
    private var settingConfig: SettingViewConfig? = null
    var mIdlingResource: RemoteIdlingResource? = null

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(activityRule)

    @Before
    fun init() {
        activityRule.activity
                .supportFragmentManager.beginTransaction()
        activityRule.activity.changeTab(HomeActivity.INDEX_SETTING)
        Intents.init()
        Util.waitThread(TIMER_SLEEP)
        mIdlingResource = (activityRule.activity.currentFragment as SettingsFragment).getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        settingConfig = (activityRule.activity.currentFragment as SettingsFragment).binding.viewSetting
    }


    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

    //    @Test
    fun settingScreen_Test() {
        check_changePasswordClickTest()
        check_aboutUsClickTest()
        check_TNCClickTest()
    }

    @Test
    fun check_notificationSettingClickTest() {
        if (settingConfig?.showNotification == true) {
            capture(activityRule.activity, "Initial_setup_notification_setting_click", TAG, "check_notificationSettingClickTest")
            onView(withId(R.id.tbPushNotification)).perform(scrollTo(), click())
            onView(ViewMatchers.withText("Notifications updated successfully.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            capture(activityRule.activity, "After_notification_setting_click_test", TAG, "check_notificationSettingClickTest")
        }
    }

    /*@Test
    fun check_editProfileClickTest() {
        if (settingConfig?.showEditProfile == true) {
            capture(activityRule.activity, "Initial_setup_edit_profile_click", TAG, "check_editProfileClickTest")
            onView(withId(R.id.rlEditProfile)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_edit_profile_click_test", TAG, "check_editProfileClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }*/

    @Test
    fun check_changePasswordClickTest() {
        if (settingConfig?.showChangePassword == true) {
            capture(activityRule.activity, "Initial_setup_change_password_click", TAG, "check_changePasswordClickTest")
            onView(withId(R.id.rlChangePassword)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_change_password_click_test", TAG, "check_changePasswordClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }

    @Test
    fun check_changePhoneNumberClickTest() {
        if (settingConfig?.showChangePhone == true) {
            capture(activityRule.activity, "Initial_setup_change_phone_click", TAG, "check_changePhoneNumberClickTest")
            onView(withId(R.id.rlChangePhoneNumber)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_change_phone_click_test", TAG, "check_changePhoneNumberClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }

//    @Test
    fun check_addFreePurchaseClickTest() {
        if (settingConfig?.showRemoveAdd == true) {
            capture(activityRule.activity, "Initial_setup_add_free_purchase_click", TAG, "check_addFreePurchaseClickTest")
            onView(withId(R.id.rlAdFree)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_add_free_purchase_click_test", TAG, "check_addFreePurchaseClickTest")
        }
    }

    @Test
    fun check_deleteAccountClickTest() {
        if (settingConfig?.showDeleteAccount == true) {
            capture(activityRule.activity, "Initial_setup_delete_account_click", TAG, "check_deleteAccountClickTest")
            onView(withId(R.id.rlDeleteAccount)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_delete_account_click_test", TAG, "check_deleteAccountClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }

    @Test
    fun check_aboutUsClickTest() {
        capture(activityRule.activity, "Initial_setup_about_us_click", TAG, "check_aboutUsClickTest")
        onView(withId(R.id.rlAboutUs)).perform(scrollTo(), click())
        Intents.intended(IntentMatchers.hasComponent(StaticPagesActivity::class.java.name))
        capture(activityRule.activity, "After_about_us_click_test", TAG, "check_aboutUsClickTest")
        Util.waitThread(TIMER_SLEEP)
    }

    @Test
    fun check_privacyPolicyClickTest() {
        capture(activityRule.activity, "Initial_setup_privacy_policy_click", TAG, "check_privacyPolicyClickTest")
        onView(withId(R.id.rlPrivacyPolicy)).perform(scrollTo(), click())
        Intents.intended(IntentMatchers.hasComponent(StaticPagesActivity::class.java.name))
        capture(activityRule.activity, "After_privacy_policy_click_test", TAG, "check_privacyPolicyClickTest")
        Util.waitThread(TIMER_SLEEP)
    }

    @Test
    fun check_TNCClickTest() {
        capture(activityRule.activity, "Initial_setup_tnc_click", TAG, "check_TNCClickTest")
        onView(withId(R.id.rlTermsConditions)).perform(scrollTo(), click())
        Intents.intended(IntentMatchers.hasComponent(StaticPagesActivity::class.java.name))
        capture(activityRule.activity, "After_tnc_click_test", TAG, "check_TNCClickTest")
        Util.waitThread(TIMER_SLEEP)
    }

    @Test
    fun check_sendFeedbackClickTest() {
        if (settingConfig?.showSendFeedback == true) {
            capture(activityRule.activity, "Initial_setup_send_feedback_click", TAG, "check_sendFeedbackClickTest")
            onView(withId(R.id.rlReportProblem)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_send_feedback_click_test", TAG, "check_sendFeedbackClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }

//    @Test
    fun check_shareAppClickTest() {
        capture(activityRule.activity, "Initial_setup_share_app_click", TAG, "check_shareAppClickTest")
        onView(withId(R.id.rlShareApp)).perform(scrollTo(), click())
        capture(activityRule.activity, "After_share_app_click_test", TAG, "check_shareAppClickTest")
        Util.waitThread(TIMER_SLEEP)
    }

    @Test
    fun check_logOutClickTest() {
        if (settingConfig?.showLogOut == true) {
            capture(activityRule.activity, "Initial_setup_logout_click", TAG, "check_logOutClickTest")
            onView(withId(R.id.btnLogOut)).perform(scrollTo(), click())
            capture(activityRule.activity, "After_logout_click_test", TAG, "check_logOutClickTest")
            Util.waitThread(TIMER_SLEEP)
        }
    }
}