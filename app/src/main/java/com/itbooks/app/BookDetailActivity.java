package com.itbooks.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.chopping.net.GsonRequestTask;
import com.chopping.net.TaskHelper;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.itbooks.R;
import com.itbooks.app.fragments.BookmarkInfoDialogFragment;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookDetail;
import com.itbooks.data.DSBookmark;
import com.itbooks.db.DB;
import com.itbooks.utils.ParallelTask;
import com.itbooks.utils.Prefs;
import com.itbooks.views.OnViewAnimatedClickedListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

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
	 * Main menu.
	 */
	private static final int BOOK_DETAIL_MENU = R.menu.book_detail;
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
	private TextView mPublisherTv;


	private ImageLoader mImageLoader;
	private DSBookDetail mBookDetail;

	private ImageButton mOpenBtn;

	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	private boolean mBookmarked;
	private MenuItem mBookmarkItem;


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
		mRefreshLayout.setRefreshing(false);
		setHasShownDataOnUI(true);
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
	public static void showInstance(Activity cxt, long bookId, View bookCoverV) {
		Intent intent = new Intent(cxt, BookDetailActivity.class);
		intent.putExtra(EXTRAS_BOOK_ID, bookId);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(cxt,  Pair.create(bookCoverV, "bookCover"));
			cxt.startActivity(intent, transitionActivityOptions.toBundle());
		} else {
			cxt.startActivity(intent);
		}
	}

	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Prefs prefs = Prefs.getInstance(getApplication());
		int curTime  = prefs.getShownDetailsTimes();
		int adsTimes = prefs.getShownDetailsAdsTimes();
		if(curTime % adsTimes == 0) {
			// Create an ad.
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();
			// Begin loading your interstitial.
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					displayInterstitial();
				}
			});
			mInterstitialAd.loadAd(adRequest);
		}
		curTime++;
		prefs.setShownDetailsTimes(curTime);



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
		mPublisherTv = (TextView) findViewById(R.id.detail_publisher_tv);

		mImageLoader = TaskHelper.getImageLoader();
		loadBookDetail();



		mOpenBtn = (ImageButton) findViewById(R.id.download_btn);
		ViewHelper.setX(mOpenBtn, -10);
		ViewHelper.setRotation(mOpenBtn, -360f * 4);
		mOpenBtn.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				showDialogFragment(new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(R.string.msg_ask_download).setPositiveButton(R.string.btn_now_load,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										downloadBrowser();
									}
								}).setNegativeButton(R.string.btn_not_yet_load, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
						return builder.create();
					}
				}, "download ask");
			}
		});

		if(!prefs.hasKnownBookmark()) {
			showDialogFragment(BookmarkInfoDialogFragment.newInstance(getApplication()), null);
		}
		showOpenButton();
	}

	private void showOpenButton() {
		int screenWidth = DeviceUtils.getScreenSize(getApplication()).Width;
		ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mOpenBtn);
		animator.x(screenWidth - getResources().getDimensionPixelSize(R.dimen.float_button_anim_qua)).rotation(0)
				.setDuration(500).start();

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
		String url = Prefs.getInstance(getApplication()).getApiBookDetail();
		url = String.format(url, mBookId + "");
		new GsonRequestTask<>(getApplication(), Method.GET, url, DSBookDetail.class).execute();
	}

	private void showBookDetail() {
		mContent.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(mBookDetail.getImageUrl())) {
			mImageLoader.get(mBookDetail.getImageUrl(), this);
		}
		mTitleTv.setText(mBookDetail.getTitle());
		mSubTitleTv.setText(mBookDetail.getSubTitle());
		mDescriptionTv.setText(mBookDetail.getDescription());
		mAuthorTv.setText(mBookDetail.getAuthor());
		mISBNTv.setText(mBookDetail.getISBN());
		mYearTv.setText(mBookDetail.getYear());
		mPageTv.setText(mBookDetail.getPage());
		mPublisherTv.setText(mBookDetail.getPublisher());

		ActivityCompat.invalidateOptionsMenu(this);
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


//	public void downloadInternal(View view) {
//		DownloadWebViewActivity.showInstance(this, mBookDetail.getDownloadUrl());
//	}

	public void downloadBrowser(   ) {
		if (mBookDetail != null && !TextUtils.isEmpty(mBookDetail.getDownloadUrl())) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setData(Uri.parse(mBookDetail.getDownloadUrl()));
			startActivity(i);

			String msg = getString(R.string.lbl_download_path, new StringBuilder().append(
							Environment.getExternalStorageDirectory()).append('/').append(
							Environment.DIRECTORY_DOWNLOADS));
			Utils.showLongToast(getApplicationContext(), msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(BOOK_DETAIL_MENU, menu);
		mBookmarkItem = menu.findItem(R.id.action_bookmark);
		new ParallelTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				mBookmarked = DB.getInstance(getApplication()).isBookmarked(mBookId);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
			}
		}.executeParallel();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bookmark:
			if(mBookDetail != null) {
				new ParallelTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						DB db = DB.getInstance(getApplication());
						if (mBookmarked) {
							db.removeBookmark(new DSBook(mBookDetail.getId(), mBookDetail.getImageUrl()));
						} else {
							db.addBookmark(new DSBookmark(new DSBook(mBookDetail.getId(), mBookDetail.getImageUrl())));
						}
						mBookmarked = db.isBookmarked(mBookId);
						return null;
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						super.onPostExecute(aVoid);
						mBookmarkItem.setIcon(mBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_not_bookmarked);
						Utils.showShortToast(getApplicationContext(), getString(
								mBookmarked ? R.string.msg_bookmark_the_book : R.string.msg_unbookmark_the_book));
					}
				}.executeParallel();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem mMenuShare = menu.findItem(R.id.action_share_book);
		if (mBookDetail != null) {
			//Getting the actionprovider associated with the menu item whose id is share.
			android.support.v7.widget.ShareActionProvider provider =
					(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(mMenuShare);
			//Setting a share intent.
			String subject = getString(R.string.lbl_share_book);
			String text = getString(R.string.lbl_share_book_content, mBookDetail.getTitle(), mBookDetail.getAuthor(),
					mBookDetail.getDownloadUrl());

			provider.setShareIntent(getDefaultShareIntent(provider, subject, text));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onReload() {
		super.onReload();
		loadBookDetail();
	}

	@Override
	public void onBackPressed() {
		ActivityCompat.finishAfterTransition(this);
	}
}
