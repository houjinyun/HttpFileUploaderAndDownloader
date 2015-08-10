package com.hjy.http.upload.parser;

/**
 * Created by hjy on 7/17/15.<br>
 */
public class StringResponseParser extends BaseResponseParser {

    /**
     * 默认不处理
     *
     * @param responseStr
     * @return
     */
    @Override
    public ParserResult<String> process(final String responseStr) throws Exception {
        ParserResult<String> result = new ParserResult<String>(responseStr) {
            @Override
            public boolean isSuccessful() {
                return true;
            }

            @Override
            public String getMsg() {
                return null;
            }
        };
        return result;
    }
}
