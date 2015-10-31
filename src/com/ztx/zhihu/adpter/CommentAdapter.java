package com.ztx.zhihu.adpter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ztx.zhihu.R;
import com.ztx.zhihu.db.CommentsBean;
import com.ztx.zhihu.util.DataInfo;

public class CommentAdapter extends BaseAdapter {
	private List<CommentsBean> dataList = new ArrayList<CommentsBean>();
	Context context;
	private LayoutInflater mInflater;

	@Override
	public int getCount() {
		return dataList==null?0:dataList.size();
	}

	public CommentAdapter(List<CommentsBean> dataList, Context context) {
		super();
		this.dataList = dataList;
		mInflater = LayoutInflater.from(context);
	}

	public void refreshAdapter(List<CommentsBean> dataList) {
		this.dataList = dataList;
		this.notifyDataSetChanged();
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
		ViewHolder myHolder = null;
		System.out.println(dataList.size() + "");
		if (convertView == null) {
			myHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.comment_item, parent,
					false);
			myHolder.contentTv = (TextView) convertView
					.findViewById(R.id.tv_comment);
			myHolder.headIconIv = (ImageView) convertView
					.findViewById(R.id.iv_author_avatar);
			myHolder.nameTv = (TextView) convertView
					.findViewById(R.id.tv_author_name);
			myHolder.timeTv = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(myHolder);
		} else {
			myHolder = (ViewHolder) convertView.getTag();
		}

		fillData(position, myHolder);
		return convertView;
	}

	/**
	 * 填充数据
	 * 
	 * @param position
	 */
	private void fillData(int position, ViewHolder myHolder) {
		myHolder.contentTv.setText(dataList.get(position).getContent());
		DataInfo.getImageLoader().displayImage(
				dataList.get(position).getAvatar(), myHolder.headIconIv,
				DataInfo.getOptions(100));
		myHolder.nameTv.setText(dataList.get(position).getName());
		myHolder.timeTv.setText("");

	}

	public class ViewHolder {
		ImageView headIconIv;
		TextView nameTv, contentTv, timeTv;

	}
}
