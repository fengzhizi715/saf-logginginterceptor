package com.safframework.http.interceptor;

import java.util.concurrent.TimeUnit;

import cn.netdiscovery.http.interceptor.LoggingInterceptor;
import okhttp3.OkHttpClient;

public class HttpLoggingInterceptorUtil {

    public static OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(20 * 1000, TimeUnit.MILLISECONDS);
        builder.connectTimeout(15 * 1000, TimeUnit.MILLISECONDS);

        LoggingInterceptor loggingInterceptor = AndroidLoggingInterceptor.build();

        //设置拦截器
        builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }
}
