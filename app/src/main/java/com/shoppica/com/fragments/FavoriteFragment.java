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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.kennyc.view.MultiStateView;
import com.loopj.android.http.RequestParams;
import com.shoppica.com.R;
import com.shoppica.com.activities.FacebookExplainActivity;
import com.shoppica.com.activities.ViewArticleActivity;
import com.shoppica.com.adapter.FeedAdapterFavorites;
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

public class FavoriteFragment extends BaseFragment implements FeedAdapterFavorites.clickListener, SwipeRefreshLayout.OnRefreshListener, NotificationCenter.NotificationCenterDelegate {

    public static final String TAG = FavoriteFragment.class.getSimpleName();

    @BindView(R.id.multi_state)
    MultiStateView mMultiStateView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.refresh)
    SwipeRefreshLayout mRefresh;

    private Context mContext;

    private FeedAdapterFavorites mAdapter;

    private int mCurrentPage;

    private Button mErrorButton, mEmptyButton;

    private ImageView mEmptyImageView;

    private TextView mErrorDescription, mEmptyDescription;

    private int mIter = 1;

    private Handler mHandler;

    private int totalItemCount, lastVisibleItem, visibleThreshold = 5;

    private boolean mLoading, mHasMore = true;

    private int[] lastPositions;

    private boolean mHasLoadedOnce;

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
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
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

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.removeFromFavoritesFragment);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.addToFavoritesFragment);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshFavoritesFragment);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.logoutFromFacebook);
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

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;

        if (!loggedIn) {
            onEmpty(getString(R.string.state_empty_no_favorites_login), getString(R.string.btn_action_login));
            return;
        }

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


    public FeedAdapterFavorites getAdapter() {
        return mAdapter;
    }


    @Override
    public void onItemClicked(FeedObject mediaObject) {

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

        if (id == NotificationCenter.removeFromFavoritesFragment) {

            try {

                FeedObject object = (FeedObject) args[0];

                if (object == null)
                    return;

                if (mAdapter == null || mAdapter.isEmpty())
                    return;

                FeedObject removableObject = null;


                for (int i = 0; i < mAdapter.getItemCount(); i++) {

                    FeedObject feedObject = mAdapter.getItem(i);

                    if (feedObject == null)
                        continue;


                    if (feedObject.getPostId().equals(object.getPostId())) {
                        removableObject = feedObject;
                        break;
                    }
                }

                mAdapter.removeItem(removableObject);
                mAdapter.notifyDataSetChanged();

                if (mAdapter.isEmpty())
                    onEmpty(getString(R.string.state_empty_no_favorites), getString(R.string.btn_action_retry));

            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        } else if (id == NotificationCenter.addToFavoritesFragment) {
            try {

                FeedObject object = (FeedObject) args[0];

                if (object == null)
                    return;

                List<FeedObject> objectList = new ArrayList<>();
                objectList.add(object);

                if (mAdapter == null) {
                    mAdapter = new FeedAdapterFavorites(mContext, objectList);
                    mAdapter.setListener(this);
                    mRecyclerView.setAdapter(mAdapter);
                } else
                    mAdapter.addItems(objectList);

                mMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);

                mCurrentViewState = MultiStateView.VIEW_STATE_CONTENT;

            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        } else if (id == NotificationCenter.refreshFavoritesFragment) {
            setUpFeedData();
        } else if (id == NotificationCenter.logoutFromFacebook) {

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter = null;
            }

            setUpFeedData();
        }


    }


    private static class MyHandler extends Handler {
        private final WeakReference<FavoriteFragment> mFragment;


        static final int MESSAGE_GET_FEED = 0;


        MyHandler(FavoriteFragment activity) {
            mFragment = new WeakReference<>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            final FavoriteFragment fragment = mFragment.get();
            if (fragment != null) {

                switch (msg.what) {

                    case MESSAGE_GET_FEED:

                        try {

                            RequestParams params = new RequestParams();
                            params.add(Constants.ACTION, Constants.GET_MY_FAVORITES);
                            params.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
                            params.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));
                            params.add(Constants.PAGE, String.valueOf(fragment.mCurrentPage));

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

                                                        objectList.add(new FeedObject(postId, postUrl, postCountry,postPromoted, postPromotedTime, postTime, postTimeExpires, postViews, postViewsToday, postViewsYesterday, postDeleted, fbAcctId, fbName, itemPrice, itemGender, itemCategory, itemDelivery, itemTitle, itemCondition, itemBrand, itemSize, itemColor, contactWhatsApp, contactMessenger, contactEmail, locationLat, locationLong, locationPlace, image1, image1Width, image1Height, image2, image3, image4, image5, thumb1, thumb1Width, thumb1Height, postStatus, timeActive, type, 0))
                                                        ;

                                                    }

                                                    if (fragment.mCurrentPage > 0)
                                                        fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);

                                                    fragment.mLoading = false;
                                                    fragment.mHasMore = objectList.size() >= 10;

                                                    if (fragment.mAdapter == null) {
                                                        fragment.mAdapter = new FeedAdapterFavorites(fragment.mContext, objectList);
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

                                                        if (fragment.mCurrentPage == 0)
                                                            fragment.onEmpty(fragment.getString(R.string.state_empty_no_favorites), fragment.getString(R.string.btn_action_retry));

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

    private static void onErrorGetFeed(FavoriteFragment fragment) {

        if (fragment.mRefresh.isRefreshing())
            fragment.mRefresh.setRefreshing(false);

        fragment.mLoading = false;
        fragment.mHasMore = false;

        if (fragment.mCurrentPage > 0)
            fragment.mAdapter.removeItem(fragment.mAdapter.getItemCount() - 1);


        fragment.onError(fragment.getString(R.string.state_error_desc), fragment.getString(R.string.btn_action_retry));

    }

    private View.OnClickListener onEmpty = view -> {

        if (mEmptyButton.getText().toString().equals(getString(R.string.btn_action_login))) {
            Intent intent = new Intent(getActivity(), FacebookExplainActivity.class);
            intent.putExtra(FacebookExplainActivity.FAVORITE, true);
            startActivityForResult(intent, 200);
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_bottom_in, R.anim.stay);
            return;
        }

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);

    };

    private View.OnClickListener onRetry = view -> {

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);
    };


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
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.removeFromFavoritesFragment);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.addToFavoritesFragment);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshFavoritesFragment);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.logoutFromFacebook);
        super.onDestroy();
    }
}
