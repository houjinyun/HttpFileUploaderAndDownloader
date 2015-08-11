package com.hjy.http.upload.uploader;

import com.hjy.http.upload.listener.OnFileTransferredListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;

/**
 * Created by hjy on 7/7/15.<br>
 */
public class CountingFileRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048;

    private final File mFile;
    private final String mContentType;
    private final OnFileTransferredListener mListener;
    private long mTotalSize;

    public CountingFileRequestBody(File file, String contentType, OnFileTransferredListener listener) {
        this.mFile = file;
        this.mContentType = contentType;
        this.mListener = listener;
        try {
            mTotalSize = contentLength();
            if(mTotalSize <= 0)
                mTotalSize = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mContentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        okio.Source source = null;
        try {
            source = Okio.source(mFile);
            long total = 0;
            long read;
            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                mListener.transferred(total, mTotalSize);

                //测试用
/*                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*/
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
}
