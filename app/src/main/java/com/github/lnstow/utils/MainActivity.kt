package com.github.lnstow.utils

import android.os.Bundle
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.lnstow.utils.ext.ApiError
import com.github.lnstow.utils.ext.IApiResp
import com.github.lnstow.utils.ext.ToastDef
import com.github.lnstow.utils.ext.expandTouchArea
import com.github.lnstow.utils.ext.hc
import com.github.lnstow.utils.ext.matchCode
import com.github.lnstow.utils.ext.myApp
import com.github.lnstow.utils.ext.toPx
import com.github.lnstow.utils.no.Test
import com.github.lnstow.utils.ui.BaseAct
import com.github.lnstow.utils.ui.BaseVm
import com.learn.cmp.LnstowCmpTest
import kotlinx.coroutines.delay

class MainActivity : BaseAct() {
    init {
        BaseAct.actBehavior = BaseAct.ActBehavior(
            enableEdgeToEdge = false,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        myApp = application
        setContent {
            MaterialTheme {
                Scaffold { p ->
                    Box(Modifier.padding(p)) {
//                        Test.Test()
                        LnstowCmpTest()
                    }
                }
            }
        }
//        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(0x009688.hc, 0x009688.hc),
            navigationBarStyle = SystemBarStyle.light(0x009688.hc, 0x009688.hc),
        )

        return
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun initView() {
        return
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
        launchIn(onError = {
            it.matchCode(2, 3, 1) {
                toast.emit(ToastDef("match code"))
            } ?: toast.emit(ToastDef("no match"))
        }) {
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