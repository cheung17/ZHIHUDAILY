package com.ztx.zhihu.activity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import com.ztx.zhihu.R;
import com.ztx.zhihu.util.DataInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WriteCommentActivity extends Activity implements OnClickListener {
	private EditText commentEt;
	private ImageView backIv;
	private ImageView sendIv;
	private String contentId;

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
		setContentView(R.layout.activity_write_comment);
		initViews();
		contentId = getIntent().getIntExtra("id", 0) + "";
	}

	private void initViews() {
		commentEt = (EditText) findViewById(R.id.et_write_content);
		backIv = (ImageView) findViewById(R.id.iv_back_comment);
		sendIv = (ImageView) findViewById(R.id.iv_send_comment);
		backIv.setOnClickListener(this);
		sendIv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back_comment:
			finish();
			break;
		case R.id.iv_send_comment:
			if (DataInfo.isNetWorkAvailable(getApplicationContext())) {
				sendComment();
			} else {
				Toast.makeText(getApplicationContext(), "网络不可用",
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void sendComment() {
		// 发送评论至服务器
		String content = commentEt.getText() + "";
		FinalHttp finalHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("avatar", DataInfo.userAvater);
		params.put("author", DataInfo.userName);
		params.put("content", content);
		params.put("userID", DataInfo.sinaUserID);
		params.put("contentID", contentId);
		// http://cheung17.java.jspee.cn
		finalHttp.post(DataInfo.ServerUrl.COMMENTURL, params,
				new AjaxCallBack<Object>() {
					@Override
					public void onSuccess(Object t) {
						System.out.println(t.toString());
						Intent intent =new Intent(WriteCommentActivity.this,
								CommentActivity.class);
						intent.putExtra("id", getIntent().getIntExtra("id", 0));
						startActivity(intent);
						finish();
					};

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						Toast.makeText(getApplicationContext(), "服务器挂了", 1)
								.show();

					}
				});
	}

	@Override
	public void onBackPressed() {
		Intent intent =new Intent(WriteCommentActivity.this,
				CommentActivity.class);
		intent.putExtra("id", getIntent().getIntExtra("id", 0));
		startActivity(intent);
		finish();
	}
}
