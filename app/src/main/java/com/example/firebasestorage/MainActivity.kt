package com.example.firebasestorage

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.icu.text.CaseMap.Title
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.firebasestorage.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

//berfungsi untuk pada saat kita mengambil URI yang kita pilih dari galeri
private const val REQUEST_CODE = 72
class MainActivity : AppCompatActivity() {

    //inisialisasi viewBinding
    private lateinit var binding : ActivityMainBinding

    //variable ini berfungsi untuk menampung gambar yang sudah kita pilih dari galeri
    private var imageUri : Uri? =null

    //membuat variabel storage yang berfungsi untuk meload instens dan references pada storage
    private val storageReferences = FirebaseStorage.getInstance().getReference("uploads")

    //berfungsi menampung text yang di input pada aplikasi
    private lateinit var title: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //berfungsi untuk meload xml pada mainActivity
        setContentView(binding.root)

        //memanggil fungsi setImageViewHome
        setImageViewHome()

        //memanggil funsi initAction
        initAction()
    }

    //fungsi delete gambar pada firebase storage
    private fun deleteImage(title: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //await berfungsi untuk mengeksekusi dilatar belakang atau asynchronous
            storageReferences.child(title).delete().await()
            withContext(Dispatchers.Main){
                //jika data berhasil di hapus, akan muncul dialog success delete image
                Toast.makeText(this@MainActivity,"Successfully delete image",Toast.LENGTH_SHORT).show()
                //memanggil fungsi reset layout
                resetLayout()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                binding.inputTextTitle.error = e.message
                //menonaktifkan progress bar
                binding.progressBarLoadingIndicator.visibility = View.GONE

            }
        }
    }

    //fungsi untuk mereset tampilan layout
    private fun resetLayout(){
        setImageViewHome()
        imageUri = null
        binding.inputTextTitle.error = null
        binding.editTextTitle.text?.clear()
        binding.progressBarLoadingIndicator.visibility = View.GONE
        binding.textViewIndicatorLoading.visibility = View.GONE
    }

    //fungsi download img dari firebase storage
    private fun downloadImage(title: String) = CoroutineScope(Dispatchers.IO).launch {
        //membuat fungsi try catch
        try {
            //jika gambar berhasil di download
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = storageReferences.child(title).getBytes(maxDownloadSize).await()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.size)

            withContext(Dispatchers.Main){
                binding.imageViewHome.load(bitmap){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(15f))
                }
                binding.progressBarLoadingIndicator.visibility = View.GONE
            }
        }catch (e:Exception){
            //jika foto gagal di download muncul dialog text
            withContext(Dispatchers.Main){
                binding.inputTextTitle.error = e.message
                binding.progressBarLoadingIndicator.visibility = View.GONE
                setImageViewHome()
            }
        }
    }

    //fungsi upload gambar ke firebase storage
    private fun uploadImage(title: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageUri?.let { uri ->
                storageReferences.child(title).putFile(uri)
                    .addOnProgressListener {
                        val progress : Int = ((100 * it.bytesTransferred) / it.totalByteCount).toInt()
                        //mengatur progress bar
                        binding.progressBarLoadingIndicator.progress = progress
                        val indicatorText = "Loading... $progress%"
                        binding.textViewIndicatorLoading.text = indicatorText
                    }.await()

                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,"Success Uploaded !",Toast.LENGTH_SHORT).show()
                }
            }

        }catch (e:java.lang.Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    //fungsi untuk engambil uri gambar yang telah dipilih
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //membuat kondisi dimana jika REQUEST_CODE ada dan , REQUEST_CODE yang dimasukan sama dengan yang REQUEST_CODE yang sudah kita buat
        //maka kita mengambil data URI nya
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            data?.data?.let {
                //jika data nya ada, maka datanya akan di set pada imageView yang sudah dibuat pada xml
                imageUri = it
                binding.imageViewHome.load(imageUri){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(15f))
                }

            }
        }

    }

    //fungsi untuk mengatur gambar pada imageView
    private fun setImageViewHome(){
        binding.imageViewHome.load(ContextCompat.getDrawable(this,R.drawable.shape)){
            //crossfade berfungsi untuk, memberikan animasi pada saat meLoad gambar
            crossfade(true)
            crossfade(500)
            //transformation berfungsi untuk menambahkan rounded pada gambar
            transformations(RoundedCornersTransformation(15f))
        }
    }

    //fungsi untuk mendeklarasikan aksi pada aplikasi
    private fun initAction(){
        //memberikan action pada button selectImage
        binding.buttonSelectImage.setOnClickListener {
            //intent untuk membuka galeri
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE)
            }
        }
        //memberikan action pada button uploadImage
        binding.buttonUploadImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            //kondisi untuk mengecek apakah gambar uri ada atau tidak
            if (imageUri != null){
                //kondisi untuk cek apakah textInputnya kosong atau tidak
                if (title.isBlank()|| title.isEmpty()){
                    binding.inputTextTitle.error = "Required"
                }else{
                    binding.progressBarLoadingIndicator.isIndeterminate = false
                    binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                    binding.textViewIndicatorLoading.visibility = View.VISIBLE
                    binding.inputTextTitle.error = null
                    //memanggil fungsi uploadImage
                    uploadImage(title)
                }
            }else{
                Toast.makeText(this,"Select Image!",Toast.LENGTH_SHORT).show()

            }
            }
        binding.buttonDownloadImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            if (title.isBlank()||title.isEmpty()){
                binding.inputTextTitle.error = "Required"
                Toast.makeText(this,"Name is required!",Toast.LENGTH_SHORT).show()
            }else{
                binding.progressBarLoadingIndicator.isIndeterminate = true
                binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                binding.inputTextTitle.error = null
                downloadImage(title)
            }
        }

        binding.buttonDeleteImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            if (title.isBlank()||title.isEmpty()){
                binding.inputTextTitle.error = "Required"
                Toast.makeText(this,"Name is required", Toast.LENGTH_SHORT).show()
            }else{
                binding.progressBarLoadingIndicator.isIndeterminate = true
                binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                binding.inputTextTitle.error = null
                deleteImage(title)
            }
        }
        binding.buttonShowAllImage.setOnClickListener {
            startActivity(Intent(this,ShowListPhotoActivity::class.java))
        }
    }
}