package com.ztx.zhihu.activity;

import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import com.ztx.zhihu.R;
import com.ztx.zhihu.util.DataInfo;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SinaOAuthActivity extends Activity {
	private WebView webView;
	private SharedPreferences sf;
	private Editor editor;

	// DataInfo
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sina_oauth);
		initViews();
	}

	private void initViews() {
		webView = (WebView) findViewById(R.id.webview_sina);
		webView.getSettings().setJavaScriptEnabled(true);// 支持js
		// webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// // html内容居中
		// webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setAppCacheEnabled(true);// 开启缓存
		webView.loadUrl(DataInfo.ServerUrl.SINAOAUTHURL);
		webView.setWebViewClient(new MyWebViewCliebt());
		getWebViewUrl();
	}

	public class MyWebViewCliebt extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		}
	}

	private void getWebViewUrl() { // 递归截取code
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				String url = webView.getUrl();
				if (url.contains("code")) {
					DataInfo.accessCode = url.substring(url.indexOf("=") + 1,
							url.length());
					Toast.makeText(getApplicationContext(),
							DataInfo.accessCode, 1).show();
					getAccessToken();
					// get
					// startActivity(new
					// Intent(SinaOAuthActivity.this,MainActivity.class));
					// finish();
				} else {
					getWebViewUrl();
				}
			}

		}, 3000);

	}

	// 得到token uid
	private void getAccessToken() {//
		final FinalHttp fHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("client_id", DataInfo.CLIENTID);
		params.put("client_secret", DataInfo.CLIENTSECRET);
		params.put("code", DataInfo.accessCode);
		params.put("redirect_uri", DataInfo.ServerUrl.REDIRECTURI);
		fHttp.post(DataInfo.ServerUrl.ACCESSURL, params,
				new AjaxCallBack<Object>() {
					public void onSuccess(Object t) {
						// access_token string
						// 用于调用access_token，接口获取授权后的access token。
						// expires_in string access_token的生命周期，单位是秒数。
						// remind_in string
						// access_token的生命周期（该参数即将废弃，开发者请使用expires_in）。
						try {
							JSONObject jsont = new JSONObject(t.toString());
							System.out.println(jsont.getString("access_token"));
							DataInfo.sinaUserID = jsont.getString("uid");
							DataInfo.accessToken = jsont
									.getString("access_token");
							DataInfo.isLogined=true;
							sf = getSharedPreferences("sina_user_info",
									MODE_PRIVATE);
							editor = sf.edit();
							editor.putString("user_id", DataInfo.sinaUserID);
							editor.putString("access_token",
									DataInfo.accessToken);
							editor.commit();
							getUserInfo(fHttp);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						super.onSuccess(t);
					}
				});
	}

	// 通过id 得到用户头像 姓名
	protected void getUserInfo(FinalHttp fHttp) {
		String url = DataInfo.ServerUrl.SINAUSERINFO + "?access_token="
				+ DataInfo.accessToken + "&uid=" + DataInfo.sinaUserID;
		fHttp.get(url, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				System.out.println(t);
				try {
					JSONObject jsont = new JSONObject(t.toString());
					DataInfo.userName = jsont.getString("screen_name");
					DataInfo.userAvater = jsont.getString("avatar_large");
					editor.putString("username", DataInfo.userName);
					editor.putString("useravater", DataInfo.userAvater);
					editor.commit();
					backToWhicActivity();

				} catch (JSONException e) {
					e.printStackTrace();
				}
				super.onSuccess(t);
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
		backToWhicActivity();
	}

	// 根据跳转来的activity 跳回
	protected void backToWhicActivity() {
		int from = getIntent().getIntExtra("from", 000);
		int contentId = getIntent().getIntExtra("contentID", 00);
		Intent intent = null;
		switch (from) {
		case DataInfo.MAINACTIVITY:
			intent = new Intent(SinaOAuthActivity.this, MainActivity.class);
			startActivity(intent);
			break;
		case DataInfo.COLLECTACTIVITY:
			intent = new Intent(SinaOAuthActivity.this, CollectActivity.class);
			startActivity(intent);
			break;
		case DataInfo.CONTENTREADACTIVITY:
			intent = new Intent(SinaOAuthActivity.this,
					ContentReadActivity.class);
			intent.putExtra("id", contentId);
			startActivity(intent);
			break;
		case DataInfo.COMMENTACTIVITY:
			intent = new Intent(SinaOAuthActivity.this, CommentActivity.class);
			intent.putExtra("id", contentId);
			startActivity(intent);
			break;
		default:
			break;
		}
		finish();
	}
}
