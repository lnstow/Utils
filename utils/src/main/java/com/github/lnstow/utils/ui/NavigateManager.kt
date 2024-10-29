package com.github.lnstow.utils.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.lnstow.utils.ext.LaunchParams
import com.github.lnstow.utils.ext.getRequestCodeFromLpClz
import com.github.lnstow.utils.ext.newFrag
import com.github.lnstow.utils.ext.showToast
import com.github.lnstow.utils.ext.startActFromFrag
import com.github.lnstow.utils.ext.startActWithCtx
import com.github.lnstow.utils.util.isValid
import kotlin.reflect.KClass

/** 建议将 用户可以手动返回退出的frag，通过事件来显示和取消。
 * 如果frag不需要启动参数，可以用普通方式启动，
 * 也可以创建一个空object作为启动参数，添加映射，通过[navigate]进行跳转 */
abstract class NavigateManager {
    protected abstract val navMapFrag: HashMap<LpClz, () -> Fragment>
    protected abstract val navMapAct: HashMap<LpClz, KClass<out FragmentActivity>>
    protected abstract val actOptionsMap: HashMap<LpClz, (LaunchParams) -> Bundle?>
    protected abstract val implicitIntent: HashMap<LpClz, (LaunchParams) -> Intent>
    protected abstract fun setSpecialNavLp(lp: LaunchParams, startAct: () -> Unit)

    private fun getFrag(lp: LaunchParams): Fragment? {
        val frag = navMapFrag[lp::class] ?: return null
        return newFrag(frag, lp)
    }

    private fun startAct(
        lp: LaunchParams,
        frag: Fragment? = null,
        ctx: Context = frag!!.requireContext()
    ) {
        val act = navMapAct[lp::class] ?: return navToImplicitIntent(lp, frag, ctx)
        val options = actOptionsMap[lp::class]?.invoke(lp)
        setSpecialNavLp(lp) {
            if (frag != null) startActFromFrag(frag, act, lp, options)
            else startActWithCtx(ctx, act, lp, options)
        }
    }

    /** 尝试启动frag和act，如果找到对应的frag，则返回frag，
     * 否则，未找到frag和启动act（无论是否成功）的情况都返回null，
     * 调用处收到返回的null应该结束跳转 */
    fun navigate(lp: LaunchParams, ctx: Context): Fragment? {
        val f = getFrag(lp)
        if (f != null) return f
        startAct(lp, ctx = ctx)
        return null
    }

    fun navigate(lp: LaunchParams, frag: Fragment): Fragment? {
        val f = getFrag(lp)
        if (f != null) return f
        startAct(lp, frag)
        return null
    }

    private fun navToImplicitIntent(
        lp: LaunchParams,
        frag: Fragment? = null,
        ctx: Context = frag!!.requireContext()
    ) {
        val intent = implicitIntent[lp::class]?.invoke(lp) ?: return
        if (!intent.isValid(ctx)) {
            ctx.showToast("No activity for ${lp::class.java}", showShort = false)
            return
        }

        val options = actOptionsMap[lp::class]?.invoke(lp)
        val code = getRequestCodeFromLpClz(lp::class)
        setSpecialNavLp(lp) {
            frag?.startActivityForResult(intent, code, options)
                ?: (ctx as FragmentActivity).startActivityForResult(intent, code, options)
        }
    }
}
private typealias LpClz = KClass<out LaunchParams>