package com.pharmacy.app.activity

import android.app.Activity
import android.os.Bundle
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityAddAddressBinding
import com.pharmacy.app.models.Address
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.SimpleLocation
import com.pharmacy.app.utils.extensions.*

class AddAddressActivity : BaseViewBindingActivity<ActivityAddAddressBinding>(), SimpleLocation.Listener {

    private var address: Address? = null
    private var simpleLocation: SimpleLocation? = null

    override fun getViewBinding(): ActivityAddAddressBinding {
        return ActivityAddAddressBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)

        simpleLocation = SimpleLocation(this)
        simpleLocation?.setListener(this)

        if (intent.hasExtra(Constants.KeyIntent.DATA)) {
            address = intent?.getSerializableExtra(Constants.KeyIntent.DATA) as Address
        }

        if (address != null) {
            title = getString(R.string.lbl_edit_address)
            binding.edtName.setText("""${address?.first_name} ${address?.last_name}""")
            binding.edtCountry.setText(address?.country)
            binding.edtCity.setText(address?.city!!)
            binding.edtState.setText(address?.state!!)
            binding.edtPinCode.setText(address?.postcode!!)
            binding.edtAddress1.setText(address?.address_1)
            binding.edtAddress2.setText(address?.address_2)
            binding.edtMobileNo.setText(address?.contact)
        } else {
            title = getString(R.string.lbl_add_new_address)
        }

        binding.btnSaveAddress.onClick {
            if (validate()) {
                showProgress(true)
                if (address == null) {
                    address = Address()
                }
                assignData()
                addAddress(address!!,onSuccess = {
                    showProgress(false)
                    if (it){
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                })
            }
        }

        binding.rlUseCurrentLocation.onClick {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), onResult = {
                if (it) {
                    if (isGPSEnable()) {
                        if (isNetworkAvailable()) {
                            showProgress(true)
                            simpleLocation?.beginUpdates()
                        } else {
                            snackBarError(getString(R.string.error_gps_not_enabled))
                        }
                    } else {
                        showGPSEnableDialog()
                    }
                } else {
                    showPermissionAlert(this)
                }
            })
        }
    }


    private fun validate(): Boolean {
        when {
            binding.edtName.checkIsEmpty() -> {
                binding.edtName.showError(getString(R.string.error_field_required))
                binding.edtName.requestFocus()
                return false
            }
            binding.edtAddress1.checkIsEmpty() -> {
                binding.edtAddress1.showError(getString(R.string.error_field_required))
                binding.edtAddress1.requestFocus()
                return false
            }

            binding.edtCity.checkIsEmpty() -> {
                binding.edtCity.showError(getString(R.string.error_field_required))
                binding.edtCity.requestFocus()
                return false
            }
            binding.edtState.checkIsEmpty() -> {
                binding.edtState.showError(getString(R.string.error_field_required))
                binding.edtState.requestFocus()
                return false
            }
            binding.edtCountry.checkIsEmpty() -> {
                binding.edtCountry.showError(getString(R.string.error_field_required))
                binding.edtCountry.requestFocus()
                return false
            }
            binding.edtPinCode.checkIsEmpty() -> {
                binding.edtPinCode.showError(getString(R.string.error_field_required))
                binding.edtPinCode.requestFocus()
                return false
            }
            binding.edtMobileNo.checkIsEmpty() -> {
                binding.edtMobileNo.showError(getString(R.string.error_field_required))
                binding.edtMobileNo.requestFocus()
                return false
            }
            else -> return true
        }
    }

    override fun onPositionChanged() {
        showProgress(false)

        val address = simpleLocation?.address
        if (address != null) {
            binding.edtState.setText(address.adminArea)
            binding.edtPinCode.setText(address.postalCode)
            binding.edtCity.setText(address.locality)
            binding.edtCountry.setText(address.countryName)
            if (address.getAddressLine(0) != null) {
                binding.edtAddress1.setText(address.getAddressLine(0))
            }
            if (address.subAdminArea != null) {
                binding.edtAddress2.setText(address.subLocality)
            }
            simpleLocation?.endUpdates()
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    private fun assignData() {
        if (address !== null) {
            address?.first_name = binding.edtName.text.toString()
            val name=binding.edtName.text.toString().split(" ")
            if (name.isNotEmpty() && name.size>1){
                address?.first_name = name[0]
                address?.last_name = name[1]
            }
            address?.fullAddress=null
            address?.city = binding.edtCity.text.toString()
            address?.state = binding.edtState.text.toString()
            address?.postcode = binding.edtPinCode.text.toString()
            address?.address_1= binding.edtAddress1.text.toString()
            address?.address_2 = binding.edtAddress2.text.toString()
            address?.country = binding.edtCountry.text.toString()
            address?.contact=binding.edtMobileNo.text.toString()
        }
    }
}