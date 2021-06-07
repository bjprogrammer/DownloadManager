package com.bobby.downloadManager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.work.*
import com.bobby.downloadManager.workers.DownloadWorker

import com.bobby.downloadManager.database.MyDatabase
import com.bobby.downloadManager.database.models.DownloadDetail
import com.bobby.downloadManager.databinding.ActivityMainBinding
import com.bobby.downloadManager.enums.DownloadStatus
import com.bobby.downloadManager.enums.FileType
import com.bobby.downloadManager.utils.Constants
import com.bobby.downloadManager.utils.HelperExtensions.toastShort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class DownloadManager(
    private var fileType: FileType,
    private var url: String,
    private val context: Context,
    private val mBinding: ActivityMainBinding
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var database = MyDatabase.getInstance(context)
    private val workManager = WorkManager.getInstance(context)
    private var uuid=""

    fun enqueueDownload() {
        uuid = startWorker().toString()
        val downloadDetail = DownloadDetail(
            downloadID = uuid,
            fileType = fileType,
            url = url,
            createdTime = Timestamp(Date().time).nanos,
            downloadStatus = DownloadStatus.Enqueued
        )
        addDownloadRequestToDatabase(downloadDetail = downloadDetail)
    }

    private fun startWorker(): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val downloadRequest = OneTimeWorkRequest
            .Builder(DownloadWorker::class.java)
            .setBackoffCriteria(BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            Constants.workerName,
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )
        return downloadRequest.id
    }

    fun getDownloadStatus(id:String) {
        coroutineScope.launch {
            val downloadDetail = database.downloadDao.getDownLoadStatus(id)

            Handler(Looper.getMainLooper()).post {
                if (downloadDetail != null) {
                    context.toastShort(downloadDetail.downloadStatus.toString())

                    if(downloadDetail.downloadStatus == DownloadStatus.Enqueued){
                        mBinding.tvCancel.visibility = View.VISIBLE
                    }else{
                        mBinding.tvCancel.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun addDownloadRequestToDatabase(downloadDetail: DownloadDetail){
        coroutineScope.launch {
            database.downloadDao.insertDownloadRecord(downloadDetail)
            Handler(Looper.getMainLooper()).post {
                context.toastShort(uuid)
                mBinding.etId.setText(uuid)
            }
        }
    }
}
