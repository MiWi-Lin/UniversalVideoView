package com.universalvideoview;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 〖Author〗 MiWi.LIN ＾＾＾＾〖E-mail〗 80383585@qq.com
 * ======================================================== Copyright(c) 2017 ==
 * 〖Version〗 1.0 <BR/>
 * 〖Date〗 2017/04/20_14:11 <BR/>
 * 〖Desc〗 <BR/>
 * 〖Modify By〗 <BR/>
 */
public class VolumeBrightnessHelper {

    private Context context;
    private ViewGroup root;
    private View onTouchView;


    private RelativeLayout mVolumeBrightnessLayout;
    private ImageView mOperationBg;
    private ImageView mOperationPercent;
    private ImageView mOperationFull;
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mVolume = -1;
    private float mBrightness = -1f;
    private View.OnTouchListener listener;

    public VolumeBrightnessHelper(@NonNull Context context, @NonNull ViewGroup percentParentView, @NonNull View gestureAreaView) {
        this.context = context;
        this.root = percentParentView;
        this.onTouchView = gestureAreaView;
    }

    public void setGestureAreaViewOnTouchListener(View.OnTouchListener listener) {
        this.listener = listener;
    }

    protected void init() {
        LayoutInflater.from(context).inflate(R.layout.uvv_volumn_control_layout, root, true);

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeBrightnessLayout = (RelativeLayout) root.findViewById(R.id.operation_volume_brightness);
        mVolumeBrightnessLayout.setVisibility(View.GONE);
        mOperationBg = (ImageView) root.findViewById(R.id.operation_bg);
        mOperationFull = (ImageView) root.findViewById(R.id.operation_full);
        mOperationPercent = (ImageView) root.findViewById(R.id.operation_percent);
        mGestureDetector = new GestureDetector(context, new VolumeBrightnesGestureListener());
        onTouchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final boolean touchEvent = mGestureDetector.onTouchEvent(motionEvent);
                Log.d("VolumeBrightnessHelper", "touchEvent:" + touchEvent);
                if (touchEvent) {
                    return true;
                }
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (listener != null) {
                            listener.onTouch(onTouchView, motionEvent);
                        }
                        Log.d("VolumeBrightnessHelper", "touchEvent:ACTION_DOWN");
                        mDismissHandler.removeMessages(0);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("VolumeBrightnessHelper", "touchEvent:ACTION_UP");
                        endGesture();
                        break;
                }
                return true;
            }
        });
    }


    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mVolumeBrightnessLayout.setVisibility(View.GONE);
            }
        }
    };

    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
        // 隐藏
        if (mVolumeBrightnessLayout.getVisibility() == View.VISIBLE){
            mDismissHandler.removeMessages(0);
            mDismissHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    private class VolumeBrightnesGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getY();
            Display disp = ((Activity) context).getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();
            //1440 2392
            if (mOldX > windowWidth * 3.0 / 5) {
                onVolumeSlide((mOldY - y) * 3 / windowHeight);
                return true;
            } else if (mOldX < windowWidth * 2.0 / 5.0) {
                onBrightnessSlide((mOldY - y) * 3 / windowHeight);
                return true;
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("VolumeBrightnesGestureL", "onSingleTapConfirmed");
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d("VolumeBrightnesGestureL", "onSingleTapUp");
            onEventUp(e);
            return true;
        }
    }

    private void onEventUp(MotionEvent e) {
        if (listener != null)
            listener.onTouch(onTouchView, e);
        endGesture();
    }


    /**
     * 声音高低
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (listener != null) {
            MotionEvent event = MotionEvent.obtain(1, 1, MotionEvent.ACTION_CANCEL, 1, 1, 1);
            listener.onTouch(onTouchView, event);
        }
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            mOperationBg.setImageResource(R.mipmap.uvv_volumn_bg);
            ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
            lp.height = mOperationFull.getLayoutParams().height * mVolume / mMaxVolume;
            mOperationPercent.setLayoutParams(lp);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;

        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.height = mOperationFull.getLayoutParams().height * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }


    /**
     * 处理屏幕亮暗
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (listener != null) {
            MotionEvent event = MotionEvent.obtain(1, 1, MotionEvent.ACTION_CANCEL, 1, 1, 1);
            listener.onTouch(onTouchView, event);
        }
        final Window window = ((Activity) context).getWindow();
        if (mBrightness < 0) {
            mBrightness = window.getAttributes().screenBrightness;
            if (mBrightness < 0.00f)
                mBrightness = 0.5f;

            mOperationBg.setImageResource(R.mipmap.uvv_brightness_bg);
            ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
            lp.height = (int) (mOperationFull.getLayoutParams().height * mBrightness);
            mOperationPercent.setLayoutParams(lp);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = window.getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.00f)
            lpa.screenBrightness = 0.00f;
        window.setAttributes(lpa);
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.height = (int) (mOperationFull.getLayoutParams().height * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

}