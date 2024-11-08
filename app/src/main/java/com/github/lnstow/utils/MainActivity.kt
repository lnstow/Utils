package com.github.lnstow.utils

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.lnstow.utils.ext.AccessCtx
import com.github.lnstow.utils.ext.ApiError
import com.github.lnstow.utils.ext.IApiResp
import com.github.lnstow.utils.ext.LoadingInfo
import com.github.lnstow.utils.ext.launch
import com.github.lnstow.utils.ext.showDialog
import com.github.lnstow.utils.ext.showToast
import com.github.lnstow.utils.ui.BaseVm
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce

class MainActivity : AppCompatActivity(), AccessCtx {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        test()
    }

    private val vm by viewModels<MainVm>()
    private fun test() {
        launch {
            showToast(packageName)
//            delay(2000)
            vm.test()
            BaseVm.err.collect {
                showDialog {
                    setMessage(it.message)
                }
            }
        }
        var loadingUI: LoadingInfo.UiObj? = null
        launch {
            BaseVm.loading.debounce(800).collect {
                if (it == null) loadingUI?.hide(this@MainActivity)
                else {
                    loadingUI?.hide(this@MainActivity)
                    loadingUI = it.createAndShow(this@MainActivity)
                }
            }
        }
    }

    override fun ctx(): Context {
        return this
    }
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