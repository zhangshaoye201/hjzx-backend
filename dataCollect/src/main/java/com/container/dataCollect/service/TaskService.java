package com.container.dataCollect.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.container.dataCollect.entity.HardnessData;
import com.container.dataCollect.entity.Task;
import com.container.dataCollect.entity.TaskQueryDTO;
import com.container.dataCollect.entity.ThicknessRecord;
import com.container.dataCollect.repository.TaskRepository;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.Pictures;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import utils.PoiTlWordUtil;

/**
 * 任务服务接口
 */
@Service
public class TaskService {
	//测厚容器记录行数
    private static final int THICKNESS_MAX_ROW_CONTAINER = 13;
    // 测厚-管道主表固定行数 
    private static final int THICKNESS_MAX_ROW_PIPE = 10;
    // 测厚续页固定行数 
    private static final int THICKNESS_MAX_ROW_CONTINUE = 29;
    // 硬度记录表格固定行数 
    private static final int HARDNESS_MAX_ROW = 20;
    // 容器分类编码 
    private static final String CATEGORY_CONTAINER = "2RD41";

	@Autowired
	private TaskRepository taskRepository;
	
	public boolean save(List<Task> tasks) {
		taskRepository.saveAll(tasks);
		return true;
	}
    
    public Page<Task> findTaskPage(TaskQueryDTO query) {
    	Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        return taskRepository.findByCondition(query,pageable);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void claimTasks(List<String> taskIds, String claimBy) {
        taskRepository.updateClaimByBatch(taskIds, claimBy);
    }
    
    public Task findTaskById(String taskId) {
        return taskRepository.findByTaskId(taskId);
    }
    
    public Map<String, Object> initDataMap(Task task,String tags) {
    	Map<String, Object> dataMap = new HashMap<>();
    	try {
    		dataMap.put("task", task);
    		
    		List<String> selectTags = Arrays.asList(tags.split(","));
    		dataMap.put("evaluate", selectTags.contains("evaluate"));
    		dataMap.put("review", selectTags.contains("review"));
    		dataMap.put("macro", selectTags.contains("macro"));
    		dataMap.put("macro1", selectTags.contains("macro1"));
    		dataMap.put("safety", selectTags.contains("safety"));
    		dataMap.put("thickness", selectTags.contains("thickness"));
    		dataMap.put("thicknessContinue", selectTags.contains("thicknessContinue"));
    		dataMap.put("thicknessImg", selectTags.contains("thicknessImg"));
    		dataMap.put("sound", selectTags.contains("sound"));
    		dataMap.put("penetrate", selectTags.contains("penetrate"));
    		dataMap.put("magnetic", selectTags.contains("magnetic"));
    		dataMap.put("hardness", selectTags.contains("hardness"));
    		
    		// ---------------------- 测厚数据处理 ----------------------
    		boolean isContainer = CATEGORY_CONTAINER.equals(task.getCategory());
    		int mainFixRow = isContainer ? THICKNESS_MAX_ROW_CONTAINER : THICKNESS_MAX_ROW_PIPE;
    		List<Object> mainRecords = new ArrayList<>();
    		List<Object> continueRecords = new ArrayList<>();

    		if (ObjectUtils.isNotEmpty(task.getThickness())) {
    		    Map<String, Object> thicknessMap = (Map<String, Object>) task.getThickness();
    		    // 读取原始测厚记录
    		    List<Object> originRecords = new ArrayList<>();
    		    if (ObjectUtils.isNotEmpty(thicknessMap.get("records"))) {
    		        originRecords = (List<Object>) thicknessMap.get("records");
    		    }
    		    // 截取主表前N行
    		    int takeNum = Math.min(originRecords.size(), mainFixRow);
    		    mainRecords.addAll(originRecords.subList(0, takeNum));
    		    // 补齐主表到固定行数
    		    fillThicknessRow(mainRecords, mainFixRow, isContainer);
    		    dataMap.put("thicknessRecords", mainRecords);

    		    // 处理续页：原始记录超过主表才生成续页
    		    if (originRecords.size() > mainFixRow) {
    		        continueRecords.addAll(originRecords.subList(mainFixRow, originRecords.size()));
    		        fillThicknessRow(continueRecords, THICKNESS_MAX_ROW_CONTINUE, isContainer);
    		        dataMap.put("thicknessContinueRecords", continueRecords);
    		    }

    		    // 处理示意图图片
    		    if (ObjectUtils.isNotEmpty(thicknessMap.get("imgs"))) {
    		        String imgsJson = thicknessMap.get("imgs").toString();
    		        List<String> urlList = new ObjectMapper().readValue(imgsJson, new TypeReference<>() {});
    		        List<Map<String, Object>> picList = new ArrayList<>();
    		        for (String url : urlList) {
    		            Map<String, Object> map = new HashMap<>();
    		            map.put("pic", Pictures.ofUrl(url).size(700, 850).create());
    		            picList.add(map);
    		        }
    		        dataMap.put("imgs", picList);
    		    }
    		} else {
    		    // 无测厚数据，直接补齐空行
    		    fillThicknessRow(mainRecords, mainFixRow, isContainer);
    		    dataMap.put("thicknessRecords", mainRecords);
    		}
    		
    		// ---------------------- 硬度数据处理 ----------------------
    		List<Map<String, Object>> hardnessList = new ArrayList<>();
    		int hardnessFixedRow = HARDNESS_MAX_ROW;
    		if(ObjectUtils.isNotEmpty(task.getHardness())) {
    			Object hardnessJson = task.getHardness();
    			Map<String, Object> hardnessMap = (Map<String, Object>) hardnessJson;
    			List<Map<String, Object>> originList = ObjectUtils.isNotEmpty(hardnessMap.get("hardnessList"))
    		            ? (List<Map<String, Object>>) hardnessMap.get("hardnessList")
    		            : new ArrayList<>();
    		    hardnessList.addAll(originList);
    		    
    		    for (Map<String, Object> item : hardnessList) {
    		    	String originalBh = (String) item.get("bh");
    		    	if(StringUtils.isNotBlank(originalBh)) {
    		    		String newBh = originalBh.replaceAll("-\\d+$", "");;
    		    		item.put("bh", newBh); 
    		    	}
    		    }
    		    Map<String, List<Map<String, Object>>> groupMap = new LinkedHashMap<>();
    		    for (Map<String, Object> row : hardnessList) {
    		    	String bh = (String) row.get("bh");
    		    	groupMap.computeIfAbsent(bh, k -> new ArrayList<>()).add(row);
    		    }
    		    // 每组第二行及以后bh设为空字符串
    		    for (List<Map<String, Object>> group : groupMap.values()) {
    		    	for (int i = 1; i < group.size(); i++) {
    		    		group.get(i).put("bh", "");
    		    	}
    		    }
    		}
    		fillHardnessFixedRow(hardnessList, hardnessFixedRow);
    		dataMap.put("hardnessList", hardnessList);
    		
    		// ---------------------- 铭牌照片处理 ----------------------
    		PictureRenderData nameplatePhotoDesc = null;
    		if(ObjectUtils.isNotEmpty(task.getMacro1())) {
    			Object macro1Json = task.getMacro1();
    			Map<String, Object> macro1Map = (Map<String, Object>) macro1Json;
    			if(ObjectUtils.isNotEmpty(macro1Map.get("nameplatePhotoDesc"))) {
    				String rawBase64 = macro1Map.get("nameplatePhotoDesc").toString();
    				nameplatePhotoDesc = PoiTlWordUtil.parseBase64Picture(rawBase64, 600, 200);
    			}
    		}
    		dataMap.put("nameplatePhotoDesc", nameplatePhotoDesc);
    	}catch (Exception e) {
    		e.printStackTrace();
		}
    	return dataMap;
    }
    
    private void fillHardnessFixedRow(List<Map<String, Object>> list, int targetRow) {
        while (list.size() < targetRow) {
            list.add(new HardnessData().toMap());
        }
    }
    private void fillThicknessRow(List<Object> list, int targetRow, boolean isContainer) {
        while (list.size() < targetRow) {
            if (isContainer) {
                list.add(new ThicknessRecord().toContainerMap());
            } else {
                list.add(new ThicknessRecord().toPipeMap());
            }
        }
    }
    
    public String initFileUrl(String category) {
    	return CATEGORY_CONTAINER.equals(category)?"template/task_container.docx":"template/task_pipe.docx";
    }
    /**
     * 根据taskId删除任务
     */
    public Map<String, Object> deleteTask(String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 判断数据是否存在
            if (!taskRepository.existsById(taskId)) {
                result.put("code", 500);
                result.put("msg", "该任务不存在");
                return result;
            }
            taskRepository.deleteById(taskId);
            result.put("code", 200);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除异常：" + e.getMessage());
        }
        return result;
    }

}