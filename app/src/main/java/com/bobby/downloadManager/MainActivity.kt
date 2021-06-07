package com.bobby.downloadManager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bobby.downloadManager.database.MyDatabase
import com.bobby.downloadManager.databinding.ActivityMainBinding
import com.bobby.downloadManager.enums.FileType
import com.bobby.downloadManager.utils.Constants.PERMISSIONS_REQUEST_CODE
import com.bobby.downloadManager.utils.HelperExtensions.toastShort

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var myDatabase: MyDatabase
    private lateinit var workManager:WorkManager
    private lateinit var coroutineScope:CoroutineScope
    private lateinit var downloadManager:DownloadManager

    private val mFileTypes = arrayOf(
        FileType.Pdf.name,
        FileType.Mp4.name,
        FileType.Mp3.name,
        FileType.Docx.name,
        FileType.Png.name,
        FileType.Jpg.name
    )

    private var mSelectedFileType: FileType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (!hasPermissions()) {
            requestPermissions()
        }

        myDatabase = MyDatabase.getInstance(applicationContext)
        workManager = WorkManager.getInstance(applicationContext)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        val arrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, mFileTypes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerFileType.adapter = arrayAdapter

        mBinding.spinnerFileType.onItemSelectedListener = this

        mBinding.tvDownload.setOnClickListener {
            enqueueCustomDownload()
        }

        mBinding.tvStatus.setOnClickListener{
            if (mBinding.etId.text.isEmpty()) {
                toastShort(message = "Please enter a valid id")
            }else{
                downloadManager.getDownloadStatus(mBinding.etId.text.toString())
            }
        }
        var future: LiveData<WorkInfo>;
        mBinding.tvCancel.setOnClickListener{
            if (mBinding.etId.text.isEmpty()) {
                toastShort(message = "Please enter a valid id")
            }else{
                mBinding.tvCancel.visibility=View.GONE
                future = WorkManager.getInstance(this).getWorkInfoByIdLiveData(UUID.fromString(mBinding.etId.text.toString()))

                future.observe(this, {
                    toastShort(it.state.toString())
                    future.removeObservers(this)
                })

                workManager.cancelWorkById(UUID.fromString(mBinding.etId.text.toString()))
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mSelectedFileType = getFileType(fileType = mFileTypes[position])
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        mSelectedFileType = null
    }

    private fun enqueueCustomDownload() {
        if (hasPermissions()) {
            if (mBinding.etUrl.text.isEmpty()) {
                toastShort(message = "Please enter a valid Url")
                return
            }


            if (mSelectedFileType == null) {
                toastShort(message = "Please select a valid file type")
                return
            }

            val url = mBinding.etUrl.text.toString()

            mSelectedFileType?.let { fileType ->
                downloadManager = DownloadManager(
                    url = url,
                    fileType = fileType,
                    context = applicationContext,
                    mBinding =  mBinding
                )
                downloadManager.enqueueDownload()
                toastShort(message = "Download Enqueued")
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            200
        )
    }

    private fun hasPermissions() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getFileType(fileType: String): FileType {
        return when (fileType) {
            FileType.Pdf.name -> {
                FileType.Pdf
            }
            FileType.Mp4.name -> {
                FileType.Mp4
            }
            FileType.Mp3.name -> {
                FileType.Mp3
            }
            FileType.Docx.name -> {
                FileType.Docx
            }
            FileType.Png.name -> {
                FileType.Png
            }
            FileType.Jpg.name -> {
                FileType.Jpg
            }
            else -> {
                FileType.Pdf
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    toastShort("Permissions granted")
                } else {
                    toastShort("Permission requests denied")
                }
            }
        }
    }
}