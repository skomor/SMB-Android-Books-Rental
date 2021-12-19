package com.example.smb.booksapp

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.smb.booksapp.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import androidx.appcompat.app.AlertDialog;
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.smb.booksapp.data.model.Book
import com.example.smb.booksapp.data.model.Tag
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.viewmodels.main.AddingFormState
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import android.widget.ArrayAdapter


class BottomAddItemDialog(private val mainViewModel: MainViewModel) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding
    var avaiableTags = arrayOf<String>()
    var selectedItemsBools = booleanArrayOf();
    lateinit var alertDialog: AlertDialog
    var imageUri: Uri? = null
    val selectedTags = mutableListOf<Tag>()
    lateinit var authorsToComplate: Array<String>
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun notifyViewModelAboutDataChange() {
        val bookName = binding.bookName;
        val bookDescription = binding.bookDescriptionInput;
        val authorSpinner = binding.authorSpinner
        mainViewModel.bookAddingDataChanged(
            bookName.text.toString(),
            bookDescription.text.toString(),
            authorSpinner.text.toString(),
            selectedTags.toList(),
            imageUri
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tagsControl = binding.tagSelector;
        val bookName = binding.bookName;
        val bookDescription = binding.bookDescriptionInput;
        val authorSpinner = binding.authorSpinner
        val addbtn = binding.idAddBtn
        val setImageBtn = binding.setImageBtn
        addbtn.isEnabled = false
        this.isCancelable = true;
       var sheet = dialog?.findViewById<View>(R.id.design_bottom_sheet)
        sheet?.getLayoutParams()?.height = ViewGroup.LayoutParams.MATCH_PARENT;

        mainViewModel.tags.observe(this@BottomAddItemDialog, {
            var temp = mutableListOf<String>()
            it.forEach {
                temp.add(it.name)
            }
            avaiableTags = arrayListOf(*temp.toTypedArray()).filterNotNull().toTypedArray()
            var temp2 = mutableListOf<Boolean>();
            avaiableTags.forEach { temp2.add(false) }
            selectedItemsBools = temp2.toBooleanArray()
        })

        val authors = mutableListOf<String>()
        mainViewModel.authors.value?.forEach {
            authors.add(it.name.toString());
        }

        authorsToComplate = authors.toTypedArray()

        mainViewModel.authors.observe(this@BottomAddItemDialog, {
            val authors1 = mutableListOf<String>()
            mainViewModel.authors.value?.forEach {
                authors1.add(it.name.toString());
            }
            authorsToComplate = authors.toTypedArray()

            adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                authorsToComplate
            )
            authorSpinner.setAdapter(adapter);
        }
        )

        adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            authorsToComplate
        )
        authorSpinner.setAdapter(adapter);

        authorSpinner.afterTextChanged {
            notifyViewModelAboutDataChange()
        }

        bookName.afterTextChanged {
            notifyViewModelAboutDataChange()
        }
        bookDescription.afterTextChanged {
            notifyViewModelAboutDataChange()
        }

        tagsControl.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select tags");
            builder.setCancelable(true);
            builder.setMultiChoiceItems(
                this@BottomAddItemDialog.avaiableTags,
                this@BottomAddItemDialog.selectedItemsBools
            )
            { dialog, which, isChecked ->
                selectedItemsBools[which] = isChecked
                val lang = avaiableTags[which]
            }
            builder.setPositiveButton("OK")
            { _, _ ->
                tagsControl.text = "Selected tags..... \n"
                selectedTags.clear()
                for (i in avaiableTags.indices) {
                    val checked = selectedItemsBools[i]
                    if (checked) {
                        tagsControl.text = "${tagsControl.text}  ${avaiableTags[i]}, "
                        selectedTags.add(Tag(avaiableTags[i]))
                    }
                }
                notifyViewModelAboutDataChange()
            }
            alertDialog = builder.create()
            alertDialog.show();
        }


        addbtn.setOnClickListener {
            mainViewModel.addBook(
                Book(
                    bookName.text.toString(),
                    bookDescription.text.toString(),
                    authorSpinner.text.toString(),
                    this.imageUri,
                    null,
                    this.selectedTags.toList()
                )
            )// take a note that this is in one block
            mainViewModel.addingResult.observe(this@BottomAddItemDialog,
                Observer {
                    if (it is Result.Success) {
                        Toast.makeText(context, "Book added", Toast.LENGTH_LONG).show()
                        this.dismiss()
                    }
                })
        }

        mainViewModel.addingFormState.observe(this@BottomAddItemDialog,
            Observer {
                val addingState = it ?: return@Observer
                setErrorsIfNeeded(addingState);
            })


        setImageBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 1000)
        }

        binding.idBtnDismiss.setOnClickListener {
            Toast.makeText(context, "Dismissed", Toast.LENGTH_LONG).show()
            this.dismiss()
        }
    }

    private fun setErrorsIfNeeded(addingState: AddingFormState) {

        val tagsControl = binding.tagSelector;
        val bookName = binding.bookName;
        val bookDescription = binding.bookDescriptionInput;
        val authorSpinner = binding.authorSpinner
        val addbtn = binding.idAddBtn
        val setImageBtn = binding.setImageBtn

        addbtn.isEnabled = addingState.isDataValid

        if (addingState.authorError != null) {
            authorSpinner.error = getString(addingState.authorError)
            addbtn.error = getString(addingState.authorError)
        }
        if (addingState.bookNameError != null) {
            bookName.error = getString(addingState.bookNameError)
            addbtn.error = getString(addingState.bookNameError)
        }
        if (addingState.descriptionError != null) {
            bookDescription.error = getString(addingState.descriptionError)
            addbtn.error = getString(addingState.descriptionError)
        }
        if (addingState.tagsError != null) {
            tagsControl.setTextColor(
                ContextCompat.getColor(
                    this.requireContext(),
                    R.color.design_default_color_error
                )
            )
            tagsControl.tooltipText = getString(addingState.tagsError)
            addbtn.error = getString(addingState.tagsError)
        } else {
            tagsControl.setTextColor(
                ContextCompat.getColor(
                    this.requireContext(),
                    R.color.black
                )
            )
            tagsControl.tooltipText = ""

        }
        if (addingState.pictureError != null) {
            setImageBtn.error = getString(addingState.pictureError)
            addbtn.error = getString(addingState.pictureError)
        } else {
            setImageBtn.error = null;
        }
        if (addingState.isDataValid) {
            addbtn.error = null;
        }
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            notifyViewModelAboutDataChange()
            imageUri = data?.data
            binding.imageViewInpu.setImageURI(null)
            binding.imageViewInpu.setImageURI(imageUri)
        }
    }
}