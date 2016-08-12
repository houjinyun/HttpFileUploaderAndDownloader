package com.hjy.http.upload.uploader;


import android.text.TextUtils;

import com.hjy.http.CustomHttpClient;
import com.hjy.http.upload.FileUploadInfo;
import com.hjy.http.upload.listener.OnFileTransferredListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hjy on 7/9/15.<br>
 */
public class OKHttpUploader extends BaseUploader {

    @Override
    public String upload(FileUploadInfo fileUploadInfo, OnFileTransferredListener fileTransferredListener) throws IOException {
        final File file = new File(fileUploadInfo.getUploadFilePath());

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        Map<String, String> paramMap = fileUploadInfo.getFormParamMap();
        if(paramMap != null && !paramMap.isEmpty()) {
            for(Map.Entry<String, String> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addFormDataPart(key, value);
            }
        }

        String mimeType = fileUploadInfo.getMimeType();
        if(TextUtils.isEmpty(mimeType))
            mimeType = "";
        builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\""),
                RequestBody.create(MediaType.parse(mimeType), file));

        RequestBody multipartBody = builder.build();
        RequestBody requestBody = new ProgressRequestBody(multipartBody, fileTransferredListener);

        Request request = new Request.Builder()
                .tag(generateTag(fileUploadInfo))
                .url(fileUploadInfo.getUrl())
                .header("Content-Type", fileUploadInfo.getMimeType())
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
