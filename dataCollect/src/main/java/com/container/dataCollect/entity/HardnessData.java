package com.container.dataCollect.entity;

import java.util.HashMap;
import java.util.Map;

public class HardnessData {
	String bh;
	String bw;
	String yd1;
	String yd2;
	String yd3;
	String yd4;
	String yd5;
	String avg;
	String bz;
	
	public Map<String, Object> toMap() {
	    Map<String, Object> map = new HashMap<>();
	    map.put("bh", bh);
	    map.put("bw", bw);
	    map.put("yd1", yd1);
	    map.put("yd2", yd2);
	    map.put("yd3", yd3);
	    map.put("yd4", yd4);
	    map.put("yd5", yd5);
	    map.put("avg", avg);
	    map.put("bz", bz);
	    return map;
	}
}
