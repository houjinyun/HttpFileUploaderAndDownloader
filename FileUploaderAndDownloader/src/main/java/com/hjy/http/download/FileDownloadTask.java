package com.hjy.http.download;

import android.os.Handler;

import com.hjy.http.CustomHttpClient;
import com.hjy.http.download.listener.OnDownloadProgressListener;
import com.hjy.http.download.listener.OnDownloadingListener;
import com.hjy.http.upload.progressaware.ProgressAware;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hjy on 8/5/15.<br>
 */
public class FileDownloadTask implements Runnable {

    private DownloadManager downloadManager;
    private FileDownloadInfo fileDownloadInfo;
    private OnDownloadingListener downloadingListener;
    private OnDownloadProgressListener progressListener;
    private volatile ProgressAware progressAware;

    private long currSize;
    private long totalSize;

    /**
     * 是否同步加载
     */
    private boolean isSyncLoading = false;

    public FileDownloadTask(FileDownloadInfo fileDownloadInfo, DownloadManager downloadManager, ProgressAware progressAware) {
        this.fileDownloadInfo = fileDownloadInfo;
        downloadingListener = fileDownloadInfo.getOnDownloadingListener();
        progressListener = fileDownloadInfo.getOnDownloadProgressListener();
        this.downloadManager = downloadManager;
        this.progressAware = progressAware;
    }

    public void setSyncLoading(boolean isSyncLoading) {
        this.isSyncLoading = isSyncLoading;
    }

    public boolean isSyncLoading() {
        return isSyncLoading;
    }

    public void resetProgressAware(final ProgressAware progressAware, Handler handler) {
        this.progressAware = progressAware;
        if(progressAware != null) {
            long t = totalSize;
            if(t == 0)
                t = Integer.MAX_VALUE;
            final int progress = (int)((currSize / (float) t) * 100);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressAware.setProgress(progress);
                }
            });
        }
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
                    this.currSize = currentSize;
                    this.totalSize = contentLength;
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

        ProgressAware pa = progressAware;
        if(pa != null) {
            downloadManager.cancelUpdateProgressTaskFor(pa);
        }
    }

    private String generateTag(FileDownloadInfo fileDownloadInfo) {
        return fileDownloadInfo.getId() + fileDownloadInfo.getUrl().hashCode();
    }

    public FileDownloadInfo getFileDownloadInfo() {
        return fileDownloadInfo;
    }

    private boolean isProgressViewCollected(ProgressAware pa) {
        if(pa.isCollected())
            return true;
        return false;
    }

    private boolean isProgressViewReused(ProgressAware pa) {
        String downloadTaskId = downloadManager.getFileDownloadInfoIdForProgressAware(pa);
        if(!fileDownloadInfo.getId().equals(downloadTaskId))
            return true;
        return false;
    }

    public void updateProgress(int progress) {
        ProgressAware pa = progressAware;
        if(pa != null) {
            if(!isProgressViewCollected(pa) && !isProgressViewReused(pa)) {
                pa.setProgress(progress);
            }
        }
    }

}