package com.prettycamera.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.prettycamera.constant.PathConstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PhotoUtils {

    public static String getPhotoPath(String pic_name) {
        return PathConstant.Image + "/" + pic_name;
    }

    /**
     * 通过文件名获取bitmap对象
     * 
     * @param path
     * @param REQUIRED_SIZE
     * @return
     */
    public static Bitmap decodeUriAsBitmap(String path, final int REQUIRED_SIZE) {
        try {
            File f = new File(path);
            if (f.isDirectory()) {
                return null;
            }
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            o = null;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过文件名获取bitmap对象
     * 
     * @param path
     * @param REQUIRED_SIZE
     * @param isRound
     * @return
     */
    public static Bitmap decodeUriAsBitmapListThum(String path, int REQUIRED_SIZE,boolean isRound) {
        try {
            File f = new File(path);
            if (f.isDirectory()) {
                return null;
            }
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            
            // Find the correct scale value. It should be the power of 2.

            // 原图片宽高
            int picWidth = o.outWidth, picHeight = o.outHeight;
            int temWidth = o.outWidth, temHeight = o.outHeight;
            int scale = 1;
            while (true) {
                if (temHeight / 2 < REQUIRED_SIZE || temWidth / 2 < REQUIRED_SIZE)
                    break;
                temWidth /= 2;
                temHeight /= 2;
                scale *= 2;
            }

            // 图片显示高宽
            float realHeight = 0, realWidth = 0;
            // 界面可显示的图片高宽最大、最小值
            float minHeight = 150, maxHeight = 250, minWidth = 150, maxWidth = 240;
            if (picHeight >= picWidth) {// 长图
                if (picHeight <= minHeight) {
                    realHeight = minHeight;
                    realWidth = picWidth * (minHeight / picHeight);
                } else if (picHeight <= maxHeight) {
                    realHeight = picHeight;
                    realWidth = picWidth;
                } else {
                    realHeight = maxHeight;
                    realWidth = picWidth * (maxHeight / picHeight);
                }

            } else {// 宽图
                if (picWidth <= minWidth) {
                    realWidth = minWidth;
                    realHeight = picHeight * (minWidth / picHeight);
                } else if (picWidth <= maxWidth) {
                    realWidth = picWidth;
                    realHeight = picHeight;
                } else {
                    realWidth = maxWidth;
                    realHeight = picHeight * (maxWidth / picWidth);
                }
            }

            o = null;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            if (picHeight < minHeight) {
                bitmap = big(bitmap, minHeight / picHeight);
            } else if (picWidth < minWidth) {
                bitmap = big(bitmap, minWidth / picWidth);
            }
            if(isRound){
            	return	getRoundedCornerBitmap(bitmap);
            }else{
            	return bitmap;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap sourceBitmap) {

        try {

            Bitmap targetBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(),
                    sourceBitmap.getHeight(), Config.RGB_565);

            // 得到画布
            Canvas canvas = new Canvas(targetBitmap);

            // 创建画笔
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // 值越大角度越明显
            float roundPx = 5;
            float roundPy = 5;

            Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
            RectF rectF = new RectF(rect);

            // 绘制
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(sourceBitmap, rect, rect, paint);

            return targetBitmap;

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 读取图片属性：旋转的角度
     * 
     * @param path
     *            图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 保存bitmap到sd卡filePath文件中 如果有，则删除
     * 
     * @param bmp
     *            　bitmap
     * @param filePath
     *            图片名
     * @return
     */
    public static boolean saveBitmap2file(Bitmap bmp, String filePath) {
        if (bmp == null) {
            return false;
        }
        CompressFormat format = CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        File file = new File(filePath);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            stream = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    /***
     * 压缩图片
     * @return
     * @throws FileNotFoundException
     */
    public static void compressBitmapToFile(String path, String outputPath,int maxKb)
    		throws FileNotFoundException,OutOfMemoryError{

        System.out.println("------------------ origion file size = "+new File(path).length() / 1024+"kb");
        /** ------------ 根据图片方向，作旋转处理 ------------ */
        int degree = readPictureDegree(path);
        WeakReference<Bitmap> bitmap = null;
        FileOutputStream fos = null;
        try {


            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            int temWidth = o.outWidth, temHeight = o.outHeight;
            int scale = 1;
            while (true) {
                if (temHeight / 2 < 500 || temWidth / 2 < 500)
                    break;
                temWidth /= 2;
                temHeight /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inInputShareable = false;
//            o2.inPurgeable = true;
//            o2.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            if (android.os.Build.VERSION.SDK_INT < 14) {
//                BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(o2, true);
//            }

            bitmap = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, o2));
            if (null != bitmap.get()) {

                if(degree!=0){
                 // 旋转
                    int width = bitmap.get().getWidth();
                    int height = bitmap.get().getHeight();
                    Matrix matrix = new Matrix();
                    matrix.setRotate(degree);
                    bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(bitmap.get(), 0, 0,width, height, matrix, true));
                    saveBitmap2file(bitmap.get(), outputPath);
                }else{
                    if(!path.equals(outputPath)){
                        FileUtils.fileChannelCopy(new File(path),new File(outputPath));
                    }
                }
                System.out.println("------------------ degree file size = "+new File(outputPath).length() / 1024+"kb");
                int quality = 100;
//                int minQuality = 30;
                while(new File(outputPath).length()>maxKb *1024){
                	fos = new FileOutputStream(outputPath);
                	quality = quality-5;

                    bitmap.get().compress(CompressFormat.JPEG, quality, fos);
                    System.out.println("------------------ bitmap compressFileSize------------"+new File(outputPath).length() / 1024+"kb");
                }

                System.out.println("------------------ bitmap 压缩结束，最终检查生成文件是否存在------------"+new File(outputPath).length() / 1024+"kb");
                System.out.println("------------------ End------------------------");
            }
            
            File outputFile = new File(outputPath);
            if(!outputFile.exists() || outputFile.length()==0){
            	System.out.println("------------------ 压缩结束后文件不存在？重新拷贝一份------------------------");
            	FileUtils.fileChannelCopy(new File(path),new File(outputPath));
            }
            
        }finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bitmap.get() != null && !bitmap.get().isRecycled()){

                bitmap.get().recycle();
                bitmap = null;
            }
        }
    }

    public static Bitmap addWaterMark(Bitmap photo, String string, Typeface typeface) {
        Bitmap newBitmap = photo;
        try {
            if (null == photo || null == string || "".equals(string.trim()))
                return null;
            int height = photo.getHeight();
            if (!newBitmap.isMutable()) {
                newBitmap = copy(photo);
            }
            Canvas canvas = new Canvas(newBitmap);// 初始化画布绘制的图像到icon上
            Paint photoPaint = new Paint(); // 建立画笔
            photoPaint.setDither(true); // 获取跟清晰的图像采样
            photoPaint.setFilterBitmap(true);// 过滤一些

            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
            textPaint.setTextSize(height / 8);// 字体大小
            textPaint.setTypeface(typeface);// 艺术字样式
            textPaint.setColor(Color.YELLOW);// 采用的颜色
            StaticLayout layout =
                    new StaticLayout(string, textPaint, photo.getWidth(), android.text.Layout.Alignment.ALIGN_OPPOSITE,
                            1.0f, 0.0f, true); // 确定换行
            canvas.translate(0, height - layout.getHeight()); // 设定画布位置
            layout.draw(canvas); // 绘制水印
        } catch (Exception e) {
            e.printStackTrace();
            return newBitmap;
        }
        return newBitmap;
    }

    /**
     * @param string 水印字符
     * @param photo 原始bitmap
     */
    public static Bitmap addWaterMark(Bitmap photo, String string) {
        return addWaterMark(photo,string,Typeface.DEFAULT);// 采用默认的字体样式
    }

    public static Bitmap big(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if(bitmap!=resizeBmp &&  bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
        return resizeBmp;
    }

    public static Bitmap bigNoRecycle(Bitmap bitmap, float scale) {
        // 不需要回收bitmap，等待ImageLoader自己回收
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

//    public static FileDALEx makeCompressFile(String uri,String compressFileId) {
//    	return makeCompressFile(uri, compressFileId,80);
//    }
//
//    public static FileDALEx makeCompressFile(String uri,String compressFileId, int maxKB) {
//        File file = new File(uri);
//        if (file == null || !file.exists()) {
////            onToast("文件不存在");
//            return null;
//        }
//
//        // 文件事先不存在
////        String compress_name = UUID.randomUUID().toString();
//        String compress_file = PhotoUtils.getPhotoPath(compressFileId);
//        File directory  = new File(CrmAppContext.PATH);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//        directory = new File(PhotoUtils.getBasePath());
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//        File picFile = new File(compress_file);
//        if (!picFile.exists()) {
//        	try {
//				picFile.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//        }
//
//        FileDALEx fileDalex = null;
//        try {
//			PhotoUtils.compressBitmapToFile(uri, compress_file, maxKB);
//			fileDalex = FileDALEx.get().createFileDALEx(compressFileId,  compress_file);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fileDalex = null;
//		}
//        return fileDalex;
//    }
    
//    public static String cropPhotoid(String photoName){
//    	if(TextUtils.isEmpty(photoName))return "";
//    	String[] pArray = photoName.split("\\.");
//    	if(pArray.length == 1){
//    		//没有后缀名
//    		return photoName;
//    	}else{
//    		//有后缀名
//    		return pArray[pArray.length-2];
//    	}
//    }
//
//    public static String cropPhotoPath(String photoPath){
//    	if(TextUtils.isEmpty(photoPath))return "";
//    	String belongs = "file://";
//		if (photoPath.startsWith(belongs)) {
//			photoPath = photoPath.substring(belongs.length());
//		}
//		return photoPath;
//    }

    /**
     * 根据原位图生成一个新的位图，并将原位图所占空间释放
     *
     * @param srcBmp 原位图
     * @return 新位图
     */
    public static Bitmap copy(Bitmap srcBmp) {
        Bitmap destBmp = null;
        try {
            // 创建一个临时文件
            File file = new File(PathConstant.Image + "temppic/tmp.txt");
            if (file.exists()) {// 临时文件 ， 用一次删一次
                file.delete();
            }
            file.getParentFile().mkdirs();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int width = srcBmp.getWidth();
            int height = srcBmp.getHeight();
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, width * height * 4);
            // 将位图信息写进buffer
            srcBmp.copyPixelsToBuffer(map);
            // 释放原位图占用的空间
            srcBmp.recycle();
            // 创建一个新的位图
            destBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            map.position(0);
            // 从临时缓冲中拷贝位图信息
            destBmp.copyPixelsFromBuffer(map);
            channel.close();
            randomAccessFile.close();
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
            destBmp = null;
            return srcBmp;
        }
        return destBmp;
    }

}