package com.app.tyst.ui.core

import org.junit.rules.TestWatcher
import org.junit.runner.Description


open class BaseTest {


    inner class ScreenshotTakingRule : TestWatcher() {

        override fun failed(e: Throwable?, description: Description) {
        }
    }
}