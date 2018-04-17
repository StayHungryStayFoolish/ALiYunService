package com.aliyunservice.www.web.rest;

import com.aliyunservice.www.web.rest.util.ALiYunORCHttpUtils;
import com.aliyunservice.www.web.rest.util.HeaderUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bonismo@hotmail.com on 2018/4/14 上午1:47
 *
 * @Version: V1.0.0
 * <p>
 * Description: 色情、暴力图片审核，不支持 https 协议
 */
@RestController
@RequestMapping("/api")
public class PornAndViolenceImageResource {

    private final Logger log = LoggerFactory.getLogger(PornAndViolenceImageResource.class);
    private static final String HOST = "http://imgaudit.market.alicloudapi.com";
    private static final String PATH = "/greenImg";
    private static final String METHOD = "POST";
    private static final String APP_CODE = "372da5885c954fce885e1fbc5a540226";

    @PostMapping("/verify-pornImg")
    public ResponseEntity<String> verifyPornImg(@RequestParam String url) {
        log.debug("Request the url :{} " + url);
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + APP_CODE);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("base64", "base64");
        bodys.put("imgUrl", url);
        bodys.put("type", "1");

        try {
            /*
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = ALiYunORCHttpUtils.doPost(HOST, PATH, METHOD, headers, querys, bodys);
            System.out.println(response.toString());
            String pornJson = EntityUtils.toString(response.getEntity());

            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("PassportEntity", url.toString()))
                .body(pornJson);
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


