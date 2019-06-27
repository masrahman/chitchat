package com.maasrahman.chitchat.ui

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.maasrahman.chitchat.R
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.iid.FirebaseInstanceId
import com.maasrahman.chitchat.utils.UserData
import com.maasrahman.chitchat.utils.showAlert


class MainActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtEmail.requestFocus()
        btnMulai.setOnClickListener {
            if(txtEmail.text.toString().isEmpty() || txtName.text.toString().isEmpty()){
                showAlert(this@MainActivity, getString(R.string.isianbelumlengkap))
                return@setOnClickListener
            }
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()){
                showAlert(this@MainActivity, getString(R.string.emailtidakvalid))
                return@setOnClickListener
            }
            insertUser()
        }
    }

    private fun insertUser(){
        dialog = Dialog(this@MainActivity, R.style.DialogBounceAnim)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.show()
        val email = txtEmail.text.toString()
        val name = txtName.text.toString()
        val user = hashMapOf(
            getString(R.string.emailvalue) to email,
            getString(R.string.namevalue) to name,
            getString(R.string.createdatvalue) to Timestamp.now()
        )

        db.collection(getString(R.string.users)).document(email).get()
            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document!!.exists()) {
                    if(document.getString(getString(R.string.namevalue)) == name){
                        toLogin(name, email)
                    }else{
                        dialog.dismiss()
                        showAlert(this@MainActivity, getString(R.string.emaildannamatidaksesuai))
                    }
                } else {
                    db.collection(getString(R.string.users)).document(email).set(user)
                        .addOnSuccessListener {
                            toLogin(name, email)
                        }
                }
            } else {
                dialog.dismiss()
                showAlert(this@MainActivity, getString(R.string.terjadikesalahan))
            }
        })
    }

    private fun toLogin(strNama: String, strEmail: String){
        UserData.saveBoolean(baseContext, getString(R.string.isLogin), true)
        UserData.saveString(baseContext, getString(R.string.emailvalue), strEmail)
        UserData.saveString(baseContext, getString(R.string.namevalue), strNama)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                dialog.dismiss()
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                val addData = hashMapOf(
                    getString(R.string.token) to token
                )
                db.collection(getString(R.string.users))
                    .document(UserData.loadString(baseContext, getString(R.string.emailvalue)))
                    .update(addData as Map<String, Any>)
                finish()
            })
            .addOnFailureListener(OnFailureListener { error ->
                dialog.dismiss()
                showAlert(this@MainActivity, error.toString())
            })
    }

    override fun onBackPressed() {}
}
