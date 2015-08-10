package com.hjy.http.download;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.hjy.http.upload.DefaultConfigurationFactory;

import java.io.File;
import java.util.concurrent.Executor;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by hjy on 8/10/15.<br>
 */
public class DownloadConfiguration {

    private static final String TAG = "DownloadConfiguration";

    private final Context mContext;
    private final Executor mTaskExecutor;
    private final boolean mIsCustomExecutor;
    private final File mCacheDir;

    private DownloadConfiguration(Builder builder) {
        mContext = builder.context;
        mTaskExecutor = builder.taskExecutor;
        mIsCustomExecutor = builder.isCustomExecutor;
        mCacheDir = builder.cacheDir;
    }

    public File getCacheDir() {
        return mCacheDir;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isCustomExecutor() {
        return mIsCustomExecutor;
    }

    public Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    /**
     * 构造器
     */
    public static class Builder {

        public static final int DEFAULT_THREAD_POOL_CORE_SIZE = 3;
        public static final int DEFAULT_THREAD_POOL_MAX_SIZE = 6;
        public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

        private Context context;
        private Executor taskExecutor;
        private boolean isCustomExecutor;
        private File cacheDir;

        private int threadPoolCoreSize = DEFAULT_THREAD_POOL_CORE_SIZE;
        private int threadPoolMaxSize = DEFAULT_THREAD_POOL_MAX_SIZE;
        private int threadPriority = DEFAULT_THREAD_PRIORITY;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder setCacheDir(File dir) {
            this.cacheDir = dir;
            return this;
        }

        public Builder setTaskExecutor(Executor executor) {
            this.taskExecutor = executor;
            return this;
        }

        public Builder setThreadPoolCoreSize(int size) {
            if(taskExecutor != null) {
                Log.d(TAG, "Call this no use because taskExecutor is not null.");
            }
            this.threadPoolCoreSize = size;
            return this;
        }

        public Builder setThreadPoolMaxSize(int size) {
            if(taskExecutor != null) {
                Log.d(TAG, "Call this no use because taskExecutor is not null.");
            }
            this.threadPoolMaxSize = size;
            return this;
        }

        public Builder setThreadPriority(int priority) {
            if(taskExecutor != null) {
                Log.d(TAG, "Call this no use because taskExecutor is not null.");
            }

            if(priority < Thread.MIN_PRIORITY)
                this.threadPriority = Thread.MIN_PRIORITY;
            else {
                if(priority > Thread.MAX_PRIORITY)
                    this.threadPriority = Thread.MAX_PRIORITY;
                else
                    this.threadPriority = priority;
            }
            return this;
        }

        /**
         * 构建FileUploadConfiguration
         *
         * @return
         */
        public DownloadConfiguration build() {
            initEmptyFieldsWithDefaultValues();
            return new DownloadConfiguration(this);
        }

        /**
         * 用默认值初始化所有没设置的参数
         */
        private void initEmptyFieldsWithDefaultValues() {
            if(taskExecutor == null) {
                taskExecutor = DefaultConfigurationFactory.createExecutor(threadPoolCoreSize, threadPoolMaxSize, threadPriority);
            } else {
                isCustomExecutor = true;
            }
            if(cacheDir == null) {
                cacheDir = getOwnCacheDirectory(context, "Download");
            }
        }

    }

    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == PackageManager.PERMISSION_GRANTED;
    }

}