package com.developer.rimon.zhihudaily.entity;

import java.util.ArrayList;

/**
 * Created by Rimon on 2016/8/26.
 */
public class Story {

    public String title;
    public String ga_prefix;
    public int type;
    public String id;
    public ArrayList<String> images = new ArrayList<>();
    public String multipic;
    public boolean isClick = false;

    public void setId(String id) {
        this.id = id;
    }

}
