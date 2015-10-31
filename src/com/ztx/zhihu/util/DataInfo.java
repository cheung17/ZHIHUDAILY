package com.ztx.zhihu.util;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.tsz.afinal.FinalHttp;

import android.R.integer;
import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class DataInfo {
	public static final int FROMMAIN = 0;
	public static final int FROMCOLLECT = 1;
	public static String accessCode = "";
	public static String CLIENTID = "2262030957";// appkey
	public static String CLIENTSECRET = "0cc9bd6b3d885c3dc823c45bdd547cd5"; // appsecret
	public static String accessToken = ""; // sina tiken
	public static String sinaUserID = ""; // sina userid
	public static boolean isLogined = false; // sina userid
	public static String userName = "";
	public static String userAvater = "";
	public static final int MAINACTIVITY = 0;
	public static final int CONTENTREADACTIVITY = 2;
	public static final int COLLECTACTIVITY = 1;
	public static final int COMMENTACTIVITY = 3;
	public static class ServerUrl {
		public static String REDIRECTURI = "https://api.weibo.com/oauth2/default.html";// 重定向地址
		public static String LATESTNEWS = "http://news-at.zhihu.com/api/4/news/latest";
		public static String LAUNCHERPIC = "http://news-at.zhihu.com/api/4/start-image/1080*1776";
		public static String CONTENTREAD = "http://news-at.zhihu.com/api/4/news/";
		public static String NEWSBEFORE = "http://news.at.zhihu.com/api/4/news/before/";
		// 获取评论 拼接 contentid+long-comments或short-comments
		public static String COMMENTSURLZHIHU = "http://news-at.zhihu.com/api/4/story/";

		// 新浪授权地址
		public static final String SINAOAUTHURL = "https://api.weibo.com/oauth2/authorize?"
				+ "client_id=2262030957&display=mobile&redirect_uri=https://api.weibo.com/oauth2/default.html";
		public static final String ACCESSURL = "https://api.weibo.com/oauth2/access_token";
		public static final String SINAUSERINFO = "https://api.weibo.com/2/users/show.json";

		// 自建服务器地址
		// 评论

		public static final String COMMENTURL = "http://cheung17.java.jspee.cn/CommentServet";
		public static final String COLLECT = "http://cheung17.java.jspee.cn/CollectServet";

	}

	public static ImageLoader getImageLoader() {
		return ImageLoader.getInstance();

	}

	public static DisplayImageOptions getOptions(int radius) {
		DisplayImageOptions roundOptions = ((Builder) new DisplayImageOptions.Builder()
				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true))
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				// 设置图片以如何的编码方式显示
				.bitmapConfig(Bitmap.Config.RGB_565)
				// 设置图片的解码类型//
				.displayer(new FadeInBitmapDisplayer(100))
				// 图片加载好后渐入的动画时间
				.imageScaleType(ImageScaleType.NONE)
				.displayer(new RoundedBitmapDisplayer(radius)) // 图片缩放方式
				.build();// 构建完成
		return roundOptions;
	}

	public static void initSinaUserInfo(ImageView headIv, TextView usernameTv,
			SharedPreferences sf) {
		Editor editor;
		editor = sf.edit();
		if (!sf.getString("user_id", "").equals("")) {
			DataInfo.sinaUserID = sf.getString("user_id", "");
			DataInfo.accessToken = sf.getString("access_token", "");
			DataInfo.userName = sf.getString("username", "");
			DataInfo.userAvater = sf.getString("useravater", "");
			DataInfo.isLogined = true;
			// options
			getImageLoader().displayImage(DataInfo.userAvater, headIv,
					getOptions(100));
			usernameTv.setText(DataInfo.userName);
		} else {
			DataInfo.isLogined = false;
		}
	}

	/***
	 * 得到上一天日期
	 * 
	 * @param todayDatime2
	 * @return
	 */
	private int initLastDay() {
		Date mDate = null;
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

	public static void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				"imageloader/Cache"); // 缓存目录
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				.threadPoolSize(3)
				// 线程池加载数量
				.threadPriority(Thread.NORM_PRIORITY - 2)
				// 线程优先级
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024)
				.discCache(new UnlimitedDiscCache(cacheDir)).writeDebugLogs()
				.build(); // 开始构建
		ImageLoader.getInstance().init(config);
	}

	public static String getWeekday(String dateStr) {
		String m = dateStr.substring(4, 6);
		String day = dateStr.substring(6, 8);
		String[] weeks = new String[] { "日", "一", "二", "三", "四", "五", "六" };
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			Date d = df.parse(dateStr);
			return m + "月" + day + "日" + " 星期" + weeks[d.getDay()];
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static int initToday() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(date);
		return Integer.parseInt(today);
	}

	public static boolean isNetWorkAvailable(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
