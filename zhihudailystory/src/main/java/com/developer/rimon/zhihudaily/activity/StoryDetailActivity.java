package com.developer.rimon.zhihudaily.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.developer.rimon.zhihudaily.Constant;
import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.entity.NewsDetail;
import com.developer.rimon.zhihudaily.entity.StoryExtra;
import com.developer.rimon.zhihudaily.utils.HttpUtil;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class StoryDetailActivity extends AppCompatActivity {

    @BindView(R.id.webView)
    WebView webview;
    @BindView(R.id.comment)
    TextView comment;
    @BindView(R.id.like)
    TextView like;
    @BindView(R.id.story_toolbar)
    Toolbar storyToolbar;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private GestureDetector gestureDetector;
    private String id;
    private int longCommentsCount;
    private int shortCommentsCount;
    private int likeCount;
    private int commentsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);
        ButterKnife.bind(this);

        id = getIntent().getStringExtra(Constant.MAIN_TO_NEWSDETAIL_INTENT_KEY_ID);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        //支持屏幕缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setLoadsImagesAutomatically(true);
        //不显示webview缩放按钮
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
        } else {
            setZoomControlGone(webview);
        }

        //webview的双击事件
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        HttpUtil.getStoryExtra(id).subscribe(new Subscriber<StoryExtra>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(StoryExtra storyExtra) {
                like.setText(String.valueOf(storyExtra.popularity));
                comment.setText(String.valueOf(storyExtra.comments));
                longCommentsCount = storyExtra.long_comments;
                shortCommentsCount = storyExtra.short_comments;
                likeCount = storyExtra.popularity;
                commentsCount = storyExtra.comments;
            }
        });
        HttpUtil.getNewsDetail(id).subscribe(new Subscriber<NewsDetail>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e("请求日报内容错误", e.toString());
            }

            @Override
            public void onNext(NewsDetail newsDetail) {
                webview.loadUrl(newsDetail.share_url);
            }
        });

        storyToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryDetailActivity.this, CommentDetailActivity.class);
                intent.putExtra(Constant.NEWS_DETAIL_TO_COMMENT_DETAIL_INTENT_KEY, id);
                intent.putExtra("long_comments", longCommentsCount);
                intent.putExtra("short_comments", shortCommentsCount);
                intent.putExtra("popularity", likeCount);
                intent.putExtra("comments", commentsCount);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webview.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //实现放大缩小控件隐藏
    public void setZoomControlGone(View view) {
        Class classType;
        Field field;
        try {
            classType = WebView.class;
            field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
            try {
                field.set(view, mZoomButtonsController);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            finish();
            return super.onDoubleTap(e);
        }
    }
}
