package com.android.customprogressbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private DonutProgress donutProgress;
    private CircleProgress circleProgress;
    private ArcProgress arcProgress;
    private View btnGo;

    @SuppressLint("SdCardPath")
    private static final String filePath = "/sdcard/Download/";
    private static final String URL = "http://192.168.1.99/download/video.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
//        circleProgress = (CircleProgress) findViewById(R.id.circle_progress);
//        arcProgress = (ArcProgress) findViewById(R.id.arc_progress);
        btnGo = findViewById(R.id.btn_go);

        btnGo.setOnClickListener(onClickListener());

        //download file from Internet
        new DownloadTask().execute(URL);
    }

    private View.OnClickListener onClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(filePath);
                intent.setDataAndType(uri, "*/file");
                startActivity(Intent.createChooser(intent, "Open Folder"));
            }
        };
    }

    private class DownloadTask extends AsyncTask<String, String, String> {
        /**
         * Downloading file in background thread
         * */
        @SuppressLint("SdCardPath")
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream to write file
                String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(f_url[0]);
                String fileName = URLUtil.guessFileName(f_url[0], null, fileExtenstion);
                OutputStream output = new FileOutputStream(filePath + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            donutProgress.setProgress(Integer.parseInt(progress[0]));
//            circleProgress.setProgress(Integer.parseInt(progress[0]));
//            arcProgress.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @SuppressLint("SdCardPath")
        @Override
        protected void onPostExecute(String file_url) {

            // dismiss progress bars after the file was downloaded
            btnGo.setVisibility(View.VISIBLE);

            Toast.makeText(MainActivity.this, "Download successful!", Toast.LENGTH_SHORT).show();
        }
    }
}