package com.examples.rentors.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.examples.rentors.domain.Ticket
import com.examples.rentors.utils.ISO_8601_FORMAT
import java.time.LocalDateTime
import java.time.ZoneId

class TicketsFirebaseRepository {
    private val databaseReference = Firebase.database.reference
    var noTickets = MutableLiveData(false)
    var noActiveTickets = ObservableBoolean(false)
    var noBookedTickets = ObservableBoolean(false)
    var noArchivedTickets = ObservableBoolean(false)
    var activeTickets = MutableLiveData<List<Ticket>>()
    var bookedTickets = MutableLiveData<List<Ticket>>()
    var archivedTickets = MutableLiveData<List<Ticket>>()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        databaseReference.child("users").child(Firebase.auth.uid.toString()).child("tickets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ticketsIds = snapshot.children.map { item -> item.getValue<String>()!! }
                    sortTickets(ticketsIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("User Tickets", error.toString())
                    noTickets.postValue(true)
                }

            })
    }

    private fun sortTickets(ticketsIds: List<String>) {
        databaseReference.child("tickets").addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val tickets = snapshot.children.map { item ->
                    item.getValue<Ticket>()!!.also {
                        it.arrivalDateTime = ISO_8601_FORMAT.parse(it.arrivalDateTimeString)!!
                        it.departureDateTime = ISO_8601_FORMAT.parse(it.departureDateTimeString)!!
                    }
                }.filter { ticket -> ticket.id in ticketsIds } as MutableList<Ticket>
                sortArchivedTickets(tickets)
                sortBookedTickets(tickets)
                sortActiveTickets(tickets)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Sorted Tickets", error.toString())
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortActiveTickets(tickets: MutableList<Ticket>) {
        activeTickets.postValue(tickets.filter { ticket ->
            LocalDateTime.ofInstant(
                ticket.departureDateTime.toInstant(), ZoneId.systemDefault()
            ) < LocalDateTime.now() && LocalDateTime.ofInstant(
                ticket.arrivalDateTime.toInstant(), ZoneId.systemDefault()
            ) > LocalDateTime.now()
        }.apply {
            noActiveTickets.set(this.isEmpty())
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortBookedTickets(tickets: MutableList<Ticket>) {
        bookedTickets.postValue(tickets.filter { ticket ->
            LocalDateTime.ofInstant(
                ticket.arrivalDateTime.toInstant(), ZoneId.systemDefault()
            ) > LocalDateTime.now() && LocalDateTime.ofInstant(
                ticket.departureDateTime.toInstant(), ZoneId.systemDefault()
            ) > LocalDateTime.now()
        }.apply {
            noBookedTickets.set(this.isEmpty())
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortArchivedTickets(tickets: MutableList<Ticket>) {
        archivedTickets.postValue(tickets.filter { ticket ->
            LocalDateTime.ofInstant(
                ticket.arrivalDateTime.toInstant(), ZoneId.systemDefault()
            ) < LocalDateTime.now()
        }.apply {
            noArchivedTickets.set(this.isEmpty())
        })
    }

    fun cancelOrder(ticket: Ticket) {
        updateRoute(ticket)
        updateUser(ticket)
        updateTickets(ticket)
    }

    private fun updateTickets(ticket: Ticket) {
        databaseReference.child("tickets").get().addOnSuccessListener { snapshot ->
            val ticketsArray =
                (snapshot.children.map { item -> item.getValue<Ticket>()!! } as MutableList<Ticket>).also {
                    it.remove(ticket)
                }
            databaseReference.child("tickets").setValue(ticketsArray)
        }
    }

    private fun updateUser(ticket: Ticket) {
        databaseReference.child("users").child(Firebase.auth.uid.toString()).child("tickets").get()
            .addOnSuccessListener { snapshot ->
                val tickets =
                    (snapshot.children.map { item -> item.getValue<String>()!! } as MutableList<String>).also {
                        it.remove(ticket.id)
                    }
                databaseReference.child("users").child(Firebase.auth.uid.toString())
                    .child("tickets").setValue(tickets)
            }
    }

    private fun updateRoute(ticket: Ticket) {
        databaseReference.child("routes").child(ticket.routeId).child("ticketsLeft").get()
            .addOnSuccessListener {
                val currentValue = it.getValue<Int>()!!
                databaseReference.child("routes").child(ticket.routeId).child("ticketsLeft")
                    .setValue(currentValue + ticket.seats)
            }
    }
}