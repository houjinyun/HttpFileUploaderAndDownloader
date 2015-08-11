package com.hjy.http.upload.listener;


import com.hjy.http.upload.FileUploadInfo;

/**
 * Created by hjy on 7/7/15.<br>
 */
public interface OnUploadListener {

    /**
     * 上传失败
     *
     * @param uploadData
     * @param errorType
     * @param msg
     */
    public void onError(FileUploadInfo uploadData, int errorType, String msg);

    /**
     * 上传成功
     *
     * @param uploadData
     * @param data 数据返回的解析结果
     */
    public void onSuccess(FileUploadInfo uploadData, Object data);

}
