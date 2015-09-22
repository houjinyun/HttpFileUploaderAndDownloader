package com.hjy.http.upload.uploader;


import android.text.TextUtils;

import com.hjy.http.CustomHttpClient;
import com.hjy.http.upload.FileUploadInfo;
import com.hjy.http.upload.listener.OnFileTransferredListener;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by hjy on 7/9/15.<br>
 */
public class OKHttpUploader extends BaseUploader {

    @Override
    public String upload(FileUploadInfo fileUploadInfo, OnFileTransferredListener fileTransferredListener) throws IOException {
        final File file = new File(fileUploadInfo.getUploadFilePath());

        MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        Map<String, String> paramMap = fileUploadInfo.getFormParamMap();
        if(paramMap != null && !paramMap.isEmpty()) {
            for(Map.Entry<String, String> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                multipartBuilder.addFormDataPart(key, value);
            }
        }

        String mimeType = fileUploadInfo.getMimeType();
        if(TextUtils.isEmpty(mimeType))
            mimeType = "";
        multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\""),
                RequestBody.create(MediaType.parse(mimeType), file));
        RequestBody multipartBody = multipartBuilder.build();
        RequestBody requestBody = new ProgressRequestBody(multipartBody, fileTransferredListener);

/*        RequestBody requestBody = multipartBuilder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\""),
                new CountingFileRequestBody(file, fileUploadInfo.getMimeType(), fileTransferredListener)).build();*/

        Request request = new Request.Builder()
                .tag(generateTag(fileUploadInfo))
                .url(fileUploadInfo.getUrl())
                .post(requestBody)
                .build();

        Response response = CustomHttpClient.execute(request);
        if(response != null) {
            if (response.isSuccessful()) {
                String respStr = response.body().string();
                return respStr;
            } else {
                throw new IOException(response.toString());
            }
        } else {
            throw new IOException("Cancelled");
        }
    }

    @Override
    public void cancel(FileUploadInfo fileUploadInfo) {
        CustomHttpClient.cancelRequest(generateTag(fileUploadInfo));
    }

    private String generateTag(FileUploadInfo fileUploadInfo) {
        return fileUploadInfo.getId() + fileUploadInfo.getUploadFilePath().hashCode();
    }

}
