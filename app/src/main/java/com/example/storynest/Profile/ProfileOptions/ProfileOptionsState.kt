package com.example.storynest.Profile.ProfileOptions


enum class ProfileOptionsState(
    val title: String,
    val showMessage: Boolean,
    val showUnfollow: Boolean,
    val showBlock: Boolean,
    val showShare: Boolean = true,
    val blockText: String = "Engelle"
) {
    BLOCKER_OPTIONS(
        title = "Engel Ayarları",
        showMessage = false,
        showUnfollow = false,
        showBlock = true,
        blockText = "Engeli Kaldır"
    ),
    BLOCKED_OPTIONS(
        title = "Profil Seçenekleri",
        showMessage = false,
        showUnfollow = false,
        showBlock = false
    ),
    NORMAL_OPTIONS(
        title = "Profil Seçenekleri",
        showMessage = true,
        showUnfollow = false,
        showBlock = true
    ),
    FOLLOWING_OPTIONS(
        title = "Takip Ettiği Kişiler",
        showMessage = true,
        showUnfollow = true,
        showBlock = true
    )
}