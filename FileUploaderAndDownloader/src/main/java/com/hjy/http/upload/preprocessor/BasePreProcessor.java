package com.hjy.http.upload.preprocessor;

/**
 * Created by hjy on 7/18/15.<br>
 */
public abstract class BasePreProcessor {

    /**
     * 对要上传的文件，是否还需要做额外的处理
     *
     * @param filePath 原文件
     * @return 处理后需要上传的新的文件路径
     */
    public abstract String process(String filePath);

}
