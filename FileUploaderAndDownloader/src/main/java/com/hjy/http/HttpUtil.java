package com.hjy.http;

import java.io.IOException;
import java.util.Map;

public class HttpUtil {

    /**
     * 进行get请求
     *
     * @param url 地址
     * @param tag 标识该请求
     * @return
     */
    public static String get(String url, String tag) {
        try {
            String respStr = CustomHttpClient.doGet(url, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行post请求，提交key-value参数
     *
     * @param url 地址
     * @param params 表单参数
     * @param tag 标识该请求
     * @return
     */
    public static String post(String url, Map<String, String> params, String tag) {
        try {
            String respStr = CustomHttpClient.doPost(url, params, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post提交json格式的数据
     *
     * @param url 地址
     * @param postJsonBody 提交的json格式数据
     * @param tag 标识该请求
     * @return
     */
    public static String post(String url, String postJsonBody, String tag) {
        try {
            String respStr = CustomHttpClient.doPost(url, postJsonBody, tag);
            return respStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取消某个请求
     *
     * @param tag 标识
     */
    public static void cancelRequest(String tag) {
        CustomHttpClient.cancelRequest(tag);
    }
}