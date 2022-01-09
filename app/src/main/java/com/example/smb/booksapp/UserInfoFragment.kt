package com.example.smb.booksapp

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.smb.booksapp.data.Result
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModelFactory

import android.content.Intent
import android.provider.MediaStore
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.example.smb.booksapp.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    var imageUri: Uri? = null

    private val userInfoViewModel: UserInfoViewModel by activityViewModels { UserInfoViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        val root: View = binding.root
        val loading = binding.loading
        loading.visibility = View.VISIBLE

        val nickname = binding.nicknametext;
        val emailTextView = binding.emailTextView;
     //   val picture = binding.profilePic;
        val description = binding.descriptionInput;
        val savebtn = binding.saveButton;
        val locationButton = binding.SetLocationButton;
        val currLocation = binding.currLoc

        userInfoViewModel.userimage.observe(viewLifecycleOwner, Observer{

        })
        userInfoViewModel.markerString.observe(viewLifecycleOwner, Observer {
            currLocation.text = it;
        })

        locationButton.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "Map should open", Toast.LENGTH_LONG).show()
            val dialog = MapDialogFragment(userInfoViewModel)
            dialog.show(parentFragmentManager, "lol")
        })

        savebtn.setOnClickListener(View.OnClickListener {
            userInfoViewModel.saveUserData(
                nickname.text.toString(),
                description.text.toString(),
                imageUri, "", ""
            ) {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            }
        })

        userInfoViewModel.userFormState.observe(viewLifecycleOwner, Observer {
             val userinfoState = it ?: return@Observer

            if (userinfoState.nickError != null) {
                nickname.error = getString(userinfoState.nickError)
            }else{
                nickname.error = null
            }
            if (userinfoState.descriptionError != null) {
                description.error = getString(userinfoState.descriptionError)
            }else{
                description.error = null
            }
            if(userinfoState.locationError == null){
                locationButton.error = null
            }else{
                locationButton.error = getString(userinfoState.locationError)
            }
            savebtn.isEnabled = userinfoState.isDataValid == true
        })

        userInfoViewModel.loadResult.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                nickname.setText(userInfoViewModel.user?.displayName.toString())
                emailTextView.text = userInfoViewModel.user?.email.toString()
                imageUri = userInfoViewModel.user?.photoUrl
                description.setText(userInfoViewModel.userAdv?.description)
                loading.visibility = View.GONE
            } else {
                Toast.makeText(activity, it.toString(), Toast.LENGTH_LONG).show();
                loading.visibility = View.GONE
            }
            userInfoViewModel.userDataChanged(
                nickname.text.toString(),
                description.text.toString(),
            )
        })

        nickname.afterTextChanged {
            userInfoViewModel.userDataChanged(
                nickname.text.toString(),
                description.text.toString(),
            )
        }
        description.afterTextChanged {
            userInfoViewModel.userDataChanged(
                nickname.text.toString(),
                description.text.toString(),
            )
        }

        binding.changeProfilePicButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 1)
        }

        return root
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
        if (resultCode == RESULT_OK && requestCode == 1) {
            imageUri = data?.data
            binding.profilePic.setImageURI(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}