package com.example.lxd.bestservice;

/**
 * Created by lxd on 2017/11/20.
 */

public interface DownloadListener
{
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCancelled();
}
