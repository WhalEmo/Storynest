package com.example.storynest.CustomViews.ConfirmDialog

enum class ConfirmDialogStatus(
    val title: String,
    val message: String,
    val confirmText: String = "Evet",
    val cancelText: String = "Vazgeç"
) {
    UN_FOLLOW_DIALOG(
        title = "Takibi Bırak",
        message = " kullanıcıyı takip etmeyi bırakmak istiyor musun?",
        confirmText = "Takibi Bırak"
    ),
    BLOCK_DIALOG(
        title = "Engelle",
        message = " kullanıcıyı engellemek istiyor musun?",
        confirmText = "Engelle"
    ),
    UN_BLOCK_DIALOG(
        title = "Engelleme Bırak",
        message = " kullanıcının engelini kaldırmak istiyor musun?",
        confirmText = "Kaldır"
    ),
    REMOVE_FOLLOW_DIALOG(
        title = "Takipçiden Çıkar",
        message = " kullanıcıyı takipçilerden çıkarmak istiyor musun?",
        confirmText = "Çıkar"
    )
}