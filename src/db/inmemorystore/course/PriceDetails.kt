package db.inmemorystore.course

import core.course.schemas.NewPriceData
import db.Timeline

class PriceDetails (
    private val id: Int,
    private val currencyCode: String,
    private val currencySymbol: String,
    private var amount: Double,
) : Timeline() {
    fun getId() = id

    fun getCurrencyCode() = currencyCode

    fun getCurrencySymbol() = currencySymbol

    fun getAmount() = amount

    override fun toString(): String {
        return "$currencySymbol$amount ($currencyCode)"
    }

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

        fun getRecords() = records.toMap()
    }
}