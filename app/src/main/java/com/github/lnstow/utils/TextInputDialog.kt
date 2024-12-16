package com.github.lnstow.utils

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.updatePadding
import com.github.lnstow.utils.ext.addWindowInsetsPadding
import com.github.lnstow.utils.ext.bgColorCode
import com.github.lnstow.utils.ui.BaseBottomDialog

class TextInputDialog : BaseBottomDialog() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return EditText(inflater.context).apply {
            bgColorCode(Color.RED, 20)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.run {
//            navigationBarColor = Color.BLUE
            decorView.addWindowInsetsPadding(consumed = true) {
//                decorView.setPadding(0)
                view.updatePadding(bottom = it.bottom * 2)
            }
        }
    }
}