package com.example.storynest.Profile.EditProfile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import coil.load
import com.example.storynest.databinding.ProfileEditBinding


class EditProfileFragment: Fragment() {

    private lateinit var binding: ProfileEditBinding

    private val name by lazy {
        requireArguments().getString(ARG_NAME) ?: ""
    }
    private val surname by lazy {
        requireArguments().getString(ARG_SURNAME) ?: ""
    }
    private val bio by lazy {
        requireArguments().getString(ARG_BIO) ?: ""
    }
    private val imageUrl by lazy {
        requireArguments().getString(ARG_IMAGE_URL) ?: ""
    }
    companion object{
        private const val ARG_NAME = "name"
        private const val ARG_SURNAME = "surname"
        private const val ARG_BIO = "bio"
        private const val ARG_IMAGE_URL = "imageUrl"

        fun newInstance(name: String, surname: String, bio: String, imageUrl: String): EditProfileFragment{
            val fragment = EditProfileFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            args.putString(ARG_SURNAME, surname)
            args.putString(ARG_BIO, bio)
            args.putString(ARG_IMAGE_URL, imageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.ivProfile.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "Hiçbir medya seçilmedi")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()

        binding.btnSave.setOnClickListener {
            if (isFormValid()) {
                "Bilgiler başarıyla güncellendi."
            }
        }
        binding.btnChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupValidation() {
        Log.d("EditProfileFragment", "name: $name, surname: $surname, bio: $bio, imageUrl: $imageUrl")

        binding.etName.setText(name)
        binding.etSurname.setText(surname)
        binding.etBio.setText(bio)
        binding.ivProfile.load(imageUrl)

        updateSaveButtonState()
        binding.etName.doAfterTextChanged { text ->
            val error = ProfileValidator.validateFullName(text.toString())
            binding.tilName.error = error
            updateSaveButtonState()
        }

        binding.etBio.doAfterTextChanged { text ->
            val error = ProfileValidator.validateBio(text.toString())
            binding.tilBio.error = error
            updateSaveButtonState()
        }
    }

    private fun isFormValid(): Boolean {
        return ProfileValidator.validateFullName(binding.etName.text.toString()) == null &&
                ProfileValidator.validateBio(binding.etBio.text.toString()) == null
    }

    private fun updateSaveButtonState() {
        binding.btnSave.isEnabled = isFormValid()
    }


    override fun onResume() {
        super.onResume()
        // Klavye açıldığında ekranı yeniden boyutlandır
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onPause() {
        super.onPause()
        // Sayfadan çıkınca eski haline döndür (genelde ADJUST_PAN varsayılandır)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

}