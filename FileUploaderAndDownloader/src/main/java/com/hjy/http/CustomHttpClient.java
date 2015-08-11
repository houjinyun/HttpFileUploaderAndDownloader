package com.hjy.http;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author houjinyun
 */
public class CustomHttpClient {

    private static final int DEFAULT_CONN_TIMEOUT = 30;

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    static {
        OK_HTTP_CLIENT.setConnectTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS);
        OK_HTTP_CLIENT.setReadTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS);
    }

    private static Call newCall(Request request) {
        Call call  = OK_HTTP_CLIENT.newCall(request);
        return call;
    }

    public static void cancelRequest(String tag) {
        OK_HTTP_CLIENT.cancel(tag);
    }

    public static Response execute(Request request) throws IOException {
        Call call = newCall(request);
        return call.execute();
    }

    public static void executeAsync(Request request, com.squareup.okhttp.Callback callback) {
        Call call = newCall(request);
        call.enqueue(callback);
    }

    /**
     * get请求
     *
     * @param url 地址
     * @param tag 标识该请求，可用于以后取消
     *
     * @return 返回的字符串
     * @throws IOException
     */
    public static String doGet(String url, String tag) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .tag(tag)
                .build();
        Response resp = execute(req);
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * post请求，表单提交方式
     *
     * @param url 地址
     * @param paramMap 参数列表
     * @param tag  标识该请求，可用于以后取消
     *
     * @return 返回字符串
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap, String tag) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag);
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for(Map.Entry<String, String> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            bodyBuilder.add(key, value == null ? "" : value);
        }
        Request req = builder.post(bodyBuilder.build()).build();
        Response resp = execute(req);
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * post请求
     *
     * @param url 地址
     * @param postBody json格式的字符串
     * @param tag 标识该请求，可用于以后取消
     *
     * @return 返回的字符串
     * @throws IOException
     */
    public static String doPost(String url, String postBody, String tag) throws IOException {
        Request.Builder builder = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), postBody))
                .url(url)
                .tag(tag);

        Request req = builder.build();
        Response resp = execute(req);
        if(resp.isSuccessful()) {
            String respStr = resp.body().string();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

}