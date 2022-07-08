package com.app.tyst.ui.transactions.category

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.tyst.R
import com.app.tyst.data.model.response.AccountResponse
import com.app.tyst.data.model.response.CategoryResponse
import com.app.tyst.databinding.ActivityAccountTransactionsListBinding
import com.app.tyst.databinding.ActivityCategoryListBinding
import com.app.tyst.databinding.ListItemAccountsBinding
import com.app.tyst.databinding.ListItemCategoryBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.subaccounts.SubAccountsViewModel
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.setSwipeToRefreshColor
import com.simpleadapter.SimpleAdapter

class CategoryListActivity : BaseActivity() {

    lateinit var binding: ActivityCategoryListBinding
    private lateinit var adapter: SimpleAdapter<CategoryResponse>

    private val viewModel: CategoryViewModel by lazy {
        ViewModelProviders.of(this@CategoryListActivity).get(CategoryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CategoryListActivity, R.layout.activity_category_list)
        initView()
    }

    private fun initView() {
        initListeners()
        iniRecycleView()
        addObserver()
        viewModel.callCategoryList()
        showHideProgressDialog(true)
    }

    private fun iniRecycleView() {
        binding.rvCategory.layoutManager = LinearLayoutManager(this)
        adapter = SimpleAdapter.with<CategoryResponse, ListItemCategoryBinding>(R.layout.list_item_category) { _, model, binding ->
            binding.accounts = model
            binding.executePendingBindings()
        }
        binding.rvCategory.adapter = adapter

        binding.srlAccounts.setSwipeToRefreshColor()
        binding.srlAccounts.setOnRefreshListener {
            if (isNetworkConnected()) {
                viewModel.callCategoryList()
            } else {
                binding.srlAccounts.isRefreshing = false
            }
        }

        adapter.setClickableViews({ _, model, _ ->
            if(isNetworkConnected()) {
                sendResult(model.category_id ?: "", model.category ?: "")
            }
        }, R.id.llMain)
    }

    private fun initListeners() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce {
                finish()
            }
        }
    }

    private fun addObserver(){
        viewModel.categoryData.observe(this@CategoryListActivity, Observer {
            showHideProgressDialog(false)
            binding.srlAccounts.isRefreshing = false
            if (it.settings?.isSuccess == true) {
                setCategoryData(it.data)
            } else if (!handleApiError(it.settings)) {
                showHideNoData(true)
            } else {
                showHideNoData(true)
            }
        })
    }

    /**
     * Show categories
     */
    private fun setCategoryData(categories: ArrayList<CategoryResponse>?) {
        if (categories?.isNotEmpty() == true) {
            showHideNoData(false)
            adapter.clear()
            adapter.addAll(categories)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showHideNoData(show: Boolean, message: String = getString(R.string.no_category_found)) {
        if (show) {
            binding.rvCategory.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvNoData.text = message
        } else {
            binding.rvCategory.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }

    /**
     * Send selected account to calling activity
     */
    private fun sendResult(categoryId: String, categoryName: String) {
        // if you want to send any type of data back, such as an object, string, int etc
        // you have to create an intent and use this bit of code
        // otherwise you can just use setResult(Activity.RESULT_OK) and finish()
        try {
            val resultIntent = Intent()
            resultIntent.putExtra("categoryId", categoryId)
            resultIntent.putExtra("categoryName", categoryName)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } catch (ex: Exception) {

            logger.dumpCrashEvent(ex)
        }
    }
}
