package com.lzp.expandtext

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class ExpandTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var collapsed = true
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    private var collapsedMaxLine = 0

    private var content: String = ""

    private var hasMore = false

    private var collapsedColor = Color.BLACK

    private var collapsedUnderLine = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ExpandTextView).apply {
            collapsed = getBoolean(R.styleable.ExpandTextView_collapsed, true)
            collapsedMaxLine = getInt(R.styleable.ExpandTextView_collapsedMaxLine, 3)
            collapsedColor = getColor(R.styleable.ExpandTextView_collapsedColor, Color.BLACK)
            collapsedUnderLine = getBoolean(R.styleable.ExpandTextView_collapsedUnderLine, false)
            recycle()
        }

        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!TextUtils.isEmpty(content)) {
            if (layout != null) {
                if (layout.lineCount > collapsedMaxLine) {
                    hasMore = true
                }

                if (collapsed) {
                    if (layout.lineCount > collapsedMaxLine) {
                        maxLines = collapsedMaxLine
                        val endIndex = layout.getLineEnd(collapsedMaxLine - 1)
                        super.setText(getCollapsedContent(endIndex))
                    }
                } else {
                    if (hasMore) {
                        maxLines = Int.MAX_VALUE
                        super.setText(getAllContent())
                        hasMore = false
                    }
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setText(content: String) {
        this.content = content
        super.setText(content)
        maxLines = Int.MAX_VALUE
        hasMore = false
    }

    fun toggleCollapsed() {
        collapsed = !collapsed
        layout.getLineMax()
    }

    private fun getAllContent(): SpannableStringBuilder {
        return SpannableStringBuilder(content)
            .apply {
                append("收起")
                setSpan(ClickableColorSpan(color = collapsedColor, underLine = collapsedUnderLine) {
                    collapsed = !collapsed
                }, length - 2, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }


    private fun getCollapsedContent(endIndex: Int): SpannableStringBuilder {
        return SpannableStringBuilder(content.substring(0, endIndex - 7))
            .apply {
                append("...全文")
                setSpan(ClickableColorSpan(color = collapsedColor, underLine = collapsedUnderLine) {
                    collapsed = !collapsed
                }, length - 5, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
    }


    class ClickableColorSpan(
        private val color: Int,
        private val underLine: Boolean,
        private val click: () -> Unit
    ) : ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = underLine
        super.updateDrawState(ds)
        }

        override fun onClick(widget: View) {
            click.invoke()
        }
    }

}