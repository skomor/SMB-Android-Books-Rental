package com.example.smb.booksapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.smb.booksapp.databinding.FragmentTagsBinding
import com.example.smb.booksapp.viewmodels.drawerFragments.TagsViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.TagsViewModelFactory
import com.example.smb.booksapp.data.Result

class TagsFragment : Fragment(), TagsAdapter.OnItemClickListener {

    private lateinit var tagsViewModel: TagsViewModel
    private var _binding: FragmentTagsBinding? = null

    private val binding get() = _binding!!

    private val adaper = TagsAdapter(this)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tagsViewModel =
            ViewModelProvider(this, TagsViewModelFactory()).get(TagsViewModel::class.java)

        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val postsRecyclerView: RecyclerView = binding.recyclerView
        postsRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        postsRecyclerView.adapter = adaper

        tagsViewModel.tags.observe(viewLifecycleOwner, Observer { yt ->
            adaper.postItems = yt
            adaper.notifyDataSetChanged()
        })
        tagsViewModel.saveResult.observe(viewLifecycleOwner, Observer { it->
            if(it is Result.Success)
                Toast.makeText(requireContext(), "Tags saved!", Toast.LENGTH_LONG).show()
            else{
                Toast.makeText(requireContext(), "Error: Could not save values!", Toast.LENGTH_LONG).show()
            }} )
        binding.saveButton.setOnClickListener {
            tagsViewModel.saveUserTags();
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        val clickedItem = tagsViewModel.tags.value?.get(position)
        clickedItem?.isUsers = !clickedItem?.isUsers!!
        adaper.notifyItemChanged(position)
    }
}