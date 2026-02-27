package com.example.storynest.GlobalEvent

data class EventCapsule<T>(
    val data: T,
    val event: FollowEvent
)
