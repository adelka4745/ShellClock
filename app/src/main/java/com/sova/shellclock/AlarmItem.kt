package com.sova.shellclock

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean
)