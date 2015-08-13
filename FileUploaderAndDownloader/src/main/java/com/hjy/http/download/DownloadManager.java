package com.hjy.http.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hjy.http.download.listener.OnDownloadProgressListener;
import com.hjy.http.download.listener.OnDownloadingListener;
import com.hjy.http.upload.progressaware.ProgressAware;

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

    /**
     * 如果需要显示下载进度条时，key为ProgressAware.getId()，value为FileDownloadInfo.id
     */
    private Map<Integer, String> mCacheKeysForProgressAwares = Collections.synchronizedMap(new HashMap<Integer, String>());


    private DownloadManager(Context context) {
        mContext = context.getApplicationContext();
    }

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
     * @param id 任务id
     * @param url 下载地址
     * @return true表示正在下载
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

    public void downloadFile(int type, String id, String url, OnDownloadingListener downloadingListener) {
        downloadFile(type, id, url, downloadingListener,null);
    }

    public void downloadFile(int type, String id, String url, OnDownloadingListener downloadingListener, OnDownloadProgressListener downloadProgressListener) {
        downloadFile(type, id, url, null, downloadingListener, downloadProgressListener);
    }

    /**
     * 下载文件
     *
     * @param type FileType
     * @param id 任务id，自己生成，必须保证唯一
     * @param url 下载地址
     * @param downloadingListener
     * @param downloadProgressListener
     */
    public void downloadFile(int type, String id, String url, ProgressAware progressAware, OnDownloadingListener downloadingListener, OnDownloadProgressListener downloadProgressListener) {
        checkConfiguration();
        synchronized (mTaskList) {
            if(isTaskExists(id, url))
                return;
            File cacheFile = generateCacheFile(url, type);
            FileDownloadInfo downloadInfo = new FileDownloadInfo(id, url, cacheFile, mOnDownloadDispatcher, mOnDwonloadProgressDispatcher);
            FileDownloadTask task = new FileDownloadTask(downloadInfo, this, progressAware);
            mTaskList.add(task);
            if(downloadingListener != null)
                mDowndloadingMap.put(task, downloadingListener);
            if(downloadProgressListener != null)
                mProgressMap.put(task, downloadProgressListener);
            if(progressAware != null) {
                prepareUpdateProgressTaskFor(progressAware, downloadInfo.getId());
            }
            mDownloadConfiguration.getTaskExecutor().execute(task);
        }
    }

    public void prepareUpdateProgressTaskFor(ProgressAware progressAware, String fileDownloadInfoId) {
        mCacheKeysForProgressAwares.put(progressAware.getId(), fileDownloadInfoId);
    }

    public void cancelUpdateProgressTaskFor(ProgressAware progressAware) {
        mCacheKeysForProgressAwares.remove(progressAware.getId());
    }

    public String getFileDownloadInfoIdForProgressAware(ProgressAware progressAware) {
        return mCacheKeysForProgressAwares.get(progressAware.getId());
    }


    public File downloadFileSync(String id, String url) {
        File cacheFile = generateCacheFile(url, FileType.TYPE_OTHER);
        return downloadFileSync(cacheFile, id, url);
    }

    public File downloadFileSync(File cacheFile, String id, String url) {
        return downloadFileSync(cacheFile, id, url, null);
    }

    public File downloadFileSync(File cacheFile, String id, String url, OnDownloadProgressListener progressListener) {
        return downloadFileSync(cacheFile, id, url, null, progressListener);
    }

    /**
     * 同步下载方法
     *
     */
    public File downloadFileSync(File cacheFile, String id, String url, ProgressAware progressAware, OnDownloadProgressListener progressListener) {
        checkConfiguration();
        SyncDownloadLister syncDownloadLister = new SyncDownloadLister();
        FileDownloadInfo downloadInfo = new FileDownloadInfo(id, url, cacheFile, syncDownloadLister, progressListener);
        FileDownloadTask task = new FileDownloadTask(downloadInfo, this, progressAware);
        task.setSyncLoading(true);
        mDowndloadingMap.put(task, syncDownloadLister);
        if(progressListener != null)
            mProgressMap.put(task, progressListener);
        task.run();
        return syncDownloadLister.getResult();
    }

    /**
     * 根据url生成缓存的文件名
     *
     * @param url 下载地址
     * @return 缓存文件名称
     */
    private String generateCacheName(String url) {
        String name = url.hashCode() + "_" + System.currentTimeMillis();
        return name;
    }

    /**
     * 生成缓存的文件
     *
     * @param url 下载url
     * @param type 0-音频，1-视频，2-图片
     * @return 缓存文件
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
            mDowndloadingMap.remove(downloadInfo);
            mProgressMap.remove(downloadInfo);
            synchronized (mTaskList) {
                mTaskList.remove(downloadInfo);
            }

            final OnDownloadingListener downloadingListener = mDowndloadingMap.get(downloadInfo);
            if(downloadingListener != null) {
                if(downloadInfo.isSyncLoading()) {
                    downloadingListener.onDownloadFailed(downloadInfo, errorType, msg);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadingListener.onDownloadFailed(downloadInfo, errorType, msg);
                        }
                    });
                }
            }
        }

        @Override
        public void onDownloadSucc(final FileDownloadTask downloadInfo, final File outFile) {
            mDowndloadingMap.remove(downloadInfo);
            mProgressMap.remove(downloadInfo);
            synchronized (mTaskList) {
                mTaskList.remove(downloadInfo);
            }
            final OnDownloadingListener downloadingListener = mDowndloadingMap.get(downloadInfo);
            if(downloadingListener != null) {
                if(downloadInfo.isSyncLoading()) {
                    downloadingListener.onDownloadSucc(downloadInfo, outFile);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadingListener.onDownloadSucc(downloadInfo, outFile);
                        }
                    });
                }
            }
        }
    };

    private OnDownloadProgressListener mOnDwonloadProgressDispatcher = new OnDownloadProgressListener() {
        @Override
        public void onProgressUpdate(final FileDownloadTask fileDownloadInfo, final long current, final long totalSize) {
            final OnDownloadProgressListener progressListener = mProgressMap.get(fileDownloadInfo);
            if (progressListener != null) {
                long t = totalSize;
                if(t == 0)
                    t = 1;
                final int progress = (int)((current / (float) t) * 100);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDownloadInfo.updateProgress(progress);
                        progressListener.onProgressUpdate(fileDownloadInfo, current, totalSize);
                    }
                });
            }
        }

    };

    private class SyncDownloadLister implements OnDownloadingListener {

        private File result = null;

        @Override
        public void onDownloadFailed(FileDownloadTask task, int errorType, String msg) {

        }

        @Override
        public void onDownloadSucc(FileDownloadTask task, File outFile) {
            result = task.getFileDownloadInfo().getOutFile();
        }

        public File getResult() {
            return result;
        }

    }

}