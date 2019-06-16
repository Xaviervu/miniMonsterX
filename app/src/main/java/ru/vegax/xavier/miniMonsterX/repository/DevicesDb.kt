package ru.vegax.xavier.miniMonsterX.repository


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.vegax.xavier.miniMonsterX.retrofit2.Converters

@Database(entities = [DeviceData::class], version = 1, exportSchema = false)
@TypeConverters( Converters::class)
abstract class DevicesDb : RoomDatabase() {
    abstract fun newsDao(): DevicesDao

    companion object {
        private var instance: DevicesDb? = null
        internal fun get(context: Context): DevicesDb {
            if (instance == null) synchronized(DevicesDb::class.java) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                            DevicesDb::class.java, "devices_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance!!
        }
    }
}
