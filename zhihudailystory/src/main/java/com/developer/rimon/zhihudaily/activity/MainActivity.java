package com.developer.rimon.zhihudaily.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.developer.rimon.zhihudaily.Constant;
import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.adapter.StoryRecyclerAdapter;
import com.developer.rimon.zhihudaily.entity.News;
import com.developer.rimon.zhihudaily.entity.Story;
import com.developer.rimon.zhihudaily.entity.TopStory;
import com.developer.rimon.zhihudaily.listener.DoubleClick;
import com.developer.rimon.zhihudaily.service.GetNewsService;
import com.developer.rimon.zhihudaily.utils.DateUtil;
import com.developer.rimon.zhihudaily.utils.HttpUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private NewsBroadcastReceiver receiver;
    private ArrayList<Story> storyArrayList = new ArrayList<>();
    private ArrayList<TopStory> topStoryList = new ArrayList<>();
    private StoryRecyclerAdapter storyRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private boolean loadMore = true;
    private boolean isRefreshing = false;
    private int pageNum = 1;
    private int dateViewPosition = 0;
    private int secondDateViewPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.inflateMenu(R.menu.menu_main_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.night_theme:
                        setTheme(R.style.NightTheme);
                        break;
                }
                return true;
            }
        });

        //双击toolbar回到顶部
        DoubleClick.registerDoubleClickListener(toolbar, new DoubleClick.OnDoubleClickListener() {
            @Override
            public void OnSingleClick(View v) {
            }

            @Override
            public void OnDoubleClick(View v) {
                //平滑滚回顶部
                int findFirstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (findFirstVisibleItemPosition < 9) {
                    recycler.smoothScrollToPosition(0);
                } else {
                    recycler.scrollToPosition(10);
                    recycler.smoothScrollToPosition(0);
                }
            }
        });

        receiver = new NewsBroadcastReceiver();
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        storyRecyclerAdapter = new StoryRecyclerAdapter(this, storyArrayList, topStoryList);
        recycler.setAdapter(storyRecyclerAdapter);

        //获取数据
        HttpUtil.getNews().subscribe(new Subscriber<News>() {
            @Override
            public void onCompleted() {
                //开启定时获取新日报服务
                GetNewsService.startActionGetNewStory(MainActivity.this, storyArrayList.get(0).id);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("请求错误", e.toString());
            }

            @Override
            public void onNext(News news) {
                for (int i = 0; i < news.top_stories.size(); i++) {
                    topStoryList.add(news.top_stories.get(i));
                }
                storyArrayList.addAll(news.stories);
                secondDateViewPosition = storyArrayList.size() + 2;
                Log.e("请求完成", news.stories.get(0).title);
                storyRecyclerAdapter.notifyDataSetChanged();
            }
        });
        //动态改变toolbar标题日期，
        changToolbarTitle();
        //滑到底部自动加载更多
        pullToLoadMore();
        //下拉刷新
        pullToRefresh();

    }

    private void changToolbarTitle() {
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
                SimpleDateFormat format2 = new SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA);
                Date currentDate = null;
                //上滑改变日期
                if (storyRecyclerAdapter.getItemViewType(firstVisibleItemPosition) == Constant.TYPE_DATE) {
                    dateViewPosition = firstVisibleItemPosition;
                    try {
                        toolbar.setTitle(firstVisibleItemPosition == 1 ? getString(R.string
                                .main_recycler_indicator_text)
                                : DateUtil.changeFormat(storyArrayList.get(firstVisibleItemPosition - 2).id));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (dateViewPosition == firstVisibleItemPosition + 1) {//下滑改变日期
                    if (dateViewPosition == 1) {
                        toolbar.setTitle(R.string.toolbar_main_title);
                    } else {
                        try {
                            currentDate = format1.parse(storyArrayList.get(dateViewPosition - 2).id);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        toolbar.setTitle(dateViewPosition == secondDateViewPosition ? getString(R.string
                                .main_recycler_indicator_text) : DateUtil.getOtherDateString(currentDate, 1, format2));
                    }
                }
            }
        });
    }

    private void pullToLoadMore() {
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (loadMore && lastVisibleItemPosition == storyArrayList.size() + 1) {
                        loadMore = false;
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                        final String date = DateUtil.getOtherDateString(null, 1 - pageNum, format);
                        HttpUtil.getBeforeNews(date).subscribe(new Subscriber<News>() {
                            @Override
                            public void onCompleted() {
                                loadMore = true;
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("请求历史文章错误", e.toString());
                            }

                            @Override
                            public void onNext(News news) {
                                Log.e("请求历史文章完成", news.stories.get(0).title);

                                Story story = new Story();
                                story.setId(String.valueOf(Integer.parseInt(date) - 1));
                                storyArrayList.add(story);
                                storyArrayList.addAll(news.stories);
                                storyRecyclerAdapter.notifyDataSetChanged();
                                pageNum++;
                            }
                        });
                    }
                }
            }
        });
    }

    private void pullToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    isRefreshing = true;
                    GetNewsService.stopActionGetNewStory(MainActivity.this);
                    HttpUtil.getNews().subscribe(new Subscriber<News>() {
                        @Override
                        public void onCompleted() {
                            toolbar.getMenu().findItem(R.id.notification).setIcon(R.drawable
                                    .ic_notifications_white_24dp);
                            isRefreshing = false;
                            swipeRefreshLayout.setRefreshing(false);
                            GetNewsService.startActionGetNewStory(MainActivity.this, storyArrayList.get(0).id);
                        }

                        @Override
                        public void onError(Throwable e) {
                            isRefreshing = false;
                            swipeRefreshLayout.setRefreshing(false);
                            Log.e("刷新文章错误", e.toString());
                        }

                        @Override
                        public void onNext(News news) {
                            int newStoryNum = 0;
                            for (int i = 0; i < news.stories.size(); i++) {
                                if (news.stories.get(i).id.equals(storyArrayList.get(0).id)) {
                                    newStoryNum = i;
                                    break;
                                }
                            }
                            for (int i = 0; i < newStoryNum; i++) {
                                storyArrayList.add(0, news.stories.get(newStoryNum - 1 - i));
                            }
                            storyRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Constant.ACTION_SEND));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public class NewsBroadcastReceiver extends BroadcastReceiver {

        public NewsBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int storyNum = intent.getIntExtra("NewsNum", 0);
            toolbar.getMenu().findItem(R.id.notification).setIcon(storyNum == 0 ? R.drawable
                    .ic_notifications_white_24dp : R.drawable.ic_notifications_active_white_24dp);
        }
    }
}
