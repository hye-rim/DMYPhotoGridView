package com.hackday.dmyphotogridview_parkhyerim.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;
import com.hackday.dmyphotogridview_parkhyerim.R;
import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class RecyclerAdapter extends StickyHeaderGridAdapter {
    //        implements ListPreloader.PreloadSizeProvider<ExifImageData>, ListPreloader.PreloadModelProvider<ExifImageData> {
    private final Map<String, ArrayList<ExifImageData>> mData;
    private final RequestBuilder<Drawable> mRequestBuilder;

    private Context mContext;
    private int[] mActualDimensions;
    private int mScreenWidth;

    public RecyclerAdapter(Context context, Map<String, ArrayList<ExifImageData>> data, RequestBuilder<Drawable> glideRequests, int screenWidth) {
        mContext = context;
        mData = data;
        mRequestBuilder = glideRequests;
        mScreenWidth = screenWidth;

        setHasStableIds(true);
    }

    @Override
    public int getSectionCount() {
        return mData.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        return mData.get(section).size();
    }


    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_header, parent, false);
        return new HeaderViewHolder(view);

    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.recycler_item, parent, false);
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
    public void onBindHeaderViewHolder(StickyHeaderGridAdapter.HeaderViewHolder viewHolder, int section) {
        final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
        final String label = "Header " + section;
        holder.titleTextView.setText(label);
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int section, int position) {
        ExifImageData current = mData.get(section).get(position);

        mRequestBuilder
                .clone()
                .thumbnail(0.1f)
                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(current.uri)
                .into(viewHolder.imageView);
    }

//    @NonNull
//    @Override
//    public List<ExifImageData> getPreloadItems(int position) {
//        return Collections.singletonList(mData.get(position));
//    }

//    @Nullable
//    @Override
//    public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull ExifImageData item) {
//        return mRequestBuilder
//                .clone()
//                .thumbnail(0.1f)
//                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
//                .load(item.uri);
//    }

//    @Nullable
//    @Override
//    public int[] getPreloadSize(@NonNull ExifImageData item, int adapterPosition, int perItemPosition) {
//        return mActualDimensions;
//    }


    static class HeaderViewHolder extends StickyHeaderGridAdapter.HeaderViewHolder {
        private TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.recycler_item_header_text_view);
        }
    }

    static class ListViewHolder extends ItemViewHolder {

        private ImageView imageView;

        ListViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recycler_item_image_view);
        }
    }

}
