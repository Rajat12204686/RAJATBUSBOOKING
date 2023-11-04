package com.examples.rentors.domain

import java.util.Date

data class User(
    val uid: String = "No data",
    val name: String = "No data",
    val lastname: String = "No data",
    val phone: String = "No data",
    val tickets: List<String> = emptyList()
)

data class Ticket(
    val id: String = "No data",
    val from: String = "No data",
    val to: String = "No data",
    val departureDateTimeString: String = "",
    val arrivalDateTimeString: String = "",
    val busPlate: String = "No data",
    val busColor: String = "No data",
    val seats: Int = 0,
    val price: Int = 0,
    val routeId: String = "No data",
    val travelTimeMinutes: Int = 0,
    val travelTimeHours: Int = 0,
    val distance: Int = 0
) {
    var departureDateTime: Date = Date()
    var arrivalDateTime: Date = Date()


    constructor(id: String, seats: Int, route: Route) : this(
        id = id,
        from = route.from,
        to = route.to,
        departureDateTimeString = route.departureDateTimeString,
        arrivalDateTimeString = route.arrivalDateTimeString,
        busPlate = route.busPlate,
        busColor = route.busColor,
        seats = seats,
        price = route.price * seats,
        routeId = route.id,
        travelTimeMinutes = route.travelTimeMinutes,
        travelTimeHours = route.travelTimeHours,
        distance = route.distance
    )
}

data class Route(
    val id: String = "No data",
    val from: String = "No data",
    val to: String = "No data",
    val departureDateTimeString: String = "",
    val arrivalDateTimeString: String = "",
    val busPlate: String = "No data",
    val busColor: String = "No data",
    val ticketsLeft: Int = 0,
    val price: Int = 0,
    val travelTimeMinutes: Int = 0,
    val travelTimeHours: Int = 0,
    val distance: Int = 0
) {
    var departureDateTime: Date = Date()
    var arrivalDateTime: Date = Date()


}

data class Stop(
    val name: String = "No data"
) {
    override fun toString() = name
}

data class ContactInfo(
    val phone: String = "+375123456789", val email: String = "some@thing.com"
)