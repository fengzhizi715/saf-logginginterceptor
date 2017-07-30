# saf-logginginterceptor

用于记录OKHttp网络请求的日志的拦截器，纯Kotlin编写

```java
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(20 * 1000, TimeUnit.MILLISECONDS);
            builder.connectTimeout(15 * 1000, TimeUnit.MILLISECONDS);

            LoggingInterceptor loggingInterceptor = new LoggingInterceptor.Builder()
                    .loggable(BuildConfig.DEBUG)
                    .request()
                    .requestTag("Request")
                    .response()
                    .responseTag("Response")
                    .addHeader("version", BuildConfig.VERSION_NAME)
                    .build();

            //设置拦截器
            builder.addInterceptor(loggingInterceptor);
```