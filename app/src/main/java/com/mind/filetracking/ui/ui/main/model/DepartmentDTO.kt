package com.mind.filetracking.ui.ui.main.model

import com.google.firebase.database.PropertyName

data class DepartmentDTO(
    @PropertyName("key") val id: String,
    @PropertyName("value") val name: String
)