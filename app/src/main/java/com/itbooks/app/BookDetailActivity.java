package com.itbooks.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.itbooks.R;
import com.itbooks.data.DSBookDetail;
import com.itbooks.utils.Prefs;

/**
 * Details of book.
 *
 * @author Xinyue Zhao
 */
public final class BookDetailActivity extends BaseActivity implements ImageListener {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_book_detail;
	/**
	 * Book id.
	 */
	public static final String EXTRAS_BOOK_ID = "com.itbooks.app.BookDetailActivity.book.id";

	/**
	 * Id of book.
	 */
	private long mBookId;

	private View mContent;
	private ImageView mThumbIv;
	private TextView mTitleTv;
	private TextView mSubTitleTv;
	private TextView mDescriptionTv;
	private TextView mAuthorTv;
	private TextView mISBNTv;
	private TextView mYearTv;
	private TextView mPageTv;


	private ImageLoader mImageLoader;
	private DSBookDetail mBookDetail;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.data.DSBookDetail}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.data.DSBookDetail}.
	 */
	public void onEvent(DSBookDetail e) {
		mBookDetail = e;
		showBookDetail();
		dismissInitView();
		mRefreshLayout.setRefreshing(false);
	}

	//------------------------------------------------

	/**
	 * Show single instance of {@link com.itbooks.app.BookDetailActivity}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param bookId
	 * 		Book's id.
	 */
	public static void showInstance(Context cxt, long bookId) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.putExtra(EXTRAS_BOOK_ID, bookId);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mBookId = savedInstanceState.getLong(EXTRAS_BOOK_ID);
		} else {
			mBookId = getIntent().getLongExtra(EXTRAS_BOOK_ID, -1);
		}

		setContentView(LAYOUT);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mContent = findViewById(R.id.content_sv);
		mThumbIv = (ImageView) findViewById(R.id.detail_thumb_iv);
		mTitleTv = (TextView) findViewById(R.id.detail_title_tv);
		mSubTitleTv = (TextView) findViewById(R.id.detail_subtitle_tv);
		mDescriptionTv = (TextView) findViewById(R.id.detail_description_tv);
		mAuthorTv = (TextView) findViewById(R.id.detail_author_tv);
		mISBNTv = (TextView) findViewById(R.id.detail_isbn_tv);
		mYearTv = (TextView) findViewById(R.id.detail_year_tv);
		mPageTv = (TextView) findViewById(R.id.detail_page_tv);

		mInitLl = findViewById(R.id.init_ll);

		mImageLoader = TaskHelper.getImageLoader();
		loadBookDetail();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mBookId = intent.getLongExtra(EXTRAS_BOOK_ID, -1);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRAS_BOOK_ID, mBookId);
	}

	@Override
	public void onRefresh() {
		loadBookDetail();
	}

	private void loadBookDetail() {
		dismissContent();
		String url = Prefs.getInstance(getApplication()).getApiBookDetail();
		url = String.format(url, mBookId + "");
		new GsonRequestTask<DSBookDetail>(getApplication(), Method.GET, url, DSBookDetail.class).execute();
	}

	private void showBookDetail() {
		mContent.setVisibility(View.VISIBLE);
		mImageLoader.get(mBookDetail.getImageUrl(), this);
		mTitleTv.setText(mBookDetail.getTitle());
		mSubTitleTv.setText(mBookDetail.getSubTitle());
		mDescriptionTv.setText(mBookDetail.getDescription());
		mAuthorTv.setText(mBookDetail.getAuthor());
		mISBNTv.setText(mBookDetail.getISBN());
		mYearTv.setText(mBookDetail.getYear());
		mPageTv.setText(mBookDetail.getPage());
	}

	@Override
	public void onResponse(ImageContainer response, boolean isImmediate) {
		if (response != null && response.getBitmap() != null) {
			mThumbIv.setImageBitmap(response.getBitmap());
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {

	}

	public void download(View view) {
		DownloadWebViewActivity.showInstance(this, mBookDetail.getDownloadUrl());
	}

	private void dismissContent() {
		mContent.setVisibility(View.GONE);
	}
}
