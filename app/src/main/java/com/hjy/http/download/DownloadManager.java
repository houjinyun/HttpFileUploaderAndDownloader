package com.hjy.http.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hjy.http.download.listener.OnDownloadProgressListener;
import com.hjy.http.download.listener.OnDownloadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hjy on 5/27/15.<br>
 */
public class DownloadManager {

    private static DownloadManager INSTANCE;

    public static DownloadManager getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new DownloadManager(context);
        }
        return INSTANCE;
    }

    private Context mContext;

    private DownloadConfiguration mDownloadConfiguration;

    /**
     * 保存正在下载url
     */
    private List<FileDownloadTask> mTaskList = new ArrayList<FileDownloadTask>();

    private Map<FileDownloadTask, OnDownloadingListener> mDowndloadingMap = Collections.synchronizedMap(new HashMap<FileDownloadTask, OnDownloadingListener>());
    private Map<FileDownloadTask, OnDownloadProgressListener> mProgressMap = Collections.synchronizedMap(new HashMap<FileDownloadTask, OnDownloadProgressListener>());

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private DownloadManager(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 初始化
     *
     * @param downloadConfiguration
     */
    public synchronized void init(DownloadConfiguration downloadConfiguration) {
        if(downloadConfiguration == null) {
            throw new IllegalArgumentException("DownloadConfiguration can not be null.");
        }
        mDownloadConfiguration = downloadConfiguration;
    }

    private void checkConfiguration() {
        if(mDownloadConfiguration == null) {
            throw new IllegalStateException("Please call init() before use.");
        }
    }

    /**
     * 下载任务是否存在
     *
     * @param id
     * @param url
     * @return
     */
    private boolean isTaskExists(String id, String url) {
        if(id == null)
            id = "";
        for(FileDownloadTask task : mTaskList) {
            FileDownloadInfo downloadInfo = task.getFileDownloadInfo();
            if(id.equals(downloadInfo.getId()) && url.equals(downloadInfo.getUrl())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param type FileType
     * @param id
     * @param url
     * @param downloadingListener
     * @param downloadProgressListener
     */
    public void downloadFile(int type, String id, String url, OnDownloadingListener downloadingListener, OnDownloadProgressListener downloadProgressListener) {
        checkConfiguration();
        synchronized (mTaskList) {
            if(isTaskExists(id, url))
                return;
            File cacheFile = generateCacheFile(url, type);
            FileDownloadInfo downloadInfo = new FileDownloadInfo(id, url, cacheFile, mOnDownloadDispatcher, mOnDwonloadProgressDispatcher);
            FileDownloadTask task = new FileDownloadTask(downloadInfo);
            mTaskList.add(task);
            if(downloadingListener != null)
                mDowndloadingMap.put(task, downloadingListener);
            if(downloadProgressListener != null)
                mProgressMap.put(task, downloadProgressListener);
            mDownloadConfiguration.getTaskExecutor().execute(task);
        }
    }

    public void downloadFileSync(File cacheFile, String id, String url, OnDownloadingListener downloadingListener, OnDownloadProgressListener progressListener) {
        checkConfiguration();
        FileDownloadInfo downloadInfo = new FileDownloadInfo(id, url, cacheFile, downloadingListener, progressListener);
        FileDownloadTask task = new FileDownloadTask(downloadInfo);
        task.run();
    }

    /**
     * 根据url生成缓存的文件名
     *
     * @param url
     * @return
     */
    private String generateCacheName(String url) {
        String name = url.hashCode() + "_" + System.currentTimeMillis();
        return name;
    }

    /**
     * 生成缓存的文件
     *
     * @param url
     * @param type 0-音频，1-视频，2-图片
     * @return
     */
    private File generateCacheFile(String url, int type) {
        File cacheDir = mDownloadConfiguration.getCacheDir();
        if (type == FileType.TYPE_AUDIO) {
            cacheDir = new File(cacheDir.getAbsolutePath() + File.separator + "audio");
        } else if (type == FileType.TYPE_VIDEO) {
            cacheDir = new File(cacheDir.getAbsolutePath() + File.separator + "video");
        } else if (type == FileType.TYPE_IMAGE) {
            cacheDir = new File(cacheDir.getAbsolutePath() + File.separator + "image");
        } else {

        }
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        String name = generateCacheName(url);
        File file = new File(cacheDir, name);
        return file;
    }


    private OnDownloadingListener mOnDownloadDispatcher = new OnDownloadingListener() {
        @Override
        public void onDownloadFailed(final FileDownloadTask downloadInfo, final int errorType, final String msg) {
            final OnDownloadingListener downloadingListener = mDowndloadingMap.get(downloadInfo);
            if(downloadingListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadingListener.onDownloadFailed(downloadInfo, errorType, msg);
                    }
                });
            }
            mDowndloadingMap.remove(downloadInfo);
            mProgressMap.remove(downloadInfo);
            synchronized (mTaskList) {
                mTaskList.remove(downloadInfo);
            }
        }

        @Override
        public void onDownloadSucc(final FileDownloadTask downloadInfo, final File outFile) {
            final OnDownloadingListener downloadingListener = mDowndloadingMap.get(downloadInfo);
            if(downloadingListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadingListener.onDownloadSucc(downloadInfo, outFile);
                    }
                });
            }
            mDowndloadingMap.remove(downloadInfo);
            mProgressMap.remove(downloadInfo);
            synchronized (mTaskList) {
                mTaskList.remove(downloadInfo);
            }
        }
    };

    private OnDownloadProgressListener mOnDwonloadProgressDispatcher = new OnDownloadProgressListener() {
        @Override
        public void onProgressUpdate(final FileDownloadTask fileDownloadInfo, final long current, final long totalSize) {
            final OnDownloadProgressListener progressListener = mProgressMap.get(fileDownloadInfo);
            if (progressListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressListener.onProgressUpdate(fileDownloadInfo, current, totalSize);
                    }
                });
            }
        }

    };

}