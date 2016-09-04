package com.developer.rimon.zhihudaily.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.developer.rimon.zhihudaily.Constant;
import com.developer.rimon.zhihudaily.entity.News;
import com.developer.rimon.zhihudaily.utils.HttpUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;

public class GetNewsService extends Service {
    private ScheduledExecutorService scheduledExecutorService;

    public GetNewsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String firstId = intent.getStringExtra(Constant.EXTRA_PARAM1);
        //检查新日报
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                HttpUtil.getNews().subscribe(new Subscriber<News>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(News news) {
                        int num = news.stories.size();
                        for (int i = 0; i < num; i++) {
                            if (news.stories.get(i).id.equals(firstId)) {
                                Intent intent = new Intent(Constant.ACTION_SEND);
                                intent.putExtra("NewsNum", i);
                                sendBroadcast(intent);
                                break;
                            }
                        }
                    }
                });
            }
        }, 0, 1, TimeUnit.MINUTES);

        return super.onStartCommand(intent, flags, startId);
    }

    public static void startActionGetNewStory(Context context, String param1) {
        Intent intent = new Intent(context, GetNewsService.class);
        intent.putExtra(Constant.EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    public static void stopActionGetNewStory(Context context) {
        Intent intent = new Intent(context, GetNewsService.class);
        context.stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scheduledExecutorService.shutdown();
    }
}
