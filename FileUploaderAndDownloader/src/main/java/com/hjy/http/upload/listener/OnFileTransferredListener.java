package com.hjy.http.upload.listener;

/**
 * Created by hjy on 7/9/15.<br>
 */
public interface OnFileTransferredListener {

    /**
     *
     * @param transferred 已经上传的大小
     * @param totalSize 文件总大小
     */
    public void transferred(long transferred, long totalSize);

}
