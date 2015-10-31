package com.ztx.zhihu.db;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import net.tsz.afinal.FinalDb;

public class DbManager {
	private FinalDb finalDb;
     
	public DbManager(Context context) {
		finalDb = FinalDb.create(context);
	}

	public void clear() {
		//finalDb.findAllByWhere(clazz, strWhere)
        finalDb.delete(ContentBean.class);
	}
	public List<ContentBean> findByContentId(int id) {
		List<ContentBean> contents=finalDb.findAllByWhere(ContentBean.class, "contentID="+id);
		return contents;
	}
	public void deleteOneBydate(String date) {
		finalDb.deleteByWhere(ContentBean.class, "date="+date);
	}

	public void addOne(ContentBean content) {
		finalDb.save(content);
	}

	public void update() {

	}

	public void addAll(List<ContentBean> contents) {
		for (int i = 0; i < contents.size(); i++) {
             finalDb.save(contents.get(i));
           //  finalDb.update(entity, strWhere)
		}
	}
	
	public List<ContentBean> findByDate(String date) {
		List<ContentBean> contents=finalDb.findAllByWhere(ContentBean.class, "date="+date);
		return contents;
	}
	public List<ContentBean> getAll() {
		List<ContentBean> contents=finalDb.findAll(ContentBean.class,"date");
		
		return contents;
	}
}
