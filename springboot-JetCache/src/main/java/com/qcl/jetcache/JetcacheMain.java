package com.qcl.jetcache;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * 主启动
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/8/5
 */
@SpringBootApplication
//@EnableMethodCache(basePackages = "com.qcl.jetcache")
//@EnableCreateCacheAnnotation
public class JetcacheMain {
    public static void main(String[] args) {
        SpringApplication.run(JetcacheMain.class, args);
    }

    @PostConstruct
    void started() {
        //时区设置：中国上海
        //time.zone: "Asia/Shanghai"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
