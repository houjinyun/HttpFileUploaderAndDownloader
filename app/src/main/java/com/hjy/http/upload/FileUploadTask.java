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

    public FileUploadTask(FileUploadEngine engine, FileUploadInfo fileUploadInfo, ProgressAware progressAware, Handler handler) {
        mFileUploadEngine = engine;
        mFileUploadConfig = engine.getFileUploadConfiguration();
        mFileUploadInfo = fileUploadInfo;
        mProgressAware = progressAware;
        mHandler = handler;
    }

    public FileUploadInfo getFileUploadInfo() {
        return mFileUploadInfo;
    }

    public void resetProgressAware(ProgressAware progressAware) {
        mProgressAware = progressAware;
        if(mProgressAware != null) {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                mProgressAware.setProgress(mCurrProgress);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressAware.setProgress(mCurrProgress);
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

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFileUploadInfo.getApiCallback() != null) {
                    if(mCanceled)
                        mFileUploadInfo.getApiCallback().onError(mFileUploadInfo, ErrorType.ERROR_TYPE_CANCELED, null);
                    else
                        mFileUploadInfo.getApiCallback().onError(mFileUploadInfo, errorType, errorMsg);
                }
                cancelUpdateProgressTask();
            }
        });
    }

    private void fireSuccessEvent(final Object result) {
        removeUploadTask();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mFileUploadInfo.getApiCallback() != null) {
                    mFileUploadInfo.getApiCallback().onSuccess(mFileUploadInfo, result);
                }

                cancelUpdateProgressTask();
            }
        });
    }

    private void fireProgressEvent(final long totalSize, final long currSize, final int progress) {
        if(mFileUploadInfo.getProgressListener() == null && mProgressAware == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mFileUploadInfo.getProgressListener() != null) {
                    mFileUploadInfo.getProgressListener().onProgress(totalSize, currSize, progress);
                }
                if(mProgressAware != null) {
                    if(!isProgressViewCollected() && !isProgressViewReused()) {
                        mProgressAware.setProgress(progress);
                    } else {
                    }
                }
            }
        });
    }

    private boolean isProgressViewCollected() {
        if(mProgressAware.isCollected())
            return true;
        return false;
    }

    private boolean isProgressViewReused() {
        String currentFileUploadId = mFileUploadEngine.getFileUploadInfoIdForProgressAware(mProgressAware);
        if(!mFileUploadInfo.getId().equals(currentFileUploadId))
            return true;
        return false;
    }

    private void cancelUpdateProgressTask() {
        if(mProgressAware != null) {
            if(isProgressViewCollected()) {
                mFileUploadEngine.cancelUpdateProgressTaskFor(mProgressAware);
            } else {
                if(!isProgressViewReused()) {
                    mFileUploadEngine.cancelUpdateProgressTaskFor(mProgressAware);
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