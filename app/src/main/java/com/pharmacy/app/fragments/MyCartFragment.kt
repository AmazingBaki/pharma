package com.pharmacy.app.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pharmacy.app.AppBaseActivity
import com.pharmacy.app.R
import com.pharmacy.app.activity.*
import com.pharmacy.app.base.BaseRecyclerAdapter
import com.pharmacy.app.databinding.ItemCartBinding
import com.pharmacy.app.models.CartResponse
import com.pharmacy.app.models.RequestModel
import com.pharmacy.app.utils.Constants.KeyIntent.PRODUCT_ID
import com.pharmacy.app.utils.extensions.*
import com.pharmacy.app.databinding.FragmentCartBinding

class MyCartFragment : BaseViewBindingFragment<FragmentCartBinding>() {

    private var mCartAdapter: BaseRecyclerAdapter<CartResponse, ItemCartBinding> =
        object : BaseRecyclerAdapter<CartResponse, ItemCartBinding>() {
            override val layoutResId: Int = R.layout.item_cart

            override fun onBindData(
                model: CartResponse,
                position: Int,
                dataBinding: ItemCartBinding
            ) {
                dataBinding.llButton.show()
                dataBinding.llMoveTocart.hide()
                dataBinding.tvOriginalPrice.applyStrike()
                dataBinding.edtQty.setText(model.quantity)
                if (model.full != null) dataBinding.ivProduct.loadImageFromUrl(model.full)
                if (model.sale_price.isNotEmpty()) {
                    dataBinding.tvPrice.text =
                        (model.sale_price.toInt() * model.quantity.toInt()).toString()
                            .currencyFormat()
                } else if (model.price.isNotEmpty()) {
                    dataBinding.tvPrice.text =
                        (model.price.toFloat().toInt() * model.quantity.toInt()).toString()
                            .currencyFormat()
                }
                if (model.regular_price.isNotEmpty()){
                    dataBinding.tvOriginalPrice.text = (model.regular_price.toFloat().toInt()*model.quantity.toInt()).toString().currencyFormat()
                }else{
                    dataBinding.tvOriginalPrice.text =""
                }
            }

            override fun onItemClick(
                view: View,
                model: CartResponse,
                position: Int,
                dataBinding: ItemCartBinding
            ) {
                when (view.id) {
                    R.id.llRemove -> {
                        val requestModel = RequestModel()
                        requestModel.pro_id = model.pro_id

                        removeCartItem(requestModel)
                    }
                    R.id.ivIncreaseQuantity -> {
                        val qty = model.quantity.toInt()
                        if (model.stock_quantity != null) {
                            if (qty < model.stock_quantity) {
                                mModelList[position].quantity = qty.plus(1).toString()
                                notifyItemChanged(position)
                                updateCartItem(mModelList[position])
                            } else {
                                activity?.snackBarError("${getString(R.string.lbl_qty_error)} ${model.stock_quantity}")
                            }
                        } else {
                            if (qty < 10) {
                                mModelList[position].quantity = qty.plus(1).toString()
                                notifyItemChanged(position)
                                updateCartItem(mModelList[position])
                            }else{
                                activity?.snackBarError("${getString(R.string.lbl_qty_error)} 10")
                            }
                        }
                    }
                    R.id.ivDecreaseQuantity -> {
                        val qty = model.quantity.toInt()
                        if (qty >1) {
                            mModelList[position].quantity = qty.minus(1).toString()
                            notifyItemChanged(position)
                            updateCartItem(mModelList[position])
                        }
                    }
                    else -> {
                        activity?.launchActivity<ProductDetailActivityNew> {
                            putExtra(
                                PRODUCT_ID,
                                model.pro_id.toInt()
                            )
                        }
                    }
                }
            }

            override fun onItemLongClick(view: View, model: CartResponse, position: Int) {}
        }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCartBinding {
        return FragmentCartBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.rvCart.setVerticalLayout()
        binding.rvCart.adapter = mCartAdapter
        binding.tvClear.onClick {
            clearCartItems()
        }
        binding.btnShopNow.onClick {
            if (requireActivity() is DashBoardActivity) {
                (activity as DashBoardActivity).loadHomeFragment()
            } else if (requireActivity() is MyCartActivity) {
                (activity as MyCartActivity).shopNow()
            }
        }
        binding.tvContinue.onClick { activity?.launchActivity<OrderSummaryActivity> { } }
        binding.llSeePriceDetail.onClick { scrollToPriceDetail() }
    }

    private fun clearCartItems() {
        showProgress()
        callApi(getRestApis(false).clearCartItems(), onApiSuccess = {
            activity?.fetchAndStoreCartData()
        })
    }

    private fun scrollToPriceDetail() {
        Handler().post { binding.nsvCart.scrollTo(0, binding.llPayment.root.top) }
    }

    private fun invalidatePaymentLayout(b: Boolean) {
        if (!b) {
            if (activity != null) {
                binding.llNoItems.show()
                binding.llPayment.root.hide()
                binding.root.findViewById<View>(R.id.lay_button)?.hide()
                binding.rvCart.hide()
                binding.tvTotalItem.hide()
                binding.tvClear.hide()
            }
        } else {
            if (activity != null) {
                binding.llNoItems.hide()
                binding.llPayment.root.show()
                binding.root.findViewById<View>(R.id.lay_button)?.show()
                binding.rvCart.show()
                binding.tvTotalItem.show()
                binding.tvClear.show()
            }
        }

    }

    private fun removeCartItem(model: RequestModel) {
        activity?.getAlertDialog(
            getString(R.string.msg_confirmation),
            onPositiveClick = { dialog, i ->
                showProgress()
                callApi(getRestApis(false).removeCartItem(request = model), onApiSuccess = {
                    hideProgress()
                    snackBar(requireActivity().getString(R.string.success))
                    activity?.fetchAndStoreCartData()
                }, onApiError = {
                    hideProgress()
                    if (activity != null && activity is DashBoardActivity) {
                        if ((activity as DashBoardActivity).selectedFragment is MyCartFragment) {
                            activity?.snackBarError(it)
                        }
                    }
                }, onNetworkError = {
                    hideProgress()
                    activity?.noInternetSnackBar()
                })
            },
            onNegativeClick = { dialog, i ->
                dialog.dismiss()
            })?.show()
    }

    private fun updateCartItem(model: CartResponse) {
        showProgress()
        val requestModel = RequestModel(); requestModel.pro_id = model.pro_id; requestModel.quantity = model.quantity.toInt(); requestModel.cart_id = model.cart_id.toInt()
        callApi(getRestApis(false).updateItemInCart(request = requestModel), onApiSuccess = {
            snackBar(getString(R.string.lbl_success))
            activity?.fetchAndStoreCartData()
            hideProgress()
        }, onApiError = {
            hideProgress()
            activity?.snackBarError(it)
        }, onNetworkError = {
            hideProgress()
            activity?.noInternetSnackBar()
        })
    }
    fun invalidateCartLayout(it: ArrayList<CartResponse>) {
        hideProgress()
        if (it.size == 0) {
            invalidatePaymentLayout(false)
        } else {
            if (activity != null) {
                binding.llNoItems.hide()
                if (it.size == 1) {
                    binding.tvTotalItem.text = getString(R.string.lbl_total_items) + "${it.size})"
                } else {
                    binding.tvTotalItem.text = getString(R.string.lbl_total_items) + "${it.size})"
                }
                if (it.size > 5) binding.txtSeePriceDetails.text =
                    getString(R.string.lbl_see_price_detail) else binding.txtSeePriceDetails.text =
                    getString(R.string.lbl_total_amount)
                val total = (activity as AppBaseActivity).getCartTotal()
                binding.tvTotalCartAmount.text = total.toString().currencyFormat()
                binding.llPayment.tvShippingCharge.text = getString(R.string.lbl_free)
                binding.llPayment.tvTotalAmount.text = total.toString().currencyFormat()
                invalidatePaymentLayout(true)
                mCartAdapter.addItems(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateCartLayout(getCartListFromPref())
    }
}
