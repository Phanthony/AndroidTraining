package com.example.androidtraining

import androidx.room.*


@Dao
interface DayEntryDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntry)

    @Query("SELECT day FROM Day_Table WHERE id = 1 LIMIT 1")
    suspend fun getDay(): String

    @Query("SELECT COUNT(*) FROM DAY_TABLE")
    suspend fun getDayCount(): Int

}

@Entity(tableName = "Day_Table")
data class DayEntry(private var day: String, @PrimaryKey private var id: Int){

    fun getDay() = day
    fun getId() = id
}