package com.pharmacy.app.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.utils.extensions.color



/**
 * Base class for fragments using View Binding
 */
abstract class BaseViewBindingFragment<VB : ViewBinding> : Fragment(), View.OnFocusChangeListener {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            (v as EditText).setTextColor(requireActivity().color(R.color.colorPrimaryDark))
            v.background = requireActivity().getDrawable(R.drawable.bg_ractangle_rounded_active)
        } else {
            (v as EditText).setTextColor(requireActivity().color(R.color.textColorPrimary))
            v.background = requireActivity().getDrawable(R.drawable.bg_ractangle_rounded_inactive)
        }
    }

    fun hideProgress() {
        if (activity != null)
            (requireActivity() as AppBaseActivity).showProgress(false)
    }

    fun showProgress() {
        if (activity != null)
            (requireActivity() as AppBaseActivity).showProgress(true)
    }

    object biggerDotTranformation : PasswordTransformationMethod() {

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(super.getTransformation(source, view))
        }

        private class PasswordCharSequence(val transformation: CharSequence) : CharSequence by transformation {
            override fun get(index: Int): Char = if (transformation[index] == DOT) {
                BIGGER_DOT
            } else {
                transformation[index]
            }
        }

        private const val DOT = '\u2022'
        private const val BIGGER_DOT = '●'
    }
}

// Keeping the original BaseFragment for backward compatibility
abstract class BaseFragment : Fragment(), View.OnFocusChangeListener {

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            (v as EditText).setTextColor(requireActivity().color(R.color.colorPrimaryDark))
            v.background = requireActivity().getDrawable(R.drawable.bg_ractangle_rounded_active)
        } else {
            (v as EditText).setTextColor(requireActivity().color(R.color.textColorPrimary))
            v.background = requireActivity().getDrawable(R.drawable.bg_ractangle_rounded_inactive)
        }
    }

    fun hideProgress() {
        if (activity != null)
            (requireActivity() as AppBaseActivity).showProgress(false)
    }

    fun showProgress() {
        if (activity != null)
            (requireActivity() as AppBaseActivity).showProgress(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    object biggerDotTranformation : PasswordTransformationMethod() {

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(super.getTransformation(source, view))
        }

        private class PasswordCharSequence(val transformation: CharSequence) : CharSequence by transformation {
            override fun get(index: Int): Char = if (transformation[index] == DOT) {
                BIGGER_DOT
            } else {
                transformation[index]
            }
        }

        private const val DOT = '\u2022'
        private const val BIGGER_DOT = '●'
    }
}