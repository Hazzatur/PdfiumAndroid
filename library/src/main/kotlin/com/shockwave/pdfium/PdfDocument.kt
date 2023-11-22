package com.shockwave.pdfium

import android.graphics.RectF
import android.os.ParcelFileDescriptor

class PdfDocument
internal constructor() {
    class Meta {
        var title: String? = null
        var author: String? = null
        var subject: String? = null
        var keywords: String? = null
        var creator: String? = null
        var producer: String? = null
        var creationDate: String? = null
        var modDate: String? = null
    }

    class Bookmark {
        val children: MutableList<Bookmark> = mutableListOf()
        var title: String? = null
        var pageIdx: Long = 0
        var mNativePtr: Long = 0
        fun hasChildren(): Boolean {
            return children.isNotEmpty()
        }
    }

    class Link(val bounds: RectF, val destPageIdx: Int? = -1, val uri: String? = "")

    var mNativeDocPtr: Long = 0
    var parcelFileDescriptor: ParcelFileDescriptor? = null
    val mNativePagesPtr: MutableMap<Int, Long> = mutableMapOf()
    fun hasPage(index: Int): Boolean {
        return mNativePagesPtr.containsKey(index)
    }
}