package com.app.tyst.ui.transactions.transactiondetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.ReasonListResponse
import com.app.tyst.data.model.response.TransactionDetailResponse
import com.app.tyst.databinding.ActivityTransactionDetailBinding
import com.app.tyst.databinding.ListItemReceiptImageBinding
import com.app.tyst.ui.core.BaseActivity
import com.app.tyst.ui.gallery.GalleryPagerActivity
import com.app.tyst.ui.subaccounts.SubAccountsActivity
import com.app.tyst.ui.transactions.category.CategoryListActivity
import com.app.tyst.ui.transactions.changetaxandtip.ChangeTaxBottomSheetDialog
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.dialog.DialogUtil
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.hideKeyBoard
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.showSnackBar
import com.simpleadapter.SimpleAdapter

/**
 * This activity is used to show transaction details.
 * @property binding ActivityTransactionDetailBinding
 * @property viewModel TransactionDetailViewModel
 */
class TransactionDetailActivity : BaseActivity() {

    companion object {
        /**
         * Start intent to open TransactionDetailActivity
         * @param mContext Context
         * @param transactionId Id of transaction whose details need to show
         * @return Intent
         */
        fun getStartIntent(mContext: Context, transactionId: String): Intent {
            return Intent(mContext, TransactionDetailActivity::class.java).apply {
                putExtra("transactionId", transactionId)
            }
        }
    }

    lateinit var binding: ActivityTransactionDetailBinding
    private lateinit var adapter: SimpleAdapter<String>

    private val viewModel: TransactionDetailViewModel by lazy {
        ViewModelProviders.of(this@TransactionDetailActivity).get(TransactionDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@TransactionDetailActivity, R.layout.activity_transaction_detail)
        initView()
    }

    private fun initView() {
        setIntentData()
        initListener()
        initRecycleView()
        addObserver()
        showHideProgressDialog(true)
        viewModel.callTransactionDetail()
    }

    /**
     * Set transactionId from [com.app.tyst.ui.transactions.account.AccountTransactionsListActivity]
     */
    private fun setIntentData() {
        viewModel.apply {
            transactionId = intent?.getStringExtra("transactionId") ?: ""
        }
    }

    private fun initListener() {
        binding.apply {
            btnBack.makeBounceable()
            btnBack.clickWithDebounce { finish() }
            btnAddTip.makeBounceable()
            btnAddTip.clickWithDebounce { openChangeTaxDialog(false) }
            btnChangeTax.makeBounceable()
            btnChangeTax.clickWithDebounce { openChangeTaxDialog(true) }
//            ivReceiptImage.clickWithDebounce {
////                startActivity(GalleryPagerActivity.getStartIntent(this@TransactionDetailActivity, arrayListOf(binding.transaction?.receiptImage
////                        ?: "")
////                        , 0))
//            }
            btnChangeCategory.makeBounceable()
            btnChangeCategory.clickWithDebounce {
                startActivityForResult(Intent(this@TransactionDetailActivity, CategoryListActivity::class.java), IConstants.REQUEST_CATEGORY)
            }
            btnDelete.makeBounceable()
            btnDelete.clickWithDebounce { performDeleteTransaction() }
        }
    }

    private fun initRecycleView() {
        binding.rvImages.layoutManager = LinearLayoutManager(this@TransactionDetailActivity, RecyclerView.HORIZONTAL, false)
        adapter = SimpleAdapter.with<String, ListItemReceiptImageBinding>(R.layout.list_item_receipt_image) { _, model, binding ->
            binding.image = model
        }
        binding.rvImages.adapter = adapter

        adapter.setClickableViews({ _, _, position ->
            binding.root.hideKeyBoard()
            startActivity(GalleryPagerActivity.getStartIntent(this@TransactionDetailActivity, adapter.all
                    , position))

        }, R.id.ivReceiptImage)
    }

    private fun setReceiptImages(images: ArrayList<String>) {
        adapter.clear()
        adapter.addAll(images)
    }

    private fun addObserver() {
        viewModel.transactionData.observe(this@TransactionDetailActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                binding.transaction = it.data
                viewModel.setTransactionData(it.data)
                setReceiptImages(it.data?.receiptImages ?: ArrayList())

                binding.llMain.visibility = View.VISIBLE
                if (it.data?.isTransactionIdAvailable() == false) {
                    binding.cvDelete.visibility = View.VISIBLE
                    binding.tvOriginalTax.visibility = View.GONE
                }else{
                    binding.tvOriginalTax.visibility =  if(it.data?.isLocationAvailable() == true) View.VISIBLE else View.GONE
                }
            } else if (!handleApiError(it.settings)) {
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity)
            }
        })

        viewModel.changeTaxLiveData.observe(this@TransactionDetailActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                refreshData(true)
                (application as MainApplication).taxChanged.value = true
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
            } else if (!handleApiError(it.settings)) {
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity)
            }
        })

        viewModel.tipAddedLiveData.observe(this@TransactionDetailActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                refreshData(false)
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
            } else if (!handleApiError(it.settings)) {
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity)
            }
        })

        viewModel.deleteTransactionLiveData.observe(this@TransactionDetailActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                (application as MainApplication).taxChanged.value = true
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler().postDelayed({ finish() }, IConstants.SNAKE_BAR_SHOW_TIME)
            } else if (!handleApiError(it.settings)) {
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity)
            }
        })

        viewModel.updateCategoryLiveData.observe(this@TransactionDetailActivity, Observer {
            showHideProgressDialog(false)
            if (it.settings?.isSuccess == true) {
                (application as MainApplication).categoryUpdated.value = true
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                showHideProgressDialog(true)
                viewModel.callTransactionDetail()
            } else if (!handleApiError(it.settings)) {
                it.settings?.message?.showSnackBar(this@TransactionDetailActivity)
            }
        })
    }

    /**
     * Open bottom sheet dialog for change tax and add tip
     * @param changeTax Boolean
     */
    private fun openChangeTaxDialog(changeTax: Boolean) {
        val dialog = ChangeTaxBottomSheetDialog.newInstance(changeTax, viewModel.getTransactionData()
                ?: TransactionDetailResponse())
        dialog.setListener(object : ChangeTaxBottomSheetDialog.ChangeValueListener {
            override fun onValueChange(forChangeTax: Boolean, newValue: String, reason: ReasonListResponse?) {
                showHideProgressDialog(true)
                if (forChangeTax) {
                    viewModel.getTransactionData()?.reason = reason?.reason
                    viewModel.getTransactionData()?.reasonId = reason?.reasonId
                    viewModel.callChangeTax(newValue, reason?.reasonId ?: "")
                } else {
                    viewModel.callAddTip(newValue)
                }
            }
        })
        dialog.show(supportFragmentManager, "Change Tax Dialog Fragment")
    }

    /**
     * Refresh data after change tax or add tip
     */
    private fun refreshData(forChangeTax: Boolean) {
        if (forChangeTax) {
            viewModel.changeTax()
        } else {
            viewModel.changeTip()
        }
        binding.transaction = viewModel.getTransactionData()
        binding.executePendingBindings()
    }

    /**
     * Show confirmation message for delete transaction
     */
    private fun performDeleteTransaction() {
        DialogUtil.alert(context = this@TransactionDetailActivity, title = getString(R.string.title_delete_transaction), msg = getString(R.string.alert_delete_transaction),
                positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
                il = object : DialogUtil.IL {
                    override fun onSuccess() {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Delete Transaction")
                        showHideProgressDialog(true)
                        viewModel.callDeleteTransaction()
                    }

                    override fun onCancel(isNeutral: Boolean) {
                    }
                }, isCancelable = true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // check if the requestCode is the wanted one and if the result is what we are expecting
           if (requestCode == IConstants.REQUEST_CATEGORY) {
               viewModel.callUpdateCategory(categoryId = data?.getStringExtra("categoryId") ?: "",
               categoryName = data?.getStringExtra("categoryName") ?: "")
               showHideProgressDialog(true)
            }
        }

    }
}
