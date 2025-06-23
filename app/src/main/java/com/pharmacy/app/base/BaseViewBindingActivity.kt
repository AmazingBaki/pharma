package com.pharmacy.app.base

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.utils.extensions.color

/**
 * Base class for activities using View Binding that extends AppBaseActivity
 * to maintain all the existing functionality like ads, progress dialog, etc.
 */
abstract class BaseViewBindingActivity<VB : ViewBinding> : AppBaseActivity(), View.OnFocusChangeListener {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    abstract fun getViewBinding(): VB
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call AppBaseActivity's onCreate first to maintain existing functionality
        super.onCreate(savedInstanceState)
        _binding = getViewBinding()
        setContentView(binding.root)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            (v as EditText).setTextColor(color(R.color.colorPrimaryDark))
            v.background = getDrawable(R.drawable.bg_ractangle_rounded_active)
        } else {
            (v as EditText).setTextColor(color(R.color.textColorPrimary))
            v.background = getDrawable(R.drawable.bg_ractangle_rounded_inactive)
        }
    }
} 