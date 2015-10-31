package com.ztx.zhihu.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import com.ztx.zhihu.R;
import com.ztx.zhihu.adpter.CommentAdapter;
import com.ztx.zhihu.db.CommentsBean;
import com.ztx.zhihu.util.DataInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class CommentActivity extends Activity implements OnClickListener,
		OnRefreshListener {
	private ImageView backIv;
	private ListView listView;
	private int contentId;
	private List<CommentsBean> dataList = new ArrayList<CommentsBean>();
	private List<CommentsBean> dataList1 = new ArrayList<CommentsBean>();
	private CommentAdapter adapter;
	private ImageView writeCommentIv;
	private SwipeRefreshLayout swipeRefreshLayout;
	private final int MYCOMMENTS = 0;
	private final int LONGCOMMENTS = 1;
	private final int SHORTCOMMENTS = 2;
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_comment);
		initViews();
		contentId = getIntent().getIntExtra("id", 0);
		getCommentsFromMyServer();
		// getLongComments();
		adapter = new CommentAdapter(dataList, getApplicationContext());
		listView.setAdapter(adapter);
	}

	private void getCommentsFromMyServer() {
		dataList.clear();
		String url = DataInfo.ServerUrl.COMMENTURL + "?contentId=" + contentId;
		getCommentFromSerVer(url, MYCOMMENTS);
	}

	private void getCommentFromSerVer(String url, final int index) {
		FinalHttp finalHttp = new FinalHttp();
		System.out.println(url);
		finalHttp.get(url, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				System.out.println(t.toString());
				parseJson(t.toString(),index);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				if (index == MYCOMMENTS) {
					getLongComments();
				} else if (index == LONGCOMMENTS) {
					getShortComments();
				} else {
					progressDialog.dismiss();
					swipeRefreshLayout.setRefreshing(false);
				}
				System.out.println(t.toString());
				System.out.println(t.toString());
			}
		});
	}

	// 长评论
	private void getLongComments() {
		String url = DataInfo.ServerUrl.COMMENTSURLZHIHU + contentId
				+ "/long-comments";
		getCommentFromSerVer(url, LONGCOMMENTS);
	}

	// 短评论
	private void getShortComments() {
		String url = DataInfo.ServerUrl.COMMENTSURLZHIHU + contentId
				+ "/short-comments";
		getCommentFromSerVer(url, SHORTCOMMENTS);
	}

	protected void parseJson(String json, int index) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray commentsArray = jsonObject.getJSONArray("comments");
			for (int i = 0; i < commentsArray.length(); i++) {
				if (commentsArray.length() == 0) {
					break;
				}
				JSONObject commentJson = commentsArray.getJSONObject(i);
				CommentsBean commentsBean = new CommentsBean();
				commentsBean.setAvatar(commentJson.getString("avatar"));
				commentsBean.setName(commentJson.getString("author"));
				// commentsBean.setTime(commentJson.getString("time"));
				commentsBean.setContent(commentJson.getString("content"));
				dataList.add(commentsBean);
			}
			if (index == MYCOMMENTS) {
				getLongComments();
			} else if (index == LONGCOMMENTS) {
				getShortComments();
			} else if (index == SHORTCOMMENTS) {
				swipeRefreshLayout.setRefreshing(false);
				adapter.notifyDataSetChanged();
				if (progressDialog != null) {
					progressDialog.dismiss();
				}

			}
			
		} catch (JSONException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	private void initViews() {
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("努力加载中");
		progressDialog.show();
		progressDialog.setCanceledOnTouchOutside(false);
		backIv = (ImageView) findViewById(R.id.iv_back_comment);
		listView = (ListView) findViewById(R.id.comment_lv);
		writeCommentIv = (ImageView) findViewById(R.id.iv_write_comment);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeRefreshLayout.setColorScheme(R.color.refresh_0);
		swipeRefreshLayout.setOnRefreshListener(this);
		writeCommentIv.setOnClickListener(this);
		backIv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_write_comment:
			writeOrLogin();
			break;
		case R.id.iv_back_comment:
			finish();
			break;
		default:
			break;
		}
	}

	private void writeOrLogin() {
		if (!DataInfo.isNetWorkAvailable(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "网络不可用", 1).show();
			return;
		}
		if (DataInfo.isLogined) {
			// 已经登陆 跳转评论界面
			Intent intent = new Intent(CommentActivity.this,
					WriteCommentActivity.class);
			intent.putExtra("id", contentId);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(CommentActivity.this,
					SinaOAuthActivity.class);
			intent.putExtra("from", DataInfo.COMMENTACTIVITY);
			intent.putExtra("contentID", contentId);
			startActivity(intent);
			System.out.println(DataInfo.CONTENTREADACTIVITY + "  content");
			finish();
		}
	}

	@Override
	public void onRefresh() {
		getCommentsFromMyServer();
	}
}
