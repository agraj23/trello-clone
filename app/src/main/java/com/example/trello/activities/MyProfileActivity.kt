package com.example.trello.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.User
import com.example.trello.utils.Constants
import com.example.trello.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.trello.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.trello.utils.Constants.showImageChooser
import com.google.common.io.Files.getFileExtension
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import org.checkerframework.checker.units.qual.m
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserDetails : User
    private var mProfileImageURL : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setupActionBar()
        FirestoreClass().loadUserData(this)
        var iv_profile_user_image : CircleImageView = findViewById(R.id.iv_profile_user_image)
        iv_profile_user_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
            else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE)
            }
        }
        var btn_update :Button = findViewById(R.id.btn_update)
        btn_update.setOnClickListener {
            if(mSelectedImageFileUri!=null) {
                uploadUserImage()
            }else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }

        }
        else {
            Toast.makeText(this,"Permission denied to access photos", Toast.LENGTH_LONG).show()
        }
    }



    private fun setupActionBar() {
        val toolbar_my_profile_activity : Toolbar = findViewById(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24dp)
            actionBar.title=resources.getString(R.string.my_profile)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user : User) {
        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_profile_user_image));

        val et_email : EditText = findViewById(R.id.et_email)
        val et_name : EditText = findViewById(R.id.et_name)
        et_email.setText(user.email)
        et_name.setText(user.name)
        if(user.mobile!=0L) {
            val et_mobile : EditText =findViewById(R.id.et_mobile)
            et_mobile.setText(user.mobile.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data !=null) {
            mSelectedImageFileUri=data.data

            try{
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(findViewById(R.id.iv_profile_user_image));
            }catch (e : IOException) {
                e.printStackTrace()
            }


        }
    }


    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri!=null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().
            reference.child("USER_IMAGE"+System.currentTimeMillis()+
                    "."+Constants.getFileExtension(this,mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                    Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.i("Downloadable Image URL", uri.toString())
                        mProfileImageURL = uri.toString()
                        updateUserProfileData()
                    }

            }.addOnFailureListener{
                exception ->
                    Toast.makeText(
                        this, exception.message, Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()

            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String,Any>()

        var anyChangesMade = false

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL!=mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }
        var et_name : EditText = findViewById(R.id.et_name)
        if(et_name.text.toString()!=mUserDetails.name) {
            userHashMap[Constants.NAME]= et_name.text.toString()
            anyChangesMade = true
        }
        var et_mobile : EditText = findViewById(R.id.et_mobile)
        if(et_mobile.text.toString()!=mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE]= et_mobile.text.toString().toLong()
            anyChangesMade = true
        }
        if(anyChangesMade) FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}