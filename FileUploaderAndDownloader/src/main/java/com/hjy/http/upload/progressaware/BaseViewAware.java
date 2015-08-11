package com.hjy.http.upload.progressaware;

import android.os.Looper;
import android.view.View;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by hjy on 7/9/15.<br>
 */
public abstract class BaseViewAware implements ProgressAware {

    protected Reference<View> mViewRef;

    public BaseViewAware(View view) {
        mViewRef = new WeakReference<View>(view);
    }

    @Override
    public int getId() {
        View view = mViewRef.get();
        if(view == null)
            return super.hashCode();
        else
            return view.hashCode();
    }

    @Override
    public boolean isCollected() {
        return mViewRef.get() == null;
    }

    @Override
    public boolean setProgress(int progress) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            View view = mViewRef.get();
            if(view != null) {
                setProgress(progress, view);
                return true;
            }
        }
        return false;
    }

    @Override
    public View getWrappedView() {
        return mViewRef.get();
    }

    @Override
    public void setVisibility(int visibility) {
        View view = mViewRef.get();
        if(view != null) {
            view.setVisibility(visibility);
        }
    }

    public abstract void setProgress(int progress, View view);

}
