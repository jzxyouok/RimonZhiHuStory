package com.developer.rimon.zhihudaily.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.entity.StartImage;
import com.developer.rimon.zhihudaily.utils.HttpUtil;
import com.developer.rimon.zhihudaily.utils.ImageLoaderUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * Created by wenmingvs on 16/5/4.
 */
public class WelcomeActivity extends Activity {

    @BindView(R.id.welcome_image)
    ImageView welcomeImage;
    private Intent mStartIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.bind(this);

        HttpUtil.getWelcomeImage().subscribe(new Subscriber<StartImage>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e("请求错误", e.toString());
            }

            @Override
            public void onNext(StartImage startImage) {
                ImageLoaderUtils.load(WelcomeActivity.this, startImage.getImg(), null, null, welcomeImage);
            }
        });

        mStartIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(Message.obtain());
            }
        }, 3000);
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            startActivity(mStartIntent);
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
