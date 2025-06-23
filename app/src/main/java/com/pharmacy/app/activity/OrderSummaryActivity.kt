package com.pharmacy.app.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RelativeLayout
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ActivityOrderSummaryBinding
import com.pharmacy.app.databinding.ItemCartBinding
import com.pharmacy.app.databinding.ItemUserAddressBinding
import com.pharmacy.app.models.*
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.KeyIntent.PRODUCT_ID
import com.pharmacy.app.utils.Constants.RequestCode.ADD_ADDRESS
import com.pharmacy.app.utils.Constants.SharedPref.CART_DATA
import com.pharmacy.app.utils.Constants.isAllowedToCreate
import com.pharmacy.app.utils.extensions.*
import java.util.*

class OrderSummaryActivity : BaseViewBindingActivity<ActivityOrderSummaryBinding>() {

    private lateinit var dialog: Dialog
    private val mImg = ArrayList<String>()
    var mCartModel: CartResponse? = null
    var selected: Int = 0

    override fun getViewBinding(): ActivityOrderSummaryBinding {
        return ActivityOrderSummaryBinding.inflate(layoutInflater)
    }

    private var mAddressAdapter = object : BaseRecyclerAdapter<Address, ItemUserAddressBinding>() {
        override val layoutResId: Int get() = R.layout.item_user_address

        override fun onBindData(
            model: Address,
            position: Int,
            dataBinding: ItemUserAddressBinding
        ) {
            if (position == selected) {
                dataBinding.included.tvEdit.show()
            } else {
                dataBinding.included.tvEdit.hide()
            }

            dataBinding.rbDefaultAddress.isChecked = position == selected
            dataBinding.included.tvUserName.text = """${model.first_name} ${model.last_name}"""
            dataBinding.included.tvAddress.text = model.getAddress()
            dataBinding.included.tvMobileNo.text = model.contact

            dataBinding.included.tvEdit.onClick {
                launchActivity<AddAddressActivity>(ADD_ADDRESS) {
                    putExtra(DATA, model)
                }
            }
        }

        override fun onItemClick(view: View, model: Address, position: Int, dataBinding: ItemUserAddressBinding) {
            setDefaultAddress(position)
        }

        override fun onItemLongClick(view: View, model: Address, position: Int) {

        }
    }

    private var cartAdapter: BaseRecyclerAdapter<CartResponse, ItemCartBinding> =
        object : BaseRecyclerAdapter<CartResponse, ItemCartBinding>() {
            override val layoutResId: Int = R.layout.item_cart

            override fun onBindData(
                model: CartResponse,
                position: Int,
                dataBinding: ItemCartBinding
            ) {
                mCartModel = model
                if (model.sale_price.isNotEmpty()) {
                    dataBinding.tvPrice.text =
                        (model.sale_price.toInt() * model.quantity.toInt()).toString()
                            .currencyFormat()
                } else {
                    dataBinding.tvPrice.text =
                        (model.price.toFloat().toInt() * model.quantity.toInt()).toString()
                            .currencyFormat()
                }
                dataBinding.tvOriginalPrice.text = model.regular_price.currencyFormat()
                dataBinding.tvOriginalPrice.applyStrike()
                dataBinding.edtQty.text = model.quantity
                if (model.full != null) dataBinding.ivProduct.loadImageFromUrl(model.full)
            }

            override fun onItemClick(view: View, model: CartResponse, position: Int, dataBinding: ItemCartBinding) {
                when (view.id) {
                    else -> {
                        launchActivity<ProductDetailActivityNew> {
                            putExtra(PRODUCT_ID, model.pro_id.toInt())
                        }
                    }
                }
            }

            override fun onItemLongClick(view: View, model: CartResponse, position: Int) {
            }
        }


    private fun setDefaultAddress(position: Int) {
        selected = position
        mAddressAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.order_summary)
        BroadcastReceiverExt(this) {
            onAction(Constants.AppBroadcasts.ADDRESS_UPDATE) {
                loadAddressList()
            }
        }
        getOffers()

        binding.rvItems.apply { setVerticalLayout(); adapter = cartAdapter }
        cartAdapter.addItems(getCartListFromPref())
        getCartTotal()

        initChangeAddressDialog()
        binding.btnChangeAddress.onClick {
            if (mAddressAdapter.size == 0) {
                launchActivity<AddAddressActivity>(ADD_ADDRESS)
            } else {
                dialog.show()
            }
        }
        val mPaymentDetail = getCartTotal()
        binding.tvReset.text = mPaymentDetail.toString().currencyFormat()
        binding.tvApply.onClick { createOrder() }
        if (getAddressList().size == 0) {
            launchActivity<AddAddressActivity>(ADD_ADDRESS)
            binding.llAddress.hide()
        } else {
            binding.llAddress.show()
        }
    }

    private fun getOffers() {
        getSlideImagesFromPref().forEach { mImg.add(it.image) }
        if (mImg.isNotEmpty()) {
            val handler = Handler()
            val runnable = object : Runnable {
                var i = 0
                override fun run() {
                    binding.ivOffer.loadImageFromUrl(mImg[i])
                    i++
                    if (i > mImg.size - 1) {
                        i = 0
                    }
                    handler.postDelayed(this, 3000)
                }
            }
            handler.postDelayed(runnable, 3000)
        }

    }

    private fun initChangeAddressDialog() {

        dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setContentView(R.layout.dialog_change_address)
        dialog.window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.findViewById<android.widget.TextView>(R.id.tvAddNewAddress).onClick {
            launchActivity<AddAddressActivity>(ADD_ADDRESS)
        }
        dialog.findViewById<android.widget.TextView>(R.id.tvItemDeliverHere).onClick {
            dialog.dismiss()
            binding.llAddress.show()
            updateAddress()
        }
        dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddress).setVerticalLayout()
        dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddress).adapter = mAddressAdapter
        loadAddressList()
        updateAddress()
    }

    private fun updateAddress() {
        if (mAddressAdapter.mModelList.isNotEmpty()) {
            var it = mAddressAdapter.mModelList[selected]
            binding.addressInclude.tvUserName.text = "${it.first_name} ${it.last_name}"
            binding.addressInclude.tvAddress.text = it.getAddress()
            binding.addressInclude.tvMobileNo.text = it.contact

        }
    }

    private fun loadAddressList() {
        val list = getAddressList()
        mAddressAdapter.clearData()
        val id = getSharedPrefInstance().getIntValue(Constants.SharedPref.KEY_ADDRESS, 0)
        list.forEachIndexed { index, address ->
            if (address.ID == id) {
                selected = index
            }
        }
        mAddressAdapter.addItems(list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ADDRESS && resultCode == Activity.RESULT_OK) {
            loadAddressList()
            dialog.show()
        }
    }

    private fun createOrder() {
        val requestModel = RequestModel()
        val mData = ArrayList<LinItemsRequest>()
        getCartListFromPref().forEach {
            val mlineitem = LinItemsRequest()
            mlineitem.product_id = it.pro_id.toInt()
            mlineitem.quantity = it.quantity.toInt()
            mlineitem.variation_id = it.pro_id.toInt()
            mData.add(mlineitem)
        }
        requestModel.line_items = mData
        if (mAddressAdapter.mModelList.isNotEmpty()){
            var it=mAddressAdapter.mModelList[selected];
            val mShippingRequest = BillingShippingRequest()
            mShippingRequest.email= getEmail()
            mShippingRequest.first_name = it.first_name
            mShippingRequest.last_name = it.last_name
            mShippingRequest.address_1 = it.address_1
            mShippingRequest.address_2 = it.address_2
            mShippingRequest.city = it.city
            mShippingRequest.state = it.state
            mShippingRequest.postcode = it.postcode
            mShippingRequest.country = it.country
            mShippingRequest.phone = it.contact
            requestModel.shipping = mShippingRequest
            requestModel.billing=mShippingRequest
        }

        requestModel.customer_id = getUserId().toInt()

        if (isAllowedToCreate) {
            getAlertDialog(getString(R.string.msg_want_to_order), onPositiveClick = { dialog, i ->
                dialog.dismiss()
                showProgress(true)
                callApi(getRestApis().createOrder(requestModel), onApiSuccess = {
                    val request = RequestModel(); request.order_id = it.id
                    callApi(getRestApis(false).getCheckoutUrl(request), onApiSuccess = { res ->
                        snackBar("Successfully Processed")
                        cartAdapter.clearData()
                        showProgress(false)
                        fetchAndStoreCartData()
                        launchActivityWithNewTask<DashBoardActivity>()
                        if (res.checkout_url.isNotEmpty()) {
                            openCustomTab(res.checkout_url)
                        }
                    }, onApiError = {
                        showProgress(false)
                        snackBarError(it)
                    }, onNetworkError = {
                        showProgress(false)
                        noInternetSnackBar()
                    })
                }, onApiError = {
                    showProgress(false)
                    snackBarError(it)
                }, onNetworkError = {
                    showProgress(false)
                    noInternetSnackBar()
                })
            }, onNegativeClick = { dialog, i ->
                dialog.dismiss()
            }).show()

        } else {
            toast(getString(R.string.msg_not_allowed))
        }
    }

    override fun onResume() {
        if (getSharedPrefInstance().getStringValue(CART_DATA) == "") {
            finish()
        }
        super.onResume()
    }
}