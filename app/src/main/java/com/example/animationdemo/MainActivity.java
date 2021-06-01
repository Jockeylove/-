package com.example.animationdemo;


import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;

import android.animation.ValueAnimator;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    MyView cv;
    private final String PERMISSION_MSG = "该应用需开启麦克风权限";
    private final int PERMISSION_CODE = 1;
    private final String[] PERMS = {Manifest.permission.RECORD_AUDIO};
    MediaRecorder mMediaRecorder;
    private String filePath = null;
    private Handler mHandler = new Handler();
    ValueAnimator valueAnimator;
    private double lastDb;
    private double currentDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_custom_view);
        cv = findViewById(R.id.main_cv);
        //Android11不再允许生成录音到空设备，需要指定文件生成后再删除
        filePath = getExternalCacheDir().getAbsolutePath() + "/test.3gp";
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(update);
        cancelAnimator();
        File file = new File(filePath);
        file.delete();
    }

    private void checkPermission() {
        if (EasyPermissions.hasPermissions(this, PERMS)) {
            // 已经申请过权限，做想做的事
            startRecord();
        } else {
            // 没有申请过权限，现在去申请
            /**
             *@param host Context对象
             *@param rationale  权限弹窗上的提示语。
             *@param requestCode 请求权限的唯一标识码
             *@param perms 一系列权限
             */
            EasyPermissions.requestPermissions(this, PERMISSION_MSG, PERMISSION_CODE, PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startRecord();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        showDialog();
    }

    public void showDialog() {
        new AppSettingsDialog.Builder(this)
                .setTitle("该功能需要麦克风权限")
                .setRationale("该功能需要麦克风权限，请在设置里面开启！")
                .build()
                .show();
    }

    private void startRecord() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
            updateDb();
        } catch (IOException e) {
            e.printStackTrace();
            showDialog();
        }
    }

    private void stopRecord() {
        if (this.mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
                mMediaRecorder = null;
            }
        }
    }

    private void updateDb() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude();
            if (ratio > 1) {
                currentDb = 20 * Math.log10(ratio);
                if (cv.MinDb == 0)
                    cv.setMinDb((float)currentDb);
                startAnimator();
            }
            lastDb = currentDb;
            mHandler.postDelayed(update, 500);
        }
    }

    Runnable update = new Runnable() {
        @Override
        public void run() {
            updateDb();
        }
    };

    private void startAnimator(){
        cancelAnimator();
        valueAnimator = ValueAnimator.ofFloat((float)lastDb,(float)currentDb);
        int flashRate = (int)getWindowManager().getDefaultDisplay().getRefreshRate();
        //对高刷新率屏幕做了适配
        valueAnimator.setDuration(500 / flashRate * 60);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float db = ((Float)valueAnimator.getAnimatedValue());
                cv.setDb(db);
                if (db > cv.MaxDb)
                    cv.setMaxDb(db);
                if (db < cv.MinDb)
                    cv.setMinDb(db);
            }
        });
        valueAnimator.start();
    }

    private void cancelAnimator() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
    }
}