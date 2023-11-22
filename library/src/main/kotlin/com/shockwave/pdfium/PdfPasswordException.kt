package com.shockwave.pdfium

import java.io.IOException

class PdfPasswordException : IOException {
    constructor() : super()
    constructor(detailMessage: String?) : super(detailMessage)
}