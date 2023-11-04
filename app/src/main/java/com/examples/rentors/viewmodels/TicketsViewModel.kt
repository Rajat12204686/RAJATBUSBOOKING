package com.examples.rentors.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.examples.rentors.domain.Ticket
import com.examples.rentors.repository.TicketsFirebaseRepository

class TicketsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TicketsFirebaseRepository()
    var activeTickets = repository.activeTickets
    var bookedTickets = repository.bookedTickets
    var archivedTickets = repository.archivedTickets
    var noActiveTickets = repository.noActiveTickets
    var noBookedTickets = repository.noBookedTickets
    var noArchivedTickets = repository.noArchivedTickets

    fun cancelOrder(ticket: Ticket) = repository.cancelOrder(ticket)
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TicketsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return TicketsViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}