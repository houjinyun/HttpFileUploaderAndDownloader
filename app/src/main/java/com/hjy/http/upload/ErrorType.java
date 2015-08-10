package com.hjy.http.upload;

/**
 * Created by hjy on 7/7/15.<br>
 */
public class ErrorType {

    public static final int ERROR_TYPE_UNKNOWN = 0;

    //网络连接异常等
    public static final int ERROR_TYPE_IO_ERROR = 1;

    //业务逻辑错误
    public static final int ERROR_TYPE_BUSINESS_LOGIC_ERROR = 2;

    //返回数据解析错误
    public static final int ERROR_TYPE_PARSE_DATA_ERROR = 3;

    //取消
    public static final int ERROR_TYPE_CANCELED = 4;

}
