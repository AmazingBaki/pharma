package com.pharmacy.app.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.databinding.ActivityOtpBinding
import com.pharmacy.app.utils.OTPEditText
import com.pharmacy.app.utils.extensions.*

class OTPActivity : BaseViewBindingActivity<ActivityOtpBinding>() {

    private var mEds: Array<EditText?>? = null
    private var timer: CountDownTimer? = null

    override fun getViewBinding(): ActivityOtpBinding {
        return ActivityOtpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mEds = arrayOf(binding.otpInclude.edDigit1, binding.otpInclude.edDigit2, binding.otpInclude.edDigit3, binding.otpInclude.edDigit4)
        OTPEditText(mEds!!, this,getDrawable(R.color.transparent)!!,getDrawable(R.drawable.bg_unselected_dot)!!)
        mEds!!.forEachIndexed { index, editText ->
            editText?.onFocusChangeListener = focusChangeListener
        }
        timer = startOTPTimer(onTimerTick = {
            binding.otpInclude.tvTimer.text = it
        }, onTimerFinished = {
            binding.otpInclude.tvTimer.hide()
            binding.otpInclude.llResend.show()
        })
        timer?.start()
        binding.otpInclude.tvResend.onClick {
            binding.otpInclude.tvTimer.show()
            binding.otpInclude.llResend.hide()
            timer?.start()

        }
        binding.ivBack.onClick {
            onBackPressed()
        }
    }

    private val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus)
            (v as EditText).background = getDrawable(R.color.transparent)
    }




}