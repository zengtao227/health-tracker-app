package com.taotao.healthtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taotao.healthtracker.data.dao.HealthDao
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import com.taotao.healthtracker.data.entity.AlmanacData

@Database(
    entities = [HealthRecord::class, UserProfile::class, AlmanacData::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to health_records
                db.execSQL("ALTER TABLE health_records ADD COLUMN bloodGlucose REAL")
                db.execSQL("ALTER TABLE health_records ADD COLUMN uricAcid REAL")
                
                // Add new column to user_profiles with default value
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN enabledModules TEXT NOT NULL DEFAULT 'bp,weight,hr'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker_database_v2"
                )
                .addMigrations(MIGRATION_8_9)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
