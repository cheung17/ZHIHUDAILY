package com.ztx.zhihu.adpter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ztx.zhihu.R;
import com.ztx.zhihu.db.ContentBean;
import com.ztx.zhihu.db.NewsDbManager;
import com.ztx.zhihu.util.DataInfo;

//import android.support.v7.widget.CardView;

public class MainAdpter extends BaseAdapter {
	private static List<ContentBean> dataList;
	private Context context;
	// private ViewHolder viewHolder;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	public static TextView titlebarTv;
	private DisplayImageOptions options;
	private final int TYPE_NORMAL = 0; // 普通布局
	private final int TYPE_DATE = 1; // 带日期布局
    private final int FROM;
	public void refreshAdapter(List<ContentBean> dataList) {
		this.dataList = dataList;
		this.notifyDataSetChanged();
	}
	public static void setDateTitle(int firstVisibleItem) {
		if (firstVisibleItem == 0) {
			titlebarTv.setText("首页");
			return;
		}if (dataList.get(firstVisibleItem -1).getDate()==DataInfo.initToday()) {
			//如果第一可见item日期等于今日 
			titlebarTv.setText("今日热闻");    
		}else {
			titlebarTv.setText(DataInfo.getWeekday(dataList.get(
					firstVisibleItem -1).getDate()
					+ ""));
		}
	}

	public MainAdpter(Context context, List<ContentBean> dataList,
			ImageLoader imageLoader, DisplayImageOptions options,
			TextView titlebarTv1,int from) {
		this.context = context;
		this.FROM=from;
		MainAdpter.dataList = dataList;
		this.imageLoader = imageLoader;
		MainAdpter.titlebarTv = titlebarTv1;
		this.options = options;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getViewTypeCount() {

		return  2;
	}

	@Override
	public int getItemViewType(int position) {
		if (FROM==DataInfo.FROMCOLLECT) {
			return TYPE_NORMAL;
		}else {
			if (position == 0
					|| dataList.get(position).getDate() != dataList.get(
							position - 1).getDate()) {
				return TYPE_DATE;
			} else {
				return TYPE_NORMAL;
			}
		}
		
	}

	@Override
	public int getCount() {
		return dataList != null? dataList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		ViewHolderWithDate dateHolder = null;
		int viewType = getItemViewType(position);
		if (convertView == null) {
			if (viewType == TYPE_NORMAL) {
				convertView = inflater.inflate(R.layout.main_content_item,
						parent, false);
				viewHolder = new ViewHolder();
				viewHolder.tvTitle = (TextView) convertView
						.findViewById(R.id.tv_item_title);
				viewHolder.ivImage = (ImageView) convertView
						.findViewById(R.id.iv_item_pic);
				convertView.setTag(viewHolder);
			} else {
				convertView = inflater.inflate(
						R.layout.main_content_item_with_date, parent, false);
				dateHolder = new ViewHolderWithDate();
				dateHolder.tvTitle = (TextView) convertView
						.findViewById(R.id.tv_item_title);
				dateHolder.ivImage = (ImageView) convertView
						.findViewById(R.id.iv_item_pic);
				dateHolder.tvDate = (TextView) convertView
						.findViewById(R.id.tv_news_date);
				convertView.setTag(dateHolder);
			}

		} else {
			if (viewType == TYPE_NORMAL) {
				viewHolder = (ViewHolder) convertView.getTag();
			} else {
				dateHolder = (ViewHolderWithDate) convertView.getTag();
			}

		}
		if (viewType == TYPE_NORMAL) {
			viewHolder.tvTitle.setText(dataList.get(position).getTitle());
			setImage(position, viewHolder.ivImage);
			NewsDbManager newsDbManager =new NewsDbManager(context);
			if (FROM==DataInfo.FROMMAIN) {
				if (newsDbManager.findByReaded(dataList.get(position).getContentID()+"")!=null) {
					viewHolder.tvTitle.setTextColor(Color.parseColor("#C0BEBE"));
				}else {
					viewHolder.tvTitle.setTextColor(Color.parseColor("#555454"));
				}
			}
			
		} else {
			dateHolder.tvTitle.setText(dataList.get(position).getTitle());
			setImage(position, dateHolder.ivImage);
			NewsDbManager newsDbManager =new NewsDbManager(context);
			if (newsDbManager.findByReaded(dataList.get(position).getContentID()+"")!=null) {
				dateHolder.tvTitle.setTextColor(Color.parseColor("#C0BEBE"));
			}else {
				dateHolder.tvTitle.setTextColor(Color.parseColor("#555454"));
			}
			if (position == 0) {
				dateHolder.tvDate.setText("今日热闻");
			} else {
				dateHolder.tvDate.setText(DataInfo.getWeekday(dataList.get(
						position).getDate()
						+ ""));
			}

		}

		return convertView;
	}

	private void setImage(int position, ImageView imageView) {
		// imageLoader 显示图片
		imageLoader.displayImage(dataList.get(position).getImage(), imageView,
				options);
	}

	public class ViewHolder {
		private TextView tvTitle;
		private ImageView ivImage;
	}

	public class ViewHolderWithDate {
		private TextView tvTitle;
		private ImageView ivImage;
		private TextView tvDate;
	}
}
