package com.example.tdm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalculationHistoryEntity::class], version = 2, exportSchema = false)
abstract class TdmDatabase : RoomDatabase() {
  abstract fun calculationHistoryDao(): CalculationHistoryDao

  companion object {
    @Volatile
    private var INSTANCE: TdmDatabase? = null

    fun getDatabase(context: Context): TdmDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          TdmDatabase::class.java,
          "tdm_insight_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
