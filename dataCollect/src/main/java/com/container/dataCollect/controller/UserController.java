package com.container.dataCollect.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.container.dataCollect.entity.User;
import com.container.dataCollect.entity.UserDTO;
import com.container.dataCollect.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin // 允许跨域（开发环境）
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> param) {
        String username = param.get("username");
        String password = param.get("password");
        return userService.loginVerify(username, password);
    }
    
    @PostMapping("/updatePwd")
    public Map<String, Object> updatePassword(@RequestBody Map<String, String> param) {
        String userId = param.get("userId");
        String oldPassword = param.get("oldPassword");
        String newPassword = param.get("newPassword");
        return userService.updatePwd(userId, oldPassword, newPassword);
    }
    
    @GetMapping("/list")
    public Page<User> getUserList(UserDTO query) {
        return userService.getUserList(query);
    }

    
    /**
     * 导入，新建
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody List<User> userList) {
    	Map<String, Object> result = new HashMap<>();
        try {
        	userService.save(userList);
            result.put("code", 200);
            result.put("msg", "保存成功，数据已同步至数据库");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
        	result.put("code", 500);
        	result.put("msg", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/delete")
    public Map<String, Object> deleteUser(@RequestParam String userId) {
        return userService.deleteUser(userId);
    }
}