package com.shockwave.pdfium.util

class SizeF(val width: Float, val height: Float) {

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this === other) {
            return true
        }
        if (other is SizeF) {
            return width == other.width && height == other.height
        }
        return false
    }

    override fun toString(): String {
        return width.toString() + "x" + height
    }

    override fun hashCode(): Int {
        return java.lang.Float.floatToIntBits(width) xor java.lang.Float.floatToIntBits(height)
    }

    fun toSize(): Size {
        return Size(width.toInt(), height.toInt())
    }
}