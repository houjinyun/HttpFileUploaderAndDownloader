package com.hjy.http.upload;


import android.text.TextUtils;

import com.hjy.http.upload.progressaware.ProgressAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by hjy on 7/8/15.<br>
 */
public class FileUploadEngine {

    private FileUploadConfiguration mFileUploadConfiguration;
    private Executor mTaskExecutor;

    /**
     * 如果需要显示上传进度条时，key为ProgressAware.getId()，value为FileUploadInfo.id
     */
    private Map<Integer, String> mCacheKeysForProgressAwares = Collections.synchronizedMap(new HashMap<Integer, String>());

    /**
     * 缓存上传任务
     */
    private List<FileUploadTask> mCacheTasks = new ArrayList<FileUploadTask>();

    private Object mLock = new Object();

    public FileUploadEngine(FileUploadConfiguration configuration) {
        mFileUploadConfiguration = configuration;
        mTaskExecutor = configuration.getTaskExecutor();
    }

    public FileUploadConfiguration getFileUploadConfiguration() {
        return mFileUploadConfiguration;
    }

    /**
     * 提交任务
     *
     * @param task
     */
    public void submit(FileUploadTask task) {
        addTask(task);
        mTaskExecutor.execute(task);
    }

    /**
     * 检查上传任务是否已经存在，如果存在则重设其对应的进度条
     *
     * @param id
     * @param filePath
     * @return
     */
    public boolean isTaskExists(String id, String filePath, ProgressAware progressAware) {
        synchronized (mLock) {
            for(FileUploadTask task : mCacheTasks) {
                FileUploadInfo info = task.getFileUploadInfo();
                if(info.getId().equals(id) && info.getOriginalFilePath().equals(filePath)) {
                    task.resetProgressAware(progressAware);
                    return true;
                }
            }
            return false;
        }
    }

    private void addTask(FileUploadTask task) {
        synchronized (mLock) {
            mCacheTasks.add(task);
        }
    }

    public void removeTask(FileUploadTask info) {
        synchronized (mLock) {
            mCacheTasks.remove(info);
        }
    }

    /**
     * 准备显示进度条进度
     *
     * @param progressAware
     * @param fileUploadInfoId
     */
    public void prepareUpdateProgressTaskFor(ProgressAware progressAware, String fileUploadInfoId) {
        mCacheKeysForProgressAwares.put(progressAware.getId(), fileUploadInfoId);
    }

    /**
     * 取消显示进度条进度
     *
     * @param progressAware
     */
    public void cancelUpdateProgressTaskFor(ProgressAware progressAware) {
        mCacheKeysForProgressAwares.remove(progressAware.getId());
    }

    public String getFileUploadInfoIdForProgressAware(ProgressAware progressAware) {
        return mCacheKeysForProgressAwares.get(progressAware.getId());
    }

    public List<FileUploadTask> getAllTask() {
        synchronized (mLock) {
            List<FileUploadTask> list = new ArrayList<FileUploadTask>();
            list.addAll(mCacheTasks);
            return list;
        }
    }

    public int getTaskCount(String mimeType) {
        synchronized (mLock) {
            if(TextUtils.isEmpty(mimeType))
                return mCacheTasks.size();
            int i = 0;
            for(FileUploadTask data : mCacheTasks) {
                String type = data.getFileUploadInfo().getMimeType();
                if(type == null)
                    type = "";
                if(type.startsWith(mimeType)) {
                    i++;
                }
            }
            return i;
        }
    }

    /**
     * 终止所有上传任务
     */
    public void stop() {
        synchronized (mLock) {
            for(FileUploadTask task : mCacheTasks) {
                task.stopTask();
            }
        }
        mCacheKeysForProgressAwares.clear();
    }

}
