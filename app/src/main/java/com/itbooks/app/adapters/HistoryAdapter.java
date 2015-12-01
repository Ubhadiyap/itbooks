package com.itbooks.app.adapters;

import java.io.File;
import java.util.List;

import android.app.DownloadManager;
import android.os.Environment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.bus.DownloadCopyEvent;
import com.itbooks.bus.DownloadDeleteEvent;
import com.itbooks.bus.DownloadOpenEvent;
import com.itbooks.bus.OpenBookDetailEvent;
import com.itbooks.data.rest.RSBook;
import com.itbooks.net.download.Download;

import de.greenrobot.event.EventBus;

/**
 * Adapter for history-list
 *
 * @author Xinyue Zhao
 */
public final class HistoryAdapter extends AbstractBookViewAdapter<HistoryAdapter.ViewHolder, Download> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_history;

	public HistoryAdapter( List<Download> downloadHistory ) {
		setData( downloadHistory );
	}


	@Override
	public HistoryAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
		View                      convertView = LayoutInflater.from( parent.getContext() ).inflate( ITEM_LAYOUT, null );
		HistoryAdapter.ViewHolder viewHolder  = new HistoryAdapter.ViewHolder( convertView );
		return viewHolder;
	}

	@Override
	public void onBindViewHolder( final ViewHolder viewHolder, int position ) {
		final Download download = getData().get( position );
		RSBook         book     = download.getBook();
		viewHolder.mBookNameTv.setText( book.getName() );
		CharSequence elapsedSeconds = DateUtils.getRelativeTimeSpanString( download.getTimeStamp(), System.currentTimeMillis(),
																		   DateUtils.MINUTE_IN_MILLIS
		);
		viewHolder.mTimeTv.setText( elapsedSeconds );
		setStatus( viewHolder, download );
		viewHolder.mDiv.setVisibility( position == getItemCount() - 1 ? View.GONE : View.VISIBLE );
	}

	private static void setStatus( final ViewHolder viewHolder, final Download download ) {
		PopupMenu menu = (PopupMenu) viewHolder.mFileV.getTag();
		menu.setOnMenuItemClickListener( new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick( MenuItem item ) {
				switch( item.getItemId() ) {
					case R.id.action_delete:
						EventBus.getDefault().post( new DownloadDeleteEvent( download ) );
						break;
					case R.id.action_copy:
						EventBus.getDefault().post( new DownloadCopyEvent( download ) );
						break;
					case R.id.action_book_info:
						EventBus.getDefault().post( new OpenBookDetailEvent( download ) );
						break;
				}
				return true;
			}
		} );
		switch( download.getStatus() ) {
			case DownloadManager.STATUS_PENDING:
				viewHolder.mFileV.setVisibility( View.INVISIBLE );
				viewHolder.mLoadingPb.setVisibility( View.VISIBLE );
				viewHolder.mStatusTv.setText( R.string.lbl_status_pending );
				viewHolder.mContentV.setOnClickListener( null );
				break;
			case DownloadManager.STATUS_RUNNING:
				viewHolder.mFileV.setVisibility( View.INVISIBLE );
				viewHolder.mLoadingPb.setVisibility( View.VISIBLE );
				viewHolder.mStatusTv.setText( R.string.lbl_status_running );
				viewHolder.mContentV.setOnClickListener( null );
				break;
			case DownloadManager.STATUS_FAILED:
				viewHolder.mFileV.setVisibility( View.INVISIBLE );
				viewHolder.mLoadingPb.setVisibility( View.GONE );
				viewHolder.mStatusTv.setText( R.string.lbl_status_failed );
				viewHolder.mContentV.setOnClickListener( null );
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				viewHolder.mFileV.setVisibility( View.VISIBLE );
				viewHolder.mLoadingPb.setVisibility( View.GONE );
				viewHolder.mStatusTv.setText( R.string.lbl_status_successfully );
				viewHolder.mContentV.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick( View v ) {
						File to = new File( App.Instance.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), download.getTargetName() );
						if( to.exists() ) {
							EventBus.getDefault().post( new DownloadOpenEvent( to ) );
						}
					}
				} );
				break;
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		private View     mFileV;
		private View     mDiv;
		private View     mContentV;
		private TextView mBookNameTv;
		private TextView mTimeTv;
		private TextView mStatusTv;
		private View     mLoadingPb;

		ViewHolder( View convertView ) {
			super( convertView );

			mFileV = convertView.findViewById( R.id.file_btn );
			final PopupMenu menu = new PopupMenu( convertView.getContext(), mFileV );
			mFileV.setTag( menu );
			menu.inflate( R.menu.history_item_menu );
			mFileV.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick( View v ) {
					menu.show();
				}
			} );

			mContentV = convertView.findViewById( R.id.content_v );
			mBookNameTv = (TextView) convertView.findViewById( R.id.book_name_tv );
			mTimeTv = (TextView) convertView.findViewById( R.id.time_tv );
			mStatusTv = (TextView) convertView.findViewById( R.id.status_tv );
			mDiv = convertView.findViewById( R.id.div );

			mLoadingPb = convertView.findViewById( R.id.loading_pb );
		}
	}
}
