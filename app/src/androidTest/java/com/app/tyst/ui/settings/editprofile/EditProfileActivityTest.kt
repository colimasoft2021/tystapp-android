package com.app.tyst.ui.settings.editprofile

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.authentication.register.signupconfig.SignUpConfig
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.util.fillPhoneNumber
import com.app.tyst.util.fillText
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EditProfileActivityTest : BaseTest() {

    private val TIMER_SLEEP_1: Int = 1
    private val TIMER_SLEEP_2: Int = 3

    @get:Rule
    val activityRule = ActivityTestRule<EditProfileActivity>(EditProfileActivity::class.java)
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources
    var mIdlingResource: RemoteIdlingResource? = null
    var activity: EditProfileActivity? = null

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(activityRule)

    companion object {
        private val TAG: String = "EditProfileActivity"
        private lateinit var signUpConfig: SignUpConfig
    }

    @Before
    fun init() {
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.MINUTES)
        signUpConfig = AppConfig.getSignUpConfiguration()
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

//    @Test
    fun editProfileScreen_Test() {

//        checkFirstName()
//        checkLastName()
//        checkUserName()
//        checkPhoneNumber()
//        checkAddress()
//        checkCity()
//        checkState()
//        checkZipCode()
//        checkDateOfBirth()
//        check_userNameAlreadyExistsTest()
//        check_editProfileTest()
    }

    /**
     * Test case for test First Name
     */
    fun checkFirstName() {
        if (signUpConfig.firstname.shouldShow()) {
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.firstname.isOptional())
                check_emptyFirstNameTest()
            check_inValidFirstNameTest()
            check_validFirstNameTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideFirstNameTest()
        }
    }

    fun check_hideFirstNameTest() {
        capture(activityRule.activity, "Initial_setup_hide_first_name", TAG, "check_hideFirstNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputFirstName)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(activityRule.activity, "After_hide_first_name_test", TAG, "check_hideFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyFirstNameTest() {
        "".fillText(R.id.inputFirstName)
        capture(activityRule.activity, "Initial_setup_empty_first_name", TAG, "check_emptyFirstNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_first_name))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_empty_first_name_test", TAG, "check_emptyFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_inValidFirstNameTest() {
        "Hary 09$".fillText(R.id.inputFirstName)
        capture(activityRule.activity, "Initial_setup_inValid_first_name", TAG, "check_inValidFirstNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_first_name_character))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_inValid_first_name_test", TAG, "check_inValidFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validFirstNameTest() {
        "Rahul`".fillText(R.id.inputFirstName)
        capture(activityRule.activity, "Initial_setup_valid_first_name", TAG, "check_validFirstNameTest")
        capture(activityRule.activity, "After_valid_first_name_test", TAG, "check_validFirstNameTest")
    }

    /**
     * Test case for test Last Name
     */
    fun checkLastName() {
        if (signUpConfig.lastname.shouldShow()) {
            Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.lastname.isOptional())
                check_emptyLastNameTest()
            check_inValidLastNameTest()
            check_validLastNameTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideLastNameTest()
        }
    }

    fun check_hideLastNameTest() {
        capture(activityRule.activity, "Initial_setup_hide_last_name", TAG, "check_hideLastNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(activityRule.activity, "After_hide_last_name_test", TAG, "check_hideLastNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyLastNameTest() {
        Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        "".fillText(R.id.inputLastName)
        capture(activityRule.activity, "Initial_setup_empty_last_name", TAG, "check_emptyLastNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_last_name))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_empty_last_name_test", TAG, "check_emptyLastNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_inValidLastNameTest() {
        "CJSD#$".fillText(R.id.inputLastName)
        capture(activityRule.activity, "Initial_setup_inValid_last_name", TAG, "check_inValidLastNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_last_name_character))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_inValid_last_name_test", TAG, "check_inValidLastNameTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validLastNameTest() {
        "HB".fillText(R.id.inputLastName)
        capture(activityRule.activity, "Initial_setup_valid_last_name", TAG, "check_validLastNameTest")
        capture(activityRule.activity, "After_valid_last_name_test", TAG, "check_validLastNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test Phone Number
     */
    fun checkPhoneNumber() {
        if (activity?.binding?.config?.phonenumber?.shouldShow() == true) {
            Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.phonenumber.isOptional())
                check_emptyPhoneNumberTest()
            check_invalidPhoneNumberTest()
            check_validPhoneNumberTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hidePhoneNumberTest()
        }
    }

    fun check_hidePhoneNumberTest() {
        capture(activityRule.activity, "Initial_setup_hide_phone_number", TAG, "check_hidePhoneNumberTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(activityRule.activity, "After_hide_phone_number_test", TAG, "check_hidePhoneNumberTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyPhoneNumberTest() {
        "".fillPhoneNumber()
        capture(activityRule.activity, "Initial_setup_phone_number", TAG, "check_emptyOtpTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_mobile_number))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_empty_phone_number_test", TAG, "check_emptyOtpTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidPhoneNumberTest() {
        "45456".fillPhoneNumber()
        capture(activityRule.activity, "Initial_setup_invalid_phone_number", TAG, "check_invalidOtpTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_phone_number))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_phone_number_test", TAG, "check_invalidOtpTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validPhoneNumberTest() {
        "9876543218".fillPhoneNumber()
        capture(activityRule.activity, "Initial_setup_valid_phone_number", TAG, "check_validOtpTest")
        capture(activityRule.activity, "After_valid_phone_number_test", TAG, "check_validOtpTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun fillInputs(userName: String) {
        Util.waitThread(TIMER_SLEEP_1)

        Espresso.onView(ViewMatchers.withId(R.id.inputFirstName)).perform(ViewActions.scrollTo())
        "Rahul".fillText(R.id.inputFirstName)
        Espresso.onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        "Shingane".fillText(R.id.inputLastName)

//        Espresso.onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
//        userName.fillText(R.id.inputUserName)
        if (activity?.binding?.config?.phonenumber?.shouldShow() == true) {
            Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
            "9876543218".fillPhoneNumber()
        }
    }


    @Test
    fun check_editProfileTest() {
        fillInputs("RuchHB01")
        ViewActions.closeSoftKeyboard()
        capture(activityRule.activity, "Initial_setup_edit_profile", TAG, "check_editProfileTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnUpdate)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Profile updated successfully.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_edit_profile", TAG, "check_editProfileTest")
    }
}