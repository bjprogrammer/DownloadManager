package com.bobby.downloadManager.database.converters

import androidx.room.TypeConverter
import com.bobby.downloadManager.enums.FileType
import com.bobby.downloadManager.utils.DatabaseUtils.defaultGson

class FileTypeConverter {
    @TypeConverter
    fun stringToFileType(data: String?): FileType? {
        if (data == null) {
            return null
        }
        val gson = defaultGson()
        return gson.fromJson(data, FileType::class.java)
    }

    @TypeConverter
    fun fileTypeToString(fileType: FileType?): String? {
        if (fileType == null) {
            return null
        }
        val gson = defaultGson()
        return gson.toJson(fileType)
    }
}
