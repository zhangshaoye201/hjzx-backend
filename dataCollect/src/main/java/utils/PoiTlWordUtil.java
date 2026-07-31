package utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;

import jakarta.servlet.http.HttpServletResponse;

public class PoiTlWordUtil {

    public static void downloadWord(String templatePath,
                                    Map<String, Object> data,
                                    String fileName,
                                    HttpServletResponse response) throws IOException {
        // 1. Spring 读取 classpath(resources) 资源，兼容 jar 包
        ClassPathResource resource = new ClassPathResource(templatePath);
        // 前置判断，给出明确错误提示
        if (!resource.exists()) {
            throw new IOException("模板不存在，路径：" + templatePath);
        }

        // 2. 设置下载响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encodeName = URLEncoder.encode(fileName + ".docx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + encodeName);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setDateHeader("Expires", 0);
        
        // 3. 用 流 编译模板（核心修复：不再传文件路径，传输入流）
        Configure config = Configure.builder()
        		.useSpringEL() // 开启SpEL才能执行 .contains() 方法、三元运算
        		.bind("thicknessRecords", new LoopRowTableRenderPolicy())
        		.bind("thicknessContinueRecords", new LoopRowTableRenderPolicy())
        		.bind("hardnessList", new LoopRowTableRenderPolicy())
        		.build();
        
        try (InputStream inputStream = resource.getInputStream();
             XWPFTemplate template = XWPFTemplate.compile(inputStream,config).render(data)) {
            template.write(response.getOutputStream());
        }
    }
    
}