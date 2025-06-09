package db.inmemorystore.course

import db.Timeline

class Category (
    val id: Int,
    val name: String
) : Timeline() {

    companion object {
        private var serial = 1
        private val records = mutableMapOf<Int, Category>()

        init {
            records[serial] = Category(serial++, "Software Development")
            records[serial] = Category(serial++, "Data Analyst")
            records[serial] = Category(serial++, "Testing")
            records[serial] = Category(serial++, "English Communication")
            records[serial] = Category(serial++, "Product Manager")
            records[serial] = Category(serial++, "Web Development")
            records[serial] = Category(serial++, "Mobile App Development")
            records[serial] = Category(serial++, "Drawing")
            records[serial] = Category(serial++, "Cooking")
            records[serial] = Category(serial++, "Others")
        }

        fun createCategory(name: String): Category {
            val category = Category(serial++, name)
            records[category.id] = category
            return category
        }

        fun getRecords(): Map<Int, Category> = records.toMap()
    }
}