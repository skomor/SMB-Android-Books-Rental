package com.example.smb.booksapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.smb.booksapp.data.model.FenceRequest
import com.example.smb.booksapp.databinding.FragmentHomeBinding
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModelFactory
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(), BooksAdapter.OnItemClickListener {

   // private lateinit var mainViewModel: MainViewModel
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private val adapter = BooksAdapter(this)
    private val model: UserInfoViewModel by activityViewModels { UserInfoViewModelFactory() }
    private val mainViewModel: MainViewModel by activityViewModels { MainViewModelFactory() }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        textView.text = "test"

        val postsRecyclerView: RecyclerView = binding.recyclerView
        postsRecyclerView.layoutManager = LinearLayoutManager(context)
        postsRecyclerView.adapter = adapter
        mainViewModel.loadBooks()
        mainViewModel.books.observe(viewLifecycleOwner, Observer {
            adapter.bookItems = it
            adapter.notifyDataSetChanged()
        })
        model.userFormState.observe(viewLifecycleOwner, {
            Log.e("lol", "catched")
        })

        binding.fab.setOnClickListener { view ->
            val dialog = BottomAddItemDialog(this.mainViewModel)

            dialog.isCancelable = true
            dialog.show(parentFragmentManager, "lol")
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(context, "Item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = mainViewModel.books.value?.get(position)
        adapter.notifyItemChanged(position)
    }
}