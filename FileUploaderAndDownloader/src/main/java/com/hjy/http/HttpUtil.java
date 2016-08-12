package com.hjy.http;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Callback;

public class HttpUtil {

    /**
     * 进行get请求
     *
     * @param url     地址
     * @param headers 头信息
     * @param tag     标识该请求
     * @return
     */
    public static String get(String url, Map<String, String> headers, String tag) {
        try {
            String respStr = CustomHttpClient.doGet(url, headers, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行get请求
     *
     * @param url 地址
     * @return
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * 进行get请求, 以流的形式返回
     *
     * @param url     地址
     * @param headers 头信息
     * @param tag     标识该请求
     * @return
     */
    public static InputStream getInputStream(String url, Map<String, String> headers, String tag) {
        try {
            return CustomHttpClient.doGetStream(url, headers, tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getInputStream(String url) {
        return getInputStream(url, null, null);
    }

    /**
     * 进行post请求，提交form表单key-value参数
     *
     * @param url     地址
     * @param headers 头信息
     * @param params  表单参数
     * @param tag     标识该请求
     * @return
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, String> params, String tag) {
        try {
            String respStr = CustomHttpClient.doPost(url, headers, params, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行post请求，提交form表单key-value参数
     *
     * @param url    地址
     * @param params 表单参数
     * @return
     */
    public static String postForm(String url, Map<String, String> params) {
        return postForm(url, null, params, null);
    }

    /**
     * post提交json格式的数据
     *
     * @param url          地址
     * @param headers      头信息
     * @param postJsonBody 提交的json格式数据
     * @param tag          标识该请求
     * @return
     */
    public static String postJson(String url, Map<String, String> headers, String postJsonBody, String tag) {
        try {
            String respStr = CustomHttpClient.doPost(url, headers, postJsonBody, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post提交json格式的数据
     *
     * @param url          地址
     * @param postJsonBody 提交的json格式数据
     * @return
     */
    public static String postJson(String url, String postJsonBody) {
        return postJson(url, null, postJsonBody, null);
    }

    /**
     * 取消某个请求
     *
     * @param tag 标识
     */
    public static void cancelRequest(String tag) {
        CustomHttpClient.cancelRequest(tag);
    }

    /**
     * 异步get请求
     *
     * @param url 地址
     */
    public static void getAsync(String url, Callback callback) {
        CustomHttpClient.doGetAsync(url, null, null, callback);
    }

    public static void postJsonAsync(String url, String postJsonBody, Callback callback) {
        CustomHttpClient.doPostAsync(url, null, postJsonBody, null, callback);
    }

    public static void postFormAsync(String url, Map<String, String> params, Callback callback) {
        CustomHttpClient.doPostAsync(url, null, params, null, callback);
    }
}