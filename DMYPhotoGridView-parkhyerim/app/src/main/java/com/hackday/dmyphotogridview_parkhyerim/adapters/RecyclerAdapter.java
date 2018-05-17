package com.hackday.dmyphotogridview_parkhyerim.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.hackday.dmyphotogridview_parkhyerim.R;
import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.util.Collections;
import java.util.List;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ListViewHolder> implements ListPreloader.PreloadSizeProvider<ExifImageData>, ListPreloader.PreloadModelProvider<ExifImageData> {
    private final List<ExifImageData> mData;
    private final RequestBuilder<Drawable> mRequestBuilder;

    private Context mContext;
    private int[] mActualDimensions;
    private int mScreenWidth;

    public RecyclerAdapter(Context context, List<ExifImageData> data, RequestBuilder<Drawable> glideRequests, int screenWidth) {
        mContext = context;
        mData = data;
        mRequestBuilder = glideRequests;
        mScreenWidth = screenWidth;

        setHasStableIds(true);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View view = inflater.inflate(R.layout.recycler_item, viewGroup, false);
        view.getLayoutParams().width = mScreenWidth;

        if (mActualDimensions == null) {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mActualDimensions == null) {
                        mActualDimensions = new int[]{view.getWidth(), view.getWidth()};
                    }
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
        }

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder viewHolder, int position) {
        ExifImageData current = mData.get(position);

        mRequestBuilder
                .clone()
                .apply(new RequestOptions().override(300,300).centerCrop())
                .load(current.uri)
                .into(viewHolder.image);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).rowId;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public List<ExifImageData> getPreloadItems(int position) {
        return Collections.singletonList(mData.get(position));
    }

    @Nullable
    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull ExifImageData item) {
        return mRequestBuilder
                .clone()
                .apply(new RequestOptions().centerCrop())
                .load(item.uri);
    }

    @Nullable
    @Override
    public int[] getPreloadSize(@NonNull ExifImageData item, int adapterPosition, int perItemPosition) {
        return mActualDimensions;
    }


    static final class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;

        ListViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recycler_item_image_view);
        }
    }

}
