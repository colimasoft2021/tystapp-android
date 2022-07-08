package com.app.tyst.ui.core

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.app.tyst.R
import com.app.tyst.databinding.InflateProgressViewBinding

class ProgressDialogFragment : DialogFragment() {

    var binding: InflateProgressViewBinding? = null

    companion object {

        var FRAGMENT_TAG = "dialog"
        fun newInstance(): ProgressDialogFragment {
            val dialogFragment = ProgressDialogFragment()
            dialogFragment.isCancelable = false
            return dialogFragment
        }

        var message: String = ""
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = InflateProgressViewBinding.inflate(inflater, container, false)
        binding?.progressView?.visibility = View.VISIBLE
        binding?.progressView?.playAnimation()
        if (message.isNotEmpty())
            showMessage()
        else
            hideMessage()
        return binding?.root
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun dismiss() {
        binding?.progressView?.cancelAnimation()
        binding?.progressView?.visibility = View.GONE
        binding?.tvProgressMessage?.text = ""
        binding?.tvProgressMessage?.visibility = View.GONE
        message = ""
        if (dialog != null)
            super.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    private fun showMessage() {
        binding?.tvProgressMessage?.text = message
        binding?.tvProgressMessage?.visibility = View.VISIBLE
    }

    private fun hideMessage(){
        binding?.tvProgressMessage?.text = ""
        binding?.tvProgressMessage?.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.dimAmount = 0f

        window?.decorView?.setBackgroundResource(R.color.transparent)
        windowParams?.flags = windowParams!!.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = windowParams
    }
}