package com.safframework.http.interceptor

import android.util.Log
import cn.netdiscovery.http.interceptor.log.LogManager
import cn.netdiscovery.http.interceptor.log.LogProxy

/**
 *
 * @FileName:
 *          com.safframework.http.interceptor.Config
 * @author: Tony Shen
 * @date: 2020-09-13 23:22
 * @version: V1.0 <描述当前版本功能>
 */

/**
 * 在使用日志拦截器之前
 * 必须要先实现 LogProxy ，否则无法打印网络请求的 request 、response
 * 所以，先调用这个方法
 */
fun init() {

    LogManager.logProxy(object : LogProxy {
        override fun e(tag: String, msg: String) {
            Log.e(tag,msg)
        }

        override fun w(tag: String, msg: String) {
            Log.w(tag,msg)
        }

        override fun i(tag: String, msg: String) {
            Log.i(tag,msg)
        }

        override fun d(tag: String, msg: String) {
            Log.d(tag,msg)
        }
    })
}