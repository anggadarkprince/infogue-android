package com.sketchproject.infogue.modules;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Custom OnScrollListener to detect when scroll content reach bottom (almost) to prepare
 * another event like load more data.
 *
 * Sketch Project Studio
 * Created by Angga on 11/04/2016 10.49 10.49.
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold;
    // The current offset index of data you have loaded
    private int currentPage;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount;
    // True if we are still waiting for the last set of data to load.
    private boolean loading;
    // Sets the starting page index
    private int startingPageIndex;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        mLinearLayoutManager = layoutManager;
        visibleThreshold = 5;
        currentPage = 0;
        previousTotalItemCount = 0;
        loading = true;
        startingPageIndex = 0;
    }

    /**
     * This happens many times a second during a scroll, so be wary of the code you place here.
     * We are given a few useful parameters to help us work out if we need to load some more data,
     * but first we check if we are waiting for the previous load to finish.
     *
     * @param view The RecyclerView which scrolled.
     * @param dx The amount of horizontal scroll.
     * @param dy The amount of vertical scroll.
     */
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
        int totalItemCount = mLinearLayoutManager.getItemCount();

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            currentPage = startingPageIndex;
            previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count. (-1 for excluding 'loading' data with null value)
        if ((loading && (totalItemCount - 1 > previousTotalItemCount)) || currentPage == 0) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            Log.i("Infogue/List", "Load more");
            currentPage++;
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }

        if (mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            onReachTop(true);
        } else {
            onReachTop(false);
        }
    }


    /**
     * Defines the process for actually loading more data based on page
     *
     * @param page current page begin from 0
     * @param totalItemsCount total item has been loaded
     */
    public abstract void onLoadMore(int page, int totalItemsCount);

    /**
     * Abstract method to determine when scroll reach top.
     *
     * @param isFirst first data
     */
    public abstract void onReachTop(boolean isFirst);
}
