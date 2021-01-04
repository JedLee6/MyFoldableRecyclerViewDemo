package com.happylee.mydemo.utils.download2;

import android.util.Log;

import com.happylee.mydemo.utils.ThreadPoolSingleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadThread {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener downloadListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    //原先想通过context拿到内部存储下载路径，但这样可能引起内存泄漏，最好直接在execute(Params... params)传入
    public DownloadThread(DownloadListener downloadListener) {
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

    public void downloadFileByThread(String downloadUrl, String downloadDirectory){
        //必须创建子线程来处理下载的任务
        Runnable downloadFileRunnable = new Runnable() {
            @Override
            public void run() {

                InputStream inputStream = null;
                RandomAccessFile randomAccessFile = null;
                File file = null;

                try {
                    long downloadedLength = 0;
                    //通过解析url拿到文件名
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    //存疑，因为Android10以上不能写入外部存储路径，但是内部存储路径必须用context才能拿到
                    //String directory = Environment.getDownloadCacheDirectory().getAbsolutePath();
                    //原先想通过context拿到内部存储下载路径，但这样可能引起内存泄漏，最好直接在execute(Params... params)传入
                    file = new File(downloadDirectory + fileName);
                    if (file.exists()) {
                        downloadedLength = file.length();
                    }
                    long contentLength = getContentLength(downloadUrl);
                    if (contentLength == 0) {
                        handleDownloadStatus(TYPE_FAILED);
                    } else if (contentLength == downloadedLength) {
                        //已下载字节和文件总字节相等，说明已经下载完成了
                        handleDownloadStatus(TYPE_SUCCESS);
                        return;
                    }
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
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
                        while ((length = inputStream.read(byteArray)) != -1) {
                            if (isCanceled) {
                                handleDownloadStatus(TYPE_CANCELED);
                                return;
                            } else if (isPaused) {
                                handleDownloadStatus(TYPE_PAUSE);
                                return;
                            } else {
                                total += length;
                            }
                            randomAccessFile.write(byteArray, 0, length);
                            //计算已下载的百分比
                            int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                            //调用AsyncTask的官方方法，用于更新UI
                            updateProgress(progress);
                        }

                    }
                    response.body().close();
                    handleDownloadStatus(TYPE_SUCCESS);
                    return;
                } catch (Exception e) {
                    Log.d("TAG", e.toString());
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
                handleDownloadStatus(TYPE_FAILED);
                return;
            }
        };
        ThreadPoolSingleton.getInstance().execute(downloadFileRunnable);
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {

                InputStream inputStream = null;
                RandomAccessFile randomAccessFile = null;
                File file = null;

                try {
                    long downloadedLength = 0;
                    //通过解析url拿到文件名
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    //存疑，因为Android10以上不能写入外部存储路径，但是内部存储路径必须用context才能拿到
                    //String directory = Environment.getDownloadCacheDirectory().getAbsolutePath();
                    //原先想通过context拿到内部存储下载路径，但这样可能引起内存泄漏，最好直接在execute(Params... params)传入
                    file = new File(downloadDirectory + fileName);
                    if (file.exists()) {
                        downloadedLength = file.length();
                    }
                    long contentLength = getContentLength(downloadUrl);
                    if (contentLength == 0) {
                        handleDownloadStatus(TYPE_FAILED);
                    } else if (contentLength == downloadedLength) {
                        //已下载字节和文件总字节相等，说明已经下载完成了
                        handleDownloadStatus(TYPE_SUCCESS);
                        return;
                    }
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
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
                        while ((length = inputStream.read(byteArray)) != -1) {
                            if (isCanceled) {
                                handleDownloadStatus(TYPE_CANCELED);
                                return;
                            } else if (isPaused) {
                                handleDownloadStatus(TYPE_PAUSE);
                                return;
                            } else {
                                total += length;
                            }
                            randomAccessFile.write(byteArray, 0, length);
                            //计算已下载的百分比
                            int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                            //调用AsyncTask的官方方法，用于更新UI
                            updateProgress(progress);
                        }

                    }
                    response.body().close();
                    handleDownloadStatus(TYPE_SUCCESS);
                    return;
                } catch (Exception e) {
                    Log.d("TAG", e.toString());
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
                handleDownloadStatus(TYPE_FAILED);
                return;
            }
        }).start();
         */
    }

    private void updateProgress(int progress){
        if (progress > lastProgress) {
            //子线程是不能更新UI的，因此只能通过接口回调主线程更新UI的方法
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    private void handleDownloadStatus(int status) {

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
