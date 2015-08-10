package com.hjy.http.download.listener;


import com.hjy.http.download.FileDownloadTask;

import java.io.File;

/**
 * Created by hjy on 15/5/13.<br>
 */
public interface OnDownloadingListener {

    /**
     * 下载失败
     *
     * @param task
     * @param errorType DownloadErrorType
     * @param msg
     */
    public void onDownloadFailed(FileDownloadTask task, int errorType, String msg);

    /**
     * 下载成功
     *
     * @param task
     * @param outFile 下载成功后的文件
     */
    public void onDownloadSucc(FileDownloadTask task, File outFile);

}
