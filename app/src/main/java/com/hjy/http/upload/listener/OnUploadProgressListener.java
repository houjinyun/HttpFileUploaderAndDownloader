package com.hjy.http.upload.listener;

/**
 * Created by hjy on 7/7/15.<br>
 */
public interface OnUploadProgressListener {

    /**
     *
     * @param totalSize 总大小
     * @param currSize 当前已上传的大小
     * @param progress 进度 0-100
     */
    public void onProgress(long totalSize, long currSize, int progress);

}
