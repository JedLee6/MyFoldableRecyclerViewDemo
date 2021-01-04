package com.happylee.mydemo.utils.download2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener downloadListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;
    //原先想通过context拿到内部存储下载路径，但这样可能引起内存泄漏，最好直接在execute(Params... params)传入
    public DownloadTask(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream inputStream = null;
        //随机流，支持对文件的读取和写入随机访问
        RandomAccessFile randomAccessFile = null;
        File file = null;

        try {
            long downloadedLength = 0;
            String downloadUrl = params[0];
            //通过解析url拿到文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //存疑，因为Android10以上不能写入外部存储路径，但是内部存储路径必须用context才能拿到
            //String directory = Environment.getDownloadCacheDirectory().getAbsolutePath();
            //原先想通过context拿到内部存储下载路径，但这样可能引起内存泄漏，最好直接在execute(Params... params)传入
            String directory = params[1];
            file = new File(directory + fileName);
            //先查看文件是否存在，存在则先记录已下载的文件大小数值，以便断点续传
            if (file.exists()) {
                downloadedLength = file.length();
            }
            //获得要下载文件的总大小
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                //要下载的文件的大小为0，则无法下载，直接回调下载失败方法
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                //已下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点下载，添加header告诉服务器想从哪个字节开始下载，因为已下载部分不需要再额外下载
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                inputStream = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file, "rw");
                //跳过已下载的字节
                randomAccessFile.seek(downloadedLength);
                byte[] byteArray = new byte[1024];
                int total = 0;
                int length;
                //从输入流中读取一定数量的字节，并将其存储在缓冲区数组byteArray中
                //下载的过程中会不断地运行这个循环，而当用户点击暂停便会将isPaused设置为false所有跳出了循环，实现了中断下载
                while ((length = inputStream.read(byteArray)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSE;
                    } else {
                        total += length;
                    }
                    randomAccessFile.write(byteArray, 0, length);
                    //计算已下载的百分比
                    int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                    //调用AsyncTask的官方方法，用于更新UI
                    publishProgress(progress);
                }

            }
            response.body().close();
            return TYPE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    /**onProgressUpdate()是会在doInBackground()方法中调用publishProgress(progress)方法时被回调*/
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    /**doInBackground()的返回结果作为onPostExecute()的回调形参*/
    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_PAUSE:
                downloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }
    public void cancelDownload() {
        isCanceled = true;
    }
}
