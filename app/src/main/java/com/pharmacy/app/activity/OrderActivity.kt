package com.pharmacy.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pharmacy.app.base.BaseViewBindingActivity
import com.pharmacy.app.R
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ActivityOrderBinding
import com.pharmacy.app.databinding.ItemOrderlistBinding
import com.pharmacy.app.models.MyOrderData
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.utils.Constants.KeyIntent.DATA
import com.pharmacy.app.utils.Constants.KeyIntent.POSITION
import com.pharmacy.app.utils.Constants.OrderStatus.CANCELLED
import com.pharmacy.app.utils.Constants.OrderStatus.COMPLETED
import com.pharmacy.app.utils.Constants.OrderStatus.ONHOLD
import com.pharmacy.app.utils.Constants.OrderStatus.PENDING
import com.pharmacy.app.utils.Constants.OrderStatus.PROCESSING
import com.pharmacy.app.utils.Constants.OrderStatus.REFUNDED
import com.pharmacy.app.utils.Constants.RequestCode.ORDER_DETAIL
import com.pharmacy.app.utils.extensions.*
import kotlin.math.roundToInt

class OrderActivity : BaseViewBindingActivity<ActivityOrderBinding>() {

    private var countLoadMore = 1
    private lateinit var mOrderAdapter: BaseRecyclerAdapter<MyOrderData, ItemOrderlistBinding>

    var isLoading = false

    override fun getViewBinding(): ActivityOrderBinding {
        return ActivityOrderBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar(binding.toolbarInclude.toolbar)
        title = getString(R.string.title_my_orders)

        mOrderAdapter = object : BaseRecyclerAdapter<MyOrderData, ItemOrderlistBinding>() {
            override fun onItemLongClick(view: View, model: MyOrderData, position: Int) {

            }

            override fun onItemClick(view: View, model: MyOrderData, position: Int, dataBinding: ItemOrderlistBinding) {
                if (view.id == R.id.rlMainOrder) {
                    launchActivity<OrderDescriptionActivity>(requestCode = ORDER_DETAIL) {
                        putExtra(DATA, model)
                        putExtra(POSITION, position)
                    }
                } else if (view.id == R.id.btnCancelOrder) {
                    getAlertDialog("Do you want to cancel order?", onPositiveClick = { dialog, i ->
                        dialog.dismiss()

                        cancelMyOrder(model.id, position)
                    }, onNegativeClick = { dialog, i ->
                        dialog.dismiss()
                    }).show()

                }
            }

            override val layoutResId: Int = R.layout.item_orderlist

            override fun onBindData(model: MyOrderData, position: Int, dataBinding: ItemOrderlistBinding) {
                dataBinding.tvOriginalPrice.applyStrike()
                if (model.line_items.size > 0) {
                    dataBinding.tvProductName.text = model.line_items[0].name
                } else {
                    dataBinding.tvProductName.text = "No Products"
                }
                dataBinding.tvPrice.text = (model.total.toFloat() - model.discount_total.toFloat()).roundToInt().toString().currencyFormat(model.currency)
                if (model.discount_total == 0.0) {
                    dataBinding.tvOriginalPrice.hide()
                } else {
                    dataBinding.tvOriginalPrice.show()
                    dataBinding.tvOriginalPrice.text = model.total.toFloat().roundToInt().toString().currencyFormat(model.currency)
                }
                dataBinding.ivCircle.setCircleColor(color(R.color.track_yellow))
                dataBinding.btnCancelOrder.hide()
                when (model.status) {
                    PENDING -> {
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#ECC134>Pending</font>").getHtmlString()
                        dataBinding.tvTrack2.text = getString(R.string.lbl_order_pending).getHtmlString()
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_yellow))
                        dataBinding.btnCancelOrder.show()
                    }
                    PROCESSING -> {
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#64B931>Processing</font>").getHtmlString()
                        dataBinding.tvTrack2.text = getString(R.string.lbl_item_delivering).getHtmlString()
                        dataBinding.ivCircle.setCircleColor(color(R.color.track_green))
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_yellow))
                    }
                    ONHOLD -> {
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#ECC134>On Hold</font>").getHtmlString()
                        dataBinding.tvTrack2.text = "Order on hold".getHtmlString()
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_yellow))
                    }
                    COMPLETED -> {
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#64B931>Placed</font>").getHtmlString()
                        dataBinding.tvTrack2.text = "Order <font color=#64B931>Completed</font>".getHtmlString()
                        dataBinding.tvProductDeliveryDate.text = toDate(model.date_completed!!)
                        dataBinding.ivCircle.setCircleColor(color(R.color.track_green))
                        dataBinding.ivLine.setLineColor(color(R.color.track_green))
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_green))
                    }
                    CANCELLED -> {
                        dataBinding.ivCircle.setCircleColor(color(R.color.track_red))
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#F61929>Cancelled</font>").getHtmlString()
                        dataBinding.tvTrack2.text = "Order Cancelled".getHtmlString()
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_red))
                    }
                    REFUNDED -> {
                        dataBinding.ivCircle.setCircleColor(color(R.color.track_grey))
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#D3D3D3>Refunded</font>").getHtmlString()
                        dataBinding.tvTrack2.text = "Order Refunded".getHtmlString()
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_yellow))
                    }
                    else -> {
                        dataBinding.ivCircle.setCircleColor(color(R.color.track_red))
                        dataBinding.tvTrack1.text = (toDate(model.date_created) + "<br/>Order <font color=#F61929>Trashed</font>").getHtmlString()
                        dataBinding.tvTrack2.text = "Order Trashed"
                        dataBinding.ivCircle1.setCircleColor(color(R.color.track_red))
                    }
                }

                if (model.status == COMPLETED) {
                    dataBinding.llDeliveryDate.show()
                    dataBinding.llDeliveryInfo.show()
                    dataBinding.rlStatus.hide()
                    dataBinding.tvProductDeliveryDate.text = toDate(model.date_completed!!)
                } else {
                    dataBinding.llDeliveryDate.hide()
                    dataBinding.llDeliveryInfo.hide()
                    dataBinding.rlStatus.show()
                }
            }
        }
        binding.rvOrder.adapter = mOrderAdapter
        /* val list = Gson().fromJson<ArrayList<MyOrderData>>(getSharedPrefInstance().getStringValue(Constants.SharedPref.KEY_ORDERS), object : TypeToken<ArrayList<MyOrderData>>() {}.type)
         if (list != null) {
             mOrderAdapter.addItems(list)
         }*/
        loadOrder(countLoadMore)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvOrder.layoutManager = linearLayoutManager

        binding.rvOrder.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val countItem = linearLayoutManager.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                val isLastPosition = countItem.minus(1) == lastVisiblePosition

                if (!isLoading && isLastPosition) {
                    isLoading = true
                    countLoadMore = countLoadMore.plus(1)
                    loadOrder(countLoadMore)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ORDER_DETAIL && data != null) {
            val orderModel = data.getSerializableExtra(Constants.KeyIntent.DATA) as MyOrderData
            val position = data.getIntExtra(Constants.KeyIntent.POSITION, -1)
            if (position != -1) {
                mOrderAdapter.mModelList[position] = orderModel
                mOrderAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun cancelMyOrder(orderId: Int, index: Int) {
        binding.progressBar.show()
        cancelOrder(orderId, onApiSuccess = {
            if (it.status == "cancelled") {
                mOrderAdapter.mModelList[index] = it
                mOrderAdapter.notifyItemChanged(index)
            }
            binding.progressBar.hide()
        }, onError = {
            binding.progressBar.hide()

        })
    }

    private fun loadOrder(page: Int) {
        binding.progressBar.show()
        callApi(getRestApis().listAllOrders(getUserId().toInt(), page), onApiSuccess = {
            isLoading = it.size != 10; binding.progressBar.hide()
            mOrderAdapter.addMoreItems(it)
            if (mOrderAdapter.itemCount == 0) binding.noDataInclude.rlNoData.show() else binding.noDataInclude.rlNoData.hide()
        }, onApiError = {
            binding.progressBar.hide(); snackBarError(it)
        }, onNetworkError = {
            binding.progressBar.hide(); noInternetSnackBar()
        })
    }

}
