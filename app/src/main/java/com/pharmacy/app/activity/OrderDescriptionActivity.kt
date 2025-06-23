package com.pharmacy.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ActivityOrderdescriptionBinding
import com.pharmacy.app.databinding.ItemOrderBinding
import com.pharmacy.app.models.MyOrderData
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.KeyIntent.POSITION
import com.pharmacy.app.utils.Constants.OrderStatus.CANCELLED
import com.pharmacy.app.utils.Constants.OrderStatus.COMPLETED
import com.pharmacy.app.utils.Constants.OrderStatus.ONHOLD
import com.pharmacy.app.utils.Constants.OrderStatus.PENDING
import com.pharmacy.app.utils.Constants.OrderStatus.PROCESSING
import com.pharmacy.app.utils.Constants.OrderStatus.REFUNDED
import com.pharmacy.app.utils.extensions.*
import kotlin.math.roundToInt

class OrderDescriptionActivity : BaseViewBindingActivity<ActivityOrderdescriptionBinding>() {

    private lateinit var mOrderItemAdapter: BaseRecyclerAdapter<MyOrderData.LineItem, ItemOrderBinding>
    lateinit var orderModel: MyOrderData;
    var position = -1

    override fun getViewBinding(): ActivityOrderdescriptionBinding {
        return ActivityOrderdescriptionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.title_my_orders)

        orderModel = intent.getSerializableExtra(DATA) as MyOrderData
        position = intent.getIntExtra(Constants.KeyIntent.POSITION, -1)
        mOrderItemAdapter = object : BaseRecyclerAdapter<MyOrderData.LineItem, ItemOrderBinding>() {
            override val layoutResId: Int = R.layout.item_order

            override fun onBindData(model: MyOrderData.LineItem, position: Int, dataBinding: ItemOrderBinding) {
                dataBinding.tvPrice.text = model.total.roundToInt().toString().currencyFormat(orderModel.currency)
                dataBinding.tvOriginalPrice.text = model.price.toString().currencyFormat(orderModel.currency)
                dataBinding.tvTotalItem.text = String.format(getString(R.string.lbl_total_item_count) + model.quantity)
                dataBinding.tvOriginalPrice.applyStrike()
            }

            override fun onItemClick(view: View, model: MyOrderData.LineItem, position: Int, dataBinding: ItemOrderBinding) {}

            override fun onItemLongClick(view: View, model: MyOrderData.LineItem, position: Int) {}

        }
        binding.rvOrderItems.setVerticalLayout()
        binding.rvOrderItems.adapter = mOrderItemAdapter
        bindOrderData(orderModel)

        binding.llTrack.onClick {
            launchActivity<TrackItemActivity> {
                putExtra(DATA, orderModel)
            }
        }
        binding.btnCancelOrder.onClick {
            getAlertDialog("Do you want to cancel order?", onPositiveClick = { dialog, i ->
                dialog.dismiss()

                cancelMyOrder()
            }, onNegativeClick = { dialog, i ->
                dialog.dismiss()
            }).show()
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.txtRatings.text = rating.toString()
        }
        binding.paymentDetailInclude.tvOffer.text = getString(R.string.text_offer_not_available)
    }

    fun toolbar(mToolbar: Toolbar) {
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace)
        mToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        mToolbar.changeToolbarFont()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun bindOrderData(orderModel: MyOrderData) {
        mOrderItemAdapter.addItems(orderModel.line_items)
        val track1: String
        val track2: String
        binding.ivCircle.setCircleColor(color(R.color.track_yellow))
        binding.btnCancelOrder.hide()

        when (orderModel.status) {
            PENDING -> {
                track1 = "Order <font color=#ECC134>Pending</font>"
                track2 = getString(R.string.lbl_order_pending)
                binding.btnCancelOrder.show()
            }
            PROCESSING -> {
                track1 = "Order <font color=#64B931>Processing</font>"
                track2 = getString(R.string.lbl_item_delivering)
                binding.ivCircle.setCircleColor(color(R.color.track_green))
            }
            ONHOLD -> {
                track1 = "Order <font color=#ECC134>On Hold</font>"
                track2 = "Order on hold"
            }
            COMPLETED -> {
                track1 = "Order <font color=#64B931>Placed</font>"
                track2 = "Order <font color=#64B931>Completed</font>"
                binding.tvDeliveryDate.text = toDate(orderModel.date_completed!!)
                binding.tvTrack2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_right_black, 0)
                binding.tvDeliveryDate.show()
                binding.tvDelivered.show()
                binding.ivCircle.setCircleColor(color(R.color.track_green))
                binding.ivLine.setLineColor(color(R.color.track_green))
                binding.ivCircle1.setCircleColor(color(R.color.track_green))
            }
            CANCELLED -> {
                binding.ivCircle.setCircleColor(color(R.color.track_red))
                track1 = "Order <font color=#F61929>Cancelled</font>"
                track2 = "Order Cancelled"
            }
            REFUNDED -> {
                binding.ivCircle.setCircleColor(color(R.color.track_grey))
                track1 = "Order <font color=#D3D3D3>Refunded</font>"
                track2 = "Order Refunded"
            }
            else -> {
                binding.ivCircle.setCircleColor(color(R.color.track_red))
                track1 = "Order <font color=#F61929>Trashed</font>"
                track2 = "Order Trashed"
            }
        }

        binding.tvDate.text = toDate(orderModel.date_created)
        binding.tvTrack1.text = track1.getHtmlString()
        binding.tvTrack2.text = track2.getHtmlString()
        binding.tvOrderId.text = orderModel.number.toString()
        title = orderModel.number.toString()
        binding.tvOrderDate.text = toDate(orderModel.date_created)
        if (orderModel.payment_method_title.isNotEmpty()) {
            binding.paymentDetailInclude.llPaymentMethod.show()
            binding.paymentDetailInclude.tvPaymentMethod.text = orderModel.payment_method_title
        }
        if (orderModel.shipping_total == 0.0) {
            binding.paymentDetailInclude.tvShippingCharge.text = getString(R.string.lbl_free)
        } else {
            binding.paymentDetailInclude.tvShippingCharge.text = orderModel.shipping_total.roundToInt().toString().currencyFormat(orderModel.currency)
        }
        binding.paymentDetailInclude.tvTotalAmount.text = ((orderModel.total - orderModel.discount_total) + orderModel.shipping_total).toString().currencyFormat(orderModel.currency)
    }

    private fun cancelMyOrder() {
        binding.progressBar.show()
        cancelOrder(orderModel.id, onApiSuccess = {
            if (it.status == "cancelled") {
                orderModel = it;
                bindOrderData(orderModel)

                val modified = Intent()
                modified.putExtra(DATA, orderModel)
                modified.putExtra(POSITION, position)
                setResult(Activity.RESULT_OK, modified)
            }
            binding.progressBar.hide()
        }, onError = {
            binding.progressBar.hide()
        })

    }

}
