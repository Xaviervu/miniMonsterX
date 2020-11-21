package ru.vegax.xavier.miniMonsterX.repository


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.vegax.xavier.miniMonsterX.retrofit2.Converters

@Database(entities = [DeviceData::class], version = 2, exportSchema = false)
@TypeConverters( Converters::class)
abstract class DevicesDb : RoomDatabase() {
    abstract fun newsDao(): DevicesDao

    companion object {
        private lateinit var instance: DevicesDb
        private val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase){
                database.execSQL("ALTER TABLE device_table ADD COLUMN hiddenInputs TEXT NOT NULL DEFAULT '[false,true,true,true,true,true]'")
            }
        }
        internal fun get(context: Context): DevicesDb {
            if (!::instance.isInitialized) synchronized(DevicesDb::class.java) {
                if (!::instance.isInitialized) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                            DevicesDb::class.java, "devices_database"
                    ).addMigrations(MIGRATION_1_2)
                     .build()
                }
            }
            return instance
        }
    }
}
