package com.hackday.dmyphotogridview_parkhyerim.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hyerim on 2018. 6. 3....
 */
public class GroupingImageData {
    public final Map<String, ArrayList<ExifImageData>> dailyGroup;
    public final Map<String, ArrayList<ExifImageData>> montlyGroup;
    public final Map<String, ArrayList<ExifImageData>> yearGroup;

    public GroupingImageData(Map<String, ArrayList<ExifImageData>> dailyGroup, Map<String, ArrayList<ExifImageData>> montlyGroup, Map<String, ArrayList<ExifImageData>> yearGroup) {
        this.dailyGroup = dailyGroup;
        this.montlyGroup = montlyGroup;
        this.yearGroup = yearGroup;
    }
}
