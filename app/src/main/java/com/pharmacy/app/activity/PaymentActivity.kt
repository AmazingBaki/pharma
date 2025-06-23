package com.pharmacy.app.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.databinding.ActivityPaymentBinding
import com.pharmacy.app.models.MyOrderData
import com.pharmacy.app.models.Payment
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.extensions.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : BaseViewBindingActivity<ActivityPaymentBinding>(), PaymentResultListener {
    private var orderData: MyOrderData? = null

    override fun getViewBinding(): ActivityPaymentBinding = ActivityPaymentBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.tb.toolbar)
        title = getString(R.string.title_payment)

        orderData = intent.getSerializableExtra(DATA) as MyOrderData

        Checkout.preload(this)

        callApi(getRestApis().paymentGateways(), onApiSuccess = {
            val paymentGatewaysAdapter = RecyclerViewAdapter<Payment>(R.layout.item_payment, onBind = { view, item, position ->
                view.findViewById<TextView>(R.id.tvPaymentGateway).text = item.method_title
            })
            binding.rvPaymentGateways.layoutManager = LinearLayoutManager(this@PaymentActivity)
            binding.rvPaymentGateways.adapter = paymentGatewaysAdapter
            paymentGatewaysAdapter.addItems(it)
            paymentGatewaysAdapter.onItemClick = { pos, view, item ->
                handlerPaymentClick(item)
            }
        }, onApiError = {
            toast(it)
        }, onNetworkError = {
            noInternetSnackBar()
        })

        addPaymentDetails()

        binding.tvPayWithPayPal.onClick { createPaymentRequest("paypal") }
        binding.tvNetBanking.onClick {}
        binding.tvCash.onClick { createPaymentRequest("cod") }
    }

    private fun handlerPaymentClick(item: Payment) {
        when (item.id) {
            "cod" -> {
                createPaymentRequest("cod")
            }
            "paypal" -> {
                createPaymentRequest("paypal")
            }
            "woobox_razorpay" -> {
                handleRazorPay()
            }
        }
    }

    private fun handleRazorPay() {
        val checkout = Checkout()
        checkout.setImage(R.drawable.ic_app_icon)

        try {
            val options = JSONObject()
            options.put("name", "Iqonic")
            options.put("description", "")
            options.put("currency", "INR")
            //options.put("order_id", orderData?.id)
            options.put("amount", (getCartTotalAmount()!! * 100).toDouble())
            options.put("image", "https://rzp-mobile.s3.amazonaws.com/images/rzp.png")

            Log.d(this.localClassName, options.toString())
            checkout.open(this, options)
        } catch (e: Exception) {
            Log.e(this.localClassName, "Error in starting Razorpay Checkout", e)
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Log.d(this.localClassName, code.toString())
        toast(response!!)
        Log.d(this.localClassName, response)
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        //Log.d(this.localClassName, razorpayPaymentID)

        val requestModel = RequestModel().apply {
            order_id = orderData?.id
            payment_method = "woobox_razorpay"
            txn_id = razorpayPaymentID
        }

        showProgress(true)
        callApi(getRestApis().processOtherPayment(requestModel), onApiSuccess = {
            callApi(getRestApis(false).clearCartItems(), onApiSuccess = {
                showProgress(false)
                fetchAndStoreCartData()
                launchActivityWithNewTask<DashBoardActivity>()
            })
        }, onApiError = {
            showProgress(false)
            toast(it)
        }, onNetworkError = {
            showProgress(false)
            noInternetSnackBar()
        })
    }

    private fun createPaymentRequest(s: String) {
        val requestModel = RequestModel()
        requestModel.order_id = orderData?.id
        requestModel.payment_method = s
        processPayment(requestModel)
    }

    private fun addPaymentDetails() {
        getCartTotal()
    }
}