package com.dicoding.eyesphere_nav.data

import android.media.Image
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PanduanHorizontal (
    val pic: Int,
    val description : String
) : Parcelable

@Parcelize
data class PanduanVertical(
    val pic: Int,
    val title: String,
    val description: String,
    val category: String
) : Parcelable