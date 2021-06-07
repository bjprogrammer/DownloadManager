package com.bobby.downloadManager.database.dao

import androidx.room.*
import com.bobby.downloadManager.database.models.DownloadDetail
import com.bobby.downloadManager.enums.DownloadStatus

@Dao
abstract class DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDownloadRecord(downloadDetail: DownloadDetail):Long

    @Query("DELETE FROM DownloadDetail")
    abstract suspend fun deleteAll()

    @Update
    abstract suspend fun updateDownloadDetail(downloadDetail: DownloadDetail)

    @Query("SELECT * FROM DownloadDetail WHERE downloadStatus LIKE :downloadStatus")
    abstract suspend fun getPendingDownload(downloadStatus: DownloadStatus = DownloadStatus.Enqueued): DownloadDetail?

    @Query("SELECT * FROM DownloadDetail WHERE downloadId LIKE :downloadId")
    abstract suspend fun getDownLoadStatus(downloadId: String): DownloadDetail?
}