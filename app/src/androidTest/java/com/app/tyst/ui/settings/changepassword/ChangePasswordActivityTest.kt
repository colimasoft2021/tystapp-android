package com.app.tyst.ui.settings.changepassword

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.util.fillConfirmPassword
import com.app.tyst.util.fillText
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class ChangePasswordActivityTest : BaseTest() {

    companion object {
        private val TAG: String = "ChangePasswordActivityTest"
    }

    private val TIMER_SLEEP_1: Int = 1
    private val TIMER_SLEEP_2: Int = 3

    @get:Rule
    val activityRule = ActivityTestRule<ChangePasswordActivity>(ChangePasswordActivity::class.java)

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(activityRule)

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources
    var mIdlingResource: RemoteIdlingResource? = null
    var activity: ChangePasswordActivity? = null

    @Before
    fun init() {
        activity = activityRule.activity
        mIdlingResource = activity?.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

    @Test
    fun changePasswordScreen_Test() {
        check_oldPassword()
        check_newPassword()
        check_oldPasswordWrongTest()
        check_passwordChangeTest()
    }

    /**
     * Test case for test new password
     */
     fun check_oldPassword() {
        Util.waitThread(TIMER_SLEEP_1)
        Espresso.onView(ViewMatchers.withId(R.id.inputOldPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyOldPasswordTest()
        check_validOldPasswordTest()
    }

     fun check_emptyOldPasswordTest() {
        "".fillText(R.id.inputOldPassword)
        capture(activityRule.activity, "Initial_setup_empty_old_password", TAG, "check_emptyFeedBackTest")
         Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo())
         Util.waitThread(1)
         Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_old_password))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_empty_old_password", TAG, "check_emptyFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputOldPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
    }

     fun check_validOldPasswordTest() {
        "Abc@123".fillText(R.id.inputOldPassword)
        capture(activityRule.activity, "Initial_setup_valid_old_password", TAG, "check_validFeedBackTest")
        capture(activityRule.activity, "After_valid_old_password_test", TAG, "check_validFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
    }

    /**
     * Test case for test new password
     */
     fun check_newPassword() {
        Util.waitThread(TIMER_SLEEP_1)
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyNewPasswordTest()
        check_invalidNewPasswordTest()
        check_validNewPasswordTest()
        checkConfirmPassword()
    }

     fun check_emptyNewPasswordTest() {
        "".fillText(R.id.inputNewPassword)
        capture(activityRule.activity, "Initial_setup_empty_new_password", TAG, "check_emptyNewPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo())
         Util.waitThread(1)
         Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_new_password))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_empty_new_password", TAG, "check_emptyNewPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

     fun check_invalidNewPasswordTest() {
        "123456".fillText(R.id.inputNewPassword)
        capture(activityRule.activity, "Initial_setup_invalid_new_password", TAG, "check_invalidNewPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_valid_password))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_invalid_new_password", TAG, "check_invalidNewPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

     fun check_validNewPasswordTest() {
        "Abc@123".fillText(R.id.inputNewPassword)
        capture(activityRule.activity, "Initial_setup_valid_new_password", TAG, "check_validNewPasswordTest")
        capture(activityRule.activity, "After_valid_new_password_test", TAG, "check_validNewPasswordTest")
        Util.waitThread(TIMER_SLEEP_1)
    }

    /**
     * Test case for test confirm password
     */
     fun checkConfirmPassword() {
        Util.waitThread(TIMER_SLEEP_1)
        Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyConfirmPasswordTest()
        check_invalidConfirmPasswordTest()
        check_validConfirmPasswordTest()
    }

     fun check_emptyConfirmPasswordTest() {
        "".fillConfirmPassword()
        capture(activityRule.activity, "Initial_setup_empty_confirm_password", TAG, "check_emptyConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_confirm_password))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_empty_confirm_password", TAG, "check_emptyConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

     fun check_invalidConfirmPasswordTest() {
        "123456".fillConfirmPassword()
        capture(activityRule.activity, "Initial_setup_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_confirm_password_not_match))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

     fun check_validConfirmPasswordTest() {
        "Abc@123".fillConfirmPassword()
        capture(activityRule.activity, "Initial_setup_valid_confirm_password", TAG, "check_validPasswordTest")
        capture(activityRule.activity, "After_valid_confirm_password_test", TAG, "check_validPasswordTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

     fun check_oldPasswordWrongTest(){
        Espresso.onView(ViewMatchers.withId(R.id.inputOldPassword)).perform(ViewActions.scrollTo())
        "Abc@1234".fillText(R.id.inputOldPassword)
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillText(R.id.inputNewPassword)
        Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillText(R.id.inputConfirmPassword)
        capture(activityRule.activity, "Initial_setup_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Your old password does not match.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

//    Your password changed successfully.
//    Your old password does not match.

     fun check_passwordChangeTest(){
        Espresso.onView(ViewMatchers.withId(R.id.inputOldPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillText(R.id.inputOldPassword)
        Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillText(R.id.inputNewPassword)
        Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillText(R.id.inputConfirmPassword)
        capture(activityRule.activity, "Initial_setup_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Your password changed successfully.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

}