package com.container.dataCollect.entity;

import java.util.HashMap;
import java.util.Map;

//测厚导出模板默认行数据
public class ThicknessRecord {

	//容器
	String partName;
	String c_spec;
	String no1;
	String c_v1;
	String no2;
	String c_v2;
	String no3;
	String c_v3;
	String no4;
	String c_v4;
	String no5;
	String c_v5;
	//管道
	String pipeName;
	String p_spec;
	String no;
	String p_v1;
	String p_v2;
	String p_v3;
	String p_v4;
	String p_v5;
	String p_v6;
	String p_v7;
	String p_v8;
	String p_v9;
	String p_v10;
	String p_v11;
	String p_v12;
	String p_v13;
	String p_v14;
	String p_v15;
	String p_v16;
	
	public Map<String, Object> toContainerMap() {
	    Map<String, Object> map = new HashMap<>();
	    map.put("partName", partName);
	    map.put("spec", c_spec);
	    map.put("no1", no1);
	    map.put("v1", c_v1);
	    map.put("no2", no2);
	    map.put("v2", c_v2);
	    map.put("no3", no3);
	    map.put("v3", c_v3);
	    map.put("no4", no4);
	    map.put("v4", c_v4);
	    map.put("no5", no5);
	    map.put("v5", c_v5);
	    return map;
	}
	
	public Map<String, Object> toPipeMap() {
	    Map<String, Object> map = new HashMap<>();
	    map.put("pipeName", pipeName);
	    map.put("spec", p_spec);
	    map.put("no", no);
	    map.put("v1", p_v1);
	    map.put("v2", p_v2);
	    map.put("v3", p_v3);
	    map.put("v4", p_v4);
	    map.put("v5", p_v5);
	    map.put("v6", p_v6);
	    map.put("v7", p_v7);
	    map.put("v8", p_v8);
	    map.put("v9", p_v9);
	    map.put("v10", p_v10);
	    map.put("v11", p_v11);
	    map.put("v12", p_v12);
	    map.put("v13", p_v13);
	    map.put("v14", p_v14);
	    map.put("v15", p_v15);
	    map.put("v16", p_v16);
	    return map;
	}
}
