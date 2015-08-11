package com.hjy.http.download.listener;


import com.hjy.http.download.FileDownloadTask;

/**
 * Created by hjy on 15/5/13.<br>
 */
public interface OnDownloadProgressListener {

    /**
     * @param downloadInfo 下载信息
     * @param current Downloaded size in bytes.
     * @param totalSize Total size in bytes.
     */
    public void onProgressUpdate(FileDownloadTask downloadInfo, long current, long totalSize);

}
