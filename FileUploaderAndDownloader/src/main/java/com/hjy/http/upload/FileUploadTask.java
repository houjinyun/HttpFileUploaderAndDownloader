package com.hjy.http.upload;

import android.os.Handler;
import android.os.Looper;

import com.hjy.http.upload.listener.OnFileTransferredListener;
import com.hjy.http.upload.parser.BaseResponseParser;
import com.hjy.http.upload.parser.ParserResult;
import com.hjy.http.upload.preprocessor.BasePreProcessor;
import com.hjy.http.upload.progressaware.ProgressAware;
import com.hjy.http.upload.uploader.BaseUploader;

import java.io.IOException;

/**
 * Created by hjy on 7/8/15.<br>
 */
public class FileUploadTask implements Runnable {

    private FileUploadEngine mFileUploadEngine;
    private FileUploadConfiguration mFileUploadConfig;
    private FileUploadInfo mFileUploadInfo;
    private Handler mHandler;

    private volatile ProgressAware mProgressAware;

    private int mCurrProgress = 0;

    private volatile boolean mCanceled;

    /**
     * 是否同步加载，默认为false
     */
    private boolean mSyncLoading = false;

    public FileUploadTask(FileUploadEngine engine, FileUploadInfo fileUploadInfo, ProgressAware progressAware, Handler handler) {
        mFileUploadEngine = engine;
        mFileUploadConfig = engine.getFileUploadConfiguration();
        mFileUploadInfo = fileUploadInfo;
        mProgressAware = progressAware;
        mHandler = handler;
    }

    public void setSyncLoading(boolean syncLoading) {
        mSyncLoading = syncLoading;
    }

    public FileUploadInfo getFileUploadInfo() {
        return mFileUploadInfo;
    }

    public void resetProgressAware(final ProgressAware progressAware) {
        mProgressAware = progressAware;
        if(progressAware != null) {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                progressAware.setProgress(mCurrProgress);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressAware.setProgress(mCurrProgress);
                    }
                });
            }
        }
    }

    private OnFileTransferredListener mFileTransferredListener = new OnFileTransferredListener() {

        long tmpTime = 0;

        @Override
        public void transferred(long transferred, long totalSize) {
            if(mCanceled)
                return;

            final long currSize = transferred;
            final int progress = (int)((transferred / (float) totalSize) * 100);
            long now = System.currentTimeMillis();

            //防止频繁刷新，阻塞主线程
            if(now - tmpTime > 100 || progress >= 100) {
                tmpTime = now;
                fireProgressEvent(totalSize, currSize, progress);
            }
            mCurrProgress = progress;
        }
    };

    @Override
    public void run() {
        if(checkCanceled())
            return;

        UploadOptions options = mFileUploadInfo.getUploadOptions();
        if(options != null) {
            BasePreProcessor preProcessor = options.getPreProcessor();
            if(preProcessor != null) {
                String tmpFilePath = preProcessor.process(mFileUploadInfo.getOriginalFilePath());
                mFileUploadInfo.setPreProcessedFile(tmpFilePath);
            }
        }

        BaseUploader fileUploader = mFileUploadConfig.getFileUploader();
        try {
            String respStr = fileUploader.upload(mFileUploadInfo, mFileTransferredListener);

            if(checkCanceled())
                return;

            try {
                BaseResponseParser processor = null;
                if(options != null) {
                    processor = options.getResponseParser();
                }
                if(processor == null) {
                    processor = mFileUploadConfig.getResponseProcessor();
                }

                ParserResult result = processor.process(respStr);
                if(checkCanceled())
                    return;

                if(result.isSuccessful()) {
                    fireSuccessEvent(result.data);
                } else {
                    fireFailEvent(ErrorType.ERROR_TYPE_BUSINESS_LOGIC_ERROR, result.getMsg());
                }
            } catch (Exception e) {
                e.printStackTrace();
                fireFailEvent(ErrorType.ERROR_TYPE_PARSE_DATA_ERROR, e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            fireFailEvent(ErrorType.ERROR_TYPE_IO_ERROR, e.getMessage());
        } catch (Exception e) {
            fireFailEvent(ErrorType.ERROR_TYPE_UNKNOWN, e.getMessage());
        }
    }

    private void fireFailEvent(final int errorType, final String errorMsg) {
        removeUploadTask();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (mFileUploadInfo.getApiCallback() != null) {
                    if (mCanceled)
                        mFileUploadInfo.getApiCallback().onError(mFileUploadInfo, ErrorType.ERROR_TYPE_CANCELED, null);
                    else
                        mFileUploadInfo.getApiCallback().onError(mFileUploadInfo, errorType, errorMsg);
                }
                ProgressAware pa = mProgressAware;
                cancelUpdateProgressTask(pa);
            }
        };

        runTask(task, null);
    }

    private void fireSuccessEvent(final Object result) {
        removeUploadTask();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(mFileUploadInfo.getApiCallback() != null) {
                    mFileUploadInfo.getApiCallback().onSuccess(mFileUploadInfo, result);
                }
                ProgressAware pa = mProgressAware;
                cancelUpdateProgressTask(pa);
            }
        };
        runTask(task, null);
    }

    private void fireProgressEvent(final long totalSize, final long currSize, final int progress) {
        if(mFileUploadInfo.getProgressListener() == null && mProgressAware == null)
            return;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                if(mFileUploadInfo.getProgressListener() != null) {
                    mFileUploadInfo.getProgressListener().onProgress(totalSize, currSize, progress);
                }
                ProgressAware pa = mProgressAware;
                if(pa != null) {
                    if(!isProgressViewCollected(pa) && !isProgressViewReused(pa)) {
                        pa.setProgress(progress);
                    } else {
                    }
                }
            }
        };
        runTask(task, mHandler);
    }

    private void runTask(Runnable task, Handler handler) {
        if(handler != null) {
            handler.post(task);
        } else {
            if(mSyncLoading) {
                task.run();
            } else {
                mHandler.post(task);
            }
        }
    }

    private boolean isProgressViewCollected(ProgressAware progressAware) {
        if(progressAware.isCollected())
            return true;
        return false;
    }

    private boolean isProgressViewReused(ProgressAware progressAware) {
        String currentFileUploadId = mFileUploadEngine.getFileUploadInfoIdForProgressAware(progressAware);
        if(!mFileUploadInfo.getId().equals(currentFileUploadId))
            return true;
        return false;
    }

    private void cancelUpdateProgressTask(ProgressAware progressAware) {
        if(progressAware != null) {
            if(isProgressViewCollected(progressAware)) {
                mFileUploadEngine.cancelUpdateProgressTaskFor(progressAware);
            } else {
                if(!isProgressViewReused(progressAware)) {
                    mFileUploadEngine.cancelUpdateProgressTaskFor(progressAware);
                }
            }
        }
    }

    private void removeUploadTask() {
        mFileUploadEngine.removeTask(this);
    }

    /**
     * 检查是否被取消
     *
     * @return
     */
    private boolean checkCanceled() {
        if(mCanceled) {
            fireFailEvent(ErrorType.ERROR_TYPE_CANCELED, null);
            return true;
        }
        return false;
    }

    public void stopTask() {
        mCanceled = true;
        BaseUploader fileUploader = mFileUploadConfig.getFileUploader();
        fileUploader.cancel(mFileUploadInfo);
    }

}