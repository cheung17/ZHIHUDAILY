package com.ztx.zhihu.activity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.ztx.zhihu.R;
import com.ztx.zhihu.db.CollectBean;
import com.ztx.zhihu.db.CollectDbManager;
import com.ztx.zhihu.db.ContentBean;
import com.ztx.zhihu.db.DbManager;
import com.ztx.zhihu.db.NewsDbManager;
import com.ztx.zhihu.db.NewsDetailBean;
import com.ztx.zhihu.util.DataInfo;
import com.ztx.zhihu.view.MyScrollView;
import com.ztx.zhihu.view.MyScrollView.OnScrollListener;

public class ContentReadActivity extends Activity implements OnClickListener,
		OnScrollListener {
	private WebView webView;
	String dataUrl = null;
	private ImageView ivTopImg;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private TextView tvTitle, tvSource;
	private ScrollView scrollView;
	private ImageView collectIv;
	private ImageView shareIv;
	private ImageView commentIV;
	private ImageView backIv;
	private boolean isCollected = false;
	private CollectBean collectBean = new CollectBean();
	private CollectDbManager cDbManager;
	private int contentId;
	private MyScrollView myScrollView;
	private RelativeLayout rvTitleLayout;
	private String title, image;
	private int slideDis;
	private boolean hasGone = false;
	private String shareUrl="";
	private String titleString="";

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		setContentView(R.layout.read_content_layout);
		DataInfo.initImageLoader(getApplicationContext());
		contentId = this.getIntent().getIntExtra("id", 0);
		initImageLoaderOption();
		initViews();
		myScrollView.setOnScrollListener(this);
		QueryIsCollected();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initViews() {
		myScrollView = (MyScrollView) findViewById(R.id.scrollview);
		rvTitleLayout = (RelativeLayout) findViewById(R.id.rv_title_read);
		tvTitle = (TextView) findViewById(R.id.tv_title_news);
		tvSource = (TextView) findViewById(R.id.tv_image_source);
		ivTopImg = (ImageView) findViewById(R.id.iv_top_of_news);
		scrollView = (ScrollView) findViewById(R.id.scrollview);
		collectIv = (ImageView) findViewById(R.id.iv_collect);
		shareIv = (ImageView) findViewById(R.id.iv_share);
		backIv = (ImageView) findViewById(R.id.iv_back_read);
		commentIV = (ImageView) findViewById(R.id.iv_comment_read);
		commentIV.setOnClickListener(this);
		backIv.setOnClickListener(this);
		shareIv.setOnClickListener(this);
		collectIv.setOnClickListener(this);
		rvTitleLayout.getBackground().setAlpha(0);
		cDbManager = new CollectDbManager(getApplicationContext());
		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);// 支持js
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); // html内容居中
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setAppCacheEnabled(true);// 开启缓存
		// webView.getSettings().setBlockNetworkImage(true); //屏蔽图片
		// 设置加载进来的页面自适应手机屏幕
		int id = getIntent().getIntExtra("id", 0);
		// getFromLocal();
		if (new NewsDbManager(getApplicationContext()).findByContentId(id + "") != null) {
			getFromLocal(id);
		} else {
			getFromServer(id);
		}
	}

	private void QueryIsCollected() {
		FinalHttp finalHttp = new FinalHttp();
		String url = DataInfo.ServerUrl.COLLECT;
		finalHttp.get(url + "?userID=" + DataInfo.sinaUserID + "&contentID="
				+ contentId, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				super.onSuccess(t);
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(t.toString());
					if (jsonObject.getString("collected").equals("yes")) {
						isCollected = true;
						collectIv.setImageResource(R.drawable.collected);
					} else {
						isCollected = false;
						collectIv.setImageResource(R.drawable.collect);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
			}
		});

	}

	private void initImageLoaderOption() {
		imageLoader = ImageLoader.getInstance();
		options = ((Builder) new DisplayImageOptions.Builder().cacheInMemory(
				true)// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true))
		// 设置下载的图片是否缓存在SD卡中
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				// 设置图片以如何的编码方式显示
				.bitmapConfig(Bitmap.Config.RGB_565)
				// 设置图片的解码类型//
				.displayer(new FadeInBitmapDisplayer(100))// 图片加载好后渐入的动画时间
				.imageScaleType(ImageScaleType.NONE) // 图片缩放方式
				.build();// 构建完成
	}

	private void getFromLocal(int id) {
		NewsDbManager newsDb = new NewsDbManager(getApplicationContext());
		NewsDetailBean newsBean = newsDb.findByContentId(id + "");
		if (newsBean == null) {
			return;
		}
		// 如果有，则删除 并用新的插入readed替换
		String json = newsBean.getJson();
		newsDb.deleteByContentId(id + ""); // 删除
		// loadData(newsBean.getJson());
		saveData(id, json); // 保存新的bean
	}

	private void getFromServer(final int id) {
		FinalHttp fHttp = new FinalHttp();
		fHttp.get(DataInfo.ServerUrl.CONTENTREAD + id,
				new AjaxCallBack<Object>() {
					@Override
					public void onSuccess(Object t) {
						saveData(id, t.toString());
						super.onSuccess(t);
					}
				});
	}

	protected void saveData(int id, String json) {
		NewsDbManager newsDb = new NewsDbManager(getApplicationContext());
		NewsDetailBean newsBean = new NewsDetailBean();
		newsBean.setContentId(id);
		newsBean.setJson(json.toString());
		newsBean.setReaded(id); // 已阅读
		newsDb.addOne(newsBean);
		loadData(json.toString());
	}

	public void loadData(String json) {
		JSONObject jsonT;
		try {
			jsonT = new JSONObject(json.toString());
			tvTitle.setText(jsonT.getString("title"));
			title = jsonT.getString("title");
			if (jsonT.has("image_source")) {
				tvSource.setText(jsonT.getString("image_source"));
			}
			shareUrl=jsonT.getString("share_url");
			dataUrl = jsonT.getString("body");
			image = jsonT.getString("image");
			imageLoader.displayImage(jsonT.getString("image"), ivTopImg,
					options);
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
		case R.id.iv_back_read:
			finish();
			break;
		case R.id.iv_collect:
			if (!DataInfo.isNetWorkAvailable(getApplicationContext())) {
				// 网络不可用 直接跳过
				Toast.makeText(getApplicationContext(), "网络不可用", 1).show();
				break;
			}
			if (DataInfo.sinaUserID == "") { // 如果没有登陆 跳转到登陆界面
				Intent intent = new Intent(ContentReadActivity.this,
						SinaOAuthActivity.class);
				intent.putExtra("from", DataInfo.CONTENTREADACTIVITY);
				intent.putExtra("contentID", contentId);
				startActivity(intent);
				finish();
			} else {
				if (isCollected) {
					deleteFromCollect();
					cDbManager.deleteByContentID(getIntent().getIntExtra("id",
							0));
					isCollected = false;
					collectIv.setImageResource(R.drawable.collect);
				} else {
					collectToSql();
					isCollected = true;
					collectIv.setImageResource(R.drawable.collected);
				}
			}
			break;
		case R.id.iv_share:
			share();
			break;
		case R.id.iv_comment_read:
			Intent intent = new Intent(ContentReadActivity.this,
					CommentActivity.class);
			intent.putExtra("id", contentId);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void deleteFromCollect() {
		collectToServer(1 + "");
	}

	private void share() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, title+" 查看更多: "+shareUrl);
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	private void collectToSql() {
		collectToServer(0 + "");
		int id = getIntent().getIntExtra("id", 0);
		DbManager dbManager = new DbManager(getApplicationContext());
		ContentBean contentBean = dbManager.findByContentId(id).get(0);
		collectBean.setContentID(id);
		collectBean.setImage(contentBean.getImage());
		collectBean.setTitle(contentBean.getTitle());
		cDbManager.addOne(collectBean);
	}

	private void collectToServer(String type) {
		FinalHttp finalHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		// String url = "http://cheung17.java.jspee.cn/CollectServet";
		params.put("userID", DataInfo.sinaUserID);
		params.put("type", type);
		params.put("contentID", getIntent().getIntExtra("id", 0) + "");
		params.put("title", title);
		params.put("image", image);
		finalHttp.post(DataInfo.ServerUrl.COLLECT, params,
				new AjaxCallBack<Object>() {
					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
					}

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
					}
				});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			slideDis = ivTopImg.getBottom();

		}
	}

	// 监听scrollview滑动 改变标题栏透明度
	@Override
	public void onScroll(int scrollY) {
		// System.out.println(slideDis + ":dis" + "scrollY: " + scrollY);
		
		if (scrollY < slideDis) {
			setAapha(scrollY);
		}else {
			rvTitleLayout.getBackground().setAlpha(255);
		}
	}
	private void setAapha(int scrollY) {
		float h = slideDis;
		float y = scrollY;
		float alpha = (y / h) * 255;
		System.out.println("scrollY: "+scrollY);
		System.out.println(alpha+"");
		rvTitleLayout.getBackground().setAlpha((int) alpha);
	}
	private void startAnimation(float start, float end) {
		Animation alphaAnimation = new AlphaAnimation(start, end);
		alphaAnimation.setDuration(500);
		alphaAnimation.setFillAfter(true);
		rvTitleLayout.startAnimation(alphaAnimation);
	}

}
