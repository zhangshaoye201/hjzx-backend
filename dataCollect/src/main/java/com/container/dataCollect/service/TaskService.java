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
    private static final int HARDNESS_MAX_ROW = 15;
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
			List<String> selectTags = Arrays.asList(tags.split(","));
			dataMap.put("evaluate", selectTags.contains("evaluate"));
			dataMap.put("review", selectTags.contains("review"));
			dataMap.put("macro", selectTags.contains("macro"));
			dataMap.put("macro1", selectTags.contains("macro1"));
			dataMap.put("safety", selectTags.contains("safety"));
			dataMap.put("thickness", selectTags.contains("thickness"));
			dataMap.put("thicknessImg", selectTags.contains("thicknessImg"));
			dataMap.put("sound", selectTags.contains("sound"));
			dataMap.put("penetrate", selectTags.contains("penetrate"));
			dataMap.put("magnetic", selectTags.contains("magnetic"));
			dataMap.put("hardness", selectTags.contains("hardness"));
			// ---------------------- 资料数据处理 ----------------------
			if (task.getReview()!=null) {
    		    Map<String, Object> reviewMap = (Map<String, Object>) task.getReview();
    		    if (reviewMap.get("designDoc")!=null) {
    		    	reviewMap.put("designDocShow", formatArrayData((List<String>) reviewMap.get("designDoc"), ""));
    		    }
    		    if (reviewMap.get("ManuDoc")!=null) {
    		    	reviewMap.put("ManuDocShow", formatArrayData((List<String>) reviewMap.get("ManuDoc"), ""));
    		    }
    		    if (reviewMap.get("fixDoc")!=null) {
    		    	reviewMap.put("fixDocShow", formatArrayData((List<String>) reviewMap.get("fixDoc"), ""));
    		    }
    		    if (reviewMap.get("useDoc")!=null) {
    		    	reviewMap.put("useDocShow", formatArrayData((List<String>) reviewMap.get("useDoc"), ""));
    		    }
    		    if (reviewMap.get("checkDoc")!=null) {
    		    	reviewMap.put("checkDocShow", formatArrayData((List<String>) reviewMap.get("checkDoc"), ""));
    		    }
    		    task.setReview(reviewMap);
			}
    		// ---------------------- 测厚数据处理 ----------------------
    		boolean isContainer = CATEGORY_CONTAINER.equals(task.getCategory());
    		int mainFixRow = isContainer ? THICKNESS_MAX_ROW_CONTAINER : THICKNESS_MAX_ROW_PIPE;
    		List<Object> mainRecords = new ArrayList<>();
    		List<Object> continueRecords = new ArrayList<>();
    		List<Map<String, Object>> picList = new ArrayList<>();

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
    		    dataMap.put("thicknessContinue", originRecords.size() > mainFixRow);

    		    // 处理示意图图片
    		    if (ObjectUtils.isNotEmpty(thicknessMap.get("imgs"))) {
    		        String imgsJson = thicknessMap.get("imgs").toString();
    		        List<Map<String,Object>> itemList = new ObjectMapper().readValue(imgsJson, new TypeReference<List<Map<String,Object>>>() {});
    		        for(Map<String,Object> item : itemList){
    		            String url = (String) item.get("url");
    		            if(url == null || url.isBlank()) continue;
    		            Map<String,Object> map = new HashMap<>();
    		            if(isContainer) {
    		            	map.put("pic", Pictures.ofUrl(url).size(700,850).create());
    		            }else {
    		            	map.put("pic", Pictures.ofUrl(url).size(900,450).create());
    		            }
    		            picList.add(map);
    		        }
    		        dataMap.put("imgs", picList);
    		    }else {
    		    	dataMap.put("imgs", new ArrayList<>());
    		    }
    		} else {
    		    // 无测厚数据，直接补齐空行
    		    fillThicknessRow(mainRecords, mainFixRow, isContainer);
    		    dataMap.put("thicknessRecords", mainRecords);
    		    fillThicknessRow(continueRecords, THICKNESS_MAX_ROW_CONTINUE, isContainer);
    		    dataMap.put("thicknessContinueRecords", continueRecords);
    		    dataMap.put("imgs", new ArrayList<>());
    		    dataMap.put("thicknessContinue", false);
    		}
    		
    		// ---------------------- 磁粉检测 magnetic 分页处理（新增） ----------------------
    		if(ObjectUtils.isNotEmpty(task.getMagnetic())){
    		    List<Map<String,Object>> magneticList = (List<Map<String, Object>>) task.getMagnetic();
    		    for(Map<String,Object> magItem : magneticList){
    		        //取出原始缺陷行
    		        List<Map<String,Object>> originMagneticList = ObjectUtils.isNotEmpty(magItem.get("magneticList"))
    		                ? (List<Map<String, Object>>) magItem.get("magneticList")
    		                : new ArrayList<>();

    		        // 主页最多5行
    		        int MAIN_PAGE_ROW = 5;
    		        int MAX_PAGE_ROW = 31;
    		        List<Map<String,Object>> page1 = new ArrayList<>();
    		        List<Map<String,Object>> page2 = new ArrayList<>();
    		        if(originMagneticList.size() <= MAIN_PAGE_ROW){
    		            page1.addAll(originMagneticList);
    		        }else{
    		            page1.addAll(originMagneticList.subList(0, MAIN_PAGE_ROW));
    		            page2.addAll(originMagneticList.subList(MAIN_PAGE_ROW, originMagneticList.size()));
    		         // 补齐空行，撑满一整页
    		            while(page2.size() < MAX_PAGE_ROW){
    		                // 构造空行map，字段和缺陷行保持一致
    		                Map<String,Object> emptyRow = new HashMap<>();
    		                emptyRow.put("num", "");
    		                emptyRow.put("location", "");
    		                emptyRow.put("type", "");
    		                emptyRow.put("size", "");
    		                emptyRow.put("level", "");
    		                emptyRow.put("notes", "");
    		                page2.add(emptyRow);
    		            }
    		        }
    		        boolean needContinuePage = page2.size() > 0;
    		        // 直接往map里面put，内存加工，不会写回数据库
    		        magItem.put("page1", page1);
    		        magItem.put("page2", page2);
    		        magItem.put("needContinuePage", needContinuePage);
    		    }
    		}
    		
    		// ---------------------- 渗透检测 penetrate 分页处理（新增） ----------------------
    		if(ObjectUtils.isNotEmpty(task.getPenetrate())){
    		    List<Map<String,Object>> penetrateList = (List<Map<String, Object>>) task.getPenetrate();
    		    for(Map<String,Object> paneItem : penetrateList){
    		        //取出原始缺陷行
    		        List<Map<String,Object>> originPenetrateList = ObjectUtils.isNotEmpty(paneItem.get("penetrateList"))
    		                ? (List<Map<String, Object>>) paneItem.get("penetrateList")
    		                : new ArrayList<>();

    		        int MAIN_PAGE_ROW = 5;
    		        int MAX_PAGE_ROW = 25;
    		        List<Map<String,Object>> page1 = new ArrayList<>();
    		        List<Map<String,Object>> page2 = new ArrayList<>();
    		        if(originPenetrateList.size() <= MAIN_PAGE_ROW){
    		            page1.addAll(originPenetrateList);
    		        }else{
    		            page1.addAll(originPenetrateList.subList(0, MAIN_PAGE_ROW));
    		            page2.addAll(originPenetrateList.subList(MAIN_PAGE_ROW, originPenetrateList.size()));
    		         // 补齐空行，撑满一整页
    		            while(page2.size() < MAX_PAGE_ROW){
    		                // 构造空行map，字段和缺陷行保持一致
    		                Map<String,Object> emptyRow = new HashMap<>();
    		                emptyRow.put("num", "");
    		                emptyRow.put("location", "");
    		                emptyRow.put("type", "");
    		                emptyRow.put("length", "");
    		                emptyRow.put("level", "");
    		                emptyRow.put("notes", "");
    		                page2.add(emptyRow);
    		            }
    		        }
    		        boolean needContinuePage = page2.size() > 0;
    		        // 直接往map里面put，内存加工，不会写回数据库
    		        paneItem.put("page1", page1);
    		        paneItem.put("page2", page2);
    		        paneItem.put("needContinuePage", needContinuePage);
    		    }
    		}
    		
    		// ---------------------- 超声检测 sound 分页处理（新增） ----------------------
    		if(ObjectUtils.isNotEmpty(task.getSound())){
    		    List<Map<String,Object>> soundList = (List<Map<String, Object>>) task.getSound();
    		    for(Map<String,Object> soundItem : soundList){
    		        //取出原始缺陷行
    		        List<Map<String,Object>> originSoundList = ObjectUtils.isNotEmpty(soundItem.get("soundList"))
    		                ? (List<Map<String, Object>>) soundItem.get("soundList")
    		                : new ArrayList<>();

    		        int MAIN_PAGE_ROW = 3;
    		        int MAX_PAGE_ROW = 26;
    		        List<Map<String,Object>> page1 = new ArrayList<>();
    		        List<Map<String,Object>> page2 = new ArrayList<>();
    		        if(originSoundList.size() <= MAIN_PAGE_ROW){
    		            page1.addAll(originSoundList);
    		        }else{
    		            page1.addAll(originSoundList.subList(0, MAIN_PAGE_ROW));
    		            page2.addAll(originSoundList.subList(MAIN_PAGE_ROW, originSoundList.size()));
    		         // 补齐空行，撑满一整页
    		            while(page2.size() < MAX_PAGE_ROW){
    		                // 构造空行map，字段和缺陷行保持一致
    		                Map<String,Object> emptyRow = new HashMap<>();
    		                emptyRow.put("num", "");
    		                emptyRow.put("location", "");
    		                emptyRow.put("depth", "");
    		                emptyRow.put("length", "");
    		                emptyRow.put("height", "");
    		                emptyRow.put("reflect", "");
    		                emptyRow.put("level", "");
    		                emptyRow.put("notes", "");
    		                page2.add(emptyRow);
    		            }
    		        }
    		        boolean needContinuePage = page2.size() > 0;
    		        // 直接往map里面put，内存加工，不会写回数据库
    		        soundItem.put("page1", page1);
    		        soundItem.put("page2", page2);
    		        soundItem.put("needContinuePage", needContinuePage);
    		    }
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
//    		fillHardnessFixedRow(hardnessList, hardnessFixedRow);
//    		dataMap.put("hardnessList", hardnessList);
    		
    		int HARDNESS_MAIN_ROW = 15;    // 主表显示行数，可根据word调整
    		int HARDNESS_MAX_CONTINUE_ROW = 25; // 续页补齐总行数
    		List<Map<String,Object>> page1 = new ArrayList<>();
    		List<Map<String,Object>> page2 = new ArrayList<>();

    		if(hardnessList.size() <= HARDNESS_MAIN_ROW){
    			page1.addAll(hardnessList);
    			// 补齐空行
    		    while (page1.size() < HARDNESS_MAIN_ROW){
    		    	page1.add(new HardnessData().toMap());
    		    }
    		}else{
    			page1.addAll(hardnessList.subList(0, HARDNESS_MAIN_ROW));
    			page2.addAll(hardnessList.subList(HARDNESS_MAIN_ROW, hardnessList.size()));
    		    // 补齐空行
    		    while (page2.size() < HARDNESS_MAX_CONTINUE_ROW){
    		    	page2.add(new HardnessData().toMap());
    		    }
    		}
    		boolean hardnessNeedContinuePage = page2.size() > 0;

    		// 放到dataMap，给poi‑tl模板使用
    		dataMap.put("page1", page1);
    		dataMap.put("page2", page2);
    		dataMap.put("hardnessContinue", hardnessNeedContinuePage);
    		
    		
    		// ---------------------- 铭牌照片处理 ----------------------
    		PictureRenderData nameplatePhotoDesc = null;
    		if(ObjectUtils.isNotEmpty(task.getMacro1())) {
    			Object macro1Json = task.getMacro1();
    			Map<String, Object> macro1Map = (Map<String, Object>) macro1Json;
    			if(ObjectUtils.isNotEmpty(macro1Map.get("nameplatePhotoDesc"))) {
    				String rawBase64 = macro1Map.get("nameplatePhotoDesc").toString();
    				nameplatePhotoDesc = PoiTlWordUtil.parseBase64Picture(rawBase64, 650, 300);
    			}
    		}
    		dataMap.put("nameplatePhotoDesc", nameplatePhotoDesc);
    		
    		dataMap.put("task", task);
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
    
    public String initFileUrl(String type,String category) {
    	StringBuffer sb =new StringBuffer("template/").append(type).append("_").append(category).append(".docx");
    	return sb.toString();
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
    
    private String formatArrayData(List<String> list, String otherValue) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        List<String> result = new ArrayList<>();
        for (String item : list) {
            String val = StringUtils.trimToEmpty(item);
            if ("other".equals(val)) {
                // 遇到标记other，取自定义值；自定义为空则跳过该条目
                String custom = StringUtils.trimToEmpty(otherValue);
                if (StringUtils.isNotBlank(custom)) {
                    result.add(custom);
                }
            } else {
                if (StringUtils.isNotBlank(val)) {
                    result.add(val);
                }
            }
        }
        return String.join("、", result);
    }

    
}