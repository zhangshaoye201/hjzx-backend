package com.container.dataCollect.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.container.dataCollect.entity.User;
import com.container.dataCollect.entity.UserDTO;
import com.container.dataCollect.repository.UserRepository;

import utils.BcryptPwdUtil;

/**
 * 任务服务接口
 */
@Service
public class UserService {

	private static final String UPDATE_PWD = "update";
	
	@Autowired
	private UserRepository userRepository;
	
	public boolean save(List<User> users) {
		for(User user :users) {
            user.setUserPwd(BcryptPwdUtil.encrypt(user.getUserPwd()));
            user.setVerifyCode(BcryptPwdUtil.encrypt(user.getVerifyCode()));
		}
		userRepository.saveAll(users);
		return true;
	}
    
	public Page<User> getUserList(UserDTO query) {
    	Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        return userRepository.getUserList(query,pageable);
    }
    
	public Page<User> getAssignList(UserDTO query) {
    	Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        return userRepository.findAll(pageable);
    }
	
	public Page<User> getVerifyList(UserDTO query) {
    	Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        return userRepository.getVerifyList(query,pageable);
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
        boolean ok = BcryptPwdUtil.matches(rawPwd, user.getUserPwd());
        if (!ok) {
        	result.put("code", 500);
        	result.put("msg", "密码错误");
        	return result;
        }
        // 登录成功
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("data", user);
        return result;
    }
	
	public Map<String, Object> updatePwd(String userId, String type,String oldPwd, String newPwd,String oldVerifyCode,String newVerifyCode) {
        Map<String, Object> res = new HashMap<>();
        // 1 查询用户
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            res.put("code", 500);
            res.put("msg", "该用户不存在");
            return res;
        }
        User user = userOpt.get();
        if(UPDATE_PWD.equals(type)) {
        	if(StringUtils.isNotBlank(newPwd)){
                boolean pwdOk = BcryptPwdUtil.matches(oldPwd, user.getUserPwd());
                if(!pwdOk){
                    res.put("code",500);
                    res.put("msg","原登录密码输入错误");
                    return res;
                }
            }
            
            if(StringUtils.isNotBlank(newVerifyCode)){
                boolean verifyOk = BcryptPwdUtil.matches(oldVerifyCode, user.getVerifyCode());
                if(!verifyOk){
                    res.put("code",500);
                    res.put("msg","原校核码输入错误");
                    return res;
                }
            }
        }

        //更新登录密码
        if(StringUtils.isNotBlank(newPwd)){
            user.setUserPwd(BcryptPwdUtil.encrypt(newPwd));
        }
        //更新校核码
        if(StringUtils.isNotBlank(newVerifyCode)){
            user.setVerifyCode(BcryptPwdUtil.encrypt(newVerifyCode));
        }
        
        user.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        userRepository.save(user);

        // 4 成功返回
        res.put("code", 200);
        res.put("msg", "密码修改成功");
        return res;
    }
	
	public Map<String, Object> formVerify(String verifier, String rawVerifyCode) {
		Optional<User> userOpt = userRepository.findByUserName(verifier);
        Map<String, Object> result = new HashMap<>();
        if(userOpt.isPresent()) {
        	User user = userOpt.get();
        	boolean pass = BcryptPwdUtil.matches(rawVerifyCode, user.getVerifyCode());
        	if(pass) {
        		result.put("code", 200);
        		result.put("msg", "校核成功");
        		result.put("verifyName", userOpt.get().getUserName());
        	}
        }else {
        	result.put("code", 500);
            result.put("msg", "校核失败");
        }
        return result;
    }
}