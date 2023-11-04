package com.examples.rentors.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.examples.rentors.R
import com.examples.rentors.databinding.FragmentMainBinding
import com.examples.rentors.databinding.MakeOrderBottomSheetDialogBinding
import com.examples.rentors.databinding.RouteRowBinding
import com.examples.rentors.domain.Route
import com.examples.rentors.viewmodels.RoutesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainFragment : Fragment() {

    private val viewModel: RoutesViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, RoutesViewModel.Factory(activity.application)
        )[RoutesViewModel::class.java]
    }

    private lateinit var binding: FragmentMainBinding
    private var routesViewModelAdapter: RouteAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_main, container, false
        )
        binding.mainSwapButton.setOnClickListener {
            val from = binding.mainFieldFrom.getText()
            val to = binding.mainFieldTo.getText()
            binding.mainFieldFrom.setText(to)
            binding.mainFieldTo.setText(from)
        }
        binding.mainFieldDate.setOnClickListener {
            val constraints =
                CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build()
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints).build()
            datePicker.addOnPositiveButtonClickListener {
                binding.mainFieldDate.setText(
                    SimpleDateFormat(
                        "dd.MM.yyyy", Locale.getDefault()
                    ).format(Date(it))
                )
                binding.mainFieldDate.clearError()
            }
            datePicker.show(childFragmentManager, "datePicker")
        }
        binding.mainSearchButton.setOnClickListener {
            if (binding.mainFieldFrom.getText() == "") {
                binding.mainFieldFrom.setError("Empty input")
                return@setOnClickListener
            }
            if (binding.mainFieldTo.getText() == "") {
                binding.mainFieldTo.setError("Empty input")
                return@setOnClickListener
            }
            if (binding.mainFieldDate.getText() == "") {
                binding.mainFieldDate.setError("Empty input")
                return@setOnClickListener
            }
            viewModel.searchForDate(
                binding.mainFieldFrom.getText(), binding.mainFieldTo.getText()
            )
        }
        routesViewModelAdapter = RouteAdapter(RouteClick { route ->
            val dialog = BottomSheetDialog(requireContext())
            val bottomSheetBinding: MakeOrderBottomSheetDialogBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.make_order_bottom_sheet_dialog, null, false
            )
            bottomSheetBinding.viewModel = viewModel
            bottomSheetBinding.lifecycleOwner = viewLifecycleOwner
            when (route.ticketsLeft) {
                1 -> {
                    bottomSheetBinding.radioButton2.isEnabled = false
                    bottomSheetBinding.radioButton3.isEnabled = false
                }

                2 -> {
                    bottomSheetBinding.radioButton3.isEnabled = false
                }
            }
            bottomSheetBinding.orderCancel.setOnClickListener { dialog.dismiss() }
            bottomSheetBinding.orderComplete.setOnClickListener {
                val buttonId = bottomSheetBinding.orderRadioGroup.checkedRadioButtonId
                val buttonValue =
                    bottomSheetBinding.root.findViewById<RadioButton>(buttonId).text.toString()
                        .toInt()
                viewModel.makeOrder(route, buttonValue)
                dialog.setCancelable(true)
                viewModel.viewModelScope.launch {
                    delay(1500)
                    dialog.dismiss()
                    viewModel.orderCompleted.postValue(false)
                    viewModel.orderInProcess.postValue(false)
                    viewModel.successfulOrder.postValue(false)
                }
            }
            dialog.setCancelable(false)
            dialog.setContentView(bottomSheetBinding.root)
            dialog.show()
        })
        binding.mainAppBarTicketsButton.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_mainFragment_to_ticketsFragment)
        }
        binding.mainAppBarProfileButton.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_mainFragment_to_profileFragment)
        }
        binding.routesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routesViewModelAdapter
        }
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.stops.observe(viewLifecycleOwner) { stops ->
            stops.apply {
                val adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, stops
                )
                binding.mainFieldFrom.setAdapter(adapter)
                binding.mainFieldTo.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            routes.apply {
                routesViewModelAdapter?.routes = routes
            }
        }
    }

}

class RouteAdapter(private val callback: RouteClick) : RecyclerView.Adapter<RouteViewHolder>() {
    var routes: List<Route> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val withDataBinding: RouteRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), RouteViewHolder.LAYOUT, parent, false
        )
        return RouteViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.viewDataBinding.also { binding ->
            binding.route = routes[position]
            binding.routeCallback = callback
        }
    }

    override fun getItemCount() = routes.size
}

class RouteViewHolder(val viewDataBinding: RouteRowBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.route_row
    }
}

class RouteClick(val block: (Route) -> Unit) {
    fun onClick(route: Route) = block(route)
}