package com.app.tyst.ui.export

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.model.response.LogListResponse
import com.app.tyst.databinding.FragmentExportBinding
import com.app.tyst.databinding.ListItemLogsBinding
import com.app.tyst.ui.core.BaseFragment
import com.app.tyst.ui.transactions.daterange.SelectDateRangeActivity
import com.app.tyst.utility.IConstants
import com.app.tyst.utility.RemoteIdlingResource
import com.app.tyst.utility.extension.clickWithDebounce
import com.app.tyst.utility.extension.makeBounceable
import com.app.tyst.utility.extension.setSwipeToRefreshColor
import com.app.tyst.utility.extension.showSnackBar
import com.app.tyst.utility.helper.LOGApp
import com.master.permissionhelper.PermissionHelper
import com.simpleadapter.SimpleAdapter
import java.io.File


class ExportFragment : BaseFragment<FragmentExportBinding>() {

    lateinit var binding: FragmentExportBinding
    private val viewModel: ExportViewModel by lazy {
        ViewModelProviders.of(this).get(ExportViewModel::class.java)
    }

    private lateinit var adapter: SimpleAdapter<LogListResponse>
    private var permissionHelper: PermissionHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentFragment(this)
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_export
    }

    override fun iniViews() {
        binding = getViewDataBinding()
        mBaseActivity?.showAdMob(binding.adView)
        mBaseActivity?.setFireBaseAnalyticsData("id-exportscreen", "view_exportscreen", "view_exportscreen")
        addObserver()
        initRecycleView()
        showHideProgressDialog(true)
        viewModel.callLogList()
    }

    override fun initListener() {
        binding.apply {
            fabAdd.clickWithDebounce {
                startActivityForResult(SelectDateRangeActivity.getStartIntent(mBaseActivity!!, true), IConstants.REQUEST_DATE_RANGE)
            }
        }
    }

    /**
     * Initialize logs recycle view and show logs records
     */
    private fun initRecycleView() {
        binding.rvLogs.layoutManager = LinearLayoutManager(mBaseActivity)
        adapter = SimpleAdapter.with<LogListResponse, ListItemLogsBinding>(R.layout.list_item_logs) { _, model, binding ->
            binding.logs = model
        }
        binding.rvLogs.adapter = adapter

        binding.srlLogs.setSwipeToRefreshColor()
        binding.srlLogs.setOnRefreshListener {
            if (mBaseActivity?.isNetworkConnected() == true) {
                viewModel.callLogList()
            } else {
                binding.srlLogs.isRefreshing = false
            }
        }

        adapter.setClickableViews({ _, model, _ ->
            if (model.logFile.isNullOrEmpty()) {
                logger.debugEvent("Download Url", "Log file downloaded url not exist")
            } else {
                mBaseActivity?.setFireBaseAnalyticsData("id-exportstatement", "click_exportstatement", "click_exportstatement")
                checkAndRequestPermissionFile(model.logFile)
            }
        }, R.id.btnExport)

        binding.rvLogs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding.fabAdd.hide()
                } else {
                    binding.fabAdd.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

    }

    fun addObserver() {
        // Listen user has purchased ad free and remove add from this screen
        (mBaseActivity?.application as MainApplication).isAdRemoved.observe(this@ExportFragment, Observer {
            if (it) {
                binding.adView.visibility = View.GONE
            }else{
                binding.adView.visibility = View.VISIBLE
            }
        })

        // Observer for notify interstitial add close
        (mBaseActivity?.application as MainApplication).addClose.observe(this@ExportFragment, Observer {
            mIdlingResource?.setIdleState(false)
        })

        viewModel.logsData.observe(this@ExportFragment, Observer {
            showHideProgressDialog(false)
            binding.srlLogs.isRefreshing = false
            if (it.settings?.isSuccess == true && it.data?.isNullOrEmpty() == false) {
                setLogsData(it.data ?: ArrayList())
            } else if (mBaseActivity?.handleApiError(it.settings) == false) {
                showHideNoData(true, it.settings?.message ?: "")
            }
        })

        viewModel.downloadedPath.observe(this@ExportFragment, Observer {
            showHideProgressDialog(false)
            if (it.isNullOrEmpty()) {
                getString(R.string.msg_file_download_failed).showSnackBar(mBaseActivity)
            } else {
//                ("Downloaded file saved at $it").showSnackBar(mBaseActivity, IConstants.SNAKBAR_TYPE_SUCCESS)
                shareDownloadedFile(it)
            }
        })

        viewModel.logGeneratedLiveData.observe(this@ExportFragment, Observer {
            when {
                it.settings?.isSuccess == true -> {
                    viewModel.callLogList()
                }
                mBaseActivity?.handleApiError(it.settings) == false -> {
                    showHideProgressDialog(false)
                    it.settings?.message?.showSnackBar(mBaseActivity)
                }
                else -> showHideProgressDialog(false)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the requestCode is the wanted one and if the result is what we are expecting
        if (resultCode == Activity.RESULT_OK && requestCode == IConstants.REQUEST_DATE_RANGE) {
            mBaseActivity?.showHideProgressDialog(true)
            viewModel.callGenerateLog(startDate = data?.getStringExtra("startDate")
                    ?: "", endDate = data?.getStringExtra("endDate") ?: "")
        }
    }

    /**
     * Show generated logs list
     */
    private fun setLogsData(logs: ArrayList<LogListResponse>) {
        showHideNoData(false)
        adapter.clear()
        adapter.addAll(logs)
        adapter.notifyDataSetChanged()
    }

    private fun showHideNoData(show: Boolean, message: String = getString(R.string.no_record_found)) {
        if (show) {
            binding.rvLogs.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvNoData.text = message
        } else {
            binding.rvLogs.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        }
    }

    /**
     * Share transaction log file
     * @param logFile String path of log file
     */
    private fun shareDownloadedFile(logFile: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/octet-stream"
        val dbUri = FileProvider.getUriForFile(mBaseActivity!!, "${mBaseActivity?.applicationContext?.packageName}.provider", File(logFile))
        shareIntent.putExtra(Intent.EXTRA_STREAM, dbUri)
        mBaseActivity?.intent?.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        mBaseActivity?.intent?.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "Share File Via"))
        mBaseActivity?.setFireBaseAnalyticsData("id-exportreport", "click_exportreportclick", "click_exportreportclick")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * this function check permissions for storage and ask, if it is not given
     */
    @SuppressLint("MissingPermission")
    private fun checkAndRequestPermissionFile(url: String) {
//        if (permissionHelper == null) {
        mIdlingResource?.setIdleState(false)
        permissionHelper = PermissionHelper(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 12)


        permissionHelper?.denied { isSystemDenied ->
            if (isSystemDenied) {
                LOGApp.d("", "Permission denied by system")
                val builder = AlertDialog.Builder(mBaseActivity!!)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(String.format(getString(R.string.msg_we_need_permission_for_gallery), getString(R.string.application_name)))
                builder.setPositiveButton("Ok") { _, _ ->
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", mBaseActivity?.packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, IConstants.CAMERA_REQUEST_CODE)
                }
                builder.setNegativeButton(R.string.cancel) { _, _ -> }

                builder.setCancelable(false)

                val alertDialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED)
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.GRAY)
            }
            mIdlingResource?.setIdleState(true)
        }
//        }

        //Request all permission
        permissionHelper?.requestAll {
            showHideProgressDialog(true)
            viewModel.callDownloadFile(url)
        }
    }

    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }
        return mIdlingResource as RemoteIdlingResource
    }
}