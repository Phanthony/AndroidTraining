package com.example.androidtraining

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single


@Dao
interface DayEntryDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDay(day: DayEntry): Completable

    @Query("SELECT day FROM Day_Table WHERE id = 1 LIMIT 1")
    fun getDay(): Maybe<String>

    @Query("SELECT COUNT(*) FROM DAY_TABLE")
    fun getDayCount(): Single<Int>

}

@Entity(tableName = "Day_Table")
data class DayEntry(private var day: String, @PrimaryKey private var id: Int){

    fun getDay() = day
    fun getId() = id
}