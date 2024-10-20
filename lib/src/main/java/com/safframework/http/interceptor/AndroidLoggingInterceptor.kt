package com.safframework.http.interceptor

import cn.netdiscovery.http.interceptor.LoggingInterceptor

/**
 *
 * @FileName:
 *          com.safframework.http.interceptor.AndroidLoggingInterceptor
 * @author: Tony Shen
 * @date: 2020-09-29 11:18
 * @version: V1.0 <描述当前版本功能>
 */
object AndroidLoggingInterceptor {

    @JvmOverloads
    @JvmStatic
    fun build(isDebug:Boolean = true, hideVerticalLine:Boolean = false, requestTag:String = "Request", responseTag:String = "Response", excludeList:MutableList<String> = mutableListOf()):LoggingInterceptor {

        init()

        val build = LoggingInterceptor.Builder()

        if (excludeList.isNotEmpty()) {
            build.excludeList.addAll(excludeList)
        }

        return if (hideVerticalLine) {
            build
                .loggable(isDebug) // TODO: 发布到生产环境需要改成false
                .androidPlatform()
                .request()
                .requestTag(requestTag)
                .response()
                .responseTag(responseTag)
                .hideVerticalLine()// 隐藏竖线边框
                .build()
        } else {
            build
                .loggable(isDebug) // TODO: 发布到生产环境需要改成false
                .androidPlatform()
                .request()
                .requestTag(requestTag)
                .response()
                .responseTag(responseTag)
                .build()
        }
    }
}
