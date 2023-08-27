package com.example.smb.booksapp

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.smb.booksapp.databinding.FragmentAudiobooksBinding
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import com.google.gson.Gson
import android.content.SharedPreferences
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.reflect.TypeToken
import java.io.File


class AudiobooksFragment : Fragment(), AudioAdapter.OnItemClickListener {

    var filesLiveData: MutableLiveData<MutableList<String>> = MutableLiveData<MutableList<String>>()
    private val files = mutableListOf<String>()
    private var fauvorites = mutableListOf<String>()
    lateinit var sharedpreferences: SharedPreferences
    private val adapter = AudioAdapter(this)
    private lateinit var binding: FragmentAudiobooksBinding
    val storage = Firebase.storage("gs://bookapp-0404.appspot.com").reference.child("Audio")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudiobooksBinding.inflate(inflater, container, false)
       /* binding.addfile.setOnClickListener {
            addfile()
        }*/
        sharedpreferences = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )!!
        fauvorites = getList()

        storage.listAll().addOnCompleteListener { it ->
            if (it.isSuccessful) {
                it.result.items.forEach { yt ->
                    val name = yt.toString().split("/").last()
                    files.add(name)
                }
                filesLiveData.value = files
            }
        }
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        filesLiveData.observe(viewLifecycleOwner, {
            var pairs = mutableListOf<Pair<String, Boolean>>()
            for (item in it) {
                if (fauvorites.contains(item))
                    pairs.add(Pair(item, true))
                else{
                    pairs.add(Pair(item, false))
                }
            }
            adapter.audioItems = pairs
            adapter.notifyDataSetChanged()
        })

        return binding.root

    }

    private fun addfile() {
        val intent = Intent()
            .setType("audio/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            val selectedfile = data?.data
            if (selectedfile != null) {
                filesLiveData.value = files
            }
        }
    }

    fun setLists(list: ArrayList<String>) {
        val gson = Gson()
        val json = gson.toJson(list)//converting list to Json
        with(sharedpreferences.edit()) {
            putString(getString(R.string.FileList), json)
            apply()
        }
    }

    fun getList(): ArrayList<String> {
        val gson = Gson()
        val files = sharedpreferences.getString(getString(R.string.FileList), "")
        if (files != "") {
            val type = object : TypeToken<ArrayList<String>>() {}.type//converting the json to list
            return gson.fromJson(files, type)//returning the list
        } else return arrayListOf()
    }

    override fun onPlayClick(position: Int) {
        Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = filesLiveData.value?.get(position)
        val localFile = File.createTempFile("audio", "mp3")
        storage.child(clickedItem!!).getFile(localFile).addOnSuccessListener {
            var intent = Intent(requireContext(), AudioService::class.java)
            intent.putExtra("path", localFile.toURI().toString());
            context?.startService(intent)

        }
    }

    override fun onStarClick(position: Int) {
        if (fauvorites.contains(filesLiveData.value?.get(position).toString())) {
            fauvorites.remove(filesLiveData.value?.get(position).toString())
        } else {
            fauvorites.add(filesLiveData.value?.get(position).toString())
        }
        adapter.audioItems[position] =
            Pair(adapter.audioItems[position].first, !adapter.audioItems[position].second)
        adapter.notifyItemChanged(position)
        setLists(fauvorites as ArrayList<String>)
    }

    override fun onPauseClick(position: Int) {
        Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
        context?.stopService(Intent(requireContext(), AudioService::class.java))
    }
}