package com.examples.rentors.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.examples.rentors.R
import com.examples.rentors.databinding.CancelOrderBottomSheetDialogBinding
import com.examples.rentors.databinding.FragmentTicketsBinding
import com.examples.rentors.databinding.TicketRowBinding
import com.examples.rentors.domain.Ticket
import com.examples.rentors.viewmodels.TicketsViewModel


class TicketsFragment : Fragment() {

    private val viewModel: TicketsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, TicketsViewModel.Factory(activity.application)
        )[TicketsViewModel::class.java]
    }

    private lateinit var binding: FragmentTicketsBinding
    private var activeTicketsAdapter: TicketsAdapter = TicketsAdapter()
    private var archivedTicketsAdapter: TicketsAdapter = TicketsAdapter()
    private lateinit var bookedTicketsAdapter: TicketsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tickets,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.ticketsAppBarBackButton.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_ticketsFragment_to_mainFragment)
        }
        activeTicketsAdapter = TicketsAdapter()
        archivedTicketsAdapter = TicketsAdapter()
        bookedTicketsAdapter = TicketsAdapter(TicketClick { ticket ->
            val dialog = BottomSheetDialog(requireContext())
            val bottomSheetBinding: CancelOrderBottomSheetDialogBinding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.cancel_order_bottom_sheet_dialog,
                null,
                false
            )
            bottomSheetBinding.viewModel = viewModel
            bottomSheetBinding.lifecycleOwner = viewLifecycleOwner
            bottomSheetBinding.cancelOrderBack.setOnClickListener { dialog.dismiss() }
            bottomSheetBinding.cancelOrderComplete.setOnClickListener {
                viewModel.cancelOrder(ticket)
                dialog.dismiss()
            }
            dialog.setContentView(bottomSheetBinding.root)
            dialog.show()
        })
        binding.ticketsActiveRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activeTicketsAdapter
        }
        binding.ticketsBookedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookedTicketsAdapter
        }
        binding.ticketsArchivedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = archivedTicketsAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activeTickets.observe(viewLifecycleOwner) { tickets ->
            tickets.apply {
                activeTicketsAdapter.tickets = tickets
            }
        }
        viewModel.bookedTickets.observe(viewLifecycleOwner) { tickets ->
            tickets.apply {
                bookedTicketsAdapter.tickets = tickets
            }
        }
        viewModel.archivedTickets.observe(viewLifecycleOwner) { tickets ->
            tickets.apply {
                archivedTicketsAdapter.tickets = tickets
            }
        }
    }
}

class TicketsAdapter(private val callback: TicketClick? = null) :
    RecyclerView.Adapter<TicketViewHolder>() {
    var tickets: List<Ticket> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val withDataBinding: TicketRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), TicketViewHolder.LAYOUT, parent, false
        )
        return TicketViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.viewDataBinding.also { binding ->
            binding.ticket = tickets[position]
            if (callback != null) binding.ticketCallback = callback
        }
    }

    override fun getItemCount() = tickets.size
}

class TicketViewHolder(val viewDataBinding: TicketRowBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.ticket_row
    }
}

class TicketClick(val block: (Ticket) -> Unit) {
    fun onClick(ticket: Ticket) = block(ticket)
}