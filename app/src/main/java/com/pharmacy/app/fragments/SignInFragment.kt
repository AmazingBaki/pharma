package com.pharmacy.app.fragments

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.activity.DashBoardActivity
import com.pharmacy.app.activity.SignInUpActivity
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentSignInBinding

class SignInFragment : BaseViewBindingFragment<FragmentSignInBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSignInBinding {
        return FragmentSignInBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtEmail.onFocusChangeListener = this
        binding.edtPassword.onFocusChangeListener = this
        binding.edtPassword.transformationMethod = biggerDotTranformation

        binding.edtEmail.setSelection(binding.edtEmail.length())
        binding.btnSignIn.onClick { if (validate()) doLogin() }
        binding.tvForget.onClick { snackBar(context.getString(R.string.lbl_coming_soon)) }
//        binding.ivFaceBook.onClick {
//            (activity as SignInUpActivity).doFacebookLogin()
//        }
//        binding.ivGoogle.onClick {
//            (activity as SignInUpActivity).doGoogleLogin()
//        }
        binding.btnSignUp.onClick { (activity as SignInUpActivity).loadSignUpFragment() }
        binding.tvForget.onClick { showForgotPasswordDialog() }
    }

    private fun validate(): Boolean {
        return when {
            binding.edtEmail.checkIsEmpty() -> {
                binding.edtEmail.showError(getString(R.string.error_field_required))
                false
            }
            binding.edtPassword.checkIsEmpty() -> {
                binding.edtPassword.showError(getString(R.string.error_field_required))
                false
            }
            else -> true
        }
    }

    private fun doLogin() {
        (activity as AppBaseActivity).signIn(
                binding.edtEmail.textToString(),
                binding.edtPassword.textToString(),
                onResult = {
                    if (it) activity?.launchActivityWithNewTask<DashBoardActivity>()
                },
                onError = {
                    activity?.snackBarError(it)
                })
    }

    private fun showForgotPasswordDialog() {
        val forgotPasswordDialog = Dialog(requireActivity())
        forgotPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        forgotPasswordDialog.setContentView(R.layout.dialog_forgot_password)
        forgotPasswordDialog.window?.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

        val btnForgotPassword = forgotPasswordDialog.findViewById<android.widget.Button>(R.id.btnForgotPassword)
        val edtForgotEmail = forgotPasswordDialog.findViewById<android.widget.EditText>(R.id.edtForgotEmail)
        
        forgotPasswordDialog.apply {
            btnForgotPassword.onClick {
                edtForgotEmail.hideSoftKeyboard()
                if (edtForgotEmail.textToString().isEmpty()) {
                    toast("Please Enter Email")
                    return@onClick
                }
                if (!edtForgotEmail.isValidEmail()) {
                    toast("Please Enter Valid Email")
                    return@onClick
                }

                val requestModel = RequestModel()
                requestModel.user_login = edtForgotEmail.textToString()

                callApi(getRestApis().forgetPassword(requestModel), onApiSuccess = {
                    edtForgotEmail.hideSoftKeyboard()
                    toast(it.message ?: "")
                    forgotPasswordDialog.dismiss()
                }, onApiError = {
                    toast(it)
                }, onNetworkError = {
                    toast(R.string.error_no_internet)
                })
            }

            show()
        }
    }
}