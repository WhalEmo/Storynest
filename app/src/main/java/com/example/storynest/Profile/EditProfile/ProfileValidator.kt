package com.example.storynest.Profile.EditProfile

object ProfileValidator {
    fun validateFullName(name: String): String? {
        return when {
            name.isBlank() -> "İsim alanı boş bırakılamaz."
            name.length < 3 -> "İsim çok kısa."
            else -> null
        }
    }

    fun validateBio(bio: String): String? {
        return if (bio.length > 150) "Biyografi 150 karakterden fazla olamaz." else null
    }
}