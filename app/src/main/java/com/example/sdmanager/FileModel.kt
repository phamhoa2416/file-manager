package com.example.sdmanager

import java.time.ZonedDateTime

data class FileModel(
    val name: String,
    val path: String,
    val child: Int,
    val isDirectory: Boolean,
    val creationDate: ZonedDateTime,
    val size: Long
)
