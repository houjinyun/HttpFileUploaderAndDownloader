package com.hjy.http.upload.parser;

/**
 * Created by hjy on 7/17/15.<br>
 */
public abstract class ParserResult<T> {

    public T data;

    public ParserResult(T data) {
        this.data = data;
    }

    public abstract boolean isSuccessful();

    public abstract String getMsg();

}
