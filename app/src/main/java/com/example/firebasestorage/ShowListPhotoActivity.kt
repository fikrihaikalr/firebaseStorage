package com.example.firebasestorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestorage.databinding.ActivityShowListPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowListPhotoActivity : AppCompatActivity() {
    //inisialisasi binding
    private lateinit var binding : ActivityShowListPhotoBinding

    //inisialisasi storage references
    private val storageReference = FirebaseStorage.getInstance().getReference("uploads")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //memanggil fun getAllImage
        getAllImage()

    }

    //membuat fun getAllImage yang berfungsi untuk mengambil semua data pada storage
    private fun getAllImage() = CoroutineScope(Dispatchers.IO).launch{
        try {
            val images = storageReference.listAll().await()
            val imageUrl = mutableListOf<String>()
            for (image in images.items){
                val url = image.downloadUrl.await()
                imageUrl.add(url.toString())
            }

            withContext(Dispatchers.Main){
                val animalAdapter = AnimalAdapter(imageUrl)
                if (animalAdapter.itemCount == 0){
                    binding.textViewNoData.visibility = View.VISIBLE
                }
                binding.progressLoadList.visibility = View.GONE
                binding.recyclerViewImage.apply {
                    adapter = animalAdapter
                    layoutManager = LinearLayoutManager(this@ShowListPhotoActivity)
                }
            }
        }catch (e:Exception){
            withContext(Dispatchers.Main){
                binding.progressLoadList.visibility = View.GONE
                Toast.makeText(this@ShowListPhotoActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }
}