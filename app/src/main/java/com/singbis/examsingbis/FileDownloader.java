package com.singbis.examsingbis;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by archie on 5/2/16.
 */
class FileDownloader extends AsyncTask<String, Integer, String> {

    public interface Events {
        public void onProgress(int progress);
        public void Done(String file_url);
    }

    private Events downloadEvents = null;

    public void setEvents(Events events) {
        downloadEvents = events;
    }

    private String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {

        String file_url = filePath + f_url[1];
        try {
            int count;
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();
            // this will be useful so that you can show a tipical 0-100%           progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream
            OutputStream output = new FileOutputStream(file_url);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int)((total*100)/lenghtOfFile));

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
        return file_url;
    }

    /**
     * Updating progress bar
     * */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        // setting progress percentage
        if(downloadEvents != null)
            downloadEvents.onProgress(progress[0]);
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded

        if(downloadEvents != null)
            downloadEvents.Done(file_url);

    }

}
