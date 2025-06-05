package db.inmemorystore.course

import core.course.schemas.NewPriceData
import db.Timeline

class PriceDetails (
    val id: Int,
    val currencyCode: String,
    val currencySymbol: String,
    var amount: Double,
) : Timeline() {
    companion object {
        private var serialId = 1
        private val records = mutableMapOf<Int, PriceDetails>()

        fun createPriceDetails(newPriceData: NewPriceData): PriceDetails {
            val priceDetails = PriceDetails(
                id = serialId++,
                currencyCode = newPriceData.currencyCode,
                currencySymbol = newPriceData.currencySymbol,
                amount = newPriceData.amount
            )

            records[priceDetails.id] = priceDetails
            println("New Price Details created.")
            return priceDetails
        }
    }
}