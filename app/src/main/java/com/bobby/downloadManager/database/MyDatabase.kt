package com.bobby.downloadManager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bobby.downloadManager.database.converters.DownloadStatusConverter
import com.bobby.downloadManager.database.converters.FileTypeConverter
import com.bobby.downloadManager.database.dao.DownloadDao
import com.bobby.downloadManager.database.models.DownloadDetail
@Database(entities = [DownloadDetail::class], version = 1)
@TypeConverters(DownloadStatusConverter::class, FileTypeConverter::class)
abstract class MyDatabase : RoomDatabase() {
    abstract val downloadDao: DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "myDatabase.db")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}