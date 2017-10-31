 package com.example.kaname.sampleopencv;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

 public class MainActivity extends AppCompatActivity {
    private ImageView LenaImage, grayLenaImage, faceLenaImage;
     private Bitmap bm;

     private static final String TAG = MainActivity.class.getSimpleName();
     private String FACE_DATA = "haarcascade_frontalface_default.xml"; //顔認識のためのデータ
     private String APP_PATH = Environment.getExternalStorageDirectory().toString() +"/"+ "SampleOpenCV"; //path
     private String FACE_PATH = APP_PATH + "/" + FACE_DATA;

     private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LenaImage = (ImageView)findViewById(R.id.image1);
        grayLenaImage = (ImageView)findViewById(R.id.image2);
        faceLenaImage = (ImageView)findViewById(R.id.image3);

        //Assetsファイルから画像を取り出す
        try {
            InputStream is = getResources().getAssets().open("lena.jpg");
            bm = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("image", e.getMessage());
        }


        //これないとOpenCVがエラー吐く
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            Log.e(TAG, "Handle initialization error");
        }


        //顔認識のためのmetaデータをローカルへ保存する
        if(PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            RequestPermission();
            return;
        }else{
            LenaImage.setImageBitmap(bm); //元画像を挿入

            grayLenaImage.setImageBitmap(setBinarization(bm)); //二値化画像を挿入

            faceLenaImage.setImageBitmap(setFacialrecognition(bm)); //顔認識した画像を挿入

        }
    }


     /**
      * OpenCVで二値化するプログラム
      * @param original_img 二値化する画像
      * @return dst 二値化した画像をリターン
      */
     private Bitmap setBinarization(Bitmap original_img) {

         Mat img = new Mat();
         Utils.bitmapToMat(original_img.copy(Bitmap.Config.ARGB_8888, true), img);

         Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY); //グレースケール
         Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_OTSU); //二値化

         Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGBA, 4);

         Bitmap binari_img = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(img, binari_img);

         return binari_img;
     }


     /**
      * OpenCVで顔認識をするプログラム
      * @param original_img　元画像
      * @return face_img 顔認識した画像
      */
     private Bitmap setFacialrecognition(Bitmap original_img){

         settingDirectory(); //assetsファイルをローカルへ保存

         Mat img = new Mat();
         Utils.bitmapToMat(original_img.copy(Bitmap.Config.ARGB_8888, true), img);

         //haarcascade_frontalface_default.xmlがローカルにあるか確認
         File metaFile = new File(FACE_PATH);
         if(!metaFile.exists()) Log.e(TAG, "FACEDATA NOT FOUND");

         //顔認識
         MatOfRect faces = new MatOfRect();
         CascadeClassifier faceDetector = new CascadeClassifier(FACE_PATH);
         faceDetector.detectMultiScale(img, faces);

         //顔を四角で囲む
         for (Rect rect : faces.toArray()) {
             Imgproc.rectangle(
                     img,
                     new Point(rect.x, rect.y),
                     new Point(rect.x + rect.width, rect.y + rect.height),
                     new Scalar(255, 0, 0));
         }

         Bitmap face_img = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(img, face_img);

         return face_img;
     }


     /**
      * asetsにあるhaarcascade_frontalface_default.xmlをローカルへ保存する
      */
     private void settingDirectory() {
         //ディレクトリの作成
         File f = new File(APP_PATH);
         if(!f.exists()){
             if(f.mkdirs()) Log.v(TAG ,"ディレクトリの作成に成功");
             else Log.e(TAG , "ディレクトリの作成に失敗");
         }else{
             Log.v(TAG, "すでにディレクトリ作成済み");
         }

         //assetsにあるデータをローカルへコピー
         f = new File(APP_PATH + "/" + FACE_DATA);
         if(!f.exists()) {
             try {
                 //assetsにある顔認識メタデータを取り出す
                 InputStream AssetsFile = getAssets().open(FACE_DATA);
                 //ローカルファイルへコピー
                 OutputStream out = new FileOutputStream(FACE_PATH);
                 byte[] buf = new byte[1024];
                 int len = 0;
                 while ((len = AssetsFile.read(buf)) > 0) out.write(buf, 0, len);

                 AssetsFile.close();
                 out.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }

     /**
      * Permissionを確認するためのAlertDialog表示
      */
     private void RequestPermission() {
         if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
             ActivityCompat.requestPermissions(MainActivity.this,
                     new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                     MY_PERMISSIONS_REQUEST_READ_CONTACTS);
             return;
         }
         //権限の取得
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
     }

     /**
      * permissionの確認
      */
     private void App_OpenSetting() {
         Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
         Uri uri = Uri.fromParts("package", getPackageName(), null);
         intent.setData(uri);
         startActivity(intent);
     }

     @Override
     public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
         switch (requestCode){
             case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                 //許可してくれなかった
                 if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                     new AlertDialog.Builder(this)
                             .setTitle("パーミションの取得エラー")
                             .setMessage("ストレージのパーミッションを取得しないと動きません")
                             .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     App_OpenSetting();
                                 }
                             }).create().show();
                 }else{//許可が降りた時の処理
                     LenaImage.setImageBitmap(bm); //元画像を挿入

                     grayLenaImage.setImageBitmap(setBinarization(bm)); //二値化画像を挿入

                     faceLenaImage.setImageBitmap(setFacialrecognition(bm)); //顔認識をした画像を挿入
                 }
                 break;

             default:
                 super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                 break;
         }
     }
 }
