package com.hjy.http.upload.uploader;

import com.hjy.http.upload.FileUploadInfo;
import com.hjy.http.upload.listener.OnFileTransferredListener;

import java.io.IOException;

/**
 * Created by hjy on 7/9/15.<br>
 */
public abstract class BaseUploader {

    public abstract String upload(FileUploadInfo fileUploadInfo, OnFileTransferredListener fileTransferredListener) throws IOException;

    public abstract void cancel(FileUploadInfo fileUploadInfo);

}
