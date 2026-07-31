package com.container.dataCollect.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@CrossOrigin // 允许跨域（开发环境）
public class LoginController {
	
	@GetMapping("/helloWorld")
    public ResponseEntity<String> helloWorld() {
        return new ResponseEntity<>("Hello World! ✅ Task接口测试成功", HttpStatus.OK);
    }
	/**
     * 登录
     */
    @PostMapping("/auth")
    public ResponseEntity<Map<String, Object>> loginAuth(
    		@RequestParam String userName,
    		@RequestParam String password) {
    	Map<String, Object> result = new HashMap<>();
        try {
            System.out.println(userName+" "+password);
            result.put("code", 200);
            result.put("msg", "鉴权成功");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
        	result.put("code", 500);
        	result.put("msg", "鉴权失败"+e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
