package com.examples.rentors.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.examples.rentors.repository.ProfileFirebaseRepository

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ProfileFirebaseRepository()
    var ticketsQuantity = repository.ticketsQuantity
    var distance = repository.distance
    var discount = repository.discount
    var email = repository.email
    var contactPhone = repository.contactPhone
    var fullName = repository.fullName
    var userPhone = repository.userPhone
    fun logout() = repository.logout()

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return ProfileViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}