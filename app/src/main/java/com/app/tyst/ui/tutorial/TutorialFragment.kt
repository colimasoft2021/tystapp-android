package com.app.tyst.ui.tutorial

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.app.tyst.R
import com.app.tyst.databinding.FragmentTutorialBinding
import com.app.tyst.ui.core.BaseFragment

class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {

    private lateinit var binding: FragmentTutorialBinding

    private val viewModel: TutorialViewModel by lazy {
        ViewModelProviders.of(this@TutorialFragment).get(TutorialViewModel::class.java)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TutorialFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(screenNumber: Int) =
                TutorialFragment().apply {
                    arguments = Bundle().apply {
                        putInt("screen_number", screenNumber)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.screenNumber = it.getInt("screen_number")
            viewModel.setInfo()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tutorial
    }

    override fun iniViews() {
        binding = getViewDataBinding()
        binding.tvTitle.text = viewModel.title
        binding.tvDescription.text = viewModel.desc
        binding.ivTutorial.setImageDrawable(viewModel.tutorialImage)

        if (viewModel.screenNumber == 3) {
            binding.tvDescription.visibility = View.GONE
            binding.tvTaxFilled.visibility = View.VISIBLE
            binding.tvTaxUnFilled.visibility = View.VISIBLE
        } else {
            binding.tvDescription.visibility = View.VISIBLE
            binding.tvTaxFilled.visibility = View.GONE
            binding.tvTaxUnFilled.visibility = View.GONE
        }
    }

    override fun initListener() {

    }
}