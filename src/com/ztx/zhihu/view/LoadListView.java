package com.ztx.zhihu.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ztx.zhihu.R;
import com.ztx.zhihu.activity.ContentReadActivity;
import com.ztx.zhihu.adpter.MainAdpter;
import com.ztx.zhihu.db.ContentBean;

public class LoadListView extends ListView implements OnScrollListener,
		OnPageChangeListener {
	private View footerView;
	private View bannerView;
	private ImageView bannerIV;
	private TextView topTitle, tip0, tip1, tip2, tip3, tip4;
	private Button btnButton;
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;
	int lastVisibleItem;
	private ImageLoader imageLoader ;
	private DisplayImageOptions options;
	private List<ContentBean> dataList = new ArrayList<ContentBean>();
	int totalItem;
	private List<View> viewList = new ArrayList<View>();
	boolean isloading;
	OnLoadListener loadListener;

	public LoadListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(context);
	}

	public LoadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(context);
	}

	public LoadListView(Context context) {
		super(context);
		initViews(context);

	}

	private void initViews(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		footerView = inflater.inflate(R.layout.bottom_footer, null);
		bannerView = inflater.inflate(R.layout.banner_view, null);
		tip0 = (TextView)bannerView.findViewById(R.id.vp0);
		tip1 = (TextView)bannerView. findViewById(R.id.vp1);
		tip2 = (TextView) bannerView.findViewById(R.id.vp2);
		tip3 = (TextView) bannerView.findViewById(R.id.vp3);
		tip4 = (TextView) bannerView.findViewById(R.id.vp4);
		bannerView.setVisibility(View.VISIBLE);
		viewPager = (ViewPager) bannerView.findViewById(R.id.viewPager);
		footerView.findViewById(R.id.load_layout).setVisibility(View.VISIBLE);
		this.addFooterView(footerView); // 添加底部view
		this.addHeaderView(bannerView);
		
	}
	private void setView(int position) {
		bannerIV = (ImageView) viewList.get(position).findViewById(
				R.id.banner_iv);
		final int index =position;
		bannerIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent =new Intent(getContext(),ContentReadActivity.class);
				intent.putExtra("id", dataList.get(index).getContentID());
				getContext().startActivity(intent);
                Toast.makeText(getContext(), dataList.get(index).getContentID()+"", 1)	.show();			
			}
		});
		topTitle = (TextView) viewList.get(position).findViewById(
				R.id.tv_top_title);
		topTitle.setText(dataList.get(position).getTitle());
		//imageLoader加载图片
	    imageLoader.displayImage(dataList.get(position).getImage(), bannerIV, options);
	}

	/**
	 * 
	 * 
	 * scrollState=SCROLL_STATE_TOUCH_SCROLL :仍在滑动
	 * scrollState=SCROLL_STATE_FLING 页面惯性滑动 scrollState=SCROLL_STATE_IDLE :滑动结束
	 * 
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 当最会一个可见的item的position等于总item且不为0时且停止滚动及达到加载条件
		if (lastVisibleItem == totalItem && totalItem > 0
				&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			if (!isloading) {
				footerView.findViewById(R.id.load_layout).setVisibility(
						View.VISIBLE);
				isloading = true;
				loadListener.onload();
			}
		}
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.totalItem = totalItemCount;
		// 最后一个可见的item的position=第一个可见的item position+可见item总数
		this.lastVisibleItem = firstVisibleItem + visibleItemCount;
		//if(firstVisibleItem!=0){
		MainAdpter.setDateTitle(firstVisibleItem);
		//}
	}

	public void onCompleteLoad() { // 加载完成时回调
		isloading = false;
		footerView.findViewById(R.id.load_layout).setVisibility(View.GONE);
	}

	public void setInterface(OnLoadListener loadListener) { // 实现接口
		this.setOnScrollListener(this);
		this.loadListener = loadListener;
	}

	public interface OnLoadListener {
		public void onload();
	}

	public void setDataList(List<ContentBean> topNewsList, ImageLoader imageLoader, DisplayImageOptions options) {
		dataList = topNewsList;
	    this.imageLoader =imageLoader;
	    this.options=options;
	    viewList.clear();
		for (int i = 0; i < dataList.size(); i++) {
			View view = View.inflate(getContext(), R.layout.banner_item, null);
			viewList.add(view);
		}
		viewPager.setAdapter(new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == arg1;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));
				setView(position);
				return viewList.get(position);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return viewList.size();
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				// TODO Auto-generated method stub
				container.removeView(viewList.get(position));
			}

		});
		viewPager.setOnPageChangeListener(this);
		switchItem();
		
	}

	private void switchItem() {
         	int curPage =viewPager.getCurrentItem();	
         	if(curPage==4){
         		viewPager.setCurrentItem(0, true);
         	}else {
         		viewPager.setCurrentItem(curPage+1, true);
			}
         	new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					switchItem();	
				}
			}, 10000);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		resetColor();
		switch (arg0) {
		case 0:
			tip0.setBackgroundResource(R.drawable.circle_selected);
			break;
		case 1:
			tip1.setBackgroundResource(R.drawable.circle_selected);
			break;
		case 2:
			tip2.setBackgroundResource(R.drawable.circle_selected);
			break;
		case 3:
			tip3.setBackgroundResource(R.drawable.circle_selected);
			break;
		case 4:
			tip4.setBackgroundResource(R.drawable.circle_selected);
			break;

		default:
			break;
		}
	}

	private void resetColor() {
		tip0.setBackgroundResource(R.drawable.circle_bg);
		tip1.setBackgroundResource(R.drawable.circle_bg);
		tip2.setBackgroundResource(R.drawable.circle_bg);
		tip3.setBackgroundResource(R.drawable.circle_bg);
		tip4.setBackgroundResource(R.drawable.circle_bg);
	}

}
