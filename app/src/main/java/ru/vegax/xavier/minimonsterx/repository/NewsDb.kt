package ru.vegax.xavier.newsfeed.repository


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.vegax.xavier.minimonsterx.repository.DevicesDao
import ru.vegax.xavier.minimonsterx.repository.DeviceData
import ru.vegax.xavier.minimonsterx.retrofit2.Converters

@Database(entities = [DeviceData::class], version = 1, exportSchema = false)
@TypeConverters( Converters::class)
abstract class NewsDb : RoomDatabase() {
    abstract fun newsDao(): DevicesDao

    companion object {
        private var instance: NewsDb? = null
        internal fun get(context: Context): NewsDb {
            if (instance == null) synchronized(NewsDb::class.java) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NewsDb::class.java, "devices_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance!!
        }
    }
}
