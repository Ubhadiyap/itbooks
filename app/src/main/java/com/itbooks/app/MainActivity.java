package com.itbooks.app;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.chopping.net.GsonRequestTask;
import com.itbooks.R;
import com.itbooks.adapters.BookListAdapter;
import com.itbooks.data.DSBook;
import com.itbooks.data.DSBookList;
import com.itbooks.utils.Prefs;


public class MainActivity extends BaseActivity implements  OnQueryTextListener, OnItemClickListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	private static final int MAIN_MENU = R.menu.main_menu;

	private ListView mLv;
	private BookListAdapter mAdp;

	private SearchRecentSuggestions mSuggestions;
	private String mKeyword;
	private SearchView mSearchView;
	private EditText mSearchKeyEt;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.data.DSBookList}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.data.DSBookList}.
	 */
	public void onEvent(DSBookList e) {
		mRefreshLayout.setRefreshing(false);

		if (mAdp == null) {
			mAdp = new BookListAdapter(e.getBooks());
			mLv.setAdapter(mAdp);
		} else {
			mAdp.setData(e.getBooks());
			mAdp.notifyDataSetChanged();
		}

		dismissInitView();
	}


	//------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
		mSuggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
				SearchSuggestionProvider.MODE);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);
		mRefreshLayout.setRefreshing(true);

		mLv = (ListView) findViewById(R.id.books_lv);
		mInitLl = findViewById(R.id.init_ll);
		mLv.setOnItemClickListener(this);

		mSearchKeyEt = (EditText) findViewById(R.id.search_keyword_et);

		handleIntent(getIntent());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(MAIN_MENU, menu);
//		final MenuItem searchMenu = menu.findItem(R.id.search);
//		mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
//		mSearchView.setOnQueryTextListener(this);
//		/* In order to close ActionView automatically after clicking keyboard. */
//		mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
//
//			@Override
//			public void onFocusChange(View _v, boolean _hasFocus) {
//				if (!_hasFocus) {
//					MenuItemCompat.collapseActionView(searchMenu);
//					mSearchView.setQuery("", false);
//				}
//			}
//		});
//		mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
//			@Override
//			public boolean onSuggestionSelect(int _pos) {
//				return false;
//			}
//
//			@Override
//			public boolean onSuggestionClick(int _pos) {
//				MenuItemCompat.collapseActionView(searchMenu);
//				mSearchView.setQuery("", false);
//				return false;
//			}
//		});
//		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//		if (searchManager != null) {
//			SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
//			mSearchView.setSearchableInfo(info);
//		}
		return true;
	}

	protected void handleIntent(Intent _intent) {
		mRefreshLayout.setRefreshing(true);
		mKeyword = _intent.getStringExtra(SearchManager.QUERY);
		if (!TextUtils.isEmpty(mKeyword)) {
			mKeyword = mKeyword.trim();
			resetSearchView();
			mSuggestions.saveRecentQuery(mKeyword, null);
		}
	}


	/**
	 * Reset the UI status of searchview.
	 */
	protected void resetSearchView() {
		if (mSearchView != null) {
			mSearchView.clearFocus();
		}
	}

	@Override
	protected void onNewIntent(Intent _intent) {
		super.onNewIntent(_intent);
		setIntent(_intent);
		handleIntent(_intent);
	}

	@Override
	public void onRefresh() {
		loadBooks();
	}

	private void loadBooks() {
		if (!TextUtils.isEmpty(mKeyword)) {
			loadByKeyword();
		} else {
			loadDefaultPage();
		}
	}

	/**
	 * Default page when nothing wanna be searched.
	 */
	private void loadDefaultPage() {
		String url = Prefs.getInstance(getApplication()).getApiDefaultBooks();
		new GsonRequestTask<DSBookList>(getApplicationContext(), Method.GET, url, DSBookList.class).execute();
	}


	/**
	 * Page when nothing wanna be searched.
	 */
	private void loadByKeyword() {
		String url = Prefs.getInstance(getApplication()).getApiSearchBooks();
		url = String.format(url, mKeyword, "1");
		new GsonRequestTask<DSBookList>(getApplicationContext(), Method.GET, url, DSBookList.class).execute();
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		loadDefaultPage();
	}


	@Override
	public boolean onQueryTextSubmit(String s) {
		InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
		resetSearchView();
		return false;
	}


	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DSBook book = (DSBook) mAdp.getItem(position);
		BookDetailActivity.showInstance(this, book.getId());
	}

	public void search(View view) {
		mKeyword = mSearchKeyEt.getText().toString();
		loadBooks();
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		loadDefaultPage();
	}
}
