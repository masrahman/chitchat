package com.maasrahman.chitchat.ui.chat

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.FirebaseFirestore
import com.maasrahman.chitchat.R
import com.maasrahman.chitchat.ui.MainActivity
import com.maasrahman.chitchat.utils.UserData
import com.maasrahman.chitchat.utils.showAlert
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.startActivity
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.maasrahman.chitchat.adapter.MessageAdapter
import com.maasrahman.chitchat.data.AppDatabase
import com.maasrahman.chitchat.data.entity.MessageEntity
import com.maasrahman.chitchat.data.network.ApiService
import com.maasrahman.chitchat.data.network.RequestData
import com.maasrahman.chitchat.utils.ImageFilePath
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


class ChatActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val list = mutableListOf<MessageEntity>()
    private lateinit var adapter: MessageAdapter
    private var token = ""
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        job = Job()
        adapter = MessageAdapter(this@ChatActivity, list){
            startActivity<ImageViewActivity>("photo" to it.message)
        }
        recyclerChat.layoutManager = LinearLayoutManager(this@ChatActivity)
        recyclerChat.itemAnimator = DefaultItemAnimator()
        recyclerChat.adapter = adapter

        listenData()

        imgSend.setOnClickListener {
            if(txtMessage.text.toString() == ""){
                showAlert(this@ChatActivity, getString(R.string.pesankosong))
                return@setOnClickListener
            }
            sendMessage(txtMessage.text.toString(), "text")
        }

        imgPhoto.setOnClickListener {
            selectImage()
        }

        if(!UserData.loadBoolean(baseContext, getString(R.string.isLogin))){
            startActivity<MainActivity>()
            return
        }
    }

    private fun listenData(){
        token = ""
        db.collection(getString(R.string.users))
            .addSnapshotListener { value, e ->
                value?.forEach { row ->
                    if(row.id != UserData.loadString(baseContext, getString(R.string.emailvalue))){
                    token = if(token == ""){
                        row.getString("token").toString()
                    }else{
                        token + "," + row.getString("token").toString()
                    }
                }
                }
            }
    }

    private fun loadLocal() = launch {
        withContext(Dispatchers.IO){
            list.clear()
            val result = AppDatabase(this@ChatActivity).messageDao().getMessages(20)
            result.forEach {
                list.add(it)
            }
            withContext(Dispatchers.Main){
                adapter.notifyDataSetChanged()
                recyclerChat.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }


    private fun sendImage(file: File) = launch {
        dialog = Dialog(this@ChatActivity, R.style.DialogBounceAnim)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.show()
        withContext(Dispatchers.IO){
            val storageRef = storage.reference
            var file = Uri.fromFile(file)
            val fileRefs = storageRef.child("images/${file.lastPathSegment}")
            fileRefs.putFile(file).addOnSuccessListener(OnSuccessListener { task ->
                    dialog.dismiss()
                    val result = task.metadata!!.reference!!.downloadUrl
                    result.addOnSuccessListener {
                        sendMessage(it.toString(), "image")
                    }
                })
                .addOnFailureListener(OnFailureListener {
                    dialog.dismiss()
                    showAlert(this@ChatActivity, getString(R.string.terjadikesalahan))
                })

        }
    }

    private fun sendMessage(strMessage: String, dataType: String) = launch{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val chat = MessageEntity(userId = UserData.loadString(baseContext, getString(R.string.emailvalue)),
            nama = UserData.loadString(baseContext, getString(R.string.namevalue)),
            dateTime = sdf.format(Date()),
            message = strMessage,
            dataType = dataType)

            try{
                val response = ApiService(this@ChatActivity).sendNotif(RequestData(
                    chat.userId,
                    chat.nama,
                    chat.dateTime,
                    chat.message,
                    chat.dataType,
                    token
                )).await()

                if(response.rc == "00"){
                    txtMessage.setText("")
                    list.add(chat)
                    adapter.notifyDataSetChanged()
                    recyclerChat.scrollToPosition(adapter.itemCount - 1)
                    db.collection(getString(R.string.chats)).add(chat)
                        .addOnSuccessListener {

                        }
                    withContext(Dispatchers.IO){
                        AppDatabase(this@ChatActivity).messageDao().insertMessage(chat)
                    }
                }else{
                    showAlert(this@ChatActivity, getString(R.string.gagalmengirim))
                }
            }catch (e: HttpException){
                showAlert(this@ChatActivity, e.message())
            }
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val value = intent.getParcelableExtra<MessageEntity>("model")
                list.add(value)
                adapter.notifyDataSetChanged()
                recyclerChat.scrollToPosition(adapter.itemCount - 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun selectImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@ChatActivity,
                    arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    80
                )
            }
        }
        val dialog = Dialog(this@ChatActivity, R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_imagechooser)
        dialog.setCancelable(true)
        val cam: ImageView
        val gal: ImageView
        cam = dialog.findViewById(R.id.imageChoosercamera)
        gal = dialog.findViewById(R.id.imageChoosergallery)
        val listen = View.OnClickListener { v ->
            val intent: Intent
            when (v.id) {
                R.id.imageChoosercamera -> {
                    intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 88)
                    dialog.dismiss()
                }
                R.id.imageChoosergallery -> {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(Intent.createChooser(intent, "Pilih Aplikasi"), 8)
                    dialog.dismiss()
                }
            }
        }
        cam.setOnClickListener(listen)
        gal.setOnClickListener(listen)
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val unmaskedRequestCode = requestCode and 0x0000ffff
        if (unmaskedRequestCode == 8 && resultCode == Activity.RESULT_OK) {
            val path = ImageFilePath.getPath(this, data?.data)
            sendImage(File(path))
        } else if (unmaskedRequestCode == 88 && resultCode == Activity.RESULT_OK) {
            var fileOutputStream: FileOutputStream? = null
            val bm = data?.extras!!.get("data") as Bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileName = "$timeStamp.jpg"
            val file = File(destinationPath, fileName)
            try {
                fileOutputStream = FileOutputStream(file)
                bm!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream!!.flush()
                        fileOutputStream!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
            sendImage(file)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 80) run {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Akses Diijinkan, Silahkan Coba Kembali", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Akses Ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this@ChatActivity).registerReceiver((receiver), IntentFilter("UPDATE_UI"))
    }

    override fun onResume() {
        super.onResume()
        loadLocal()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this@ChatActivity).unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
