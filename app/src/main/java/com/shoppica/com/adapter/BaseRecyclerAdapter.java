package com.shoppica.com.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

    protected final String TAG = getClass().getSimpleName();

    private List<T> mItems;


    protected LayoutInflater mInflater;

    protected Resources mResources;

    protected boolean mAllEnabled;


    public BaseRecyclerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }


    public BaseRecyclerAdapter(Context context, List<T> collection) {
        mItems = collection;
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    /**
     * Simple constructor for creating a BaseRecyclerAdapter
     *
     * @param context    The context the adapter is running in
     * @param collection A list of items to populate the adapter with, can be null. If passing a null list,
     *                   {@link #addItem(Object)} will throw an exception as the list type is undefined. The list
     *                   needs to be created first with {@link #addItems(List)}
     */

    /**
     * Adds an item to the list, {@link #notifyItemRangeInserted(int, int)} will be called
     *
     * @param object Object to add to the adapter
     */
    public void addItem(T object) {
        // An exception is thrown instead of creating a List object since the type of list in unknown
        if (mItems == null) throw new NullPointerException("Adapter list has not been initialized");
        mItems.add(object);
        notifyItemInserted(mItems.size());
    }

    /**
     * Adds an item to the list at the given position, {@link #notifyItemRangeInserted(int, int)} will be called
     *
     * @param object   Object to add to the adapter
     * @param position Position to add the object
     */
    public void addItem(T object, int position) {
        // An exception is thrown instead of creating a List object since the type of list in unknown
        if (mItems == null)
            mItems = new ArrayList<>();

        mItems.add(position, object);
        notifyItemRangeInserted(position, 1);
    }




    /**
     * Adds a list of items to the adapter list, {@link #notifyItemRangeInserted(int, int)} will be called
     *
     * @param items List of items to add to the adapter
     */
    public void addItems(List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int startingSize = 0;

        if (mItems == null) {
            mItems = items;
        } else {
            startingSize = mItems.size();
            mItems.addAll(items);
        }

        int endSize = mItems.size();
        notifyItemRangeInserted(startingSize, endSize);
    }

    /**
     * Adds a list of items to the adapter list at the given position, {@link #notifyItemRangeInserted(int, int)} will be called
     *
     * @param items    List of items to add to the adapter
     * @param position The position to add the items into the adapter
     */
    public void addItems(List<T> items, int position) {
        if (items == null || items.isEmpty()) {
            return;
        }

        mItems.addAll(position, items);
        notifyItemRangeInserted(position, items.size());
    }

    /**
     * Removes an object from the list, {@link #notifyItemRangeRemoved(int, int)} (int, int)} will be called
     *
     * @param object The object to remove from the adapter
     * @return If the object was removed
     */
    public boolean removeItem(T object) {
        if (mItems != null) {
            int position = mItems.indexOf(object);
            return position >= 0 && removeItem(position) != null;
        }

        return false;
    }

    public void setItem(T object, int position) {

        if (mItems != null) {
            mItems.set(position, object);
            notifyItemChanged(position);
        }
    }

    /**
     * Removes an item at the given position, {@link #notifyItemRemoved(int)} will be called
     *
     * @param position The position to remove from the adapter
     * @return The item removed
     */
    public T removeItem(int position) {
        if (mItems != null) {
            T removedItem = mItems.remove(position);
            notifyItemRemoved(position);
            return removedItem;
        }

        return null;
    }

    /**
     * Removes a range of items from the adapter, {@link #notifyItemRangeRemoved(int, int)} will be called
     *
     * @param start Starting position of removal
     * @param end   Ending position of removal
     */
    public void removeItems(int start, int end) {
        mItems.subList(start, end).clear();
        notifyItemRangeRemoved(start, end - start);
    }

    /**
     * Removes all items from the list, {@link #notifyItemRangeRemoved(int, int)} will be called
     */
    public void clear() {
        if (mItems != null) {
            int size = mItems.size();
            mItems.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    /**
     * Returns the index of the given item
     *
     * @param object
     * @return
     */
    public int indexOf(T object) {
        return mItems != null ? mItems.indexOf(object) : -1;
    }

    /**
     * Returns the entire list. This is <b><i>not</i></b> a copy of the list. If a copy of the list is
     * needed, see {@link #retainItems()}
     *
     * @return The entire list of items in the adapter
     */
    protected List<T> getAllItems() {
        return mItems;
    }

    /**
     * Returns the object for the given position
     *
     * @param position The position to return
     * @return The item at the given position
     */
    public T getItem(int position) {
        return mItems.get(position);
    }

    /**
     * Returns an ArrayList of the items in the adapter, used for saving the items for configuration changes
     *
     * @return A copy of the items in the adapter
     */
    public ArrayList<T> retainItems() {
        return new ArrayList<>(mItems);
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    /**
     * Returns if the adapter is empty
     *
     * @return If the adapter is empty
     */
    public boolean isEmpty() {
        return mItems == null || mItems.size() <= 0;
    }

    /**
     * Frees up any resources tied to the adapter. Should be called in an activities onDestroy lifecycle method if needed
     */
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
    }

    @ColorInt
    public int getColor(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mResources.getColor(color, null);
        } else {
            return mResources.getColor(color);
        }
    }

    public abstract static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}