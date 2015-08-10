package com.hjy.http.upload;


import com.hjy.http.upload.listener.OnUploadListener;
import com.hjy.http.upload.listener.OnUploadProgressListener;

import java.util.Map;

/**
 * Created by hjy on 7/8/15.<br>
 */
public class FileUploadInfo {

    private Map<String, String> formParamMap;

    private String id;
    private String filePath;             //要上传的文件路径
    private String mimeType;
    private String url;

    private OnUploadListener apiCallback;
    private OnUploadProgressListener progressListener;

    private UploadOptions uploadOptions;

    private String preProcessedFile;     //上传前对文件预处理后，生成的临时文件

    public FileUploadInfo(Map<String, String> formParamMap, String id, String filePath, String mimeType, String url,
                          OnUploadListener apiCallback, OnUploadProgressListener progressListener, UploadOptions uploadOptions) {
        this.formParamMap = formParamMap;
        this.id = id;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.url = url;
        this.apiCallback = apiCallback;
        this.progressListener = progressListener;
        this.uploadOptions = uploadOptions;
    }

    @Override
    public String toString() {
        return "FileUploadInfo{" +
                "apiCallback=" + apiCallback +
                ", formParamMap=" + formParamMap +
                ", id='" + id + '\'' +
                ", filePath='" + filePath + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", url='" + url + '\'' +
                ", progressListener=" + progressListener +
                ", uploadOptions=" + uploadOptions +
                '}';
    }

    public String getOriginalFilePath() {
        return filePath;
    }

    public String getUploadFilePath() {
        if(preProcessedFile != null && !preProcessedFile.trim().equals("")) {
            return preProcessedFile;
        }
        return filePath;
    }

    public OnUploadListener getApiCallback() {
        return apiCallback;
    }

    public Map<String, String> getFormParamMap() {
        return formParamMap;
    }

    public String getId() {
        return id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setPreProcessedFile(String preProcessedFile) {
        this.preProcessedFile = preProcessedFile;
    }

    public OnUploadProgressListener getProgressListener() {
        return progressListener;
    }

    public UploadOptions getUploadOptions() {
        return uploadOptions;
    }

    public String getUrl() {
        return url;
    }

}
