package com.itbooks.app.adapters;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.data.rest.RSBook;
import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;


public final class BookListAdapter extends AbstractBookViewAdapter<BookListAdapter.ViewHolder, RSBook> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_book_list;
	/**
	 * Main layout for this component.
	 */
	private static final int NO_IMAGE_ITEM_LAYOUT = R.layout.no_image_item_book_list;

	private boolean mShowImages;

	public BookListAdapter(List<RSBook> books, boolean showImages) {
		setData(books);
		mShowImages = showImages;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		//		boolean landscape = cxt.getResources().getBoolean(R.bool.landscape);
		View convertView = LayoutInflater.from(cxt).inflate(mShowImages ? ITEM_LAYOUT : NO_IMAGE_ITEM_LAYOUT, parent, false);
		return new BookListAdapter.ViewHolder(convertView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final RSBook book = getData().get(position);
		if(mShowImages) {
			try {
				Picasso picasso = Picasso.with(holder.itemView.getContext());
				picasso.load(Utils.uriStr2URI(book.getCoverUrl()).toASCIIString()).placeholder(R.drawable.ic_book)
						.tag(holder.itemView.getContext()).into(holder.mBookThumbIv);
			} catch (NullPointerException e) {
				holder.mBookThumbIv.setImageResource(R.drawable.ic_book);
			}
		}
		holder.mBookTitleTv.setText(book.getName());
		holder.mBookDescTv.setText(book.getDescription());
		holder.mBookSubTitleTv.setText(book.getAuthor());
		holder.mISBNTv.setText(String.format("ISBN: %s", book.getISBN()));
		holder.mSizeTv.setText(book.getSize());
		holder.mContentV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new OpenBookDetailEvent(book));
			}
		});
	}

	@Override
	public void setShowImages(boolean showImages) {
		mShowImages = showImages;
	}

	@Override
	public boolean showImages() {
		return mShowImages;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private View mContentV;
		private ImageView mBookThumbIv;
		private TextView mBookTitleTv;
		private TextView mBookDescTv;
		private TextView mBookSubTitleTv;
		private TextView mISBNTv;
		private TextView mSizeTv;

		/**
		 * Constructor of {@link com.itbooks.app.adapters.BookListAdapter.ViewHolder}.
		 *
		 * @param convertView
		 * 		The root {@link android.view.View}.
		 */
		public ViewHolder(View convertView) {
			super(convertView);
			mContentV  =   convertView.findViewById(R.id.content_v);
			mBookThumbIv = (ImageView) convertView.findViewById(R.id.book_thumb_iv);
			mBookTitleTv = (TextView) convertView.findViewById(R.id.book_title_tv);
			mBookDescTv = (TextView) convertView.findViewById(R.id.book_desc_tv);
			mBookSubTitleTv = (TextView) convertView.findViewById(R.id.book_subtitle_tv);
			mISBNTv = (TextView) convertView.findViewById(R.id.book_isbn_tv);
			mSizeTv  = (TextView) convertView.findViewById(R.id.book_size_tv);
		}
	}
}
