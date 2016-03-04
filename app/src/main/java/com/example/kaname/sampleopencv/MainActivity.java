 package com.example.kaname.sampleopencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;

 public class MainActivity extends AppCompatActivity {
    private ImageView LenaImage, grayLenaImage;
     private Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LenaImage = (ImageView)findViewById(R.id.image1);
        grayLenaImage = (ImageView)findViewById(R.id.image2);

        //Assetsファイルから画像を取り出す
        try {
            InputStream is = getResources().getAssets().open("lena.jpg");
            bm = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("image", e.getMessage());
        }

        LenaImage.setImageBitmap(bm); //元画像を挿入

        grayLenaImage.setImageBitmap(setBinarization(bm)); //二値化画像を挿入

    }

     /**
      * OpenCVで二値化するプログラム
      * @param binarization 二値化する画像
      * @return dst 二値化した画像をリターン
      */
     public Bitmap setBinarization(Bitmap binarization) {

         if (!OpenCVLoader.initDebug()) {//これないとエラー出る
             // Handle initialization error
             Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
         }

         Mat img = new Mat();
         Utils.bitmapToMat(binarization.copy(Bitmap.Config.ARGB_8888, true), img);

         Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY); //グレースケール
         Imgproc.threshold(img, img, 0, 255, Imgproc.THRESH_OTSU); //二値化

         Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGBA, 4);
         Bitmap dst = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(img, dst);

         return dst;
     }
 }
