package com.hjy.http.upload.progressaware;

import android.view.View;

/**
 * Created by hjy on 7/9/15.<br>
 */
public interface ProgressAware {

    public int getId();

    /**
     * 是否被回收
     *
     * @return
     */
    public boolean isCollected();

    /**
     * 设置进度
     *
     * @param progress
     */
    public boolean setProgress(int progress);

    public View getWrappedView();

    public void setVisibility(int visibility);

}
