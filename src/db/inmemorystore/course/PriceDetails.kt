package db.inmemorystore.course

import db.Timeline

class PriceDetails (
    val id: Int,
    val currencyCode: String,
    val currencySymbol: String,
    var amount: Double,
) : Timeline() {
    companion object {
        private val records = mutableMapOf<Int, PriceDetails>()
    }
}