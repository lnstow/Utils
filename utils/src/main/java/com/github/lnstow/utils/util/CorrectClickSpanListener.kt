package com.github.lnstow.utils.util

import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.text.getSpans
import kotlin.math.roundToInt

// 次要参考：https://blog.csdn.net/ruanxiaoyao/article/details/88062213
// 主要参考：https://juejin.cn/post/6844903793612554254#heading-6
/** 解决连续多个span点击错位的问题 */
class CorrectClickSpanListener : View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v !is TextView || event == null || v.text !is Spanned) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                var x = event.x
                var y = event.y
                x -= v.totalPaddingLeft
                y -= v.totalPaddingTop
                x += v.scrollX
                y += v.scrollY

                val layout = v.layout
                // 根据 y 得到对应的行 line
                val line = layout.getLineForVertical(y.roundToInt())
                // 判断得到的 line 是否正确
                if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)
                    || y < layout.getLineTop(line) || y > layout.getLineBottom(line)
                ) {
                    return false
                }
                // 根据 line 和 x 得到对应的下标
                var off = layout.getOffsetForHorizontal(line, x)
                // getOffsetForHorizontal 获得的下标会往右偏
                // 获得下标处字符左边的左边，如果大于点击的 x，就可能点的是前一个字符
                val xLeft = layout.getPrimaryHorizontal(off)
                if (xLeft < x) off += 1
                else if (xLeft > x) off -= 1
                // （待验证）end 应该是 off + 1，如果也是 off，得到的结果会往左偏
                val link = (v.text as Spanned).getSpans<IClickableSpan>(off, off)
                if (link.isNotEmpty()) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        link[0].onClick(v)
                    }
                    return true
                }
            }
        }
        return false
    }
}

interface IClickableSpan {
    fun onClick(v: View)
}