package com.clickygame

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.clickygame.api.APIService
import com.clickygame.api.EarthviewDetailModel
import com.clickygame.utils.ApiUtils
import com.google.gson.Gson

import java.io.File
import java.io.FileOutputStream

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by prashant.patel on 7/18/2017.
 */

class ActWPList : AppCompatActivity() {
    internal lateinit var metrics: DisplayMetrics
    internal lateinit var wallpaperManager: WallpaperManager
    internal var height = 1920
    internal var width = 1080

    private var mAPIService: APIService? = null
    internal lateinit var fab: FloatingActionButton
    internal lateinit var fabSetWallpaper: FloatingActionButton
    internal lateinit var ivPhoto: ImageView
    internal var bitmapWallpaper: Bitmap? = null

    internal var TAG = "===ActWPList=="
    internal var strPath = "istanbul-turkey-1888.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wplist)
        fab = findViewById(R.id.fab) as FloatingActionButton
        fabSetWallpaper = findViewById(R.id.fabSetWallpaper) as FloatingActionButton
        ivPhoto = findViewById(R.id.ivPhoto) as ImageView


        fab.setOnClickListener { sendPost() }
        fab.setOnLongClickListener {
            val intActInsetDatabase = Intent(this@ActWPList, ActInsertDatabase::class.java)
            //Intent intActInsetDatabase = new Intent(ActWPList.this,GridViewExampleActivity.class);
            startActivity(intActInsetDatabase)
            false
        }

        ivPhoto.setOnClickListener {
            Log.e(TAG, "===image click==sendPost==")
            sendPost()
        }

        fabSetWallpaper.setOnClickListener {
            Log.e(TAG, "===fabSetWallpaper click==sendPost==")
            if (bitmapWallpaper != null) {

                try {

                    setSaveImageWallpaper()

                    /* Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(uri, "image/jpeg");
                        intent.putExtra("mimeType", "image/jpeg");
                        this.startActivity(Intent.createChooser(intent, "Set as:"));

                        //direct set wallpaper
                        wallpaperManager.setBitmap(bitmapWallpaper);
                        wallpaperManager.suggestDesiredDimensions(width, height);

                        Toast.makeText(ActWPList.this, "Wallpaper Set", Toast.LENGTH_SHORT).show();*/

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


        //https://earthview.withgoogle.com/dubai-united-arab-emirates-1779
        mAPIService = ApiUtils.getAPIService()
        sendPost()

        wallpaperManager = WallpaperManager.getInstance(this@ActWPList)
        metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        // get the height and width of screen
        height = metrics.heightPixels
        width = metrics.widthPixels


    }

    private fun setSaveImageWallpaper() {
        try {
            val filename = File("/sdcard/wallpaper.jpg")
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(filename)
                bitmapWallpaper!!.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (out != null) {

                        val uri = Uri.fromFile(File("/sdcard/wallpaper.jpg"))


                        val intent = Intent(Intent.ACTION_ATTACH_DATA)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.setDataAndType(uri, "image/*")
                        intent.putExtra("mimeType", "image/*")
                        this.startActivity(Intent.createChooser(intent, "Set as:"))

                        /*//direct set wallpaper
                    wallpaperManager.setBitmap(bitmapWallpaper);
                    wallpaperManager.suggestDesiredDimensions(width, height);
*/
                        Toast.makeText(this@ActWPList, "Wallpaper Set", Toast.LENGTH_SHORT).show()
                        out.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    internal var earthviewDetailModel: EarthviewDetailModel? = null

    fun sendPost() {


        mAPIService!!.getEarthviewDetail(strPath).enqueue(object : Callback<EarthviewDetailModel> {
            override fun onResponse(call: Call<EarthviewDetailModel>, response: Response<EarthviewDetailModel>) {


                if (response.isSuccessful) {

                    //Log.i(TAG, "post submitted to API." + response.body().toString());

                    if (response != null && response.body() != null) {

                        earthviewDetailModel = response.body()
                        showResponse()
                        Log.w(TAG, "=-Short-RESPONSE=" + Gson().toJson(response))
                        //   Log.e(TAG, "--Full--RESPONSE-->"+new GsonBuilder().setPrettyPrinting().create().toJson(response));
                    }
                }
            }

            override fun onFailure(call: Call<EarthviewDetailModel>, t: Throwable) {
                Log.e(TAG, "Unable to submit post to API.")
            }
        })
    }

    fun showResponse() {
        if (earthviewDetailModel != null) {

            if (earthviewDetailModel!!.nextApi != null) {
                Log.i(TAG, "===Next Page nextApi==" + earthviewDetailModel!!.nextApi)

                Log.i(TAG, "===Next Page url==" + earthviewDetailModel!!.nextApi)

                strPath = earthviewDetailModel!!.nextApi.replace("/_api/", "")
                Log.i(TAG, "===Next Page strPath==" + strPath)

            }

            if (earthviewDetailModel!!.photoUrl != null) {


                Log.i(TAG, "===this Page earthviewDetailModel.photoUrl==" + earthviewDetailModel!!.photoUrl)


                //Loading image from below url into imageView
                /* Glide.with(this)
                        .load(earthviewDetailModel.photoUrl)
                        .placeholder(R.color.colorPlaceholder)
                        .error(R.color.colorError)
                        .fitCenter()
                        .into(ivPhoto);*/

                Glide.with(this).load(earthviewDetailModel!!.photoUrl).asBitmap().into(object : BitmapImageViewTarget(ivPhoto) {
                            override fun setResource(resource: Bitmap) {
                                // Do bitmap magic here
                                bitmapWallpaper = resource
                                super.setResource(resource)
                            }
                        })
            }


            if (earthviewDetailModel!!.photoUrl != null)
                Log.i(TAG, "===photoUrl==" + earthviewDetailModel!!.photoUrl)
        }

    }
}
