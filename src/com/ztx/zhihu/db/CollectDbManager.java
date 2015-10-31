package com.ztx.zhihu.db;

import java.util.List;

import android.content.Context;
import android.widget.Toast;

import net.tsz.afinal.FinalDb;

public class CollectDbManager {
	FinalDb finalDb;
	Context context;

	public CollectDbManager(Context context) {
		finalDb = FinalDb.create(context);
		this.context = context;
	}

	public List<CollectBean> getAll() {
		return finalDb.findAll(CollectBean.class);
	}

	public void deleteByContentID(int id){
		finalDb.deleteByWhere(CollectBean.class, "contentId="+id);
		
	}

	public CollectBean findByContentId(int id) {
		if (finalDb.findAllByWhere(CollectBean.class, "contentId=" + id).size() == 0) {
			return null;
		} else {
			return finalDb.findAllByWhere(CollectBean.class, "contentId=" + id)
					.get(0);
		}

	}

	public void addOne(CollectBean content) {
		finalDb.save(content);
	}

}
