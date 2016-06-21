package com.prettycamera;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.prettycamera.constant.ActivityResultCode;
import com.prettycamera.constant.PathConstant;
import com.prettycamera.util.FileUtils;
import com.prettycamera.util.PhotoUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by apple on 16/6/21.
 */
public abstract class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            pic_name = savedInstanceState.getString("pic_name");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("pic_name",pic_name);
    }

    private String pic_name;
    public void camera(){
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            // 拍照
            pic_name = cropCameraPhoto(false);
        }
    }


    /**
     * 拍照，返回临时照片地址
     * @return
     */
    private String cropCameraPhoto(boolean isCrop) {
        int MaxLimit = 10;
        int retianMB = FileUtils.freeSpaceOnSd();
        if(retianMB<MaxLimit){
            Toast.makeText(this,"存储空间不足" + MaxLimit + "M，未能获取图片",Toast.LENGTH_SHORT).show();
            return "";
        }
        String tempImage = null;
        try {
            String dir = PathConstant.Image;
            File root = new File(dir);
            if(!root.exists()){
                root.mkdirs();
            }

            tempImage = UUID.randomUUID().toString()+".png";

            File picFile = new File(dir+"/"+tempImage);
            if (!picFile.exists()) {
                picFile.createNewFile();
            }
            Uri photoUri = Uri.fromFile(picFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            if (android.os.Build.VERSION.SDK_INT > 13) {// 4.0以上
                cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);// 低质量
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            if(isCrop){
                startActivityForResult(cameraIntent, ActivityResultCode.CAMERA_CROP_WITH_DATA);
            }else{
                startActivityForResult(cameraIntent, ActivityResultCode.CAMERA_WITH_DATA);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempImage;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            /**
//             *  拍照返回，跳转至裁剪
//             **/
//            case ActivityResultCode.CAMERA_CROP_WITH_DATA: // 拍照回来后裁剪
//                try {
//                    Thread.sleep(100);
//                    if (resultCode == RESULT_OK) {
//
//                        Bitmap bitmap = PhotoUtils.decodeUriAsBitmap(PhotoUtils.getPhotoPath(pic_name), 200);
//                        int degree = PhotoUtils.readPictureDegree(PhotoUtils.getPhotoPath(pic_name));
//                        Matrix matrix = new Matrix();
//                        matrix.setRotate(degree);
//                        // 旋转
//                        int width = bitmap.getWidth();
//                        int height = bitmap.getHeight();
//
//                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//                        PhotoUtils.saveBitmap2file(bitmap, PhotoUtils.getPhotoPath(pic_name));
//                        Uri uri = Uri.fromFile(new File(PhotoUtils.getPhotoPath(pic_name)));
//                        cropImageUri(uri, 250, 250, CrmAppContext.Constant.ActivityResult_CROP_SMALL_PICTURE);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            /**
//             * 裁剪返回
//             **/
//            case ActivityResultCode.CROP_SMALL_PICTURE:
//                if (resultCode == RESULT_OK) {
//                    try {
//                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                        if(PhotoUtils.saveBitmap2file(bitmap, PhotoUtils.getPhotoPath(pic_name))){
////                            if(listener!=null){
////                                fileDALEx = FileDALEx.get().createFileDALEx(pic_name, PhotoUtils.getPhotoPath(pic_name));
////                                listener.onCropCamera(bitmap,fileDALEx);
////                            }
//
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//                break;
            /**
             *  拍照返回
             **/
            case ActivityResultCode.CAMERA_WITH_DATA: // 拍照回来
                try {
                    Thread.sleep(100);
                    if (resultCode == RESULT_OK) {
                        String path = PhotoUtils.getPhotoPath(pic_name);
                        Bitmap bitmap = PhotoUtils.decodeUriAsBitmap(path, 400);
                        int degree = PhotoUtils.readPictureDegree(PhotoUtils.getPhotoPath(pic_name));
                        Matrix matrix = new Matrix();
                        matrix.setRotate(degree);
                        // 旋转
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);


                        PhotoUtils.saveBitmap2file(bitmap, PhotoUtils.getPhotoPath(pic_name));
                        onCamera(PhotoUtils.getPhotoPath(pic_name));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;



        }

    }

    abstract void onCamera(String path);

}
