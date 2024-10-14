@file:Suppress("CommentSpacing", "NoConsecutiveBlankLines")

package com.github.lnstow.utils.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

/** 页面启动方式说明，例子[example]。
 * 1. 每个页面 定义自己的[LaunchParams]和[ResultParams]，代表页面入参和出参，存放数据
 * 2. 调用[startAct]方法，传入目标页面，入参
 * 3. 目标页面使用[getLp]获取入参，使用[setResult]设置出参
 * 4. 原页面在[FragmentActivity.onActivityResult]中调用[parseResult]获取出参
 */
interface LaunchParams : Parcelable
interface ResultParams : Parcelable

//@kotlinx.parcelize.Parcelize
//object EmptyLp : LaunchParams
//
//@kotlinx.parcelize.Parcelize
//object EmptyRp : ResultParams

private const val LAUNCH_PARAMS = "custom_launch_params"
private const val RESULT_PARAMS = "custom_result_params"
private const val REQUEST_CODE = 12398

//private fun Fragment.example() {
////    startAct<FragmentActivity>(EmptyLp)
////    startActForResult<FragmentActivity, EmptyRp> { toString() }
//}
//
//private fun FragmentActivity.example() {
//    startAct<FragmentActivity>()
////    startActForResult<FragmentActivity, EmptyRp>(EmptyLp) { toString() }
//}

// -------------------- 下面是 目标页面 获取入参和 设置出参的方法 --------------------

/** fragment获取入参 */
fun <T : LaunchParams> Fragment.getLp() = arguments!!.getParcelable<T>(LAUNCH_PARAMS)!!

/** fragment新建并设置入参 */
fun <T : Fragment> newFrag(frag: () -> T, lp: LaunchParams? = null) = frag().apply {
    if (arguments == null) arguments = Bundle()
    arguments!!.putParcelable(LAUNCH_PARAMS, lp)
}

/** activity获取入参 */
fun <T : LaunchParams> FragmentActivity.getLp() =
    intent.getParcelableExtra<T>(LAUNCH_PARAMS)!!

/** activity设置出参 */
fun FragmentActivity.setResult(rp: ResultParams?, code: Int = FragmentActivity.RESULT_OK) {
    setResult(code, Intent().putExtra(RESULT_PARAMS, rp))
}

// -------------------- 下面是原页面 设置入参和 获取出参的方法 --------------------

// class CustomContract<LP : LaunchParams?, RP : ResultParams>(
//    private val clz: KClass<out FragmentActivity>
// ) : ActivityResultContract<LP, RP?>() {
//
//    /** activity设置入参 */
//    override fun createIntent(context: Context, input: LP): Intent {
//        return Intent(context, clz.java).putExtra(LAUNCH_PARAMS, input)
//    }
//
//    /** activity获取出参 */
//    override fun parseResult(resultCode: Int, intent: Intent?): RP? {
//        return com.maimemo.android.common.ui.parseResult(resultCode, intent)
//    }
// }
//
// /** activity新方式启动并设置入参，获取出参
// * @param ACT 需要启动的目标activity
// * @param RP 目标activity的出参类型
// * @param lp 目标activity需要的入参
// * @param onResult 目标activity的出参回调，等价于[FragmentActivity.onActivityResult]
// */
// inline fun <reified ACT : FragmentActivity, RP : ResultParams> FragmentActivity.startActForResult(
//    lp: LaunchParams? = null,
//    options: ActivityOptionsCompat? = null,
//    crossinline onResult: RP.() -> Unit
// ) = registerForActivityResult(CustomContract<LaunchParams?, RP>(ACT::class)) {
//    it?.onResult()
// }.launch(lp, options)
//
// /** activity新方式启动并设置入参，获取出参，详见[FragmentActivity.startActForResult] */
// inline fun <reified ACT : FragmentActivity, RP : ResultParams> Fragment.startActForResult(
//    lp: LaunchParams? = null,
//    options: ActivityOptionsCompat? = null,
//    crossinline onResult: RP.() -> Unit
// ) = requireActivity().startActForResult<ACT, RP>(lp, options, onResult)
//
// /** activity新方式启动并设置入参，不需要出参，详见[FragmentActivity.startActForResult] */
// inline fun <reified ACT : FragmentActivity> FragmentActivity.startAct(
//    lp: LaunchParams? = null,
//    options: ActivityOptionsCompat? = null,
// ) = startActForResult<ACT, ResultParams>(lp, options, onResult = {})
//
// /** activity新方式启动并设置入参，不需要出参，详见[FragmentActivity.startActForResult] */
// inline fun <reified ACT : FragmentActivity> Fragment.startAct(
//    lp: LaunchParams? = null,
//    options: ActivityOptionsCompat? = null,
// ) = startActForResult<ACT, ResultParams>(lp, options, onResult = {})

// -------------------- 下面是旧方式启动页面并设置参数 --------------------

/** 用于在[FragmentActivity.onActivityResult]中转换结果，
 * 如果结果成功转换成给定类型，返回对象，否则返回null，不需要requestCode */
inline fun <reified RP : ResultParams> parseResult(
    resultCode: Int,
    data: Intent?,
    block: (RP) -> Unit
): RP? = if (resultCode == Activity.RESULT_OK) runCatching {
    data?.getParcelableExtra<RP>("custom_result_params") as RP
}.getOrNull()?.also(block) else null

inline fun <reified LP : LaunchParams> parseResultFromImplicitIntent(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
    block: (Intent) -> Unit
) = if (resultCode == Activity.RESULT_OK
    && getRequestCodeFromLpClz(LP::class) == requestCode
) data?.let(block) else null

/** activity旧方式启动并设置入参 */
inline fun <reified T : FragmentActivity> Fragment.startAct(
    lp: LaunchParams? = null,
    options: Bundle? = null
) = startActFromFrag(this, T::class, lp, options)

@Deprecated("使用内联方法来调用", ReplaceWith("frag.startAct<>(lp,options)"))
fun startActFromFrag(
    frag: Fragment,
    target: KClass<out FragmentActivity>,
    lp: LaunchParams?,
    options: Bundle?
) = frag.startActivityForResult(
    Intent(frag.requireContext(), target.java).putExtra(LAUNCH_PARAMS, lp),
    REQUEST_CODE, options
)

/** activity旧方式启动并设置入参 */
inline fun <reified T : FragmentActivity> Context.startAct(
    lp: LaunchParams? = null,
    options: Bundle? = null
) = startActWithCtx(this, T::class, lp, options)

/** activity旧方式启动并设置入参 */
@Deprecated("使用内联方法来调用", ReplaceWith("context.startAct<>(lp,options)"))
fun startActWithCtx(
    context: Context,
    target: KClass<out FragmentActivity>,
    lp: LaunchParams?,
    options: Bundle?
) = context.activity()?.startActivityForResult(
    Intent(context, target.java).putExtra(LAUNCH_PARAMS, lp),
    REQUEST_CODE, options
)

fun Context.activity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> this.baseContext.activity()
    else -> {
        Toast.makeText(myApp, this::class.simpleName, Toast.LENGTH_LONG).show()
        null
    }
}

private val requestCodeMap = HashMap<KClass<out LaunchParams>, Int>()
fun getRequestCodeFromLpClz(lp: KClass<out LaunchParams>) =
    requestCodeMap.getOrPut(lp) { lp.java.name.hashCode() and 0x00000fff }
