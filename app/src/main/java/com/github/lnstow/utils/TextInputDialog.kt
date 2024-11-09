package com.github.lnstow.utils

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
}