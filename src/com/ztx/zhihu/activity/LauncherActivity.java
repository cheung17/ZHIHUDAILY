package com.ztx.zhihu.activity;

import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import com.ztx.zhihu.R;
import com.ztx.zhihu.util.DataInfo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author laughing 启动图片显示 3秒后跳转至MainActivity
 */
public class LauncherActivity extends Activity {
	private ImageView bgIV;
	private TextView bgAuthorTv;
	private SharedPreferences sf;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		setContentView(R.layout.activity_launch);
		DataInfo.initImageLoader(getApplicationContext());
		initviews();
		initBg();
	}

	private void initBg() {
		int date=sf.getInt("date", 0);
		System.out.println(date+": date");
		String cacheInfo = sf.getString("launch_info", "0");
		//如果sf保存的日期不等于今日 且网络可用
		if (date!=DataInfo.initToday()&&DataInfo.isNetWorkAvailable(getApplicationContext())) {
			FinalHttp finalHttp = new FinalHttp();
			finalHttp.get(DataInfo.ServerUrl.LAUNCHERPIC,
					new AjaxCallBack<Object>() {
						@Override
						public void onSuccess(Object t) {
							super.onSuccess(t);
							parseJson(t.toString());
							editor.putString("launch_info", t.toString());
							editor.putInt("date", DataInfo.initToday());
							editor.commit();
						}

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							bgIV.setImageResource(R.drawable.default_bg);
							bgAuthorTv.setText("image: 1tu/pitrs");
							setScaleAnimation();
							super.onFailure(t, errorNo, strMsg);
						}
					});
		} else if(date==DataInfo.initToday()) {
			//保存日期等于今日日期
			parseJson(cacheInfo);
		}else {
			bgIV.setImageResource(R.drawable.default_bg);
			bgAuthorTv.setText("image: 1tu/pitrs");
			setScaleAnimation();
			}
	}

	protected void setScaleAnimation() {
		Animation animation = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f,
				Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF,
				0.3f);
		animation.setFillAfter(true);
		animation.setDuration(2000);
		bgIV.startAnimation(animation);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(LauncherActivity.this, MainActivity.class));
				finish();
			}
		}, 2000);
	}

	private void initviews() {
		bgAuthorTv = (TextView) findViewById(R.id.tv_launch_img_author);
		bgIV = (ImageView) findViewById(R.id.iv_launch);
		sf = getSharedPreferences("launch_info", MODE_PRIVATE);
		editor = sf.edit();
	}

	protected void parseJson(String string) {
		try {
			System.out.println(string);
			System.out.println(string);
			JSONObject jsonObject = new JSONObject(string);
			DataInfo.getImageLoader().displayImage(jsonObject.getString("img"),
					bgIV, DataInfo.getOptions(0));
			bgAuthorTv.setText("image: " + jsonObject.getString("text"));
			setScaleAnimation();
		} catch (JSONException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	

}
