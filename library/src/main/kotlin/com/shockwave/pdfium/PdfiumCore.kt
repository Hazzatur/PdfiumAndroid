package com.shockwave.pdfium

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import com.shockwave.pdfium.util.Size
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

class PdfiumCore(context: Context) {
    private val mCurrentDpi: Int = context.resources.displayMetrics.densityDpi
    private external fun nativeOpenDocument(fd: Int, password: String?): Long
    private external fun nativeOpenMemDocument(data: ByteArray, password: String?): Long
    private external fun nativeCloseDocument(docPtr: Long)
    private external fun nativeGetPageCount(docPtr: Long): Int
    private external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long
    private external fun nativeLoadPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray
    private external fun nativeClosePage(pagePtr: Long)
    private external fun nativeClosePages(pagesPtr: LongArray)
    private external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageWidthPoint(pagePtr: Long): Int
    private external fun nativeGetPageHeightPoint(pagePtr: Long): Int

    private external fun nativeRenderPage(
        pagePtr: Long, surface: Surface, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeRenderPageBitmap(
        pagePtr: Long, bitmap: Bitmap, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeGetDocumentMetaText(docPtr: Long, tag: String): String?
    private external fun nativeGetFirstChildBookmark(docPtr: Long, bookmarkPtr: Long?): Long?
    private external fun nativeGetSiblingBookmark(docPtr: Long, bookmarkPtr: Long): Long?
    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String?
    private external fun nativeGetBookmarkDestIndex(docPtr: Long, bookmarkPtr: Long): Long
    private external fun nativeGetPageSizeByIndex(docPtr: Long, pageIndex: Int, dpi: Int): Size
    private external fun nativeGetPageLinks(pagePtr: Long): LongArray
    private external fun nativeGetDestPageIndex(docPtr: Long, linkPtr: Long): Int?
    private external fun nativeGetLinkURI(docPtr: Long, linkPtr: Long): String?
    private external fun nativeGetLinkRect(linkPtr: Long): RectF?
    private external fun nativePageCoordsToDevice(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point

    init {
        Log.d(TAG, "Starting PdfiumAndroid " + BuildConfig.VERSION_NAME)
    }

    fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocument {
        val document = PdfDocument()
        document.parcelFileDescriptor = fd
        synchronized(lock) {
            document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password)
        }
        return document
    }

    fun newDocument(data: ByteArray, password: String?): PdfDocument {
        val document = PdfDocument()
        synchronized(lock) {
            document.mNativeDocPtr = nativeOpenMemDocument(data, password)
        }
        return document
    }

    fun getPageCount(doc: PdfDocument): Int {
        synchronized(lock) {
            return nativeGetPageCount(doc.mNativeDocPtr)
        }
    }

    fun openPage(doc: PdfDocument, pageIndex: Int): Long {
        synchronized(lock) {
            val pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex)
            doc.mNativePagesPtr[pageIndex] = pagePtr
            return pagePtr
        }
    }

    fun openPage(doc: PdfDocument, fromIndex: Int, toIndex: Int): LongArray {
        synchronized(lock) {
            val pagesPtr = nativeLoadPages(doc.mNativeDocPtr, fromIndex, toIndex)
            var pageIndex = fromIndex
            for (page in pagesPtr) {
                if (pageIndex > toIndex) break
                doc.mNativePagesPtr[pageIndex] = page
                pageIndex++
            }
            return pagesPtr
        }
    }

    fun getPageWidth(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[index] ?: return 0
            return nativeGetPageWidthPixel(pagePtr, mCurrentDpi)
        }
    }

    fun getPageHeight(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[index] ?: return 0
            return nativeGetPageHeightPixel(pagePtr, mCurrentDpi)
        }
    }

    fun getPageWidthPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[index] ?: return 0
            return nativeGetPageWidthPoint(pagePtr)
        }
    }

    fun getPageHeightPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[index] ?: return 0
            return nativeGetPageHeightPoint(pagePtr)
        }
    }

    fun getPageSize(doc: PdfDocument, index: Int): Size {
        synchronized(lock) {
            return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, mCurrentDpi)
        }
    }

    fun renderPage(
        doc: PdfDocument,
        surface: Surface,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        synchronized(lock) {
            try {
                nativeRenderPage(
                    doc.mNativePagesPtr[pageIndex] ?: return, surface, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception thrown from native", e)
            }
        }
    }

    fun renderPageBitmap(
        doc: PdfDocument,
        bitmap: Bitmap,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        synchronized(lock) {
            try {
                nativeRenderPageBitmap(
                    doc.mNativePagesPtr[pageIndex] ?: return, bitmap, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception thrown from native", e)
            }
        }
    }

    fun closeDocument(doc: PdfDocument) {
        synchronized(lock) {
            doc.mNativePagesPtr.values.forEach { nativeClosePage(it) }
            doc.mNativePagesPtr.clear()

            nativeCloseDocument(doc.mNativeDocPtr)

            doc.parcelFileDescriptor?.run {
                try {
                    close()
                } catch (e: IOException) {
                    // Ignored
                }
                doc.parcelFileDescriptor = null
            }
        }
    }

    fun getDocumentMeta(doc: PdfDocument): PdfDocument.Meta {
        synchronized(lock) {
            return PdfDocument.Meta().apply {
                title = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Title")
                author = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Author")
                subject = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Subject")
                keywords = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Keywords")
                creator = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Creator")
                producer = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Producer")
                creationDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "CreationDate")
                modDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "ModDate")
            }
        }
    }

    fun getTableOfContents(doc: PdfDocument): List<PdfDocument.Bookmark> {
        synchronized(lock) {
            val topLevel = mutableListOf<PdfDocument.Bookmark>()
            nativeGetFirstChildBookmark(doc.mNativeDocPtr, null)?.let {
                recursiveGetBookmark(topLevel, doc, it)
            }
            return topLevel
        }
    }

    private fun recursiveGetBookmark(
        tree: MutableList<PdfDocument.Bookmark>,
        doc: PdfDocument,
        bookmarkPtr: Long
    ) {
        val bookmark = PdfDocument.Bookmark().apply {
            mNativePtr = bookmarkPtr
            title = nativeGetBookmarkTitle(bookmarkPtr)
            pageIdx = nativeGetBookmarkDestIndex(doc.mNativeDocPtr, bookmarkPtr)
        }
        tree.add(bookmark)

        nativeGetFirstChildBookmark(doc.mNativeDocPtr, bookmarkPtr)?.let {
            recursiveGetBookmark(bookmark.children, doc, it)
        }

        nativeGetSiblingBookmark(doc.mNativeDocPtr, bookmarkPtr)?.let {
            recursiveGetBookmark(tree, doc, it)
        }
    }

    fun getPageLinks(doc: PdfDocument, pageIndex: Int): List<PdfDocument.Link> {
        synchronized(lock) {
            val links = mutableListOf<PdfDocument.Link>()
            val nativePagePtr = doc.mNativePagesPtr[pageIndex] ?: return links
            val linkPtrs = nativeGetPageLinks(nativePagePtr)
            linkPtrs.forEach { linkPtr ->
                val index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr)
                val uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr)
                val rect = nativeGetLinkRect(linkPtr)

                if (rect != null && (index != null || uri != null)) {
                    links.add(PdfDocument.Link(rect, index, uri))
                }
            }
            return links
        }
    }

    private fun mapPageCoordsToDevice(
        doc: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double
    ): Point {
        val pagePtr = doc.mNativePagesPtr[pageIndex] ?: return Point()
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY)
    }

    fun mapRectToDevice(
        doc: PdfDocument,
        pageIndex: Int,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: RectF
    ): RectF {
        val leftTop = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.left.toDouble(), coords.top.toDouble()
        )
        val rightBottom = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.right.toDouble(), coords.bottom.toDouble()
        )
        return RectF(
            leftTop.x.toFloat(),
            leftTop.y.toFloat(),
            rightBottom.x.toFloat(),
            rightBottom.y.toFloat()
        )
    }

    companion object {
        private val TAG = PdfiumCore::class.java.name
        private val FD_CLASS = FileDescriptor::class.java
        private const val FD_FIELD_NAME = "descriptor"

        init {
            try {
                System.loadLibrary("c++_shared")
                System.loadLibrary("modpng")
                System.loadLibrary("modft2")
                System.loadLibrary("modpdfium")
                System.loadLibrary("jniPdfium")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native libraries failed to load - $e")
            }
        }

        private val lock = Any()
        private var mFdField: Field? = null
        fun getNumFd(fdObj: ParcelFileDescriptor): Int {
            return try {
                if (mFdField == null) {
                    mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                    mFdField!!.isAccessible = true
                }
                mFdField!!.getInt(fdObj.fileDescriptor)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
                -1
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                -1
            }
        }
    }
}