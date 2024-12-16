package com.github.lnstow.utils

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.lnstow.utils.ext.ApiError
import com.github.lnstow.utils.ext.IApiResp
import com.github.lnstow.utils.ext.expandTouchArea
import com.github.lnstow.utils.ext.hc
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ext.toPx
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.BaseVm
import kotlinx.coroutines.delay

class MainActivity : BaseAct() {
    init {
        BaseAct.actBehavior = BaseAct.ActBehavior(
            enableEdgeToEdge = false,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        myApp = application
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(0x009688.hc, 0x009688.hc),
            navigationBarStyle = SystemBarStyle.light(0x009688.hc, 0x009688.hc),
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun initView() {
        vm.test()
        findViewById<TextView>(R.id.test_tv).apply {
            expandTouchArea(50.toPx())
            setOnClickListener {
                TextInputDialog().show(supportFragmentManager, null)
            }
        }
    }

    private val vm by viewModels<MainVm>()
}

class MainVm : BaseVm() {
    fun test() {
        launchIn {
            delay(1500)
            throw ApiError(
                object : IApiResp<String> {
                    override val code: Any
                        get() = 1
                    override val message: String?
                        get() = "test ok"
                    override val data: String?
                        get() = "123"

                    override fun isOk(): Boolean {
                        return false
                    }
                }
            )
        }
    }
}