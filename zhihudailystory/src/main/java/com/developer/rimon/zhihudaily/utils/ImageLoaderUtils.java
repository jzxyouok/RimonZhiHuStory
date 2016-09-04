package com.developer.rimon.zhihudaily.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developer.rimon.zhihudaily.R;

/**
 * Created by Rimon on 2016/8/19.
 */
public class ImageLoaderUtils {

    public static void load(Context context, String url, @Nullable Drawable placeholder, @Nullable Drawable error,
                            ImageView view) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeholder)
                .fitCenter()
                .error(error)
                .into(view);
    }

    public static void load(Context context, String url, int placeholder, ImageView view) {
        if (url.endsWith(".gif")) {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .placeholder(R.drawable.message_image_default)
                    .into(view);

        } else {
            Glide.with(context)
                    .load(url)
                    .placeholder(placeholder)
                    .fitCenter()
                    .into(view);
        }
    }
}
