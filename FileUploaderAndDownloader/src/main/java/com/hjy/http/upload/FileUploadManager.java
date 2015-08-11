package com.hjy.http.upload;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.hjy.http.upload.listener.OnUploadListener;
import com.hjy.http.upload.listener.OnUploadProgressListener;
import com.hjy.http.upload.progressaware.ProgressAware;

import java.util.Map;

/**
 * Created by hjy on 7/8/15.<br>
 */
public class FileUploadManager {

    private static volatile FileUploadManager INSTANCE;

    public static FileUploadManager getInstance() {
        if(INSTANCE == null) {
            synchronized(FileUploadManager.class) {
                if(INSTANCE == null)
                    INSTANCE = new FileUploadManager();
            }
        }
        return INSTANCE;
    }

    private FileUploadConfiguration mFileUploadConfiguration;
    private FileUploadEngine mFileUploadEngine;
    private Handler mHandler;

    private FileUploadManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 初始化
     *
     * @param fileUploadConfiguration
     */
    public synchronized void init(FileUploadConfiguration fileUploadConfiguration) {
        if(fileUploadConfiguration == null) {
            throw new IllegalArgumentException("FileUploadConfiguration can not be null.");
        }
        mFileUploadConfiguration = fileUploadConfiguration;
        mFileUploadEngine = new FileUploadEngine(fileUploadConfiguration);
    }

    /**
     * 检查是否初始化过
     */
    private void checkConfiguration() {
        if(mFileUploadConfiguration == null) {
            throw new IllegalStateException("Please call init() before use.");
        }
    }

    public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback) {
        uploadFile(paramMap, id, filePath, mimeType, url, apiCallback, null);
    }

    public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, UploadOptions options) {
        uploadFile(paramMap, id, filePath, mimeType, url, apiCallback, null, options);
    }

    public void uploadFile(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, OnUploadProgressListener uploadProgressListener, UploadOptions options) {
        uploadFile(null, paramMap, id, filePath, mimeType, url, apiCallback, options);
    }

    public void uploadFile(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, UploadOptions options) {
        uploadFile(progressAware, paramMap, id, filePath, mimeType, url, apiCallback, null, options);
    }

    /**
     * 提交上传
     *
     * @param progressAware
     * @param paramMap
     * @param id
     * @param filePath
     * @param mimeType
     * @param url
     * @param apiCallback
     * @param uploadProgressListener
     */
    public void uploadFile(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, OnUploadProgressListener uploadProgressListener, UploadOptions options) {
        checkConfiguration();

        if(progressAware != null) {
            mFileUploadEngine.prepareUpdateProgressTaskFor(progressAware, id);
        }
        //是否上传任务已经存在，如果已经存在，则返回
        if(checkUploadTaskExistsAndResetProgressAware(id, filePath, progressAware)) {
            return;
        }
        FileUploadInfo fileUploadInfo = createFileUploadInfo(paramMap, id, filePath, mimeType, url, apiCallback, uploadProgressListener, options);
        FileUploadTask fileUploadTask = new FileUploadTask(mFileUploadEngine, fileUploadInfo, progressAware, mHandler);
        mFileUploadEngine.submit(fileUploadTask);
    }

    public Object uploadFileSync(Map<String, String> paramMap, String id, String filePath, String mimeType, String url) {
        return uploadFileSync(paramMap, id, filePath, mimeType, url, null);
    }

    public Object uploadFileSync(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, UploadOptions options) {
        return uploadFileSync(null, paramMap, id, filePath, mimeType, url, options);
    }

    public Object uploadFileSync(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url, UploadOptions options) {
        return uploadFileSync(progressAware, paramMap, id, filePath, mimeType, url, null, options);
    }

    /**
     * 同步上传文件
     *
     * @param progressAware
     * @param paramMap
     * @param id
     * @param filePath
     * @param mimeType
     * @param url
     * @param uploadProgressListener
     *
     * @return 根据BaseResponseParser解析http response返回的数据，默认是http请求返回的String，为null则表示上传失败了
     */
    public Object uploadFileSync(ProgressAware progressAware, Map<String, String> paramMap, String id, String filePath, String mimeType, String url,  OnUploadProgressListener uploadProgressListener, UploadOptions options) {
        checkConfiguration();
        if(progressAware != null) {
            mFileUploadEngine.prepareUpdateProgressTaskFor(progressAware, id);
        }

        SyncUploadListener listener = new SyncUploadListener();
        FileUploadInfo fileUploadInfo = createFileUploadInfo(paramMap, id, filePath, mimeType, url, listener, uploadProgressListener, options);
        FileUploadTask fileUploadTask = new FileUploadTask(mFileUploadEngine, fileUploadInfo, progressAware, mHandler);
        fileUploadTask.run();
        return listener.getResult();
    }

    /**
     * 创建文件上传信息
     *
     * @param paramMap 表单参数
     * @param id
     * @param filePath 文件路径
     * @param mimeType 例如：image/*
     * @param url
     * @param apiCallback 回调
     * @param uploadProgressListener 上传进度监听
     * @return
     */
    private FileUploadInfo createFileUploadInfo(Map<String, String> paramMap, String id, String filePath, String mimeType, String url, OnUploadListener apiCallback, OnUploadProgressListener uploadProgressListener, UploadOptions options) {
        FileUploadInfo fileUploadInfo = new FileUploadInfo(paramMap, id, filePath, mimeType, url, apiCallback, uploadProgressListener, options);
        return fileUploadInfo;
    }

    /**
     * 检查上传任务是否已经存在，根据"id，文件路径"判断是否是相同的任务
     *
     * @param id
     * @param filePath
     * @return
     */
    private boolean checkUploadTaskExistsAndResetProgressAware(String id, String filePath, ProgressAware progressAware) {
        boolean isExists = mFileUploadEngine.isTaskExists(id, filePath, progressAware);
        return isExists;
    }

    /**
     * 获取任务数
     *
     * @param mimeType 上传文件的类型例如图片：image/*，为null则取全部的
     * @return
     */
    public int getTaskCount(String mimeType) {
        return mFileUploadEngine.getTaskCount(mimeType);
    }

    /**
     * 更新已有上传任务的进度，如果没有则不显示
     *
     * @param id
     * @param filePath
     * @param progressAware
     */
    public void updateProgress(String id, String filePath, ProgressAware progressAware) {
        if(progressAware == null)
            return;
        boolean isExists = mFileUploadEngine.isTaskExists(id, filePath, progressAware);
        //如果不存在
        if(!isExists) {
            progressAware.setVisibility(View.GONE);
        } else {
            mFileUploadEngine.prepareUpdateProgressTaskFor(progressAware, id);
        }
    }

    /**
     * 更新已有上传任务的进度，如果没有则显示默认进度值
     *
     * @param id
     * @param filePath
     * @param progressAware
     * @param defProgress 默认进度 0-100
     */
    public void updateProgress(String id, String filePath, ProgressAware progressAware, int defProgress) {
        if(progressAware == null)
            return;
        boolean isExists = mFileUploadEngine.isTaskExists(id, filePath, progressAware);
        //如果不存在
        if(!isExists) {
            progressAware.setProgress(defProgress);
        } else {
            mFileUploadEngine.prepareUpdateProgressTaskFor(progressAware, id);
        }
    }

    private class SyncUploadListener implements OnUploadListener {

        private Object result;

        @Override
        public void onError(FileUploadInfo uploadData, int errorType, String msg) {
        }

        @Override
        public void onSuccess(FileUploadInfo uploadData, Object data) {
            this.result = data;
        }

        public Object getResult() {
            return result;
        }

    }

}