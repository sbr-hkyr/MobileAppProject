package com.example.tdm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
  @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
  fun getAllHistory(): Flow<List<CalculationHistoryEntity>>

  @Query("SELECT * FROM calculation_history WHERE id = :id LIMIT 1")
  suspend fun getById(id: Long): CalculationHistoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: CalculationHistoryEntity): Long

  @Query("DELETE FROM calculation_history WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM calculation_history")
  suspend fun deleteAll()
}
