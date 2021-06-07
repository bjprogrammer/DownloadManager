package com.bobby.downloadManager.database.converters

import androidx.room.TypeConverter
import com.bobby.downloadManager.enums.DownloadStatus
import com.bobby.downloadManager.utils.DatabaseUtils.defaultGson

class DownloadStatusConverter {
    @TypeConverter
    fun stringToDownloadStatus(data: String?): DownloadStatus? {
        if (data == null) {
            return null
        }
        val gson = defaultGson()
        return gson.fromJson(data, DownloadStatus::class.java)
    }

    @TypeConverter
    fun downloadStatusToString(downloadStatus: DownloadStatus?): String? {
        if (downloadStatus == null) {
            return null
        }
        val gson = defaultGson()
        return gson.toJson(downloadStatus)
    }
}
