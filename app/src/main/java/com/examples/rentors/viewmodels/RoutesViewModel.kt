package com.examples.rentors.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.examples.rentors.domain.Route
import com.examples.rentors.repository.RoutesFirebaseRepository

class RoutesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RoutesFirebaseRepository()
    var stops = repository.stops
    var routes = repository.routes
    var successfulOrder = repository.successfulOrder
    var orderInProcess = repository.orderInProcess
    var orderCompleted = repository.orderCompleted

    fun searchForDate(from: String, to: String) =
        repository.searchForRoutes(from, to)

    fun makeOrder(route: Route, selectedQty: Int) = repository.makeOrder(route, selectedQty)

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return RoutesViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}