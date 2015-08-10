package com.hjy.http.download;

import com.hjy.http.CustomHttpClient;
import com.hjy.http.download.listener.OnDownloadProgressListener;
import com.hjy.http.download.listener.OnDownloadingListener;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by hjy on 8/5/15.<br>
 */
public class FileDownloadTask implements Runnable {

    private FileDownloadInfo fileDownloadInfo;
    private OnDownloadingListener downloadingListener;
    private OnDownloadProgressListener progressListener;

    public FileDownloadTask(FileDownloadInfo fileDownloadInfo) {
        this.fileDownloadInfo = fileDownloadInfo;
        downloadingListener = fileDownloadInfo.getOnDownloadingListener();
        progressListener = fileDownloadInfo.getOnDownloadProgressListener();
    }

    @Override
    public void run() {
        Request req = new Request.Builder()
                .url(fileDownloadInfo.getUrl())
                .tag(generateTag(fileDownloadInfo))
                .build();
        try {
            Response resp = CustomHttpClient.execute(req);
            if(resp.isSuccessful()) {
                ResponseBody body = resp.body();
                long contentLength = body.contentLength();
                InputStream is = body.byteStream();
                FileOutputStream fos = new FileOutputStream(fileDownloadInfo.getOutFile());
                byte[] buffer = new byte[1024];
                int size = 0;
                long currentSize = 0;
                while ((size = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, size);
                    currentSize += size;
                    if(progressListener != null) {
                        progressListener.onProgressUpdate(this, currentSize, contentLength);
                    }
                }
                is.close();
                fos.close();
                if(downloadingListener != null)
                    downloadingListener.onDownloadSucc(this, fileDownloadInfo.getOutFile());
            } else {
                if(downloadingListener != null)
                    downloadingListener.onDownloadFailed(this, DownloadErrorType.ERROR_OTHER, resp.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(downloadingListener != null)
                downloadingListener.onDownloadFailed(this, DownloadErrorType.ERROR_NETWORK, e.getMessage());
        }

    }

    private String generateTag(FileDownloadInfo fileDownloadInfo) {
        return fileDownloadInfo.getId() + fileDownloadInfo.getUrl().hashCode();
    }

    public FileDownloadInfo getFileDownloadInfo() {
        return fileDownloadInfo;
    }
}
