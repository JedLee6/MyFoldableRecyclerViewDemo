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
                    //通过文件名和下载路径，尝试拿到File对象
                    file = new File(downloadDirectory + fileName);
                    if (file.exists()) {
                        //若File存在，则获取当前File的大小
                        downloadedLength = file.length();
                    }
                    //通过OkHttpClient的response.body().contentLength()拿到要下载文件的总大小
                    long contentLength = getContentLength(downloadUrl);
                    //若网络传输回来的contentLength为0，代表下载URL异常
                    if (contentLength == 0) {
                        handleDownloadStatus(TYPE_FAILED);
                        return;
                    } else if (contentLength == downloadedLength) {
                        //已下载字节和文件总字节相等，说明已经下载完成了
                        handleDownloadStatus(TYPE_SUCCESS);
                        return;
                    }
                    //若代码执行到这，代表文件没下载或者没只下载了一部分
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            //Range: bytes=500- 表示从第 500 字节开始到文件结束部分的内容 
                            .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                            .url(downloadUrl)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    if (response != null) {
                        //在下载文件等场景下，用response.body().byteStream()形式获取输入流时
                        inputStream = response.body().byteStream();
                        //RandomAccessFile对象包含了一个记录指针，用以标识当前读写处的位置，当读/写了n个字节后，文件记录指针将会后移n个字节
                        randomAccessFile = new RandomAccessFile(file, "rw");
                        //跳过已下载的字节，即开启断点续传，void seek(long pos)将文件指针定位到pos位置
                        randomAccessFile.seek(downloadedLength);
                        byte[] byteArray = new byte[1024];
                        int total = 0;
                        int length;
                        //read(byte[] b)从输入流中读取一定数量的字节，并将其存储在缓冲区数组b中,以整数形式返回实际读取的字节数
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
                            //RandomAccessFile.write(int b) 方法将指定字节写入到该文件
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
