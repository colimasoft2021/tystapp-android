package com.app.tyst.ui.home

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith




@RunWith(AndroidJUnit4::class)
class HomeActivityTest : BaseTest() {

    private val TIMER_SLEEP = 1

    companion object {
        const val TAG = "HomeActivityTest"
    }

    @get:Rule
    var activityRule = ActivityTestRule<HomeActivity>(HomeActivity::class.java)

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(activityRule)
    @Before
    fun init() {
    }

    @Test
    fun homeScreen_Test() {
        Util.waitThread(5)
        check_homeTab()
        check_exportTab()
        check_receiptTab()
        check_myProfileTab()
        check_settingTab()
    }

     fun check_homeTab() {
        val matcher = allOf(withText("Home"),isDescendantOfA(withId(R.id.bottom_navigation)))
        capture(activityRule.activity, "Initial_setup_home_tab", TAG, "check_homeTab")
        onView(matcher).perform(ViewActions.click())
        capture(activityRule.activity, "After_click_home_tab", TAG, "check_homeTab")
        Util.waitThread(TIMER_SLEEP)
    }

     fun check_exportTab() {
        capture(activityRule.activity, "Initial_setup_friends_tab", TAG, "check_friendsTab")
        onView(withText("Export")).perform(ViewActions.click())
        capture(activityRule.activity, "After_click_friends_tab", TAG, "check_friendsTab")
        Util.waitThread(TIMER_SLEEP)
    }

     fun check_receiptTab() {
        capture(activityRule.activity, "Initial_setup_message_tab", TAG, "check_messageTab")
        onView(withText("Receipt")).perform(ViewActions.click())
        capture(activityRule.activity, "After_click_message_tab", TAG, "check_messageTab")
        Util.waitThread(TIMER_SLEEP)
    }

     fun check_myProfileTab() {
        capture(activityRule.activity, "Initial_setup_myProfile_tab", TAG, "check_myProfileTab")
        onView(withText("Profile")).perform(ViewActions.click())
        capture(activityRule.activity, "After_click_myProfile_tab", TAG, "check_myProfileTab")
        Util.waitThread(TIMER_SLEEP)
    }

     fun check_settingTab() {
        capture(activityRule.activity, "Initial_setup_setting_tab", TAG, "check_settingTab")
        onView(withText("Settings")).perform(ViewActions.click())
        capture(activityRule.activity, "After_click_setting_tab", TAG, "check_settingTab")
        Util.waitThread(TIMER_SLEEP)
    }

}