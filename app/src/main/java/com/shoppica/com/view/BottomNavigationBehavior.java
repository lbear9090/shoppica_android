package com.shoppica.com.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.shoppica.com.utils.AndroidUtilities;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<TabLayout> {
 
    public BottomNavigationBehavior() {
        super();
    }
 
    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, TabLayout child, View dependency) {
        boolean dependsOn = dependency instanceof FrameLayout;
        return dependsOn;
    }
 
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, TabLayout child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
 
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, TabLayout child, View target, int dx, int dy, int[] consumed) {
        if (dy < 0) {
            showBottomNavigationView(child);
        } else if (dy > AndroidUtilities.dp(10)) {
            hideBottomNavigationView(child);
        }
    }
 
    public void hideBottomNavigationView(TabLayout view) {
        view.animate().translationY(view.getHeight());
    }

    public void showBottomNavigationView(TabLayout view) {
        view.animate().translationY(0);
    }
}