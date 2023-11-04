package com.examples.rentors.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.examples.rentors.domain.Route
import com.examples.rentors.domain.Stop
import com.examples.rentors.domain.Ticket
import com.examples.rentors.utils.ISO_8601_FORMAT
import java.time.LocalDateTime
import java.time.ZoneId

class RoutesFirebaseRepository {

    private val databaseReference = Firebase.database.reference
    var stops = MutableLiveData<List<Stop>>()
    var routes = MutableLiveData<List<Route>>()
    var successfulOrder = MutableLiveData(false)
    var orderInProcess = MutableLiveData(false)
    var orderCompleted = MutableLiveData(false)

    init {
        refreshStops()
    }

    private fun refreshStops() {
        databaseReference.child("stops").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stopsArray = snapshot.children.map { item -> item.getValue<Stop>()!! }
                stops.postValue(stopsArray)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Stops", error.toString())
            }

        })
    }

    fun searchForRoutes(from: String, to: String) {
        databaseReference.child("routes").orderByChild("departureDateString")
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val routesArray = snapshot.children
                        .map { item -> item.getValue<Route>()!!
                            .also {
                                it.arrivalDateTime = ISO_8601_FORMAT.parse(it.arrivalDateTimeString)!!
                                it.departureDateTime = ISO_8601_FORMAT.parse(it.departureDateTimeString)!!
                            }
                        }
                        .filter { route -> route.from == from }.filter { route -> route.to == to }
                        .filter { route -> route.ticketsLeft.toString().toInt() > 0 }
                        .filter { route ->
                            LocalDateTime.ofInstant(
                                route.departureDateTime.toInstant(), ZoneId.systemDefault()
                            ) > LocalDateTime.now().plusMinutes(10)
                        }
                    routes.postValue(routesArray)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Routes", "refreshStops:onCancelled", error.toException())
                }

            })
    }

    fun makeOrder(route: Route, seats: Int) {
        orderInProcess.postValue(true)
        val ticketKey = databaseReference.child("tickets").push().key.toString()
        databaseReference.child("users").child(Firebase.auth.uid.toString()).child("tickets").get()
            .addOnSuccessListener {
                var tickets = it.getValue<List<String>>()
                Log.d("Log", tickets.toString())
                tickets = if (tickets == null){
                    mutableListOf()
                }else{
                    tickets as MutableList<String>
                }
                tickets.add(ticketKey)
                val ticket = Ticket(ticketKey, seats, route)
                val childUpdates = hashMapOf(
                    "/routes/${route.id}/ticketsLeft" to route.ticketsLeft - seats,
                    "/users/${Firebase.auth.uid}/tickets" to tickets,
                    "/tickets/$ticketKey" to ticket
                )
                updateDatabase(childUpdates)
            }
    }

    private fun updateDatabase(childUpdates: HashMap<String, Any>) {
        try {
            databaseReference.updateChildren(childUpdates)
            successfulOrder.postValue(true)
        } catch (e: Exception) {
            successfulOrder.postValue(false)
            Log.e("Order", e.toString())
        } finally {
            orderCompleted.postValue(true)
        }
    }

}