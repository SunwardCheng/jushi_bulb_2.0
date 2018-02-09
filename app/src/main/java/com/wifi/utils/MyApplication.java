package com.wifi.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;

public class MyApplication extends Application {

	private Map<String, Object> map;

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	
	public MyApplication() {
		
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		map = new HashMap<String, Object>();
		map = Collections.synchronizedMap(map);
	}
}
