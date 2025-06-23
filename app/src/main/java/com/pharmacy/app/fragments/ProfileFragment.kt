package com.pharmacy.app.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.BuildConfig
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants.SharedPref.IS_SOCIAL_LOGIN
import com.pharmacy.app.utils.Constants.SharedPref.USER_PASSWORD
import com.pharmacy.app.utils.ImagePicker
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentProfileBinding
import java.io.File
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import com.pharmacy.app.R
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import android.app.Activity.RESULT_OK
import com.pharmacy.app.activity.DashBoardActivity
import androidx.activity.result.contract.ActivityResultContracts


class ProfileFragment : BaseViewBindingFragment<FragmentProfileBinding>() {

    private var encodedImage: String? = null
    
    // Modern Activity Result API for cropping
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the cropped image URI
            val croppedImageUri = result.uriContent
            if (croppedImageUri != null) {
                binding.ivProfileImage.setImageURI(croppedImageUri)
                val imageStream = requireActivity().contentResolver.openInputStream(croppedImageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                encodedImage = encodeImage(selectedImage)
                if (encodedImage != null) {
                    updateProfilePhoto()
                }
            }
        } else {
            // An error occurred
            val exception = result.error
            if (exception?.message != null) {
                snackBar(exception.message!!)
            }
        }
    }
    


    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isLoggedIn()) {
            binding.edtEmail.setText(getEmail())
            binding.edtFirstName.setText(getFirstName())
            binding.edtLastName.setText(getLastName())
            binding.edtFirstName.setSelection(binding.edtFirstName.text.length)
            binding.ivProfileImage.loadImageFromUrl(getUserProfile(),aPlaceHolderImage = R.drawable.ic_profile)
            if (getSharedPrefInstance().getBooleanValue(IS_SOCIAL_LOGIN)) {
                binding.btnChangePassword.hide()
            } else {
                binding.btnChangePassword.show()
            }
        }
        setUpListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle ImagePicker result and launch crop
        if (data != null && data.data != null) {
            val path: String? = ImagePicker.getImagePathFromResult(requireActivity(), requestCode, resultCode, data)
            if (path != null) {
                val uri = FileProvider.getUriForFile(
                    requireActivity(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    File(path)
                )
                // Launch crop with the selected image
                cropImage.launch(
                    CropImageContractOptions(
                        uri = uri,
                        cropImageOptions = CropImageOptions(
                            outputCompressQuality = 40
                        )
                    )
                )
            } else {
                // Direct image selection
                cropImage.launch(
                    CropImageContractOptions(
                        uri = data.data,
                        cropImageOptions = CropImageOptions(
                            outputCompressQuality = 40
                        )
                    )
                )
            }
        }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun setUpListener() {
        binding.btnSaveProFile.onClick {
            if (validate()) {
                showProgress()
                updateProfile()
            }
        }
        binding.btnChangePassword.onClick { showChangePasswordDialog() }
        binding.btnDeactivate.onClick {

        }
        binding.editProfileImage.onClick {
            activity?.requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), onResult = {
                    if (it) {
                       /* ImagePicker.pickImage(
                            this@ProfileFragment,
                            context.getString(R.string.lbl_select_image),
                            ImagePicker.mPickImageRequestCode,
                            false
                        )*/
                        // Use modern Activity Result API
                        cropImage.launch(
                            CropImageContractOptions(
                                uri = null,
                                cropImageOptions = CropImageOptions(
                                    aspectRatioX = 1,
                                    aspectRatioY = 1,
                                    fixAspectRatio = true,
                                    guidelines = CropImageView.Guidelines.OFF,
                                    outputCompressQuality = 40
                                )
                            )
                        )
                    } else {
                        requireActivity().showPermissionAlert(this)
                    }
                })
        }

    }

    private fun updateProfilePhoto() {
        showProgress()
        val requestModel = RequestModel()
        requestModel.base64_img = encodedImage
        requireActivity().saveProfileImage(requestModel, onSuccess = {
            hideProgress()
            encodedImage=null
            (activity as DashBoardActivity).changeProfile()
        })

    }

    private fun showChangePasswordDialog() {
        val changePasswordDialog = Dialog(requireActivity())
        changePasswordDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        changePasswordDialog.setContentView(R.layout.dialog_reset)
        changePasswordDialog.window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val edtOldPwd = changePasswordDialog.findViewById<android.widget.EditText>(R.id.edtOldPwd)
        val edtConfirmPwd = changePasswordDialog.findViewById<android.widget.EditText>(R.id.edtConfirmPwd)
        val edtNewPwd = changePasswordDialog.findViewById<android.widget.EditText>(R.id.edtNewPwd)
        val btnChangePassword = changePasswordDialog.findViewById<android.widget.Button>(R.id.btnChangePassword)
        
        edtOldPwd.transformationMethod = biggerDotTranformation
        edtConfirmPwd.transformationMethod = biggerDotTranformation
        edtNewPwd.transformationMethod = biggerDotTranformation
        btnChangePassword.onClick {
            val mPassword = getSharedPrefInstance().getStringValue(USER_PASSWORD)
            when {
                edtOldPwd.checkIsEmpty() -> {
                    edtOldPwd.showError(getString(R.string.error_field_required))
                }
                edtNewPwd.checkIsEmpty() -> {
                    edtNewPwd.showError(getString(R.string.error_field_required))
                }
                edtNewPwd.validPassword() -> {
                    edtNewPwd.showError(getString(R.string.error_pwd_digit_required))
                }
                edtConfirmPwd.checkIsEmpty() -> {
                    edtConfirmPwd.showError(getString(R.string.error_field_required))
                }
                edtConfirmPwd.validPassword() -> {
                    edtConfirmPwd.showError(getString(R.string.error_pwd_digit_required))
                }
                !edtConfirmPwd.text.toString().equals(
                    edtNewPwd.text.toString(),
                    false
                ) -> {
                    edtConfirmPwd.showError(getString(R.string.error_password_not_matches))
                }
                !edtOldPwd.text.toString().equals(mPassword, false) -> {
                    edtOldPwd.showError(getString(R.string.error_old_password_not_matches))
                }
                edtNewPwd.text.toString().equals(mPassword, false) -> {
                    edtNewPwd.showError(getString(R.string.error_new_password_same))
                }
                else -> {
                    val requestModel = RequestModel()
                    requestModel.password = edtNewPwd.text.toString()
                    showProgress()
                    requireActivity().changePassword(requestModel, onSuccess = {
                        hideProgress()
                        changePasswordDialog.dismiss()
                    })
                }
            }
        }
        changePasswordDialog.show()
    }

    private fun updateProfile() {
        val requestModel = RequestModel()
        requestModel.email = binding.edtEmail.textToString()
        requestModel.first_name = binding.edtFirstName.textToString()
        requestModel.last_name = binding.edtLastName.textToString()
        (activity as AppBaseActivity).createCustomer(requestModel) {
            snackBar(getString(R.string.lbl_profile_saved))
                hideProgress()
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
            else -> true
        }

    }
}