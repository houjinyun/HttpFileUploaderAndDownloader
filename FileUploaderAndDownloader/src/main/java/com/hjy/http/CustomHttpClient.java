package com.hjy.http;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author houjinyun
 */
public class CustomHttpClient {

    private static final int DEFAULT_CONN_TIMEOUT = 30;

    private static final OkHttpClient OK_HTTP_CLIENT;

    static {
        OK_HTTP_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    private static Call newCall(Request request) {
        Call call  = OK_HTTP_CLIENT.newCall(request);
        return call;
    }

    public static void cancelRequest(String tag) {
        if(tag == null)
            return;

        List<Call> list = OK_HTTP_CLIENT.dispatcher().runningCalls();
        if(list != null) {
            for(Call call : list) {
                if(tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
        list = OK_HTTP_CLIENT.dispatcher().queuedCalls();
        if(list != null) {
            for(Call call : list) {
                if(tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }

    public static Response execute(Request request) throws IOException {
        Call call = newCall(request);
        return call.execute();
    }

    public static void executeAsync(Request request, Callback callback) {
        Call call = newCall(request);
        call.enqueue(callback);
    }

    private static Request buildRequest(String url, Map<String, String> headers, String tag) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag);
        if(headers != null) {
            for(Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request req = builder.build();
        return req;
    }

    /**
     * get请求
     *
     * @param url 地址
     * @param headers 头信息
     * @param tag 标识该请求，可用于以后取消
     *
     * @return 返回的字符串
     * @throws IOException
     */
    public static String doGet(String url, Map<String, String> headers,  String tag) throws IOException {
        Response resp = execute(buildRequest(url, headers, tag));
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * 异步get请求
     *
     * @param url 地址
     * @param headers 头信息
     * @param tag 标识该请求，可用于以后取消
     * @param callback 回调
     *
     */
    public static void doGetAsync(String url, Map<String, String> headers, String tag, Callback callback){
        executeAsync(buildRequest(url, headers, tag), callback);
    }

    /**
     * 对于数据量过大的响应body，应使用流的方式来处理body
     *
     * @param url 地址
     * @param headers 头信息
     * @param tag 标识该请求,可用于以后取消
     *
     * @return InputStream
     * @throws IOException
     */
    public static InputStream doGetStream(String url, Map<String, String> headers, String tag) throws IOException {
        Response resp = execute(buildRequest(url, headers, tag));
        if(resp.isSuccessful()) {
            return resp.body().byteStream();
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    private static Request buildFormRequest(String url, Map<String, String> headers,  Map<String, String> paramMap, String tag) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag);
        if(headers != null) {
            for(Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if(paramMap != null) {
            for(Map.Entry<String, String> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                bodyBuilder.add(key, value == null ? "" : value);
            }
        }
        Request req = builder.post(bodyBuilder.build()).build();
        return req;
    }

    /**
     * post请求，表单提交方式
     *
     * @param url 地址
     * @param headers 头信息
     * @param paramMap 参数列表
     * @param tag  标识该请求，可用于以后取消
     *
     * @return 返回字符串
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> headers,  Map<String, String> paramMap, String tag) throws IOException {
        Response resp = execute(buildFormRequest(url, headers, paramMap, tag));
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * 异步post请求，表单提交方式
     *
     * @param url 地址
     * @param headers 头信息
     * @param paramMap 参数列表
     * @param tag  标识该请求，可用于以后取消
     * @param callback 回调
     */
    public static void doPostAsync(String url, Map<String, String> headers,  Map<String, String> paramMap, String tag, Callback callback){
        Request request = buildFormRequest(url, headers, paramMap, tag);
        executeAsync(request, callback);
    }

    private static Request buildJsonRequest(String url, Map<String, String> headers, String postBody, String tag) {
        Request.Builder builder = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), postBody))
                .url(url)
                .tag(tag);
        if(headers != null) {
            for(Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request req = builder.build();
        return req;
    }

    /**
     * post请求, json数据提交
     *
     * @param url 地址
     * @param headers 头信息
     * @param postBody json格式的字符串
     * @param tag 标识该请求，可用于以后取消
     *
     * @return 返回的字符串
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> headers, String postBody, String tag) throws IOException {
        Response resp = execute(buildJsonRequest(url, headers, postBody, tag));
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * 异步post请求, json数据提交
     *
     * @param url 地址
     * @param headers 头信息
     * @param postBody json格式的字符串
     * @param tag 标识该请求，可用于以后取消
     * @param callback 回调
     */
    public static void doPostAsync(String url, Map<String, String> headers, String postBody, String tag, Callback callback) {
        executeAsync(buildJsonRequest(url, headers, postBody, tag), callback);
    }

}