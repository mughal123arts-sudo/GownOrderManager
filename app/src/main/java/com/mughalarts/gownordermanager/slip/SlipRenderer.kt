package com.mughalarts.gownordermanager.slip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.mughalarts.gownordermanager.R
import com.mughalarts.gownordermanager.data.BusinessSettings
import com.mughalarts.gownordermanager.data.Order
import com.mughalarts.gownordermanager.util.PaymentStatus
import com.mughalarts.gownordermanager.util.formatDate
import com.mughalarts.gownordermanager.util.formatDateOrDash
import com.mughalarts.gownordermanager.util.formatMoney
import kotlin.math.ceil
import kotlin.math.max

/**
 * Draws the "Agreement Slip" as one tall Bitmap.
 * The same bitmap is shown on screen and exported as JPEG, so what you see is what you share.
 */
object SlipRenderer {

    private const val WIDTH = 1080
    private const val MARGIN = 64f
    private const val CONTENT_WIDTH = WIDTH - 2 * 64

    private val NAVY = Color.parseColor("#17365D")
    private val GOLD = Color.parseColor("#D4A72C")
    private val GREEN = Color.parseColor("#237A4B")
    private val AMBER = Color.parseColor("#8A6A00")
    private val TEXT = Color.parseColor("#202124")
    private val GRAY = Color.parseColor("#5F6368")
    private val LIGHT_LINE = Color.parseColor("#D9DCE1")

    private fun textPaint(sizePx: Float, colorInt: Int, bold: Boolean = false): TextPaint {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG)
        p.textSize = sizePx
        p.color = colorInt
        p.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        return p
    }

    private val NAME_PAINT = textPaint(66f, NAVY, true)
    private val CONTACT_PAINT = textPaint(28f, GRAY)
    private val TITLE_PAINT = textPaint(46f, NAVY, true).apply { letterSpacing = 0.12f }
    private val HEADER_PAINT = textPaint(32f, Color.WHITE, true)
    private val LABEL_PAINT = textPaint(32f, GRAY)
    private val VALUE_PAINT = textPaint(34f, TEXT)
    private val VALUE_BOLD_PAINT = textPaint(34f, TEXT, true)
    private val PAID_PAINT = textPaint(36f, GREEN, true)
    private val PENDING_PAINT = textPaint(36f, AMBER, true)
    private val FOOTER_PAINT = textPaint(36f, NAVY, true)

    private fun makeLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()
    }

    /** Draws when [canvas] is not null; with a null canvas it only measures (y keeps moving). */
    private class Painter(val canvas: Canvas?) {
        var y = 0f
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        fun drawLayout(layout: StaticLayout, x: Float, top: Float) {
            val c = canvas ?: return
            c.save()
            c.translate(x, top)
            layout.draw(c)
            c.restore()
        }

        fun rect(left: Float, top: Float, right: Float, bottom: Float, colorInt: Int, radius: Float = 0f) {
            val c = canvas ?: return
            fill.style = Paint.Style.FILL
            fill.color = colorInt
            if (radius > 0f) {
                c.drawRoundRect(RectF(left, top, right, bottom), radius, radius, fill)
            } else {
                c.drawRect(left, top, right, bottom, fill)
            }
        }

        fun gap(h: Float) {
            y += h
        }

        fun header(logo: Bitmap?, name: String, contact: String) {
            val top = y
            val logoH = 170f
            var logoW = 0f
            if (logo != null && logo.height > 0) {
                logoW = logoH * logo.width / logo.height
                canvas?.drawBitmap(logo, null, RectF(MARGIN, top, MARGIN + logoW, top + logoH), bitmapPaint)
            }
            val textX = if (logo != null) MARGIN + logoW + 36f else MARGIN
            val textW = (WIDTH - MARGIN - textX).toInt()
            val nameLayout = makeLayout(name, NAME_PAINT, textW)
            val contactLayout = if (contact.isNotBlank()) makeLayout(contact, CONTACT_PAINT, textW) else null
            val contentH = nameLayout.height + (if (contactLayout != null) 10 + contactLayout.height else 0)
            val startY = top + max(0f, (logoH - contentH) / 2f)
            drawLayout(nameLayout, textX, startY)
            if (contactLayout != null) {
                drawLayout(contactLayout, textX, startY + nameLayout.height + 10f)
            }
            y = top + max(logoH, contentH.toFloat()) + 30f
        }

        fun goldLine() {
            rect(MARGIN, y, WIDTH - MARGIN, y + 6f, GOLD)
            y += 6f
        }

        fun title(text: String) {
            val l = makeLayout(text, TITLE_PAINT, CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER)
            drawLayout(l, MARGIN, y)
            y += l.height + 20f
        }

        fun sectionHeader(title: String) {
            val h = 58f
            rect(MARGIN, y, WIDTH - MARGIN, y + h, NAVY, 14f)
            val l = makeLayout(title, HEADER_PAINT, CONTENT_WIDTH - 48)
            drawLayout(l, MARGIN + 24f, y + (h - l.height) / 2f)
            y += h + 22f
        }

        fun row(label: String, value: String, valuePaint: TextPaint = VALUE_PAINT) {
            val labelWidth = 380
            val valueWidth = CONTENT_WIDTH - labelWidth - 16
            val l = makeLayout(label, LABEL_PAINT, labelWidth)
            val v = makeLayout(value.ifBlank { "-" }, valuePaint, valueWidth)
            drawLayout(l, MARGIN, y)
            drawLayout(v, MARGIN + labelWidth + 16, y)
            y += max(l.height, v.height) + 16f
        }

        fun divider() {
            rect(MARGIN, y, WIDTH - MARGIN, y + 2f, LIGHT_LINE)
            y += 18f
        }

        fun paragraph(text: String, paint: TextPaint, alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL) {
            val l = makeLayout(text, paint, CONTENT_WIDTH, alignment)
            drawLayout(l, MARGIN, y)
            y += l.height + 16f
        }

        fun border(totalHeight: Float) {
            val c = canvas ?: return
            fill.style = Paint.Style.STROKE
            fill.strokeWidth = 5f
            fill.color = NAVY
            c.drawRect(RectF(14f, 14f, WIDTH - 14f, totalHeight - 14f), fill)
            fill.style = Paint.Style.FILL
        }
    }

    private fun qty(selected: Boolean, quantity: Int): String =
        if (selected && quantity > 0) quantity.toString() else "-"

    private fun paintSlip(p: Painter, order: Order, settings: BusinessSettings, logo: Bitmap?): Float {
        p.y = 56f

        val name = settings.businessName.ifBlank { "MUGHAL ARTS" }
        val contact = listOf(settings.phone, settings.address)
            .filter { it.isNotBlank() }
            .joinToString("\n")
        p.header(logo, name, contact)
        p.goldLine()
        p.gap(26f)

        p.title("AGREEMENT SLIP")
        p.gap(10f)
        p.row("Order Number", order.orderNumber, VALUE_BOLD_PAINT)
        p.row("Date", formatDate(order.orderDate))
        p.gap(10f)

        p.sectionHeader("CUSTOMER INFORMATION")
        p.row("Customer Name", order.customerName)
        p.row("School / College", order.schoolName)
        p.row("Contact Number", order.contactNumber)
        p.row("Address", order.address)
        p.gap(10f)

        p.sectionHeader("GOWN DETAILS")
        p.row("Blue", qty(order.blueSelected, order.blueQuantity))
        p.row("Green", qty(order.greenSelected, order.greenQuantity))
        p.row("Mehroon", qty(order.mehroonSelected, order.mehroonQuantity))
        p.row("Black", qty(order.blackSelected, order.blackQuantity))
        p.divider()
        p.row("Total Gowns", order.totalGowns.toString(), VALUE_BOLD_PAINT)
        p.gap(10f)

        p.sectionHeader("PAYMENT DETAILS")
        p.row("Total Amount", formatMoney(order.totalAmount))
        p.row("Advance Payment", formatMoney(order.advanceAmount))
        p.row("Remaining Balance", formatMoney(order.remainingBalance), VALUE_BOLD_PAINT)
        val paid = order.paymentStatus == PaymentStatus.PAID
        p.row(
            "Payment Status",
            if (paid) PaymentStatus.PAID else PaymentStatus.PENDING,
            if (paid) PAID_PAINT else PENDING_PAINT
        )
        p.gap(10f)

        p.row("Delivery Date", formatDateOrDash(order.deliveryDate), VALUE_BOLD_PAINT)

        if (order.specialInstructions.isNotBlank()) {
            p.gap(10f)
            p.sectionHeader("SPECIAL INSTRUCTIONS")
            p.paragraph(order.specialInstructions, VALUE_PAINT)
        }

        val footer = settings.footerText
        if (footer.isNotBlank()) {
            p.gap(8f)
            p.divider()
            p.paragraph(footer, FOOTER_PAINT, Layout.Alignment.ALIGN_CENTER)
        }

        val totalHeight = p.y + 30f
        p.border(totalHeight)
        return totalHeight
    }

    fun render(context: Context, order: Order, settings: BusinessSettings): Bitmap {
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.ma_logo)

        // Pass 1: measure the height. Pass 2: draw.
        val height = ceil(paintSlip(Painter(null), order, settings, logo)).toInt()
        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        paintSlip(Painter(canvas), order, settings, logo)

        logo?.recycle()
        return bitmap
    }
}
