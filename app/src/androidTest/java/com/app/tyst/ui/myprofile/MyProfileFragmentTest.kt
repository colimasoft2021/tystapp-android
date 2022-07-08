package com.app.tyst.ui.myprofile

import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.gallery.GalleryPagerActivity
import com.app.tyst.ui.home.HomeActivity
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyProfileFragmentTest {
    private val TIMER_SLEEP = 1

    companion object {
        const val TAG = "MyProfileFragmentTest"
    }

    @get:Rule
    var activityRule = ActivityTestRule<HomeActivity>(HomeActivity::class.java)
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
        activityRule.activity.changeTab(HomeActivity.INDEX_MY_PROFILE)
        Intents.init()
        Util.waitThread(TIMER_SLEEP)
        mIdlingResource = (activityRule.activity.currentFragment as MyProfileFragment).getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

   @Test
    fun check_profileImageClickAdd() {
//        capture(activityRule.activity, "Initial_setup_profile_image_click", TAG, "check_profileImageClickAdd")
//        Espresso.onView(ViewMatchers.withId(R.id.ivUserImage)).perform(ViewActions.click())
//        Intents.intended(IntentMatchers.hasComponent(GalleryPagerActivity::class.java.name))
//        capture(activityRule.activity, "After_profile_image_click_test", TAG, "check_profileImageClickAdd")
//        Util.waitThread(TIMER_SLEEP)
    }
}