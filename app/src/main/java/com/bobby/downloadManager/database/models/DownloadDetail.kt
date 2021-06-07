package com.bobby.downloadManager.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bobby.downloadManager.enums.DownloadStatus
import com.bobby.downloadManager.enums.FileType

@Entity
data class DownloadDetail(
    @PrimaryKey
    var downloadID:String,
    var fileType: FileType,
    var url: String,
    var createdTime: Int,
    var downloadStatus: DownloadStatus
) {
    var filePath: String = ""
}