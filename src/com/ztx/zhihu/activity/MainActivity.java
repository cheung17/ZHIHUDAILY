package com.ztx.zhihu.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ztx.zhihu.R;
import com.ztx.zhihu.adpter.MainAdpter;
import com.ztx.zhihu.db.ContentBean;
import com.ztx.zhihu.db.DbManager;
import com.ztx.zhihu.db.NewsDbManager;
import com.ztx.zhihu.db.NewsDetailBean;
import com.ztx.zhihu.util.DataInfo;
import com.ztx.zhihu.view.LoadListView;
import com.ztx.zhihu.view.LoadListView.OnLoadListener;

public class MainActivity extends Activity implements OnClickListener,
		OnLoadListener, OnRefreshListener {
	private DrawerLayout drawerLayout;
	private ImageView IvMenu;
	private LoadListView contentLv;
	private DisplayImageOptions options;
	private SwipeRefreshLayout swipeRefreshLayout;
	private List<ContentBean> dataList = new ArrayList<ContentBean>();
	private List<ContentBean> topNewsList = new ArrayList<ContentBean>();
	private MainAdpter contentAdpter;
	private TextView tvDateTitle;
	private ImageView downLoadIv, ImageLoaderIV;
	private boolean setFirsrt = true;
	private ImageLoader imageLoader;
	private Date mDate = new Date();
	private WebView webView;
	private ImageView HeadIv;
	private RelativeLayout homeLayout, collectLaout;
	private int datetime;
	private RelativeLayout downloadLayout;
	private TextView downLoadTv, userNameTv;
	private ImageView overFlowIv;
	int firstItem;
	int position = 0;

	@SuppressLint("InlinedApi")
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
		setContentView(R.layout.activity_main);
		DataInfo.initImageLoader(this);
		imageLoader = DataInfo.getImageLoader();
		options = DataInfo.getOptions(0);
		initViews();
		datetime = DataInfo.initToday();
		initFromlocal();// 加载本地
		contentAdpter = new MainAdpter(MainActivity.this, dataList,
				imageLoader, options, tvDateTitle, DataInfo.FROMMAIN);
		contentLv.setAdapter(contentAdpter);
		contentLv.setInterface(this);
	}

	private void initFromlocal() {
		DbManager dbManager = new DbManager(this);
		if (dbManager.getAll().size() > 0) {
			// 如果数据表里数据不为空，首先查询当日内容
			// 如果当日没有，查询前一天，直到查到
			if (dbManager.findByDate(datetime + "").size() == 0) {
				// 如果没有查到 继续千一天
				datetime = initLastDay();
				initFromlocal();
			}
			dataList = dbManager.findByDate(datetime + "");
			topNewsList = dbManager.findByDate(000 + ""); // 寻找banner的数据
			contentLv.setDataList(topNewsList, imageLoader, options);
		} else {
			// 数据表为空 从网络获取当日最新消息
			refreshData();
		}
	}

	@SuppressWarnings("static-access")
	@SuppressLint("SimpleDateFormat")
	private int initLastDay() {
		Date date = mDate;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, -1);// 把日期往后增加一天.整数往后推,负数往前移动
		date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String lastDay = sdf.format(date);
		mDate = date;
		return Integer.parseInt(lastDay);
	}

	private void LoadDataFromServer(String url) {
		FinalHttp fh = new FinalHttp();
		fh.get(url, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				try {
					ContentBean contentDb = null;
					JSONObject JSonT = new JSONObject(t.toString());
					JSONArray stories = JSonT.getJSONArray("stories");
					for (int i = 0; i < stories.length(); i++) {
						contentDb = new ContentBean();
						JSONObject story = stories.getJSONObject(i);
						contentDb.setTitle(story.getString("title"));
						contentDb.setContentID(story.getInt("id"));
						JSONArray images = story.getJSONArray("images");
						contentDb.setImage(images.getString(0));
						contentDb.setDate(Integer.parseInt(JSonT
								.getString("date")));
						dataList.add(contentDb);
						DbManager dbManager = new DbManager(MainActivity.this);
						dbManager.addOne(contentDb);
					}
					contentLv.onCompleteLoad();
					if (setFirsrt) {
						// 第一次 不刷新
						setFirsrt = false;
					} else {
						contentAdpter.refreshAdapter(dataList);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				super.onSuccess(t);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println("网络问题");
				System.out.println(strMsg.toString());
				System.out.println(t.toString());
				System.out.println(errorNo + "");
			}
		});

	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		contentLv = (LoadListView) findViewById(R.id.lv_content_main);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		IvMenu = (ImageView) findViewById(R.id.iv_menu);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setColorScheme(R.color.refresh_0);
		swipeRefreshLayout.setOnRefreshListener(this);
		tvDateTitle = (TextView) findViewById(R.id.tv_title_bar_main);
		IvMenu.setOnClickListener(this);
		overFlowIv = (ImageView) findViewById(R.id.iv_overflow);
		tvDateTitle.setOnClickListener(this);
		overFlowIv.setOnClickListener(this);
		downloadLayout = (RelativeLayout) findViewById(R.id.layout_download);
		downLoadTv = (TextView) findViewById(R.id.download_tv);
		downLoadIv = (ImageView) findViewById(R.id.iv_download);
		downLoadIv.setOnClickListener(this);
		HeadIv = (ImageView) findViewById(R.id.iv_head_icon);
		HeadIv.setOnClickListener(this);
		userNameTv = (TextView) findViewById(R.id.tv_user_name);
		// 初始化用户信息
		DataInfo.initSinaUserInfo(HeadIv, userNameTv,
				getSharedPreferences("sina_user_info", MODE_PRIVATE));
		// 隐藏的imageview 和webview 用以缓存webview 以及 Imageloader图片
		webView = (WebView) findViewById(R.id.webview_content);
		ImageLoaderIV = (ImageView) findViewById(R.id.iv_gone);
		initWebview();
		initDrawer();
		// 点击item 跳转阅读
		contentLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position > dataList.size()) {
					// 点击的是上拉加载那一项
					return;
				}
				Intent intent = new Intent(MainActivity.this,
						ContentReadActivity.class);
				intent.putExtra("id", dataList.get(position - 1).getContentID());
				startActivity(intent);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						contentAdpter.refreshAdapter(dataList);
					}
				}, 1000);

			}

		});
	}

	private void initDrawer() {
		homeLayout = (RelativeLayout) findViewById(R.id.home_layout);
		homeLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
		collectLaout = (RelativeLayout) findViewById(R.id.collect_layout);
		collectLaout.setOnClickListener(this);
		homeLayout.setOnClickListener(this);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebview() {
		webView.getSettings().setJavaScriptEnabled(true);// 支持js
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); // html内容居中
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setAppCacheEnabled(true);// 开启缓存
	}

	private void cacheFromServer(final int id) {
		FinalHttp fHttp = new FinalHttp();
		fHttp.get(DataInfo.ServerUrl.CONTENTREAD + id,
				new AjaxCallBack<Object>() {
					@Override
					public void onSuccess(Object t) {
						NewsDbManager newsDb = new NewsDbManager(
								getApplicationContext());
						NewsDetailBean newsBean = new NewsDetailBean();
						newsBean.setContentId(id);
						newsBean.setJson(t.toString());
						JSONObject jsonT;
						try {
							jsonT = new JSONObject(t.toString());
							imageLoader.displayImage(jsonT.getString("image"),
									ImageLoaderIV, options);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						newsDb.addOne(newsBean);
						loadData(t.toString());
						position = position + 1;
						if (position < dataList.size()) {
							getNewPosition(dataList.get(position)
									.getContentID(), newsDb);// 判断是否已保存
						}
						super.onSuccess(t);
					}

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						downLoadTv.setText("网络不可用");
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								startAnimation(1, 0);
							}
						}, 3000);
					}

				});
	}

	private void getNewPosition(int id, NewsDbManager newsDb) {
		downLoadTv.setText("正在离线下载最新内容" + position + "/" + dataList.size());
		if (position < dataList.size() - 1) {
			if (newsDb.findByContentId(id + "") == null) {
				// 如果没有，position可用
				cacheFromServer(dataList.get(position).getContentID());
			} else {
				// 如果已经有，index+1 继续检测
				position = position + 1;
				getNewPosition(dataList.get(position).getContentID(), newsDb);
			}
		} else {
			downLoadTv.setText("离线完毕");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					startAnimation(1, 0);
				}
			}, 3000);
			position = 0;
		}
	}

	public void loadData(String json) {
		// dataUrl=json.
		JSONObject jsonT;
		String dataUrl = null;
		try {
			jsonT = new JSONObject(json.toString());
			dataUrl = jsonT.getString("body");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
		String html = "<html><head>" + css + "</head><body>" + dataUrl
				+ "</body></html>";
		html = html.replace("<div class=\"img-place-holder\">", "");
		webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_menu:
			drawerLayout.openDrawer(Gravity.LEFT);
			break;
		case R.id.tv_title_bar_main:
			contentLv.setSelection(0);
			break;
		case R.id.iv_overflow:

			break;
		case R.id.iv_download:
			cacheWebView();
			break;
		case R.id.home_layout:
			drawerLayout.closeDrawers();
			break;
		case R.id.collect_layout:
			if (!DataInfo.isLogined) {
				gotoLogin();
			} else {
				startActivity(new Intent(MainActivity.this,
						CollectActivity.class));
				drawerLayout.closeDrawers();
				finish();
			}

			break;
		case R.id.iv_head_icon:
			if (!DataInfo.isLogined) {
				gotoLogin();
			}
			break;
		default:

			break;
		}
	}

	private void gotoLogin() {
		if (!DataInfo.isNetWorkAvailable(getApplicationContext())) {
			return;
		}
		Intent intent = new Intent(MainActivity.this, SinaOAuthActivity.class);
		intent.putExtra("from", DataInfo.MAINACTIVITY);
		startActivity(intent);
		finish();
	}

	private void cacheWebView() {
		if (DataInfo.isNetWorkAvailable(getApplicationContext())) {
			System.out.println("点击");
			downloadLayout.setVisibility(View.VISIBLE);
			// downLoadTv.setText("正在离线下载最新内容");
			startAnimation((float) 0.5, 1);
			cacheFromServer(dataList.get(position).getContentID());
		} else {
			Toast.makeText(getApplicationContext(), "网络问题", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void startAnimation(float start, float end) {
		Animation alphaAnimation = new AlphaAnimation(start, end);
		alphaAnimation.setDuration(500);
		alphaAnimation.setFillAfter(true);
		downloadLayout.startAnimation(alphaAnimation);
	}

	@Override
	public void onload() {
		datetime = initLastDay();
		DbManager dbManager = new DbManager(getApplicationContext());
		if (DataInfo.isNetWorkAvailable(getApplicationContext())) {
			// 网络可用 从网络读取
			dbManager.deleteOneBydate(datetime + "");// 删除之前可能并不完整的条目
			LoadDataFromServer(DataInfo.ServerUrl.NEWSBEFORE + (datetime + 1));
		} else {
			List<ContentBean> newList = dbManager.findByDate(datetime + "");
			for (int i = 0; i < newList.size(); i++) {
				dataList.add(newList.get(i));
			}
			Toast.makeText(getApplicationContext(),
					dataList.size() + "onload本地" + datetime + "",
					Toast.LENGTH_SHORT).show();
			contentLv.onCompleteLoad();
			contentAdpter.refreshAdapter(dataList);
		}
	}

	@Override
	public void onRefresh() {
		// 下拉刷新
		refreshData();
	}

	private void refreshData() {
		// 初始化用户信息
		DataInfo.initSinaUserInfo(HeadIv, userNameTv,
				getSharedPreferences("sina_user_info", MODE_PRIVATE));
		FinalHttp fh = new FinalHttp();
		fh.get(DataInfo.ServerUrl.LATESTNEWS, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				try {
					List<ContentBean> latestNews = new ArrayList<ContentBean>();
					DbManager dbManager = new DbManager(MainActivity.this);
					JSONObject JSonT = new JSONObject(t.toString());
					JSONArray stories = JSonT.getJSONArray("stories");
					for (int i = 0; i < stories.length(); i++) {
						ContentBean contentDb = new ContentBean();
						JSONObject story = stories.getJSONObject(i);
						// 如果数据库中不存在此文，则继续解析 反之跳出循环
						if (dbManager.findByContentId(story.getInt("id"))
								.size() == 0) {
							contentDb.setTitle(story.getString("title"));
							contentDb.setContentID(story.getInt("id"));
							JSONArray images = story.getJSONArray("images");
							contentDb.setImage(images.getString(0));
							contentDb.setDate(Integer.parseInt(JSonT
									.getString("date")));
							latestNews.add(contentDb);
							dbManager.addOne(contentDb);
						} else {
							break;
						}
					}
					for (int i = latestNews.size() - 1; i >= 0; i--) {
						// 添加到datalist里
						dataList.add(0, latestNews.get(i));
					}
					JSONArray topStories = JSonT.getJSONArray("top_stories");
					dbManager.deleteOneBydate(000 + "");// 清楚之前数据库的banner数据
					topNewsList.clear();
					for (int i = 0; i < topStories.length(); i++) {
						JSONObject jsonTop = topStories.getJSONObject(i);
						ContentBean contentDb = new ContentBean();
						contentDb.setImage(jsonTop.getString("image"));
						contentDb.setContentID(jsonTop.getInt("id"));
						contentDb.setTitle(jsonTop.getString("title"));
						contentDb.setDate(000);// date为000表示为banner的数据
						topNewsList.add(contentDb);
					}
					dbManager.addAll(topNewsList); // 存入新的banner数据
					contentLv.setDataList(topNewsList, imageLoader, options); // banner初始化
					// 停止刷新
					swipeRefreshLayout.setRefreshing(false);
					contentAdpter.refreshAdapter(dataList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				super.onSuccess(t);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println("网络问题");
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeRefreshLayout.setRefreshing(false);
						Toast.makeText(getApplicationContext(), "网络问题",
								Toast.LENGTH_SHORT).show();
					}
				}, 3000);
			}
		});

	}

	private long firstTime = 0;

	@Override
	public void onBackPressed() {
		long secondTime = System.currentTimeMillis();
		if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
			Toast.makeText(this, "再按一次退出日报", Toast.LENGTH_SHORT).show();
			firstTime = secondTime;// 更新firstTime
		} else { // 两次按键小于2秒时，退出应用
			finish();
			System.exit(0);
		}
	}

}
