package com.hjy.http.upload.preprocessor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by hjy on 7/18/15.<br>
 */
public class ImagePreProcessor extends BasePreProcessor{

    private File mCacheDir;
    private int mMaxWidth;
    private int mMaxHeight;

    /**
     *
     * @param cacheDir 缓存目录
     * @param maxWidth 图片最大宽度
     * @param maxHeight 图片最大高度
     */
    public ImagePreProcessor(File cacheDir, int maxWidth, int maxHeight) {
        mCacheDir = cacheDir;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }

    @Override
    public String process(String filePath) {
        return getSmallImg(filePath);
    }


    /**
     * 处理大图
     *
     * @param file
     * @return
     */
    public String getSmallImg(String file) {
        //处理图片小小
        try {
            if(!mCacheDir.exists()) {
                mCacheDir.mkdirs();
            }
            File tmpFile = new File(mCacheDir, generatePhotoName());
            String filePath = file;

            //压缩图片
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            int sampleSize = computeSampleSize(options, Math.min(mMaxWidth, mMaxHeight), mMaxWidth * mMaxHeight);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            //处理图片旋转等问题
            ExifInfo exifInfo = defineExifOrientation(filePath);
            if (exifInfo.rotation != 0 || exifInfo.flipHorizontal) {
                Matrix m = new Matrix();
                // Flip bitmap if need
                boolean flipHorizontal = exifInfo.flipHorizontal;
                int rotation = exifInfo.rotation;
                if (flipHorizontal) {
                    m.postScale(-1, 1);
                }
                // Rotate bitmap if need
                if (rotation != 0) {
                    m.postRotate(rotation);
                }

                try {
                    Bitmap rotateBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                    if(bmp != rotateBmp) {
                        bmp.recycle();
                        bmp = null;
                    }
                    bmp = rotateBmp;
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }

            saveImageToFile(bmp, tmpFile);
            if (bmp != null && !bmp.isRecycled())
                bmp.recycle();
            return tmpFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 随机生成一个图片名字
     *
     * @return
     */
    protected String generatePhotoName() {
        String str = System.currentTimeMillis() + "-" + new Random().nextInt(1000);
        return str;
    }

    /**
     * 获取照片的旋转角度等信息
     *
     * @param file
     * @return
     */
    private static ExifInfo defineExifOrientation(String file) {
        int rotation = 0;
        boolean flip = false;
        try {
            ExifInterface exif = new ExifInterface(file);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    flip = true;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotation = 0;
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ExifInfo(rotation, flip);
    }

    /**
     *
     * 保存图片到文件
     *
     * @param bmp
     * @param file
     * @return true-保存成功
     */
    private static boolean saveImageToFile(Bitmap bmp, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static class ExifInfo {

        public final int rotation;
        public final boolean flipHorizontal;

        protected ExifInfo() {
            this.rotation = 0;
            this.flipHorizontal = false;
        }

        protected ExifInfo(int rotation, boolean flipHorizontal) {
            this.rotation = rotation;
            this.flipHorizontal = flipHorizontal;
        }
    }

    /**
     *
     * @param options
     * @param minSideLength 最小宽或高
     * @param maxNumOfPixels 最大像素
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

}