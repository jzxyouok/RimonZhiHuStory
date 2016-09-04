package com.developer.rimon.zhihudaily.utils;

import com.developer.rimon.zhihudaily.APIService;
import com.developer.rimon.zhihudaily.entity.CommentList;
import com.developer.rimon.zhihudaily.entity.News;
import com.developer.rimon.zhihudaily.entity.NewsDetail;
import com.developer.rimon.zhihudaily.entity.StartImage;
import com.developer.rimon.zhihudaily.entity.StoryExtra;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Rimon on 2016/8/27.
 */
public class HttpUtil {
    public static APIService apiService;
    public static String storyBaseUrl = "http://news-at.zhihu.com/api/4/";

    public static Observable<StartImage> getWelcomeImage() {
        return getAPIService().getWelcomeImage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<News> getNews() {
        return getAPIService().getNews()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<NewsDetail> getNewsDetail(String id) {
        return getAPIService().getNewsDetail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<News> getBeforeNews(String date) {
        return getAPIService().getBeforeNews(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<StoryExtra> getStoryExtra(String id) {
        return getAPIService().getStoryExtra(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<CommentList> getLongComment(String id) {
        return getAPIService().getLongComment(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<CommentList> getShortComment(String id) {
        return getAPIService().getShortComment(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static APIService getService(String baseUrl) {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            apiService = retrofit.create(APIService.class);
            return apiService;
        }
        return apiService;
    }

    public static APIService getAPIService() {
        return getService(storyBaseUrl);
    }
}
