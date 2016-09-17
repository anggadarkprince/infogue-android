package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.GalleryFragment.OnImageInteractionListener;
import com.sketchproject.infogue.fragments.holders.InfoViewHolder;
import com.sketchproject.infogue.fragments.holders.LoadingViewHolder;
import com.sketchproject.infogue.models.Image;

import java.util.List;

/**
 * Sketch Project Studio
 * Created by angga on 16/09/16.
 */
public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    private List<Image> mImages;
    private OnImageInteractionListener mImageListener;

    public GalleryRecyclerViewAdapter(List<Image> items, OnImageInteractionListener listListener) {
        mImages = items;
        mImageListener = listListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mImages.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            Image image = mImages.get(position);
            if (image.getId() > 0) {
                return VIEW_TYPE_IMAGE;
            } else {
                return VIEW_TYPE_EMPTY;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_LOADING) {
            view = inflater.inflate(R.layout.fragment_loading, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE) {
            view = inflater.inflate(R.layout.fragment_gallery_row, parent, false);
            return new ImageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.fragment_list_info, parent, false);
            return new InfoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Image image = mImages.get(holder.getAdapterPosition());

        switch (getItemViewType(holder.getAdapterPosition())) {
            case VIEW_TYPE_IMAGE:
                final ImageViewHolder imageHolder = (ImageViewHolder) holder;
                imageHolder.mItem = image;
                Glide.with(imageHolder.mView.getContext())
                        .load(image.getSource())
                        .placeholder(R.drawable.placeholder_square)
                        .centerCrop()
                        .crossFade()
                        .into(imageHolder.mImage);
                imageHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageListener.onImageClicked(image);
                    }
                });

                imageHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mImageListener.onDeleteImage(image);
                        return true;
                    }
                });
                break;
            case VIEW_TYPE_LOADING:
                final LoadingViewHolder progressbarHolder = (LoadingViewHolder) holder;
                progressbarHolder.mProgressBar.setIndeterminate(true);
                break;
            case VIEW_TYPE_EMPTY:
                final InfoViewHolder emptyHolder = (InfoViewHolder) holder;
                emptyHolder.mMessageView.setText(R.string.label_no_image);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImage;
        public Image mItem;

        public ImageViewHolder(View view) {
            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return mItem.getSource();
        }
    }
}
