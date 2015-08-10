package com.hjy.http.upload.parser;

/**
 * Created by hjy on 7/17/15.<br>
 *
 * 负责解析处理http上传完成后的response数据，根据解析的结果，判断上传是否成功。<br/>
 * 默认采用StringResponseProcessor不解析，直接返回原字符串，使用时需要根据实际的api来决定
 *
 */
public abstract class BaseResponseParser {

    public abstract ParserResult process(String responseStr) throws Exception;

}
