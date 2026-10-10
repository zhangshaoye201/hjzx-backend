package com.version.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/apk")
@CrossOrigin
public class ApkUpdateController {

    // Windows本地路径
    private final String savePath = "C:/dataCollect/bak/";
    // 访问前缀，本机测试：http://localhost:8080/file/apk/
    private final String filePrefix = "http://ym.njjcys.cn:9528/file/apk/";

    /**
     * 直接上传apk接口，不需要页面
     * Post请求，form‑data格式
     * 参数：
     *  file: apk文件
     *  versionName: "V1.0.1"
     *  versionCode: 整数版本号
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadApk(
            @RequestParam MultipartFile file,
            @RequestParam String versionName,
            @RequestParam Integer versionCode
    ) throws IOException {
        // 创建文件夹
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        File destFile = new File(savePath + originalFilename);
        // 写入磁盘
        file.transferTo(destFile);

        // 拼接完整下载地址
        String downloadUrl = filePrefix + originalFilename;

        // =========这里你可以写数据库保存版本信息=========
        // apkVersionMapper.insert(....);
        // 设置这条记录 is_latest = 1；旧版本改为0

        return ResponseEntity.ok(Map.of(
                "code", 200,
                "msg", "上传成功",
                "data", Map.of(
                        "downloadUrl", downloadUrl,
                        "versionName", versionName,
                        "versionCode", versionCode
                )
        ));
    }

    /**
     * 获取最新版本信息接口（给apk客户端调用）
     */
    @GetMapping("/getLatest")
    public ResponseEntity<?> getLatestApk() {
        // 查询数据库 is_latest=1 的那条记录
        /*
        ApkVersion latest = apkVersionMapper.selectOne(...);
        return ResponseEntity.ok(Map.of(
                "code",200,
                "data", latest
        ));
        */

        // 测试模拟返回，正式换成数据库查询
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", Map.of(
                        "downloadUrl", "http://localhost:8080/file/apk/app.apk",
                        "versionName", "V1.0.1",
                        "versionCode", 2
                )
        ));
    }
}