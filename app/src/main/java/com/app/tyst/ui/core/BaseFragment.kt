package com.app.tyst.ui.core

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.app.tyst.ui.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.tyst.ui.home.HomeActivity
import com.hb.logger.Logger

/**
 * Created by HB on 21/8/19.
 */
abstract class BaseFragment<T : ViewDataBinding> : Fragment() {
    lateinit var mViewDataBinding: T
    open var mRootView: View? = null
    protected var mBaseActivity: BaseActivity? = null
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRootView == null) {
            mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
            mRootView = mViewDataBinding.root
            iniViews()
            initListener()
        } else {
            container!!.removeView(mRootView)
        }
        return mRootView
    }

    fun getViewDataBinding(): T {
        return mViewDataBinding
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = activity as BaseActivity?
    }

    /**
     * Get Login Parent activity
     */
     fun getLoginActivity(): LoginWithEmailSocialActivity {
         return mBaseActivity as LoginWithEmailSocialActivity
     }

    /**
     * Get Home Parent activity
     */
    fun getHomeActivity(): HomeActivity {
        return mBaseActivity as HomeActivity
    }


    /**
     * Set current fragment
     *
     * @param fragment Current visible fragment
     */
    fun setCurrentFragment(fragment: BaseFragment<*>) {
        mBaseActivity!!.currentFragment = fragment
    }

    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    /**
     * This abstract method is used to initialize or create views object.
     */
    abstract fun iniViews()

    /**
     * This abstract method is used to initialize all listener
     */
    abstract fun initListener()

    fun onBackPressed(): Boolean {
        return false
    }

    fun showHideProgressDialog(isShow: Boolean) {
        mBaseActivity?.showHideProgressDialog(isShow)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
    }
}
