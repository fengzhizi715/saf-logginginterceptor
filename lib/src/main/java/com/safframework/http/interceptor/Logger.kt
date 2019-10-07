package com.safframework.http.interceptor

import android.text.TextUtils
import android.util.Log
import okhttp3.FormBody
import okhttp3.Request
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Created by Tony Shen on 2017/7/9.
 */
class Logger {

    companion object {

        private const val JSON_INDENT = 3
        private const val MAX_STRING_LENGTH = 4000
        private const val MAX_LONG_SIZE = 120
        private const val N = "\n"
        private const val T = "\t"

        private const val TOP_LEFT_CORNER = '╔'
        private const val BOTTOM_LEFT_CORNER = '╚'
        private const val DOUBLE_DIVIDER = "═════════════════════════════════════════════════"
        private val TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private val BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private val LINE_SEPARATOR = System.getProperty("line.separator")

        private fun String.isLineEmpty() = isEmpty() || N == this || T == this || this.trim { it <= ' ' }.isEmpty()

        private fun getDoubleSeparator(hideVerticalLine: Boolean = false) = if (hideVerticalLine) "$LINE_SEPARATOR  $LINE_SEPARATOR" else "$LINE_SEPARATOR║ $LINE_SEPARATOR"

        /**
         * 支持超长日志的打印
         */
        private fun printLog(tag: String, logString: String, logLevel: LoggingInterceptor.LogLevel) {

            if (logString.length > MAX_STRING_LENGTH) {

                var i = 0

                while (i < logString.length) {

                    if (i + MAX_STRING_LENGTH < logString.length)
                        log(tag, logString.substring(i, i + MAX_STRING_LENGTH), logLevel)
                    else
                        log(tag, logString.substring(i, logString.length), logLevel)
                    i += MAX_STRING_LENGTH
                }
            } else
                log(tag, logString, logLevel);
        }

        private fun log(tag: String, msg: String, logLevel: LoggingInterceptor.LogLevel = LoggingInterceptor.LogLevel.INFO) {
            when (logLevel) {
                LoggingInterceptor.LogLevel.ERROR -> Log.e(tag, msg)
                LoggingInterceptor.LogLevel.WARN  -> Log.w(tag, msg)
                LoggingInterceptor.LogLevel.INFO  -> Log.i(tag, msg)
                LoggingInterceptor.LogLevel.DEBUG -> Log.d(tag, msg)
            }
        }

        @JvmStatic
        fun printJsonRequest(builder: LoggingInterceptor.Builder, request: Request) {

            val tag = builder.getTag(true)
            val hideVerticalLine = builder.hideVerticalLineFlag
            val logLevel = builder.logLevel
            val requestBody = request.body

            val requestString = StringBuilder().apply {

                append("  ")
                        .append(LINE_SEPARATOR)
                        .append(TOP_BORDER)
                        .append(LINE_SEPARATOR)
                        .append(getRequest(request, hideVerticalLine, builder.enableThreadName))

                val header = request.headers.toString()

                if (!header.isLineEmpty()) {
                    if (hideVerticalLine) {
                        append(" Headers:" + LINE_SEPARATOR + dotHeaders(header, hideVerticalLine))
                    } else {
                        append("║ Headers:" + LINE_SEPARATOR + dotHeaders(header))
                    }
                }

                if (requestBody!=null) {

                    val requestBodyString = if (hideVerticalLine) {
                        " $LINE_SEPARATOR Body:$LINE_SEPARATOR"
                    } else {
                        "║ $LINE_SEPARATOR║ Body:$LINE_SEPARATOR"
                    }

                    val bodyString = bodyToString(request).split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    append(requestBodyString + logLines(bodyString, hideVerticalLine))
                }

                append(BOTTOM_BORDER)
            }.toString()

            log(tag, requestString, logLevel)
        }

        @JvmStatic
        fun printFileRequest(builder: LoggingInterceptor.Builder, request: Request) {

            val tag = builder.getTag(true)
            val logLevel = builder.logLevel

            val sb = StringBuilder()
            sb.append("  ").append(LINE_SEPARATOR).append(TOP_BORDER).append(LINE_SEPARATOR)
            sb.append(getRequest(request))

            val requestBody = " " + LINE_SEPARATOR

            val binaryBodyString = binaryBodyToString(request).split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            sb.append(requestBody + logLines(binaryBodyString))
            sb.append(BOTTOM_BORDER)

            log(tag, sb.toString(), logLevel)
        }

        @JvmStatic
        fun printJsonResponse(builder: LoggingInterceptor.Builder, chainMs: Long, isSuccessful: Boolean,
                              code: Int, headers: String, bodyString: String, segments: List<String>) {

            val tag = builder.getTag(false)
            val hideVerticalLine = builder.hideVerticalLineFlag
            val logLevel = builder.logLevel

            val responseString = StringBuilder().apply {
                append("  ").append(LINE_SEPARATOR).append(TOP_BORDER).append(LINE_SEPARATOR)
                append(getResponse(headers, chainMs, code, isSuccessful, segments, hideVerticalLine, builder.enableThreadName))

                val responseBody = if (hideVerticalLine) {
                    " $LINE_SEPARATOR Body:$LINE_SEPARATOR"
                } else {
                    "║ $LINE_SEPARATOR║ Body:$LINE_SEPARATOR"
                }

                val bodyString = getJsonString(bodyString).split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                append(responseBody + logLines(bodyString, hideVerticalLine))

                append(BOTTOM_BORDER)
            }.toString()

            printLog(tag, responseString, logLevel)
        }

        @JvmStatic
        fun printFileResponse(builder: LoggingInterceptor.Builder, chainMs: Long, isSuccessful: Boolean,
                              code: Int, headers: String, segments: List<String>) {

            val tag = builder.getTag(false)
            val logLevel = builder.logLevel

            val sb = StringBuilder()
            sb.append("  ").append(LINE_SEPARATOR).append(TOP_BORDER).append(LINE_SEPARATOR)
            sb.append(getResponse(headers, chainMs, code, isSuccessful, segments))
            sb.append(BOTTOM_BORDER)

            log(tag, sb.toString(), logLevel)
        }

        private fun getRequest(request: Request, hideVerticalLine: Boolean = false, enableThreadName: Boolean = true): String {

            if (hideVerticalLine) {

                return " URL: " + request.url + getDoubleSeparator(hideVerticalLine) + " Method: @" + request.method + getDoubleSeparator(hideVerticalLine) +
                        if (enableThreadName) " Thread: " + Thread.currentThread().name + getDoubleSeparator(hideVerticalLine) else ""
            } else {

                return "║ URL: " + request.url + getDoubleSeparator() + "║ Method: @" + request.method + getDoubleSeparator() +
                        if (enableThreadName) "║ Thread: " + Thread.currentThread().name + getDoubleSeparator() else ""
            }
        }

        private fun getResponse(header: String, tookMs: Long, code: Int, isSuccessful: Boolean,
                                segments: List<String>, hideVerticalLine: Boolean = false, enableThreadName: Boolean = true): String {

            var segmentString: String?

            if (hideVerticalLine) {

                segmentString = " " + slashSegments(segments)

                return (if (!TextUtils.isEmpty(segmentString)) segmentString + " - " else "") + "is success : " + isSuccessful + " - " + "Received in: " + tookMs + "ms" + getDoubleSeparator(hideVerticalLine) + " Status Code: " +
                        code + getDoubleSeparator(hideVerticalLine) +
                        (if (enableThreadName) " Thread: " + Thread.currentThread().name + getDoubleSeparator(hideVerticalLine) else "") +
                        if (header.isLineEmpty()) " " else " Headers:" + LINE_SEPARATOR + dotHeaders(header, hideVerticalLine)
            } else {

                segmentString = "║ " + slashSegments(segments)

                return (if (!TextUtils.isEmpty(segmentString)) segmentString + " - " else "") + "is success : " + isSuccessful + " - " + "Received in: " + tookMs + "ms" + getDoubleSeparator() + "║ Status Code: " +
                        code + getDoubleSeparator() +
                        (if (enableThreadName) "║ Thread: " + Thread.currentThread().name + getDoubleSeparator() else "") +
                        if (header.isLineEmpty()) "║ " else "║ Headers:" + LINE_SEPARATOR + dotHeaders(header)
            }
        }

        private fun slashSegments(segments: List<String>) = StringBuilder().apply {
            for (segment in segments) {
                append("/").append(segment)
            }
        }.toString()

        private fun dotHeaders(header: String, hideVerticalLine: Boolean = false): String {
            val headers = header.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return StringBuilder().apply {
                if (headers != null && headers.isNotEmpty()) {
                    for (item in headers) {
                        if (hideVerticalLine) {
                            append(" - ").append(item).append("\n")
                        } else {
                            append("║ - ").append(item).append("\n")
                        }
                    }
                } else {
                    append(LINE_SEPARATOR)
                }
            }.toString()
        }

        private fun logLines(lines: Array<String>, hideVerticalLine: Boolean = false) = StringBuilder().apply {

            for (line in lines) {
                val lineLength = line.length
                for (i in 0..lineLength / MAX_LONG_SIZE) {
                    val start = i * MAX_LONG_SIZE
                    var end = (i + 1) * MAX_LONG_SIZE
                    end = if (end > line.length) line.length else end

                    if (hideVerticalLine) {
                        append(" " + line.substring(start, end)).append(LINE_SEPARATOR)
                    } else {
                        append("║ " + line.substring(start, end)).append(LINE_SEPARATOR)
                    }
                }
            }
        }.toString()

        private fun bodyToString(request: Request): String {
            try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                if (copy.body == null) return ""

                copy.body?.writeTo(buffer)
                return getJsonString(buffer.readUtf8())
            } catch (e: IOException) {
                return "{\"err\": \"" + e.message + "\"}"
            }
        }

        private fun binaryBodyToString(request: Request): String {

            val copy = request.newBuilder().build()
            val requestBody = copy.body?:return ""

            var buffer: String?
            val contentType = requestBody.contentType()
            if (contentType != null) {
                buffer = "Content-Type: " + contentType.toString()
            } else {
                buffer = ""
            }

            if (requestBody.contentLength() > 0) {
                buffer += LINE_SEPARATOR + "Content-Length: " + requestBody.contentLength()
            }

            if (contentType != null) {

                val contentTypeString = contentType.toString()
                if (contentTypeString.contains("application/x-www-form-urlencoded")) {
                    buffer += LINE_SEPARATOR
                    if (requestBody is FormBody) {
                        val size = requestBody.size
                        for (i in 0 until size) {
                            buffer += requestBody.name(i) + "=" + requestBody.value(i) + "&"
                        }

                        buffer = buffer.take(buffer.length - 1)
                    }
                }
            }

            return buffer
        }

        @JvmStatic
        fun getJsonString(msg: String): String {

            var message: String
            try {
                if (msg.startsWith("{")) {
                    val jsonObject = JSONObject(msg)
                    message = jsonObject.toString(JSON_INDENT)
                } else if (msg.startsWith("[")) {
                    val jsonArray = JSONArray(msg)
                    message = jsonArray.toString(JSON_INDENT)
                } else {
                    message = msg
                }
            } catch (e: JSONException) {
                message = msg
            }

            return message
        }
    }

}