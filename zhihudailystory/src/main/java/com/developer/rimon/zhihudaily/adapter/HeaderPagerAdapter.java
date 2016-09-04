package com.developer.rimon.zhihudaily.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.developer.rimon.zhihudaily.utils.ImageLoaderUtils;
import com.developer.rimon.zhihudaily.R;
import com.developer.rimon.zhihudaily.activity.StoryDetailActivity;
import com.developer.rimon.zhihudaily.entity.TopStory;

import java.util.ArrayList;

/**
 * Created by Rimon on 2016/8/28.
 */
public class HeaderPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<TopStory> topStories;
    private LayoutInflater layoutInflater;

    public HeaderPagerAdapter(Context context, ArrayList<TopStory> topStories) {
        this.mContext = context;
        this.topStories = topStories;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return topStories == null ? 0 : topStories.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = layoutInflater.inflate(R.layout.item_image_viewpager, null);
        TextView title = (TextView) view.findViewById(R.id.viewpager_title);
        ImageView imageView = (ImageView) view.findViewById(R.id.viewpager_image);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.image_indicator_layout);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, StoryDetailActivity.class);
                intent.putExtra("id", topStories.get(position).id);
                mContext.startActivity(intent);
            }
        });

        for (int i = 0; i < topStories.size(); i++) {
            ImageView dot = new ImageView(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                    .WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 5, 5, 5);
            dot.setLayoutParams(layoutParams);
            dot.setImageResource(i == position ? R.drawable.dot_selected : R.drawable.dot);
            linearLayout.addView(dot);
        }
        title.setText(topStories.get(position).title);
        ImageLoaderUtils.load(mContext, topStories.get(position).image, null, null, imageView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
