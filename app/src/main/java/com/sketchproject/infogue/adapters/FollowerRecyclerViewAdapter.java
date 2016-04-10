package com.sketchproject.infogue.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.FollowerFragment.OnListFragmentInteractionListener;
import com.sketchproject.infogue.fragments.dummy.DummyFollowerContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<FollowerRecyclerViewAdapter.FollowerViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public FollowerRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public FollowerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_follower_row, parent, false);
        return new FollowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FollowerViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).name);
        holder.mLocationView.setText(mValues.get(position).location);
        holder.mAvatarImage.setImageResource(mValues.get(position).avatar);
        if(mValues.get(position).isFollowing){
            holder.mFollowButton.setImageResource(R.drawable.btn_unfollow);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class FollowerViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mLocationView;
        public final ImageButton mFollowButton;
        public final ImageView mAvatarImage;
        public DummyItem mItem;

        public FollowerViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mLocationView = (TextView) view.findViewById(R.id.location);
            mFollowButton = (ImageButton) view.findViewById(R.id.follow);
            mAvatarImage = (ImageView) view.findViewById(R.id.avatar);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
