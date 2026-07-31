package com.container.dataCollect.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.container.dataCollect.entity.User;
import com.container.dataCollect.entity.UserDTO;
import com.container.dataCollect.repository.UserRepository;

import utils.SHA256Util;

/**
 * 任务服务接口（JPA 版本）
 */
@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Value("${password.salt}")
    private String pwdSalt;
	
	public boolean save(List<User> users) {
		for(User user :users) {
			String encryptPwd = SHA256Util.sha256Encrypt(user.getUserPwd(), pwdSalt);
            user.setUserPwd(encryptPwd);
		}
		userRepository.saveAll(users);
		return true;
	}
    
	public Page<User> getUserList(UserDTO query) {
    	Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        return userRepository.getUserList(query,pageable);
    }
    
	public Map<String, Object> deleteUser(String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 判断数据是否存在
            if (!userRepository.existsById(userId)) {
                result.put("code", 500);
                result.put("msg", "该用户不存在");
                return result;
            }
            // JPA删除
            userRepository.deleteById(userId);
            result.put("code", 200);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除异常：" + e.getMessage());
        }
        return result;
    }
	
	public Map<String, Object> loginVerify(String username, String rawPwd) {
        Map<String, Object> result = new HashMap<>();
        Optional<User> userOpt = userRepository.findByUserName(username);

        // 用户名不存在
        if (userOpt.isEmpty()) {
            result.put("code", 500);
            result.put("msg", "用户名不存在");
            return result;
        }
        User user = userOpt.get();
        if(user.getUserPwd().length()!=64) {
        	if (!user.getUserPwd().equals(rawPwd)) {
        		result.put("code", 500);
        		result.put("msg", "密码错误");
        		return result;
        	}
        }else {
        	
        	String encryptPwd = SHA256Util.sha256Encrypt(rawPwd, pwdSalt);
        	if (!user.getUserPwd().equals(encryptPwd)) {
        		result.put("code", 500);
        		result.put("msg", "密码错误");
        		return result;
        	}
        }
        // 登录成功
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("data", user);
        return result;
    }
	
	public Map<String, Object> updatePwd(String userId, String oldPwd, String newPwd) {
        Map<String, Object> res = new HashMap<>();
        // 1 查询用户
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            res.put("code", 500);
            res.put("msg", "该用户不存在");
            return res;
        }
        User user = userOpt.get();
        String encryptOldPwd = SHA256Util.sha256Encrypt(oldPwd, pwdSalt);
        // 2 校验原密码（测试明文，上线必须BCrypt加密比对）
        if (!user.getUserPwd().equals(encryptOldPwd)) {
            res.put("code", 500);
            res.put("msg", "原密码输入错误");
            return res;
        }

        // 3 更新新密码
        String encryptNewPwd = SHA256Util.sha256Encrypt(newPwd, pwdSalt);
        user.setUserPwd(encryptNewPwd);
        user.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        userRepository.save(user);

        // 4 成功返回
        res.put("code", 200);
        res.put("msg", "密码修改成功");
        return res;
    }
}