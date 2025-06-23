package com.pharmacy.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.databinding.ActivityAddressManagerBinding
import com.pharmacy.app.databinding.ItemAddressNewBinding
import com.pharmacy.app.models.Address
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.AppBroadcasts.ADDRESS_UPDATE
import com.pharmacy.app.utils.Constants.KeyIntent
import com.pharmacy.app.utils.Constants.RequestCode.ADD_ADDRESS
import com.pharmacy.app.utils.extensions.*

class AddressManagerActivity : BaseViewBindingActivity<ActivityAddressManagerBinding>() {
    var selected: Int = 0

    private var addressAdapter = object : BaseRecyclerAdapter<Address, ItemAddressNewBinding>() {
        override val layoutResId: Int = R.layout.item_address_new

        override fun onBindData(model: Address, position: Int, dataBinding: ItemAddressNewBinding) {
            dataBinding.rbDefaultAddress.isChecked = position == selected
            if (selected==position){
                dataBinding.included.tvDefault.show()
            }else{
                dataBinding.included.tvDefault.hide()

            }
            dataBinding.included.tvUserName.text = """${model.first_name} ${model.last_name}"""
            dataBinding.included.tvAddress.text = model.getAddress()
            dataBinding.included.tvMobileNo.text = model.contact

        }

        override fun onItemClick(view: View, model: Address, position: Int, dataBinding: ItemAddressNewBinding) {
            when (view.id) {
                R.id.addressLayout -> setDefaultAddress(position)
                R.id.fabEdit -> {
                    dataBinding.swipeLayout.close(true)
                    runDelayed(200) {
                        launchActivity<AddAddressActivity>(ADD_ADDRESS) {
                            putExtra(KeyIntent.DATA, model)
                        }
                    }
                }
                R.id.fabDelete -> {
                    getAlertDialog(getString(R.string.msg_confirmation), onPositiveClick = { dialog, i ->
                        removeAddress(model.ID!!, position)
                    }, onNegativeClick = { dialog, i ->
                        dialog.dismiss()
                    }).show()
                }
            }
        }

        override fun onItemLongClick(view: View, model: Address, position: Int) {

        }

    }

    override fun getViewBinding(): ActivityAddressManagerBinding {
        return ActivityAddressManagerBinding.inflate(layoutInflater)
    }

    private fun removeAddress(id: Int, position: Int) {
        showProgress(true)
        val request = RequestModel()
        request.ID = id
        removeAddress(request, onSuccess = {
            showProgress(false)
            if (it) addressAdapter.removeItem(position)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.lbl_address_manager)

        BroadcastReceiverExt(this) { onAction(ADDRESS_UPDATE) { loadAddressList() } }

        binding.rvAddress.setVerticalLayout(); binding.rvAddress.adapter = addressAdapter
        loadAddressList()

        binding.btnAddNew.onClick { launchActivity<AddAddressActivity>(ADD_ADDRESS) }

    }

    private fun loadAddressList() {
        addressAdapter.clearData()
        addressAdapter.addItems(getAddressList())
        setDefaultAddress(0)
        val id= getSharedPrefInstance().getIntValue(Constants.SharedPref.KEY_ADDRESS,0)
        addressAdapter.mModelList.forEachIndexed { index, address ->
            if (address.ID==id){
                selected=index
            }
        }
        addressAdapter.notifyDataSetChanged()
    }

    private fun setDefaultAddress(position: Int) {
        selected = position
        getSharedPrefInstance().setValue(Constants.SharedPref.KEY_ADDRESS,addressAdapter.mModelList[position].ID)
        addressAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ADDRESS && resultCode == Activity.RESULT_OK) {
            loadAddressList()
        }
    }
}