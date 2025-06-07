package db.inmemorystore.course

import db.Timeline

class Category (
    private val id: Int,
    private val name: String
) : Timeline() {
    fun getId() = id

    fun getName() = name

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