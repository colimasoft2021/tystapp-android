package com.app.tyst.ui.authentication.register

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivityTest
import com.app.tyst.ui.authentication.register.signupconfig.SignUpConfig
import com.app.tyst.ui.core.AppConfig
import com.app.tyst.util.*
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {
    private val TIMER_SLEEP_1: Int = 1
    private val TIMER_SLEEP_2: Int = 3
    @get:Rule
    val signUpActivityRule = ActivityTestRule<SignUpActivity>(SignUpActivity::class.java)
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources
    var mIdlingResource: RemoteIdlingResource? = null
    var activity: SignUpActivity? = null

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(signUpActivityRule)

    companion object {
        private val TAG: String = "SignUpActivityTest"
        private lateinit var signUpConfig: SignUpConfig
//        @BeforeClass
//        @JvmStatic
//        fun init() {
//
//            mIdlingResource = activity?.getIdlingResource()
//            IdlingRegistry.getInstance().register(mIdlingResource)
//        }
    }

    @Before
    fun init() {
        signUpConfig = AppConfig.getSignUpConfiguration()
        activity = signUpActivityRule.activity
        mIdlingResource = activity?.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

    /*  @Test
      fun firstNameDisplayTest() {
          capture(signUpActivityRule.activity, "Initial_firstNameDisplayTest", TAG, "firstNameDisplayTest")
          if (signUpConfig.firstname.shouldShow()) {
              onView(withId(R.id.inputFirstName)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
          } else {
              onView(withId(R.id.inputFirstName)).check(matches(withEffectiveVisibility(Visibility.GONE)))
          }
          capture(signUpActivityRule.activity, "After_firstNameDisplayTest", TAG, "firstNameDisplayTest")
      }

      @Test
      fun emptyFirstNameValidationTest() {
          capture(signUpActivityRule.activity, "Initial_emptyFirstNameValidationTest", TAG, "emptyFirstNameValidationTest")
          if (signUpConfig.firstname.shouldShow() && signUpConfig.firstname.optional.equals("1")) {
              fillInputText(R.id.inputFirstName, "")
              onView(withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
              onView(withText(resources.getString(R.string.alert_enter_first_name))).check(matches(isDisplayed()))
          }
          capture(signUpActivityRule.activity, "After_emptyFirstNameValidationTest", TAG, "emptyFirstNameValidationTest")
      }

      @Test
      fun emptyLastNameValidationTest() {
          capture(signUpActivityRule.activity, "Initial_firstNameDisplayTest", TAG, "firstNameDisplayTest")
          if (signUpConfig.firstname.shouldShow()) {
              onView(withId(R.id.inputFirstName)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
          } else {
              onView(withId(R.id.inputFirstName)).check(matches(withEffectiveVisibility(Visibility.GONE)))
          }
          capture(signUpActivityRule.activity, "After_firstNameDisplayTest", TAG, "firstNameDisplayTest")
      }

      private fun fillInputText(inputId: Int, strInputValue: String) {
          onView(withId(inputId)).perform(ViewActions.replaceText(strInputValue))
      }
  }*/

    @Test
    fun singUpScreen_Test() {

        checkFirstName()
        checkLastName()
        checkEmailAddress()
        checkUserName()
        checkPhoneNumber()
//        checkAddress()
//        checkCity()
//        checkState()
//        checkZipCode()
//        checkDateOfBirth()
        checkPassword()
        checkConfirmPassword()
        check_TermAndConditionsTest()
//        check_emailAlreadyExistsTest()
        check_signUpTest()
    }

    fun check_imagePickerTest() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.flProfile)).perform(ViewActions.scrollTo(), ViewActions.click())
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withText(R.string.camera)).perform(ViewActions.scrollTo(), ViewActions.click())
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
        capture(signUpActivityRule.activity, "Initial_setup_hide_first_name", TAG, "check_hideFirstNameTest")
        onView(ViewMatchers.withId(R.id.inputFirstName)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_first_name_test", TAG, "check_hideFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyFirstNameTest() {
        "".fillText(R.id.inputFirstName)
        capture(signUpActivityRule.activity, "Initial_setup_empty_first_name", TAG, "check_emptyFirstNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_first_name))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_first_name_test", TAG, "check_emptyFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_inValidFirstNameTest() {
        "Hary 09$".fillText(R.id.inputFirstName)
        capture(signUpActivityRule.activity, "Initial_setup_inValid_first_name", TAG, "check_inValidFirstNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_first_name_character))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_inValid_first_name_test", TAG, "check_inValidFirstNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validFirstNameTest() {
        "Hary".fillText(R.id.inputFirstName)
        capture(signUpActivityRule.activity, "Initial_setup_valid_first_name", TAG, "check_validFirstNameTest")
        capture(signUpActivityRule.activity, "After_valid_first_name_test", TAG, "check_validFirstNameTest")
    }

    /**
     * Test case for test Last Name
     */
    fun checkLastName() {
        if (signUpConfig.lastname.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
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
        capture(signUpActivityRule.activity, "Initial_setup_hide_last_name", TAG, "check_hideLastNameTest")
        onView(ViewMatchers.withId(R.id.inputLastName)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_last_name_test", TAG, "check_hideLastNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyLastNameTest() {
        onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        "".fillText(R.id.inputLastName)
        capture(signUpActivityRule.activity, "Initial_setup_empty_last_name", TAG, "check_emptyLastNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_last_name))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_last_name_test", TAG, "check_emptyLastNameTest")
        onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_inValidLastNameTest() {
        "CJSD#$".fillText(R.id.inputLastName)
        capture(signUpActivityRule.activity, "Initial_setup_inValid_last_name", TAG, "check_inValidLastNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_last_name_character))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_inValid_last_name_test", TAG, "check_inValidLastNameTest")
        onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validLastNameTest() {
        "Hary".fillText(R.id.inputLastName)
        capture(signUpActivityRule.activity, "Initial_setup_valid_last_name", TAG, "check_validLastNameTest")
        capture(signUpActivityRule.activity, "After_valid_last_name_test", TAG, "check_validLastNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test email address
     */
    fun checkEmailAddress() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyEmailTest()
        check_invalidEmailTest()
        check_validEmailTest()
    }

    fun check_emptyEmailTest() {
        "".fillEmail()
        capture(signUpActivityRule.activity, "Initial_setup_empty_email", TAG, "check_emptyEmailTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_email))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_email_test", TAG, "check_emptyEmailTest")
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidEmailTest() {
        "citmotog@gmail@.com".fillEmail()
        capture(signUpActivityRule.activity, "Initial_setup_invalid_email", TAG, "check_invalidEmailTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_valid_email))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_email_test", TAG, "check_invalidEmailTest")
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validEmailTest() {
        "citmotog@gmail.com".fillEmail()
        capture(signUpActivityRule.activity, "Initial_setup_valid_email", TAG, "check_validEmailTest")
        capture(signUpActivityRule.activity, "After_valid_email_test", TAG, "check_validEmailTest")
        Util.waitThread(TIMER_SLEEP_2)
    }


    /**
     * Test case for test User Name
     */
    fun checkUserName() {
        if (signUpConfig.username.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.username.isOptional())
                check_emptyUserNameTest()
            check_userNameLengthTest()
            check_inValidUserNameTest()
            check_validUserNameTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideUserNameTest()
        }
    }

    fun check_hideUserNameTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_user_name", TAG, "check_hideUserNameTest")
        onView(ViewMatchers.withId(R.id.inputUserName)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_user_name_test", TAG, "check_hideUserNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyUserNameTest() {
        onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
        "".fillText(R.id.inputUserName)
        capture(signUpActivityRule.activity, "Initial_setup_empty_user_name", TAG, "check_emptyUserNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_user_name))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_user_name_test", TAG, "check_emptyUserNameTest")
        onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_userNameLengthTest() {
        "CJ".fillText(R.id.inputUserName)
        capture(signUpActivityRule.activity, "Initial_setup_length_user_name", TAG, "check_userNameLengthTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(String.format(resources.getString(R.string.alert_min_user_name_length),
                resources.getInteger(R.integer.user_name_min_length)))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_length_user_name_test", TAG, "check_userNameLengthTest")
        onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_inValidUserNameTest() {
        "CJSD #$".fillText(R.id.inputUserName)
        capture(signUpActivityRule.activity, "Initial_setup_inValid_user_name", TAG, "check_inValidUserNameTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_user_name_character))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_inValid_user_name_test", TAG, "check_inValidUserNameTest")
        onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validUserNameTest() {
        "Hary09".fillText(R.id.inputUserName)
        capture(signUpActivityRule.activity, "Initial_setup_valid_user_name", TAG, "check_validUserNameTest")
        capture(signUpActivityRule.activity, "After_valid_user_name_test", TAG, "check_validUserNameTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test Phone Number
     */
    fun checkPhoneNumber() {
        if (signUpConfig.phonenumber.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
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
        capture(signUpActivityRule.activity, "Initial_setup_hide_phone_number", TAG, "check_hidePhoneNumberTest")
        onView(ViewMatchers.withId(R.id.inputPhoneNumber)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_phone_number_test", TAG, "check_hidePhoneNumberTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyPhoneNumberTest() {
        "".fillPhoneNumber()
        capture(signUpActivityRule.activity, "Initial_setup_phone_number", TAG, "check_emptyOtpTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_mobile_number))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_phone_number_test", TAG, "check_emptyOtpTest")
        onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidPhoneNumberTest() {
        "45456".fillPhoneNumber()
        capture(signUpActivityRule.activity, "Initial_setup_invalid_phone_number", TAG, "check_invalidOtpTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_phone_number))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_phone_number_test", TAG, "check_invalidOtpTest")
        onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validPhoneNumberTest() {
        "9876543218".fillPhoneNumber()
        capture(signUpActivityRule.activity, "Initial_setup_valid_phone_number", TAG, "check_validOtpTest")
        capture(signUpActivityRule.activity, "After_valid_phone_number_test", TAG, "check_validOtpTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test Address
     */
    fun checkAddress() {
        if (signUpConfig.streetaddress.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputAddress)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.streetaddress.isOptional())
                check_emptyAddressTest()
            check_validAddressTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideAddressTest()
        }
    }

    fun check_hideAddressTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_address", TAG, "check_hideAddressTest")
        onView(ViewMatchers.withId(R.id.inputPhoneNumber)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_address_test", TAG, "check_hideAddressTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyAddressTest() {
        "".fillText(R.id.inputAddress)
        capture(signUpActivityRule.activity, "Initial_setup_address", TAG, "check_emptyAddressTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_select_street_address))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_address_test", TAG, "check_emptyAddressTest")
        onView(ViewMatchers.withId(R.id.inputAddress)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validAddressTest() {
        "The Appineers, Woodstock Road, Roswell, GA, USA".fillText(R.id.inputAddress)
        capture(signUpActivityRule.activity, "Initial_setup_invalid_address", TAG, "check_validAddressTest")
        capture(signUpActivityRule.activity, "After_valid_address_test", TAG, "check_validAddressTest")
        Util.waitThread(TIMER_SLEEP_2)
    }


    /**
     * Test case for test city
     */
    fun checkCity() {
        if (signUpConfig.city.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputCity)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.city.isOptional())
                check_emptyCityTest()
            check_validCityTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideCityTest()
        }
    }

    fun check_hideCityTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_city", TAG, "check_hideCityTest")
        onView(ViewMatchers.withId(R.id.inputCity)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_city_test", TAG, "check_hideCityTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyCityTest() {
        "".fillText(R.id.inputCity)
        capture(signUpActivityRule.activity, "Initial_setup_city", TAG, "check_emptyCityTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_city))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_city_test", TAG, "check_emptyCityTest")
        onView(ViewMatchers.withId(R.id.inputCity)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validCityTest() {
        "Roswell".fillText(R.id.inputCity)
        capture(signUpActivityRule.activity, "Initial_setup_invalid_city", TAG, "check_validCityTest")
        capture(signUpActivityRule.activity, "After_valid_city_test", TAG, "check_validCityTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test state
     */
    fun checkState() {
        if (signUpConfig.state.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputState)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.state.isOptional())
                check_emptyStateTest()
            check_validStateTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideStateTest()
        }
    }

    fun check_hideStateTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_state", TAG, "check_hideStateTest")
        onView(ViewMatchers.withId(R.id.inputState)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_state_test", TAG, "check_hideStateTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyStateTest() {
        "".fillText(R.id.inputState)
        capture(signUpActivityRule.activity, "Initial_setup_state", TAG, "check_emptyStateTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_select_state))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_state_test", TAG, "check_emptyStateTest")
        onView(ViewMatchers.withId(R.id.inputState)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validStateTest() {
//        "GA".fillText(R.id.inputState)
        onView(ViewMatchers.withId(R.id.inputState)).perform(ViewActions.scrollTo(), ViewActions.click())
        capture(signUpActivityRule.activity, "Initial_setup_invalid_state", TAG, "check_validStateTest")
        capture(signUpActivityRule.activity, "After_valid_state_test", TAG, "check_validStateTest")
        Util.waitThread(TIMER_SLEEP_2)
    }


    /**
     * Test case for test zip code
     */
    fun checkZipCode() {
        if (signUpConfig.zip.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputZipCode)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.zip.isOptional())
                check_emptyZipTest()
            check_zipCodeLengthTest()
            check_invalidZipTest()
            check_validZipTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideZipTest()
        }
    }

    fun check_hideZipTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_zip", TAG, "check_hideZipTest")
        onView(ViewMatchers.withId(R.id.inputZipCode)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_zip_test", TAG, "check_hideZipTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyZipTest() {
        "".fillText(R.id.inputZipCode)
        capture(signUpActivityRule.activity, "Initial_setup_zip", TAG, "check_emptyZipTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_zip_code))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_zip_test", TAG, "check_emptyZipTest")
        onView(ViewMatchers.withId(R.id.inputZipCode)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_zipCodeLengthTest() {
        "3075".fillText(R.id.inputZipCode)
        capture(signUpActivityRule.activity, "Initial_setup_length_zip_code", TAG, "check_zipCodeLengthTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(String.format(resources.getString(R.string.alert_min_zip_code_length),
                resources.getInteger(R.integer.zip_code_min_length)))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_length_zip_code_test", TAG, "check_zipCodeLengthTest")
        onView(ViewMatchers.withId(R.id.inputZipCode)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidZipTest() {
        "4#zs3dfs".fillText(R.id.inputZipCode)
        capture(signUpActivityRule.activity, "Initial_setup_invalid_zip", TAG, "check_invalidZipTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_invalid_zip_code_character))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_zip_test", TAG, "check_invalidZipTest")
        onView(ViewMatchers.withId(R.id.inputZipCode)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validZipTest() {
        "30075".fillText(R.id.inputZipCode)
        capture(signUpActivityRule.activity, "Initial_setup_valid_zip", TAG, "check_validOtpTest")
        capture(signUpActivityRule.activity, "After_valid_zip_test", TAG, "check_validOtpTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test DOB
     */
    fun checkDateOfBirth() {
        if (signUpConfig.dateofbirth.shouldShow()) {
            onView(ViewMatchers.withId(R.id.inputDOB)).perform(ViewActions.scrollTo())
            Util.waitThread(TIMER_SLEEP_1)
            if (!signUpConfig.dateofbirth.isOptional())
                check_emptyDateOfBirthTest()
            check_validDateOfBirthTest()
        } else {
            Util.waitThread(TIMER_SLEEP_1)
            check_hideDateOfBirthTest()
        }
    }

    fun check_hideDateOfBirthTest() {
        capture(signUpActivityRule.activity, "Initial_setup_hide_dateOfBirth", TAG, "check_hideDateOfBirthTest")
        onView(ViewMatchers.withId(R.id.inputDOB)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        capture(signUpActivityRule.activity, "After_hide_dateOfBirth_test", TAG, "check_hideDateOfBirthTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_emptyDateOfBirthTest() {
        "".fillText(R.id.inputDOB)
        capture(signUpActivityRule.activity, "Initial_setup_dateOfBirth", TAG, "check_emptyDateOfBirthTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_dob))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_empty_dateOfBirth_test", TAG, "check_emptyDateOfBirthTest")
        onView(ViewMatchers.withId(R.id.inputDOB)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validDateOfBirthTest() {
        onView(ViewMatchers.withId(R.id.inputDOB)).perform(ViewActions.scrollTo(), ViewActions.click())
//        "SEP 02 1997".fillText(R.id.inputDOB)
        capture(signUpActivityRule.activity, "After_valid_dateOfBirth_test", TAG, "check_validDateOfBirthTest")
        Util.waitThread(TIMER_SLEEP_2)
    }


    /**
     * Test case for test password
     */
    fun checkPassword() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyPasswordTest()
        check_invalidPasswordTest()
        check_validPasswordTest()
    }

    fun check_emptyPasswordTest() {
        "".fillPassword()
        capture(signUpActivityRule.activity, "Initial_setup_empty_password", TAG, "check_emptyPasswordTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_password))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_empty_password", TAG, "check_emptyPasswordTest")
        onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidPasswordTest() {
        "123456".fillPassword()
        capture(signUpActivityRule.activity, "Initial_setup_invalid_password", TAG, "check_invalidPasswordTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_valid_password))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_invalid_password", TAG, "check_invalidPasswordTest")
        onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validPasswordTest() {
        "Abc@123".fillPassword()
        capture(signUpActivityRule.activity, "Initial_setup_valid_password", TAG, "check_validPasswordTest")
        capture(signUpActivityRule.activity, "After_valid_password_test", TAG, "check_validPasswordTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test confirm password
     */
    fun checkConfirmPassword() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyConfirmPasswordTest()
        check_invalidConfirmPasswordTest()
        check_validConfirmPasswordTest()
    }

    fun check_emptyConfirmPasswordTest() {
        "".fillConfirmPassword()
        capture(signUpActivityRule.activity, "Initial_setup_empty_confirm_password", TAG, "check_emptyConfirmPasswordTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_confirm_password))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_empty_confirm_password", TAG, "check_emptyConfirmPasswordTest")
        onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_invalidConfirmPasswordTest() {
        "123456".fillConfirmPassword()
        capture(signUpActivityRule.activity, "Initial_setup_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_msg_password_and_confirm_password_not_same))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_confirm_password", TAG, "check_invalidConfirmPasswordTest")
        onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun check_validConfirmPasswordTest() {
        "Abc@123".fillConfirmPassword()
        capture(signUpActivityRule.activity, "Initial_setup_valid_confirm_password", TAG, "check_validPasswordTest")
        capture(signUpActivityRule.activity, "After_valid_confirm_password_test", TAG, "check_validPasswordTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    /**
     * Test case for test confirm password
     */
    fun check_TermAndConditionsTest() {
        uncheck_TermAndConditionsTest()
//        checked_TermAndConditionsTest()
    }

    fun uncheck_TermAndConditionsTest() {
//        check(matches(isChecked()))
        capture(signUpActivityRule.activity, "Initial_setup_uncheck_TermAndConditions", TAG, "uncheck_TermAndConditionsTest")
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_accept_tnc_and_privacy))).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_uncheck_TermAndConditions", TAG, "uncheck_TermAndConditionsTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun checked_TermAndConditionsTest() {
//        check(matches(isChecked()))
        capture(signUpActivityRule.activity, "Initial_setup_unchecked_TermAndConditions", TAG, "checked_TermAndConditionsTest")
        onView(ViewMatchers.withId(R.id.cbTNC)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        capture(signUpActivityRule.activity, "After_checked_TermAndConditions", TAG, "checked_TermAndConditionsTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

    fun fillInputs() {
        Util.waitThread(TIMER_SLEEP_1)
//        onView(ViewMatchers.withId(R.id.flProfile)).perform(ViewActions.scrollTo(), ViewActions.click())
//        check_imagePickerTest()

        onView(ViewMatchers.withId(R.id.inputFirstName)).perform(ViewActions.scrollTo())
        "hiddene".fillText(R.id.inputFirstName)
        onView(ViewMatchers.withId(R.id.inputLastName)).perform(ViewActions.scrollTo())
        "braine".fillText(R.id.inputLastName)
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        "hb12345@gmail.com".fillEmail()
//        onView(ViewMatchers.withId(R.id.inputUserName)).perform(ViewActions.scrollTo())
//        "Hary46".fillText(R.id.inputUserName)
        onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.scrollTo())
        "9876549846".fillPhoneNumber()
//        onView(ViewMatchers.withId(R.id.inputAddress)).perform(ViewActions.scrollTo())
//        "The Appineers, Woodstock Road, Roswell, GA, USA".fillText(R.id.inputAddress)
//        onView(ViewMatchers.withId(R.id.inputCity)).perform(ViewActions.scrollTo())
//        "Roswell".fillText(R.id.inputCity)
//        onView(ViewMatchers.withId(R.id.inputState)).perform(ViewActions.scrollTo())
//        onView(ViewMatchers.withId(R.id.inputState)).perform(ViewActions.click())
////        " ".fillText(R.id.inputState) // This will append in city, that's why make it " "
//        onView(ViewMatchers.withId(R.id.inputZipCode)).perform(ViewActions.scrollTo())
//        "30075".fillText(R.id.inputZipCode)
//        onView(ViewMatchers.withId(R.id.inputDOB)).perform(ViewActions.scrollTo())
//        onView(ViewMatchers.withId(R.id.inputDOB)).perform(ViewActions.click())
//        " ".fillText(R.id.inputDOB)
        onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillPassword()
        onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.scrollTo())
        "Abc@123".fillConfirmPassword()
    }

    fun check_emailAlreadyExistsTest() {
        fillInputs()
        capture(signUpActivityRule.activity, "Initial_setup_email_already_exist", LoginWithEmailSocialActivityTest.TAG, "check_emailAlreadyExistsTest")
        onView(ViewMatchers.withId(R.id.cbTNC)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText("Account with this email already exists. ")).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_invalid_login", LoginWithEmailSocialActivityTest.TAG, "check_emailAlreadyExistsTest")
        Util.waitThread(TIMER_SLEEP_1)
    }

    fun check_phoneNumberAlreadyExistsTest() {
        fillInputs()
        capture(signUpActivityRule.activity, "Initial_setup_phone_number_already_exist", LoginWithEmailSocialActivityTest.TAG, "check_phoneNumberAlreadyExistsTest")
        onView(ViewMatchers.withId(R.id.cbTNC)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText("Account with this mobile number already exists. ")).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_exist_phone_number", LoginWithEmailSocialActivityTest.TAG, "check_phoneNumberAlreadyExistsTest")
        Util.waitThread(TIMER_SLEEP_1)
    }

    fun check_userNameAlreadyExistsTest() {
        fillInputs()
        capture(signUpActivityRule.activity, "Initial_setup_user_name_already_exist", LoginWithEmailSocialActivityTest.TAG, "check_userNameAlreadyExistsTest")
        onView(ViewMatchers.withId(R.id.cbTNC)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText("Account with this username already exists. ")).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_exist_user_name", LoginWithEmailSocialActivityTest.TAG, "check_userNameAlreadyExistsTest")
        Util.waitThread(TIMER_SLEEP_1)
    }

    fun check_signUpTest() {
        fillInputs()
        capture(signUpActivityRule.activity, "Initial_setup_signup", LoginWithEmailSocialActivityTest.TAG, "check_signUpTest")
        onView(ViewMatchers.withId(R.id.cbTNC)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.btnCreate)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText("You have successfully registered. Please check your email and confirm your registration.")).check(matches(ViewMatchers.isDisplayed()))
        capture(signUpActivityRule.activity, "After_signup", LoginWithEmailSocialActivityTest.TAG, "check_signUpTest")
        Util.waitThread(5)
    }


}