package com.ztx.zhihu.db;

import android.content.Context;
import net.tsz.afinal.FinalDb;

public class NewsDbManager {
	private FinalDb finalDb;

	public NewsDbManager(Context context) {
		finalDb = FinalDb.create(context);
	}

	public NewsDetailBean findByContentId(String contentId) {
		if (finalDb.findAllByWhere(NewsDetailBean.class,
				"contentId=" + contentId).size() != 0) {
			return finalDb.findAllByWhere(NewsDetailBean.class,
					"contentId=" + contentId).get(0);
		} else {
			// finalDb.
			return null;
		}
	}
	public NewsDetailBean findByReaded(String contentId) {
		if (finalDb.findAllByWhere(NewsDetailBean.class,
				"readed=" + contentId).size() != 0) {
			return finalDb.findAllByWhere(NewsDetailBean.class,
					"readed=" + contentId).get(0);
		} else {
			// finalDb.
			return null;
		}
	}

	public void addOne(NewsDetailBean news) {
		finalDb.save(news);
	}

	public void deleteByContentId(String contentId) {
		finalDb.deleteByWhere(NewsDetailBean.class, "contentId=" + contentId);
	}
	
}
