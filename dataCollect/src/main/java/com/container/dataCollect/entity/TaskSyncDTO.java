package com.container.dataCollect.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

// 接收前端同步任务的DTO（数据传输对象）
@Data
public class TaskSyncDTO implements Serializable{
	private static final long serialVersionUID = 1L;
    // 前端传递的username字段
    private String username;
    // 前端传递的tasks数组（对应多个Task对象）
    private List<Task> tasks;
    
    
    
}