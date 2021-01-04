package com.happylee.mydemo.utils.download2;

public interface DownloadListener {
    int DOWNLOAD_ON_PROGRESS = 1;    
    int DOWNLOAD_SUCCESS = 2;
    int DOWNLOAD_FAILED = 3;
    int DOWNLOAD_PAUSED = 4;
    int DOWNLOAD_ON_CANCELED = 5;


    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
