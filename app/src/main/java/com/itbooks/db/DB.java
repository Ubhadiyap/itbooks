package com.itbooks.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;

import javax.annotation.Nullable;


/**
 * Defines methods that operate on database.
 * <p/>
 * <b>Singleton pattern.</b>
 * <p/>
 * <p/>
 *
 * @author Xinyue Zhao
 */
public final class DB {
	/**
	 * {@link android.content.Context}.
	 */
	private Context mContext;
	/**
	 * Impl singleton pattern.
	 */
	private static DB sInstance;
	/**
	 * Helper class that create, delete, update tables of database.
	 */
	private DatabaseHelper mDatabaseHelper;
	/**
	 * The database object.
	 */
	private SQLiteDatabase mDB;

	/**
	 * Constructor of {@link DB}. Impl singleton pattern so that it is private.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	private DB(Context cxt) {
		mContext = cxt;
	}

	/**
	 * Get instance of  {@link  DB} singleton.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 *
	 * @return The {@link DB} singleton.
	 */
	public static DB getInstance(Context cxt) {
		if (sInstance == null) {
			sInstance = new DB(cxt);
		}
		return sInstance;
	}

	/**
	 * Open database.
	 */
	public synchronized void open() {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mDB = mDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Close database.
	 */
	public synchronized void close() {
		mDatabaseHelper.close();
	}

	/**
	 * @return The count of downloaded instance.
	 */
	public synchronized int getDownloadsCount() {
		return getObjectDbCount(DownloadsTbl.TABLE_NAME);
	}

	/**
	 * Get tables rows count.
	 *
	 * @param tableName
	 * 		Table name.
	 *
	 * @return Count of all rows.
	 */
	private synchronized int getObjectDbCount(String tableName) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		String countQuery = "SELECT " + DownloadsTbl.ID + " FROM " + tableName;
		Cursor cursor = mDB.rawQuery(countQuery, null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;
	}

	/**
	 * Insert a new download instance.
	 *
	 * @param download
	 * 		{@link Download} A download instance.
	 *
	 * @return {@code true} if insert successed.
	 */
	public synchronized boolean insertNewDownload(Download download) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		boolean success = false;
		try {
			long rowId = -1;
			ContentValues v = new ContentValues();

			RSBook book = download.getBook();

			v.put(DownloadsTbl.BOOK_NAME, book.getName());
			v.put(DownloadsTbl.BOOK_AUTH, book.getAuthor());
			v.put(DownloadsTbl.BOOK_SIZE, book.getSize());
			v.put(DownloadsTbl.BOOK_PAGES, book.getPages());
			v.put(DownloadsTbl.BOOK_LINK, book.getLink());
			v.put(DownloadsTbl.BOOK_ISBN, book.getISBN());
			v.put(DownloadsTbl.BOOK_YEAR, book.getYear());
			v.put(DownloadsTbl.BOOK_PUB, book.getPublisher());
			v.put(DownloadsTbl.BOOK_DESC, book.getDescription());
			v.put(DownloadsTbl.BOOK_COVER_URL, book.getCoverUrl());
			v.put(DownloadsTbl.DOWNLOAD_ID, download.getDownloadId());
			v.put(DownloadsTbl.EDIT_TIME, download.getTimeStamp());

			rowId = mDB.insert(DownloadsTbl.TABLE_NAME, null, v);
			success = rowId != -1;
		} finally {
			close();
		}
		return success;
	}

	/**
	 * Get instance of download.
	 *
	 * @param downloadId
	 * 		The id provided by Android when started downloading.
	 *
	 * @return {@link Download}.
	 */
	public
	@Nullable
	Download getDownload(long downloadId) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		Cursor c;
		String whereClause = DownloadsTbl.DOWNLOAD_ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(downloadId) };
		c = mDB.query(DownloadsTbl.TABLE_NAME, null, whereClause, whereArgs, null, null, null, null);
		Download item = null;
		try {
			RSBook book;
			while (c.moveToNext()) {
				book = new RSBook(c.getString(c.getColumnIndex(DownloadsTbl.BOOK_NAME)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_AUTH)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_SIZE)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_PAGES)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_LINK)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_ISBN)), c.getString(
						c.getColumnIndex(DownloadsTbl.BOOK_YEAR)), c.getString(c.getColumnIndex(DownloadsTbl.BOOK_PUB)),
						c.getString(c.getColumnIndex(DownloadsTbl.BOOK_DESC)), c.getString(c.getColumnIndex(
						DownloadsTbl.BOOK_COVER_URL)));

				item = new Download(book);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			close();
		}
		return item;
	}
}
