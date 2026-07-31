package com.container.dataCollect.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.container.dataCollect.entity.Task;
import com.container.dataCollect.entity.TaskQueryDTO;
import com.container.dataCollect.service.TaskService;

import jakarta.servlet.http.HttpServletResponse;
import utils.PoiTlWordUtil;

@RestController
@RequestMapping("/api/task")
@CrossOrigin // 允许跨域
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/helloWorld")
    public ResponseEntity<String> helloWorld() {
        return new ResponseEntity<>("Hello World! ✅ Task接口测试成功", HttpStatus.OK);
    }
    
    /**
     * 条件查询任务列表
     * @param taskName  任务名称（模糊查询）
     * @param unitNo    单位内编号
     * @param status    状态
     * @param createBy  创建人
     * @return 任务列表
     */
    @GetMapping("/list")
    public Page<Task> getTaskList(TaskQueryDTO query) {
        return taskService.findTaskPage(query);
    }

    @PutMapping("/claim")
    public ResponseEntity<?> claimTask(@RequestBody Map<String, Object> params) {
        List<String> taskIds = (List<String>) params.get("taskIds");
        String claimBy = (String) params.get("claimBy");

        taskService.claimTasks(taskIds, claimBy);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "msg", "认领成功"
        ));
    }
    
    /**
     * 导入，新建保存
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> importTasks(@RequestBody List<Task> taskList) {
    	Map<String, Object> result = new HashMap<>();
        try {
            taskService.save(taskList);
            result.put("code", 200);
            result.put("msg", "保存成功，数据已同步至数据库");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
        	result.put("code", 500);
        	result.put("msg", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    /**
     * 根据taskId查询单个任务
     * @param taskId 任务ID
     * @return 单个任务信息（含状态码和提示语）
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getTaskById(@RequestParam String taskId) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        try {
            // 直接返回 Task，没有 Optional
            Task task = taskService.findTaskById(taskId);
            
            if (task != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", task);
            } else {
                result.put("code", 404);
                result.put("msg", "未找到该任务");
                result.put("data", null);
            }
            
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据taskId导出任务
     * @param taskId 任务ID
     * @return HttpServletResponse
     */
    @GetMapping("/export")
    public void exportSingleTask(@RequestParam String taskId, @RequestParam String tags,HttpServletResponse response) {
    try {
    	Task task = taskService.findTaskById(taskId);
        // 3. 调用 poi-tl 工具下载Word
        PoiTlWordUtil.downloadWord(
        		taskService.initFileUrl(task.getCategory()),  // 模板路径
                taskService.initDataMap(task, tags),
                "任务报告_" + taskId,   // 下载文件名
                response
        );
    } catch (Exception e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try {
            response.getWriter().write("导出失败：" + e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
    /**
     * 删除任务接口
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteTask(@RequestParam String taskId) {
        return taskService.deleteTask(taskId);
    }
}