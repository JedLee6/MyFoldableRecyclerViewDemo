package com.happylee.mydemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.happylee.mydemo.R;
import com.happylee.mydemo.utils.DUtil;
import com.happylee.mydemo.utils.Utils;
import com.happylee.mydemo.utils.download.DownloadManager;
import com.happylee.mydemo.utils.download.callback.DownloadCallback;
import com.happylee.mydemo.utils.download2.DownloadListener;
import com.happylee.mydemo.utils.download2.DownloadTask;
import com.happylee.mydemo.utils.download2.DownloadThread;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadFileActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST = 1;
    private static String APP_NAME = "WeChat";

    private TextView downloadStatusTextView;
    private TextView downloadProgressTextView;
    private ProgressBar downloadProgressBar;
    private Button pauseButton;
    private Button resumeButton;
    private Button cancelButton;
    private Button restartButton;

    private Context context;
    private static final String DOWNLOAD_URL = "https://dldir1.qq.com/weixin/android/weixin7021android1800_arm64.apk";;
    private String downloadDirectory;
    private DownloadManager downloadManager;

    private TextView responseTextView;

    //private DownloadTask downloadTask;
    private DownloadThread downloadTask;

    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == DownloadListener.DOWNLOAD_ON_PROGRESS) {
                //给ProgressBar更新进度条
                downloadProgressBar.setProgress(msg.arg1);
            } else if (msg.what == DownloadListener.DOWNLOAD_PAUSED) {
                Toast.makeText(context, "Download Paused", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            Log.d("TAG", "更新UI的onProgress()方法运行的线程是："+Thread.currentThread().getName());
            //给ProgressBar更新进度条，如果直接写下面这行代码，将导致在子线程中运行UI更新的代码
            //downloadProgressBar.setProgress(progress);
            //新建一个Message，用于存储下载进度这个数值
            Message message = handler.obtainMessage();
            message.what = DOWNLOAD_ON_PROGRESS;
            //把进度条的progress传递给message.arg1
            message.arg1 = progress;
            //发送Message通知主线程的Handler更新UI
            handler.sendMessage(message);
        }

        @Override
        public void onPaused() {
            Log.d("TAG", "onPause()方法运行的线程是:"+Thread.currentThread().getName());
            downloadTask = null;

//            Looper.prepare();
//            Toast.makeText(context, "Download Paused", Toast.LENGTH_SHORT).show();
//            Looper.loop();
            //新建一个Message，用于存储下载进度这个数值
            Message message = handler.obtainMessage();
            message.what = DOWNLOAD_PAUSED;
            //message.arg1 = progress;
            //发送Message通知主线程的Handler弹出Toast
            handler.sendMessage(message);
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            Toast.makeText(context, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            Toast.makeText(context, "Download canceled", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //downloadFile();
                downloadFileByOkHttp();
            } else {
                Toast.makeText(this, "没有文件存储的权限!", Toast.LENGTH_SHORT).show();
            }
        }
        //按道理来讲，这行代码是不需要的
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "下载需要文件存储的权限!", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST);
                }
            } else {
                //downloadFile();
                downloadFileByOkHttp();
            }
        } else {
            //downloadFile();
            //当Android API小于23(即小于Android6)那么不需要申请运行时权限，直接下载文件
            downloadFileByOkHttp();
        }
    }

    private void createThrealPool() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<Runnable>();
        //创建基本的线程池
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, corePoolSize * 2, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
        executorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void downloadFile() {
        downloadManager = DUtil.init(context)
                .url(DOWNLOAD_URL)
                .path(context.getFilesDir() + "/DUtil/")
                .name(APP_NAME + ".apk")
                .childTaskCount(3)
                .build()
                .start(new DownloadCallback() {

                    @Override
                    public void onStart(long currentSize, long totalSize, float progress) {
                        downloadStatusTextView.setText(APP_NAME + "：准备下载中...");
                        downloadProgressBar.setProgress((int) progress);
                        downloadProgressTextView.setText(Utils.formatSize(currentSize) + " / " + Utils.formatSize(totalSize) + "--------" + progress + "%");
                    }

                    @Override
                    public void onProgress(long currentSize, long totalSize, float progress) {
                        downloadStatusTextView.setText(APP_NAME + "：下载中...");
                        downloadProgressBar.setProgress((int) progress);
                        downloadProgressTextView.setText(Utils.formatSize(currentSize) + " / " + Utils.formatSize(totalSize) + "--------" + progress + "%");
                    }

                    @Override
                    public void onPause() {
                        downloadStatusTextView.setText(APP_NAME + "：暂停中...");
                    }

                    @Override
                    public void onCancel() {
                        downloadStatusTextView.setText(APP_NAME + "：已取消...");
                    }

                    @Override
                    public void onFinish(File file) {
                        downloadStatusTextView.setText(APP_NAME + "：下载完成...");
                        Uri uri = Uri.fromFile(file);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        //下载完成后这里闪退了
                        startActivity(intent);
                    }

                    @Override
                    public void onWait() {
                    }

                    @Override
                    public void onError(String error) {
                        downloadStatusTextView.setText(APP_NAME + "：下载出错...");
                    }
                });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.pause(DOWNLOAD_URL);
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.resume(DOWNLOAD_URL);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.cancel(DOWNLOAD_URL);
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.restart(DOWNLOAD_URL);
            }
        });
    }


    private void downloadFileByOkHttp() {
        resumeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (downloadTask == null) {
                    //DOWNLOAD_URL=url;
                    downloadDirectory = getFilesDir().getAbsolutePath();
                    downloadTask = new DownloadThread(downloadListener);
                    //downloadTask = new DownloadTask(downloadListener);
                    //下载url通过DownloadTask的execute()方法传入
                    //downloadTask.execute(DOWNLOAD_URL, downloadDirectory);
                    downloadTask.downloadFileByThread(DOWNLOAD_URL, downloadDirectory);
                    Toast.makeText(DownloadFileActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadTask != null) {
                    downloadTask.pauseDownload();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadTask != null) {
                    downloadTask.cancelDownload();
                } else {
                    if (DOWNLOAD_URL != null) {
                        //取消下载时需将文件删除
                        String fileName = DOWNLOAD_URL.substring(DOWNLOAD_URL.lastIndexOf("/"));
                        String directory = DownloadFileActivity.this.getFilesDir().getAbsolutePath();
                        File file = new File(directory + fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        Toast.makeText(DownloadFileActivity.this, "Download canceled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        context = this;
        downloadStatusTextView = findViewById(R.id.download_status_text_view);
        downloadProgressTextView = findViewById(R.id.download_progress_text_view);
        downloadProgressBar = findViewById(R.id.download_progress_bar);
        pauseButton = findViewById(R.id.pause_button);
        resumeButton = findViewById(R.id.resume_button);
        cancelButton = findViewById(R.id.cancel_button);
        restartButton = findViewById(R.id.restart_button);

        requestPermission();

        responseTextView = findViewById(R.id.response_text_view);
        //sendHttpPost();
        /*
        sendHttpRequestByEnqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {}
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                showResponse(responseString);
            }
        });
        */
        //sendHttpRequest();
    }

    private void sendHttpPost() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username", "admin")
                            .add("password", "123456")
                            .build();
                    Request request = new Request.Builder().url("https://www.baidu.com")
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseString = response.body().string();
                    showResponse(responseString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendHttpRequestByEnqueue(Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://www.baidu.com").build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    private void sendHttpRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://www.baidu.com")
                        .build();
                try {
                    //不在子线程写耗时操作会报错：NetworkOnMainThreadException
                    Response response = okHttpClient.newCall(request).execute();
                    String responseString = response.headers().toString();
                    showResponse(responseString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(String responseString) {
        //如果不加这行限定修饰，该方法体将默认在子线程中修改UI，而这是不允许的
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responseTextView.setText(responseString);
            }
        });
    }
}