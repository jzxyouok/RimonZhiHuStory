package com.developer.rimon.zhihudaily.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.developer.rimon.zhihudaily.Constant;
import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.adapter.CommentRecyclerAdapter;
import com.developer.rimon.zhihudaily.entity.Comment;
import com.developer.rimon.zhihudaily.entity.CommentList;
import com.developer.rimon.zhihudaily.utils.HttpUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class CommentDetailActivity extends AppCompatActivity {

    @BindView(R.id.comment_detail_toolbar)
    Toolbar commentDetailToolbar;
    @BindView(R.id.comment_recyclerview)
    RecyclerView commentRecyclerview;
    private String id;
    private ArrayList<Comment> comments = new ArrayList<>();
    private CommentRecyclerAdapter.OnCommentLayoutClickListener onCommentLayoutClickListener;
    private CommentRecyclerAdapter commentAdapter;
    private boolean canLoadShortComment = true;
    private int commentsCount;
    public static int shortCommentsCount;
    public static int longCommentsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commet_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        id = intent.getStringExtra(Constant.MAIN_TO_NEWSDETAIL_INTENT_KEY_ID);
        commentsCount = intent.getIntExtra("comments", 0);
        shortCommentsCount = intent.getIntExtra("short_comments", 0);
        longCommentsCount = intent.getIntExtra("long_comments", 0);
        commentDetailToolbar.setTitle(commentsCount + "条点评");
        commentDetailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commentAdapter = new CommentRecyclerAdapter(this, comments);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        onCommentLayoutClickListener = new CommentRecyclerAdapter.OnCommentLayoutClickListener() {
            @Override
            public void onClick() {
                if (canLoadShortComment) {
                    canLoadShortComment = false;
                    HttpUtil.getShortComment(id).subscribe(new Subscriber<CommentList>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("请求评论错误", e.toString());
                        }

                        @Override
                        public void onNext(CommentList commentList) {
                            commentAdapter.setHeaderAndFooterCount(1);
                            comments.add(new Comment());
                            comments.addAll(commentList.comments);
                            commentAdapter.notifyDataSetChanged();
                            commentRecyclerview.scrollBy(0, layoutManager.findViewByPosition((longCommentsCount + 1))
                                    .getTop());
                        }
                    });
                }
            }
        };
        commentRecyclerview.setAdapter(commentAdapter);
        commentRecyclerview.setLayoutManager(layoutManager);
        commentRecyclerview.setHasFixedSize(true);
        commentAdapter.setOnCommentLayoutClickListener(onCommentLayoutClickListener);

        HttpUtil.getLongComment(id).subscribe(new Subscriber<CommentList>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e("请求评论错误", e.toString());
            }

            @Override
            public void onNext(CommentList commentList) {
                commentAdapter.setFooterViewPosition(commentList.comments.size() + 1);
                comments.addAll(commentList.comments);
                commentAdapter.notifyDataSetChanged();
                Log.e("请求评论成功", commentList.comments.get(0).author + commentList.comments.size());
            }
        });
    }
}
