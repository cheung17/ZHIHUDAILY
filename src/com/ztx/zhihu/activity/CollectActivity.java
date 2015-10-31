package com.ztx.zhihu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.ztx.zhihu.R;
import com.ztx.zhihu.adpter.MainAdpter;
import com.ztx.zhihu.db.CollectBean;
import com.ztx.zhihu.db.CollectDbManager;
import com.ztx.zhihu.db.ContentBean;
import com.ztx.zhihu.db.DbManager;
import com.ztx.zhihu.util.DataInfo;

public class CollectActivity extends Activity implements OnClickListener,
		OnRefreshListener {
	private ImageView ivMenu, headIV;
	private RelativeLayout homeLayout, collectLayout;
	private ListView listView;
	private DrawerLayout drawerLayout;
	private TextView usernameTv;
	private List<CollectBean> dataList = new ArrayList<CollectBean>();
	private List<ContentBean> contentBeans = new ArrayList<ContentBean>();
	private MainAdpter mainAdpter;
	private ImageLoader imageLoader = DataInfo.getImageLoader();
	private DisplayImageOptions options = DataInfo.getOptions(0);
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_collect);
		initViews();
		ReadCollects();
		mainAdpter = new MainAdpter(getApplicationContext(), contentBeans,
				imageLoader, options, MainAdpter.titlebarTv,
				DataInfo.FROMCOLLECT);
		listView.setAdapter(mainAdpter);
	}

	private void ReadCollects() {
		dataList.clear();
		// ;
		if (DataInfo.isNetWorkAvailable(getApplicationContext())) {
			LoadFromServer(); // 网络可用读取网络数据
		} else { // 不可用 读取本地数据
			CollectDbManager cdb = new CollectDbManager(getApplicationContext());
			DbManager dbManager = new DbManager(getApplicationContext());
			dataList = cdb.getAll();
			converToBean();
		}
		swipeRefreshLayout.setRefreshing(false);
	}

	private void converToBean() {
		System.out.println(dataList.size() + "");
		System.out.println(dataList.size() + "");
		if (dataList.size() != 0) {
			contentBeans.clear();
			for (int i = dataList.size() - 1; i >= 0; i--) {
				ContentBean contentBean = new ContentBean();
				contentBean.setContentID(dataList.get(i).getContentID());
				contentBean.setImage(dataList.get(i).getImage());
				contentBean.setTitle(dataList.get(i).getTitle());
				System.out.println(dataList.get(i).getTitle());
				contentBeans.add(contentBean);
			}
			if (mainAdpter != null) {
				mainAdpter.notifyDataSetChanged();
			}
		}
	}

	private void LoadFromServer() {
		FinalHttp finalHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("type", "2");
		params.put("userID", DataInfo.sinaUserID);
		// http://cheung17.java.jspee.cn
		String url = "http://cheung17.java.jspee.cn/CollectServet";
		finalHttp.post(url, params, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				super.onSuccess(t);
				parseJson(t.toString());
				converToBean();
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println(t.toString());
				System.out.println(t.toString());
				System.out.println(t.toString());
			}
		});
	}

	protected void parseJson(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray array = jsonObject.getJSONArray("collects");
			CollectBean collectBean;
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				collectBean = new CollectBean();
				collectBean.setContentID(Integer.parseInt(json
						.getString("contentId")));
				collectBean.setImage(json.getString("image"));
				collectBean.setTitle(json.getString("title"));
				dataList.add(collectBean);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@SuppressLint("ResourceAsColor")
	private void initViews() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_collect);
		ivMenu = (ImageView) findViewById(R.id.iv_menu_collect);
		headIV = (ImageView) findViewById(R.id.iv_head_icon);
		usernameTv = (TextView) findViewById(R.id.tv_user_name);
		homeLayout = (RelativeLayout) findViewById(R.id.home_layout);
		collectLayout = (RelativeLayout) findViewById(R.id.collect_layout);
		collectLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_collect);
		swipeRefreshLayout.setColorScheme(R.color.refresh_0);
		swipeRefreshLayout.setOnRefreshListener(this);
		ivMenu.setOnClickListener(this);
		headIV.setOnClickListener(this);
		homeLayout.setOnClickListener(this);
		collectLayout.setOnClickListener(this);
		// 初始化用户个人信息
		DataInfo.initSinaUserInfo(headIV, usernameTv,
				getSharedPreferences("sina_user_info", MODE_PRIVATE));
		listView = (ListView) findViewById(R.id.lv_collect);
		setListViewClick();
	}

	private void setListViewClick() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(CollectActivity.this,
						ContentReadActivity.class);
				intent.putExtra("id", contentBeans.get(position).getContentID());
				startActivity(intent);
			}

		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_menu_collect:
			drawerLayout.openDrawer(Gravity.LEFT);
			break;
		case R.id.iv_head_icon:
			if (!DataInfo.isLogined) {
				startActivity(new Intent(CollectActivity.this,
						SinaOAuthActivity.class));
				finish();
			}
			break;
		case R.id.home_layout:
			startActivity(new Intent(CollectActivity.this, MainActivity.class));
			finish();
			break;
		case R.id.collect_layout:
			drawerLayout.closeDrawers();
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		if (contentBeans != null) {
			ReadCollects();
		}

	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		startActivity(new Intent(CollectActivity.this, MainActivity.class));
		finish();
	}
}
