package com.pharmacy.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pharmacy.app.BaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.activity.SignInUpActivity
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentSignUpBinding

class SignUpFragment : BaseViewBindingFragment<FragmentSignUpBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSignUpBinding {
        return FragmentSignUpBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFragment()
    }

    private fun initializeFragment() {
        binding.edtEmail.onFocusChangeListener = this
        binding.edtPassword.onFocusChangeListener = this
        binding.edtConfirmPassword.onFocusChangeListener = this
        binding.edtFirstName.onFocusChangeListener = this
        binding.edtLastName.onFocusChangeListener = this
        binding.edtPassword.transformationMethod = biggerDotTranformation
        binding.edtConfirmPassword.transformationMethod = biggerDotTranformation

        binding.btnSignUp.onClick { if (validate()) createCustomer() }
        binding.btnSignIn.onClick { (activity as SignInUpActivity).loadSignInFragment() }
    }

    private fun createCustomer() {
        val requestModel = RequestModel()
        requestModel.email = binding.edtEmail.textToString()
        requestModel.first_name = binding.edtFirstName.textToString()
        requestModel.last_name = binding.edtLastName.textToString()
        requestModel.password = binding.edtPassword.textToString()
        requestModel.username=binding.edtFirstName.textToString()
        (requireActivity() as AppBaseActivity).createCustomer(requestModel) {
            runDelayedOnUiThread(1000) {
                (activity as SignInUpActivity).loadSignInFragment()
            }
        }
    }

    private fun validate(): Boolean {
        return when {
            binding.edtFirstName.checkIsEmpty() -> {
                binding.edtFirstName.showError(getString(R.string.error_field_required))
                false
            }
            binding.edtLastName.checkIsEmpty() -> {
                binding.edtLastName.showError(getString(R.string.error_field_required))
                false
            }
            binding.edtEmail.checkIsEmpty() -> {
                binding.edtEmail.showError(getString(R.string.error_field_required))
                false
            }
            !binding.edtEmail.isValidEmail() -> {
                binding.edtEmail.showError(getString(R.string.error_enter_valid_email))
                false
            }
            binding.edtPassword.checkIsEmpty() -> {
                binding.edtPassword.showError(getString(R.string.error_field_required))
                false
            }
            binding.edtConfirmPassword.checkIsEmpty() -> {
                binding.edtConfirmPassword.showError(getString(R.string.error_field_required))
                false
            }
            !binding.edtPassword.text.toString().equals(binding.edtConfirmPassword.text.toString(), false) -> {
                binding.edtConfirmPassword.showError(getString(R.string.error_password_not_matches))
                false
            }
            else -> true
        }
    }
}