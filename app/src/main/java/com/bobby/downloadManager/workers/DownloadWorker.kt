package com.bobby.downloadManager.workers

import android.content.Context

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bobby.downloadManager.utils.FileUtils
import com.bobby.downloadManager.utils.NotificationUtils.sendStatusNotification
import com.bobby.downloadManager.database.MyDatabase
import com.bobby.downloadManager.database.models.DownloadDetail
import com.bobby.downloadManager.enums.DownloadStatus

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.sql.Timestamp
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class DownloadWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val myDatabase = MyDatabase.getInstance(applicationContext)
    var count = 0

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            performWork()
            Result.success()
        } catch (error: Throwable) {
            if(count>3){
                Result.failure()
            }
            else{
                count++ ;
                Result.retry();
            }
        }
    }

    private suspend fun performWork() {
        val downloadDetail = getActiveDownload()
        if (downloadDetail != null && downloadDetail.downloadStatus != DownloadStatus.Completed) {
            showFeedback(message = "download started")
            downloadFile(downloadDetail)
            performWork()
        } else {
            return
        }
    }

    private suspend fun downloadFile(downloadDetail: DownloadDetail) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(downloadDetail.url)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = client.newCall(request).execute()

            if (response.body != null) {
                if (downloadDetail.filePath.isNotEmpty()) {
                    val file = File(downloadDetail.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                val file = FileUtils.createFile(
                    applicationContext,
                    fileType = downloadDetail.fileType,
                    System.currentTimeMillis().toString()
                )
                if (file != null) {
                    downloadDetail.filePath = file.absolutePath
                    myDatabase.downloadDao.updateDownloadDetail(downloadDetail = downloadDetail)
                    val inputStream = response.body?.byteStream()
                    inputStream?.let {
                        file.copyInputStreamToFile(inputStream)
                        inputStream.close()
                        downloadDetail.downloadStatus = DownloadStatus.Completed
                        myDatabase.downloadDao.updateDownloadDetail(downloadDetail = downloadDetail)
                        val message = " download success" + downloadDetail.filePath
                        showFeedback(message = message)
                    }
                } else {
                    updateDownloadFailed(downloadDetail = downloadDetail)
                }
            } else {
                updateDownloadFailed(downloadDetail = downloadDetail)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            updateDownloadFailed(downloadDetail = downloadDetail)
        } catch (e: Exception) {
            e.printStackTrace()
            updateDownloadFailed(downloadDetail = downloadDetail)
        }
    }

    private fun updateDownloadFailed(downloadDetail: DownloadDetail) {
        val message =  "download failed"
        showFeedback(message)
    }

    private fun showFeedback(message: String) {
        sendStatusNotification(message = message, context = applicationContext,  Timestamp(Date().time).nanos)
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }

    private suspend fun getActiveDownload(): DownloadDetail? {
        return myDatabase.downloadDao.getPendingDownload()
    }
}