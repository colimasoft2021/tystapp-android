package com.app.tyst.ui.authentication.login.loginwithemailsocial

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail.ForgotPasswordWithEmailActivity
import com.app.tyst.ui.authentication.register.SignUpActivity
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.util.fillEmail
import com.app.tyst.util.fillPassword
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginWithEmailSocialActivityTest : BaseTest() {
    private val TIMER_SLEEP: Int = 1

    @get:Rule
    var activityRule = ActivityTestRule<LoginWithEmailSocialActivity>(LoginWithEmailSocialActivity::class.java)
    var mIdlingResource: RemoteIdlingResource? = null

    @JvmField
    @Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(ScreenshotTakingRule())
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources


    companion object {
        const val TAG = "LoginWithEmailSocialActivityTest"
    }

    @Before
    fun init() {
        mIdlingResource = activityRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    fun fillInputText(strEmail: String, strPassword: String) {
        strEmail.fillEmail()
        Util.waitThread(TIMER_SLEEP)
        strPassword.fillPassword()
        Util.waitThread(TIMER_SLEEP)
    }

    //    @Test
    fun loginScreenTest() {

//        Util.waitThread(TIMER_SLEEP)
//        login_googleTest()

        Util.waitThread(TIMER_SLEEP)
        check_emptyEmailTest()
        Util.waitThread(TIMER_SLEEP)
        check_invalidEmailTest()
        Util.waitThread(TIMER_SLEEP)
        check_emptyPasswordTest()
        Util.waitThread(TIMER_SLEEP)
        check_invalidLoginTest()
//        Util.waitThread(TIMER_SLEEP)
//        check_unverifiedEmailTest()
        Util.waitThread(TIMER_SLEEP)
        login_clickTest()
//        Util.waitThread(TIMER_SLEEP)
//        skipTest()
        Util.waitThread(TIMER_SLEEP)
        forgotPasswordClick_Test()
        Util.waitThread(TIMER_SLEEP)
        signupClick_Test()
        Util.waitThread(TIMER_SLEEP)
        login_facebookTest()
        login_googleTest()

    }

    @Test
    fun check_emptyEmailTest() {
        val strEmptyEmail = ""
        val strPassword = "Hidden@123"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_setup_empty_email", TAG, "check_emptyEmailTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_email))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_empty_email_test", TAG, "check_emptyEmailTest")
    }

    @Test
    fun check_invalidEmailTest() {
        val strEmptyEmail = "citmotog@gmail@.com"
        val strPassword = "Hidden@123"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_setup_invalid_email", TAG, "check_invalidEmailTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_valid_email))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_email_test", TAG, "check_invalidEmailTest")
    }

    @Test
    fun check_emptyPasswordTest() {
        val strEmptyEmail = "citmotog@gmail.com"
        val strPassword = ""
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_setup_empty_password", TAG, "check_emptyPasswordTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_password))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_empty_password", TAG, "check_emptyPasswordTest")
    }

    @Test
    fun check_invalidLoginTest() {
        val strEmptyEmail = "citmotog1@gmail.com"
        val strPassword = "xyzabc"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_setup_invalid_login", TAG, "check_invalidLoginTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Please provide valid login credentials.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_login", TAG, "check_invalidLoginTest")
    }

   /* @Test
    fun check_unverifiedEmailTest() {
        val strEmptyEmail = "citmotog@gmail.com"
        val strPassword = "Abc@123"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_setup_unverified_email_login", TAG, "check_invalidLoginTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Your account is not yet active. Please activate your account through verification email. Do you want to resend verification email ?")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_login", TAG, "check_invalidLoginTest")
    }*/


    @Test
    fun login_clickDeleteAccountTest() {
        val strEmptyEmail = "delete@mailinator.com"
        val strPassword = "Abc@123"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_login_correct_credentials", TAG, "login_clickTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Please provide valid login credentials.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_correct_credentials", TAG, "login_clickTest")
    }

    @Test
    fun login_clickTest() {
        val strEmptyEmail = "neel@mailinator.com"
        val strPassword = "Abc@123"
        fillInputText(strEmptyEmail, strPassword)
        capture(activityRule.activity, "Initial_login_correct_credentials", TAG, "login_clickTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin)).perform(ViewActions.scrollTo(), ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        capture(activityRule.activity, "After_correct_credentials", TAG, "login_clickTest")
    }

    /*@Test
    fun skipTest() {
        capture(activityRule.activity, "Initial_skip_button_click", TAG, "skipTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnSkip)).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        capture(activityRule.activity, "After_skip_button_click", TAG, "skipTest")
    }*/

    @Test
    fun forgotPasswordClick_Test() {
        capture(activityRule.activity, "Initial_forgot_password_click", TAG, "forgotPasswordClick_Test")
        Espresso.onView(ViewMatchers.withId(R.id.btnForgotPassword)).perform(ViewActions.scrollTo(), ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(ForgotPasswordWithEmailActivity::class.java.name))
        capture(activityRule.activity, "After_forgot_password_click", TAG, "forgotPasswordClick_Test")
    }

    @Test
    fun signupClick_Test() {
        capture(activityRule.activity, "Initial_signup_click", TAG, "signupClick_Test")
        Espresso.onView(ViewMatchers.withId(R.id.btnSignUp)).perform(ViewActions.scrollTo(), ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(SignUpActivity::class.java.name))
        capture(activityRule.activity, "After_signup_click_result", TAG, "signupClick_Test")
    }

    @Test
    fun login_facebookTest() {
        Espresso.onView(ViewMatchers.withId(R.id.btnFb)).perform(ViewActions.scrollTo(), ViewActions.click())
        capture(activityRule.activity, "After_fb_click", TAG, "login_facebookTest")
    }

    @Test
    fun login_googleTest() {
        Espresso.onView(ViewMatchers.withId(R.id.btnGoogle)).perform(ViewActions.scrollTo(), ViewActions.click())
        capture(activityRule.activity, "After_google_click", TAG, "login_googleTest")
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

}