package com.shoppica.com.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.kennyc.view.MultiStateView;
import com.loopj.android.http.RequestParams;
import com.shoppica.com.R;
import com.shoppica.com.activities.ViewArticleActivity;
import com.shoppica.com.adapter.FeedAdapter;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.objects.FeedObject;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.shoppica.com.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DiscoverFragment extends BaseFragment implements FeedAdapter.clickListener, SwipeRefreshLayout.OnRefreshListener, NotificationCenter.NotificationCenterDelegate {

    public static final String TAG = DiscoverFragment.class.getSimpleName();

    public static final String BOLD = "fonts/MONTSERRAT-BOLD.TTF";
    public static final String MEDIUM = "fonts/MONTSERRAT-MEDIUM.TTF";

    private static final int CATEGORY_MEN_STANDARD_POSITION = 26;
    private static final int CATEGORY_WOMEN_STANDARD_POSITION = 23;
    private static final int CATEGORY_KIDS_STANDARD_POSITION = 20;
    private static final int CATEGORY_UNISEX_STANDARD_POSITION = 25;
    private static final int CATEGORY_GENDER_POSITION = 4;
    private static final int CATEGORY_CONDITION_POSITION = 3;
    private static final int CATEGORY_COLOR_POSITION = 10;
    private static final int CATEGORY_SEND_POSITION = 3;

    @BindView(R.id.multi_state)
    MultiStateView mMultiStateView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.refresh)
    SwipeRefreshLayout mRefresh;

    private Context mContext;

    private FeedAdapter mAdapter;

    private int mCurrentPage;

    private Button mErrorButton, mEmptyButton;

    private ImageView mEmptyImageView;

    private TextView mErrorDescription, mEmptyDescription;

    private int mIter = 1;

    private Handler mHandler;

    private int totalItemCount, lastVisibleItem, visibleThreshold = 2;

    private boolean mLoading, mHasMore = true;

    private int[] lastPositions;

    private boolean mHasLoadedOnce;

    private boolean mIsFilterOpen;

    private int mCurrentViewState = MultiStateView.VIEW_STATE_CONTENT;

    private String mCurrentViewDesc, mCurrentViewBtnText;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

    }

    private void initViews() {


        StaggeredGridLayoutManager mManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        recyclerViewOnScroll(mRecyclerView, mManager);

        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorAccent));

        mEmptyImageView = Objects.requireNonNull(mMultiStateView.getView(MultiStateView.VIEW_STATE_EMPTY)).findViewById(R.id.into_empty_view);
        mEmptyDescription = Objects.requireNonNull(mMultiStateView.getView(MultiStateView.VIEW_STATE_EMPTY)).findViewById(R.id.into_desc_empty);
        mEmptyButton = Objects.requireNonNull(mMultiStateView.getView(MultiStateView.VIEW_STATE_EMPTY)).findViewById(R.id.into_btn_empty);
        mEmptyButton.setOnClickListener(onEmpty);

        mErrorDescription = Objects.requireNonNull(mMultiStateView.getView(MultiStateView.VIEW_STATE_ERROR)).findViewById(R.id.into_desc_error);
        mErrorButton = Objects.requireNonNull(mMultiStateView.getView(MultiStateView.VIEW_STATE_ERROR)).findViewById(R.id.into_btn_error);
        mErrorButton.setOnClickListener(onRetry);

        mHandler = new MyHandler(this);

        if (mHasLoadedOnce && getAdapter() != null && !getAdapter().isEmpty())
            mRecyclerView.setAdapter(mAdapter);

        if (mCurrentViewState == MultiStateView.VIEW_STATE_EMPTY)
            onEmpty(mCurrentViewDesc, mCurrentViewBtnText);
        else if (mCurrentViewState == MultiStateView.VIEW_STATE_ERROR)
            onError(mCurrentViewDesc, mCurrentViewBtnText);
        else
            mMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);


        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshDiscoverFragment);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.isFilterOpen);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateArticle);

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && !mHasLoadedOnce) {
            initData();
            mHasLoadedOnce = true;
        }

    }


    private void initData() {

        if (getAdapter() == null || getAdapter().isEmpty()) {
            setUpFeedData();
        } else
            mRecyclerView.setAdapter(mAdapter);


    }

    private void setUpFeedData() {
        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_LOADING);
        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);
    }

    private void fetchFeedData() {


        mHandler.removeCallbacksAndMessages(null);

        if (mAdapter != null)
            mAdapter.clear();

        mCurrentPage = 0;

        mIter = 1;

        mLoading = true;

        mHandler.sendMessage(mHandler.obtainMessage(MyHandler.MESSAGE_GET_FEED));

    }

    public Handler mIncomingHandler = new Handler(msg -> {

        if (msg.what == 1 || msg.what == 0) {
            fetchFeedData();
        } else {

            if (getAdapter() == null || getAdapter().isEmpty())
                onError(getString(R.string.state_connection_desc), getString(R.string.btn_action_retry));
            else {
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), R.string.snackbar_error_connection, Snackbar.LENGTH_SHORT).show();
                if (mRefresh.isRefreshing())
                    mRefresh.setRefreshing(false);
            }

        }

        return true;
    });


    public FeedAdapter getAdapter() {
        return mAdapter;
    }


    @Override
    public void onItemClicked(FeedObject mediaObject) {

        if (mIsFilterOpen)
            return;

        Intent intent = new Intent(mContext, ViewArticleActivity.class);
        intent.putExtra(ViewArticleActivity.OBJECT, mediaObject);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
    }

    @Override
    public void onItemMoreClicked(FeedObject object) {

    }

    @Override
    public void onLongItemClicked(FeedObject mediaObject) {

    }


    private void fetchMoreItems() {

        if (!NetworkUtils.isConnected()) {
            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), R.string.toast_error_no_internet_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }

        mRecyclerView.post(() -> mAdapter.addItem(null));

        mHandler.sendMessage(mHandler.obtainMessage(MyHandler.MESSAGE_GET_FEED));
    }

    @Override
    public void onRefresh() {

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.refreshDiscoverFragment) {
            setUpFeedData();
        } else if (id == NotificationCenter.isFilterOpen) {
            mIsFilterOpen = (boolean) args[0];
        }else if (id == NotificationCenter.updateArticle) {
            String postId = (String) args[0];
            String postViews = (String) args[1];

            if (TextUtils.isEmpty(postId) || TextUtils.isEmpty(postViews) || mAdapter == null)
                return;

            for (int i = 0; i < mAdapter.getItemCount(); i++) {

                FeedObject object = mAdapter.getItem(i);

                if (object == null)
                    continue;

                if (object.getPostId().equals(postId)) {
                    object.setPostViews(postViews);
                    mAdapter.notifyDataSetChanged();
                    break;
                }


            }

        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<DiscoverFragment> mFragment;


        static final int MESSAGE_GET_FEED = 0;


        MyHandler(DiscoverFragment activity) {
            mFragment = new WeakReference<>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            final DiscoverFragment fragment = mFragment.get();
            if (fragment != null) {

                switch (msg.what) {

                    case MESSAGE_GET_FEED:

                        try {

                            boolean isFilterOn = Prefs.getBoolean(Constants.KEY_FILTER_ACTIVE, false);

                            RequestParams params = new RequestParams();
                            params.add(Constants.ACTION, Constants.GET_FEED);
                            params.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
                            params.add(Constants.TAB, String.valueOf(1));
                            params.add(Constants.POST_COUNTRY,
                                    Objects.equals(Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null).toLowerCase(), Constants.BE) ? Constants.NL : Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null).toLowerCase());
                            params.add(Constants.PAGE, String.valueOf(fragment.mCurrentPage));

                            if (isFilterOn) {

                                int mPosCategory = Prefs.getInt(Constants.KEY_FILTER_CATEGORY, -1);
                                int mPosGender = Prefs.getInt(Constants.KEY_FILTER_GENDER, -1);
                                int mPosCondition = Prefs.getInt(Constants.KEY_FILTER_CONDITION, -1);
                                int mPosColor = Prefs.getInt(Constants.KEY_FILTER_COLOR, -1);
                                int mPosSend = Prefs.getInt(Constants.KEY_FILTER_SEND, -1);
                                int mFilterDistance = Prefs.getInt(Constants.KEY_FILTER_DISTANCE_BAR, 0);
                                float mFilterLatitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LATITUDE, 0);
                                float mFilterLongitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LONGITUDE, 0);
                                int mFilterPriceMinimum = Prefs.getInt(Constants.KEY_FILTER_PRICE_BAR_MIN, 0);
                                int mFilterPriceMaximum = Prefs.getInt(Constants.KEY_FILTER_PRICE_BAR_MAX, 0);

                                if (mPosCategory != -1) {

                                    int checkValue = CATEGORY_WOMEN_STANDARD_POSITION;

                                    if (mPosGender == 1)
                                        checkValue = CATEGORY_MEN_STANDARD_POSITION;
                                    else if (mPosGender == 2)
                                        checkValue = CATEGORY_KIDS_STANDARD_POSITION;
                                    else if (mPosGender == 3)
                                        checkValue = CATEGORY_UNISEX_STANDARD_POSITION;

                                    if (mPosCategory != checkValue)
                                        params.add(Constants.ITEM_CATEGORY, String.valueOf(mPosCategory));
                                }

                                if (mPosGender != -1 && mPosGender != CATEGORY_GENDER_POSITION)
                                    params.add(Constants.ITEM_GENDER, String.valueOf(mPosGender));

                                if (mPosCondition != -1 && mPosCondition != CATEGORY_CONDITION_POSITION)
                                    params.add(Constants.ITEM_CONDITION, String.valueOf(mPosCondition));

                                if (mPosColor != -1 && mPosColor != CATEGORY_COLOR_POSITION) {
                                    String[] color = fragment.getResources().getStringArray(R.array.color_filter);
                                    params.add(Constants.ITEM_COLOR, color[mPosColor]);
                                }

                                if (mPosSend != -1 && mPosSend != CATEGORY_SEND_POSITION)
                                    params.add(Constants.ITEM_DELIVERY, String.valueOf(mPosSend));

                                if (mFilterLatitude != 0 && mFilterLongitude != 0 && mFilterDistance > 0) {
                                    params.add(Constants.ITEM_DISTANCE, String.valueOf(mFilterDistance));
                                    params.add(Constants.ITEM_LATITUDE, String.valueOf(mFilterLatitude));
                                    params.add(Constants.ITEM_LONGITUDE, String.valueOf(mFilterLongitude));
                                }

                                if (mFilterPriceMinimum != 0 && mFilterPriceMaximum != 0) {
                                    params.add(Constants.ITEM_PRICE_MINIMUM, String.valueOf(mFilterPriceMinimum));
                                    params.add(Constants.ITEM_PRICE_MAXIMUM, String.valueOf(mFilterPriceMaximum));
                                }


                            }

                            request.execute(params, new request.getResponseListener() {
                                        @Override
                                        public void onSuccess(JSONObject jsonResponse) {

                                            try {
                                                int success = jsonResponse.getInt(Constants.SUCCESS);

                                                if (fragment.getActivity() == null || fragment.getView() == null) {
                                                    return;
                                                }

                                                if (success == 1) {

                                                    JSONArray userList = jsonResponse.getJSONArray(Constants.ARTICLES);

                                                    if (userList.length() == 0 && fragment.mCurrentPage == 0) {
                                                        fragment.onEmpty(fragment.getString(R.string.state_empty_desc), fragment.getString(R.string.btn_action_retry));
                                                        return;
                                                    } else if (userList.length() == 0 && fragment.mCurrentPage > 0) {
                                                        fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);
                                                        return;
                                                    }

                                                    List<FeedObject> objectList = new ArrayList<>();

                                                    for (int i = 0; i < userList.length(); i++) {

                                                        int type;

                                                        JSONObject object = userList.getJSONObject(i);

                                                        String url = Constants.CDN;

                                                        String postId = object.getString(Constants.FEED_POST_ID);
                                                        String postUrl = object.getString(Constants.FEED_URL);
                                                        String postPromoted = object.getString(Constants.POST_PROMOTED);
                                                        String postPromotedTime = object.getString(Constants.POST_PROMOTED_TIME);
                                                        String postTime = object.getString(Constants.POST_TIME);
                                                        String postTimeExpires = object.getString(Constants.POST_TIME_EXPIRES);
                                                        String postViews = object.getString(Constants.POST_VIEWS);
                                                        String postViewsToday = object.getString(Constants.POST_VIEWS_TODAY);
                                                        String postViewsYesterday = object.getString(Constants.POST_VIEWS_YESTERDAY);
                                                        String postDeleted = object.getString(Constants.POST_DELETED);
                                                        String fbAcctId = object.getString(Constants.FB_ACCT_ID);
                                                        String fbName = object.getString(Constants.FB_NAME);

                                                        String itemPrice = object.getString(Constants.ITEM_PRICE);
                                                        itemPrice = itemPrice.replace(".", ",");
                                                        String itemGender = object.getString(Constants.ITEM_GENDER);
                                                        String itemCategory = object.getString(Constants.ITEM_CATEGORY);
                                                        String itemDelivery = object.getString(Constants.ITEM_DELIVERY);
                                                        String itemTitle = object.getString(Constants.ITEM_TITLE);
                                                        String itemCondition = object.getString(Constants.ITEM_CONDITION);
                                                        String itemBrand = object.getString(Constants.ITEM_BRAND);
                                                        String itemSize = object.getString(Constants.ITEM_SIZE);
                                                        String itemColor = object.getString(Constants.ITEM_COLOR);

                                                        String contactWhatsApp = object.getString(Constants.CONTACT_WHATSAPP);
                                                        String contactMessenger = object.getString(Constants.CONTACT_INSTAGRAM);
                                                        String contactEmail = object.getString(Constants.CONTACT_EMAIL);

                                                        String locationLat = object.getString(Constants.LOCATION_LAT);
                                                        String locationLong = object.getString(Constants.LOCATION_LONG);
                                                        String locationPlace = object.getString(Constants.LOCATION_PLACE);

                                                        String image1 = object.getString(Constants.IMAGE_1);
                                                        String image1Width = object.getString(Constants.IMAGE_1_WIDTH);
                                                        String image1Height = object.getString(Constants.IMAGE_1_HEIGHT);

                                                        String image2 = object.getString(Constants.IMAGE_2);
                                                        String image3 = object.getString(Constants.IMAGE_3);
                                                        String image4 = object.getString(Constants.IMAGE_4);
                                                        String image5 = object.getString(Constants.IMAGE_5);

                                                        String thumb1 = url + "t/" + image1 + ".jpg";
                                                        String thumb1Width = object.getString(Constants.FEED_THUMB_1_WIDTH);
                                                        String thumb1Height = object.getString(Constants.FEED_THUMB_1_HEIGHT);

                                                        String postStatus = object.getString(Constants.POST_STATUS);
                                                        String timeActive = object.getString(Constants.TIME_ACTIVE);
                                                        String postCountry = object.getString(Constants.POST_COUNTRY);

                                                        if (image1 != null && !image1.isEmpty() && !image1.equals("null"))
                                                            image1 = url + "i/" + image1 + ".jpg";

                                                        if (image2 != null && !image2.isEmpty() && !image2.equals("null"))
                                                            image2 = url + "i/" + image2 + ".jpg";

                                                        if (image3 != null && !image3.isEmpty() && !image3.equals("null"))
                                                            image3 = url + "i/" + image3 + ".jpg";

                                                        if (image4 != null && !image4.isEmpty() && !image4.equals("null"))
                                                            image4 = url + "i/" + image4 + ".jpg";

                                                        if (image5 != null && !image5.isEmpty() && !image5.equals("null"))
                                                            image5 = url + "i/" + image5 + ".jpg";

                                                        if (fragment.mIter == 1)
                                                            type = 0;
                                                        else {
                                                            type = 1;
                                                            fragment.mIter = 0;
                                                        }

                                                        fragment.mIter++;

                                                        objectList.add(new FeedObject(postId, postUrl,postCountry, postPromoted, postPromotedTime, postTime, postTimeExpires, postViews, postViewsToday, postViewsYesterday, postDeleted, fbAcctId, fbName, itemPrice, itemGender, itemCategory, itemDelivery, itemTitle, itemCondition, itemBrand, itemSize, itemColor, contactWhatsApp, contactMessenger, contactEmail, locationLat, locationLong, locationPlace, image1, image1Width, image1Height, image2, image3, image4, image5, thumb1, thumb1Width, thumb1Height, postStatus, timeActive, type, 0));

                                                    }

                                                    if (fragment.mCurrentPage > 0)
                                                        fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);

                                                    fragment.mLoading = false;
                                                    fragment.mHasMore = objectList.size() >= 10;

                                                    if (fragment.mAdapter == null) {
                                                        fragment.mAdapter = new FeedAdapter(fragment.mContext, objectList);
                                                        fragment.mAdapter.setListener(fragment);
                                                        fragment.mRecyclerView.setAdapter(fragment.mAdapter);
                                                    } else
                                                        fragment.mAdapter.addItems(objectList);

                                                    fragment.mMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                                                    fragment.mCurrentViewState = MultiStateView.VIEW_STATE_CONTENT;

                                                    if (fragment.mRefresh.isRefreshing())
                                                        fragment.mRefresh.setRefreshing(false);

                                                } else {

                                                    if (jsonResponse.has(Constants.ERROR)) {
                                                        fragment.onEmpty(fragment.getString(R.string.state_empty_desc), fragment.getString(R.string.btn_action_retry));
                                                    } else {
                                                        if (fragment.mCurrentPage == 0) {
                                                            boolean isFilterActivated = Prefs.getBoolean(Constants.KEY_FILTER_ACTIVE, false);
                                                            if (isFilterActivated)
                                                                fragment.onFilter(fragment.getString(R.string.state_filter_desc), fragment.getString(R.string.btn_action_retry));
                                                            else
                                                                fragment.onEmpty(fragment.getString(R.string.state_empty_desc), fragment.getString(R.string.btn_action_retry));
                                                        }

                                                    }
                                                    fragment.mLoading = false;
                                                    fragment.mHasMore = false;

                                                    if (fragment.mCurrentPage > 0)
                                                        fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);
                                                }


                                            } catch (JSONException e) {
                                                Crashlytics.logException(e);
                                                onErrorGetFeed(fragment);
                                                Log.d(TAG, "ERROR == " + e.getLocalizedMessage());
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {

                                            onErrorGetFeed(fragment);

                                        }
                                    }
                            );

                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            onErrorGetFeed(fragment);
                            Log.d(TAG, "ERROR == " + e.getLocalizedMessage());
                        }
                        break;

                }
            }
        }

    }

    private static void onErrorGetFeed(DiscoverFragment fragment) {

        if (fragment.mRefresh.isRefreshing())
            fragment.mRefresh.setRefreshing(false);

        fragment.mLoading = false;
        fragment.mHasMore = false;

        if (fragment.mCurrentPage > 0)
            fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);

        fragment.onError(fragment.getString(R.string.state_error_desc), fragment.getString(R.string.btn_action_retry));

    }

    private View.OnClickListener onEmpty = view -> {

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);

    };

    private View.OnClickListener onRetry = view -> {

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);
    };

    public void onFilter(String contentText, String buttonText) {

        if (getActivity() == null || getView() == null || !isAdded())
            return;

        if (mRefresh.isRefreshing())
            mRefresh.setRefreshing(false);

        mEmptyImageView.setImageResource(R.drawable.ic_empty_filter);
        mEmptyDescription.setText(contentText);
        mEmptyButton.setText(buttonText);

        mCurrentViewDesc = contentText;
        mCurrentViewBtnText = buttonText;

        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
        mCurrentViewState = MultiStateView.VIEW_STATE_EMPTY;
    }


    public void onEmpty(String contentText, String buttonText) {

        if (getActivity() == null || getView() == null || !isAdded())
            return;

        if (mRefresh.isRefreshing())
            mRefresh.setRefreshing(false);

        mEmptyImageView.setImageResource(R.drawable.ic_empty);
        mEmptyDescription.setText(contentText);
        mEmptyButton.setText(buttonText);

        mCurrentViewDesc = contentText;
        mCurrentViewBtnText = buttonText;

        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
        mCurrentViewState = MultiStateView.VIEW_STATE_EMPTY;
    }


    public void onError(String contentText, String buttonText) {


        if (getActivity() == null || getView() == null || !isAdded())
            return;

        if (mRefresh.isRefreshing())
            mRefresh.setRefreshing(false);

        mErrorDescription.setText(contentText);
        mErrorButton.setText(buttonText);

        mCurrentViewDesc = contentText;
        mCurrentViewBtnText = buttonText;

        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_ERROR);
        mCurrentViewState = MultiStateView.VIEW_STATE_ERROR;
    }

    private void recyclerViewOnScroll(RecyclerView recyclerView, final StaggeredGridLayoutManager staggeredGridLayoutManager) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mRefresh.setEnabled(topRowVerticalPosition >= 0);

                if (dy > 0) {

                    totalItemCount = staggeredGridLayoutManager.getItemCount();
                    if (lastPositions == null)
                        lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                    lastPositions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(lastPositions);
                    lastVisibleItem = Math.max(lastPositions[0], lastPositions[1]);//findMax(lastPositions);

                    if (!mLoading && mHasMore && totalItemCount >= 10 && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached

                        mCurrentPage++;

                        fetchMoreItems();

                        mLoading = true;
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshDiscoverFragment);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.isFilterOpen);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateArticle);
        super.onDestroy();
    }
}
