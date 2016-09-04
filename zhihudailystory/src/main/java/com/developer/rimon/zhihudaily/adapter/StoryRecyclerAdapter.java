package com.developer.rimon.zhihudaily.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.developer.rimon.zhihudaily.Constant;
import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.activity.StoryDetailActivity;
import com.developer.rimon.zhihudaily.entity.Story;
import com.developer.rimon.zhihudaily.entity.TopStory;
import com.developer.rimon.zhihudaily.utils.DateUtil;
import com.developer.rimon.zhihudaily.utils.ImageLoaderUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rimon on 2016/8/26.
 */
public class StoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<Story> storyArrayList;
    private ArrayList<TopStory> topStoryList;

    private int oldPosition = 0;//记录上一次点的位置
    private int currentItem; //当前页面
    private ScheduledExecutorService scheduledExecutorService;
    public ViewPager viewPager;
    private Bundle viewpagerPosition = new Bundle();
    private SharedPreferences isClick;

    public StoryRecyclerAdapter(Context mContext, ArrayList<Story> storyArrayList, ArrayList<TopStory> topStoryList) {
        this.mContext = mContext;
        this.storyArrayList = storyArrayList;
        this.topStoryList = topStoryList;
        isClick = mContext.getSharedPreferences("isClick", Activity.MODE_PRIVATE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case Constant.TYPE_HEADER:
                View headView = inflater.inflate(R.layout.item_story_header, parent, false);
                return new HeaderViewHolder(headView);
            case Constant.TYPE_DATE:
                View dateView = inflater.inflate(R.layout.item_story_date, parent, false);
                return new DateViewHolder(dateView);
            case Constant.TYPE_ITEM:
                View itemView = inflater.inflate(R.layout.item_story, parent, false);
                return new ItemViewHolder(itemView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder viewHolder = (ItemViewHolder) holder;
            final Story story = storyArrayList.get(position - 2);
            if (isClick.getBoolean(story.id, false)) {
                viewHolder.titleText.getPaint().setFakeBoldText(false);
            } else {
                viewHolder.titleText.getPaint().setFakeBoldText(true);
            }
            viewHolder.titleText.setText(story.title);
            if (story.images.size() != 0) {
                ImageLoaderUtils.load(mContext, story.images.get(0), null, null, viewHolder.titleImage);
            }
            viewHolder.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = isClick.edit();
                    editor.putBoolean(story.id, true);
                    editor.apply();
                    viewHolder.titleText.getPaint().setFakeBoldText(false);
                    Intent intent = new Intent(mContext, StoryDetailActivity.class);
                    intent.putExtra(Constant.MAIN_TO_NEWSDETAIL_INTENT_KEY_ID, story.id);
                    mContext.startActivity(intent);
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            HeaderPagerAdapter adapter = new HeaderPagerAdapter(mContext, topStoryList);
            viewPager = viewHolder.viewpager;
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(viewpagerPosition.getInt("position", 0));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    oldPosition = position;
                    currentItem = position;
                    viewpagerPosition.putInt("position", position);
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

        } else if (holder instanceof DateViewHolder) {
            DateViewHolder viewHolder = (DateViewHolder) holder;
            try {
                viewHolder.dateIndicator.setText(position == 1 ? mContext.getString(R.string
                        .main_recycler_indicator_text)
                        : DateUtil.changeFormat(storyArrayList.get(position - 2).id));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return storyArrayList.size() == 0 ? 0 : storyArrayList.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return Constant.TYPE_HEADER;
        } else if (position == 1 || TextUtils.isEmpty(storyArrayList.get(position - 2).title)) {
            return Constant.TYPE_DATE;
        }
        return Constant.TYPE_ITEM;
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof HeaderViewHolder) {
            scheduledExecutorService.shutdownNow();
            Log.e("定时器关闭?", String.valueOf(scheduledExecutorService.isShutdown()));
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder instanceof HeaderViewHolder) {
            Log.e("定时器", "发送");
            startSchedule();
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ViewPager viewpager;

        HeaderViewHolder(View itemView) {
            super(itemView);
            viewpager = (ViewPager) itemView.findViewById(R.id.viewpager);
        }
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateIndicator;
        private ImageView redDotIndicator;

        DateViewHolder(View itemView) {
            super(itemView);
            dateIndicator = (TextView) itemView.findViewById(R.id.date_indicator);
            redDotIndicator = (ImageView) itemView.findViewById(R.id.red_dot_indicator);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        ImageView titleImage;
        CardView cardview;

        ItemViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            titleImage = (ImageView) itemView.findViewById(R.id.title_image);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            viewPager.setCurrentItem(currentItem);
        }
    };

    private void startSchedule() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                currentItem = (currentItem + 1) % topStoryList.size();
                mHandler.obtainMessage().sendToTarget();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

}
