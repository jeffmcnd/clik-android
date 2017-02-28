package xyz.mcnallydawes.clik

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import xyz.mcnallydawes.clik.models.Picture
import xyz.mcnallydawes.clik.models.User
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class TakePhotoActivity : Activity() {
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var userRef: DatabaseReference
    lateinit var storage: FirebaseStorage

    lateinit var user: User

    lateinit var photoPath: String

    val REQUEST_IMAGE_CAPTURE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("user").child(auth.currentUser!!.uid)
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if(snapshot != null && snapshot.value != null) {
                    user = snapshot.getValue(User().javaClass)
                    dispatchTakePictureIntent()
                }
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        photoPath = image.absolutePath
        return image
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                val photoFile = createImageFile()
                val photoURI = FileProvider.getUriForFile(this, "xyz.mcnallydawes.clik.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (ex: IOException) {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(photoPath, options)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()

            val path = "pictures/" + auth.currentUser!!.uid + "-" + UUID.randomUUID() + ".jpg"
            val storageRef = storage.getReference(path)

            val context = this
            val task = storageRef.putBytes(byteArray)
            task.addOnSuccessListener {
                val picture = Picture()
                picture.url = it.downloadUrl.toString()
                picture.expirationDate = Date().time
                userRef.setValue(user)

                val f = File(photoPath)
                f.delete()

                context.finish()
            }
        }
    }
}
