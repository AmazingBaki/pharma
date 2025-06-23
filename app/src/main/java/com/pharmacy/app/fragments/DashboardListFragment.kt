package com.pharmacy.app.fragments

import android.os.Bundle
import android.provider.SyncStateContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import com.pharmacy.app.R

import com.pharmacy.app.utils.extensions.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pharmacy.app.adapter.RecyclerViewAdapter
import com.pharmacy.app.utils.Constants
import com.pharmacy.app.databinding.DialogDashboardSelectionBinding


class DashboardListFragment : BottomSheetDialogFragment() {

    private var _binding: DialogDashboardSelectionBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        var  tag="DashboardList"
        fun newInstance(): DashboardListFragment = DashboardListFragment()
    }
     var list= arrayListOf<Int>(R.drawable.ic_dashboard1,R.drawable.ic_dashboard2);
    var position = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogDashboardSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDone.onClick {
            getSharedPrefInstance().setValue(Constants.SharedPref.KEY_DASHBOARD,position)
            dismiss()
        }
        position= getSharedPrefInstance().getIntValue(Constants.SharedPref.KEY_DASHBOARD,0)
        var dashboardAdapter = RecyclerViewAdapter(
            R.layout.item_dashboard,
            onBind = { view: View, _: Int, pos: Int ->
                view.findViewById<android.widget.ImageView>(R.id.ivDashboard).setImageResource(list[pos])
                view.findViewById<android.widget.CheckBox>(R.id.cbDashboard).isChecked = if (pos == position) {
                    view.findViewById<View>(R.id.viewOverlay).show()
                    true
                } else {
                    view.findViewById<View>(R.id.viewOverlay).hide()
                    false
                }
            })
        dashboardAdapter.onItemClick = { pos: Int, _: View, _: Int ->
            position = pos
            dashboardAdapter.notifyDataSetChanged()
        }
        binding.rvDashboard.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = dashboardAdapter
        }
        dashboardAdapter.addItems(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}