package com.example.storynest.Follow

enum class FollowListEmptyDescription(
    val title: String,
    val description: String,
    val showDiscoverPeople: Boolean = true
) {
    MY_FOLLOWERS(
        title = "Henüz Takipçi Yok",
        description = "Görünüşe göre burası biraz ıssız. Hikayelerini paylaşarak topluluğunu büyütmeye ne dersin?"
    ),
    MY_FOLLOWING(
        title = "Kimseyi Takip Etmiyorsunuz",
        description = "Yeni insanlar keşfetmek ve akışınızı canlandırmak için önerilen hesaplara göz atın.",
        showDiscoverPeople = true
    ),
    USER_FOLLOWERS(
        title = "Takipçi Bulunmuyor",
        description = "Bu kullanıcının henüz bir takipçisi yok. İlk takipçisi siz olmak ister misiniz?",
        showDiscoverPeople = false
    ),
    USER_FOLLOWING(
        title = "Takip Edilen Kimse Yok",
        description = "Bu kullanıcı henüz kimseyi takip etmiyor.",
        showDiscoverPeople = false
    )
}