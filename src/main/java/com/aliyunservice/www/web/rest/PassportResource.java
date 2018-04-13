package com.aliyunservice.www.web.rest;

import com.aliyunservice.www.web.rest.errors.InternalServerErrorException;
import com.aliyunservice.www.web.rest.util.ALiYunORCHttpUtils;
import com.aliyunservice.www.web.rest.util.FileUtils;
import com.aliyunservice.www.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bonismo@hotmail.com on 2018/4/13 下午11:38
 *
 * @Version: V1.0.0
 * <p>
 * Description: 护照识别接口
 */
@RestController
@RequestMapping("/api")
public class PassportResource {

    private final Logger log = LoggerFactory.getLogger(PassportResource.class);

    // 护照 OCR URL
    private static final String HOST = "https://ocrhz.market.alicloudapi.com";

    // 请求的 json 数据路径
    private static final String PATH = "/rest/160601/ocr/ocr_passport.json";

    // 请求方式
    private static final String METHOD = "POST";

    // 阿里云申请后的 app_code
    private static final String APP_CODE = "372da5885c954fce885e1fbc5a540226";

    /**
     * Post  /scan : get passport info
     *
     * @description: 根据传入的护照图片返回扫描后的 Json 数据
     * @param: imgFile 护照图片
     * @return: Json 数据
     * @author: Bonismo
     * @version: V1.0.0
     * @date: 2018/4/14 上午12:04
     */
    @PostMapping("/get-ocr-passport")
    @Timed
    public ResponseEntity<String> getOcrPassport(@Valid @RequestParam MultipartFile imgFile) {
        log.debug("Rest request to get Passport MultipartFile : {} ", imgFile.toString());
        if (imgFile.isEmpty()) {
            throw new InternalServerErrorException("ImgFile can not be null ! ");
        }
        String imgBase64 = "";
        try {
            File file = FileUtils.convert(imgFile);
            byte[] content = new byte[(int) file.length()];
            FileInputStream finputstream = new FileInputStream(file);
            finputstream.read(content);
            // finputstream.close();
            // 图片扫描后，直接删除，不保存在当前程序中
            file.delete();
            imgBase64 = new String(Base64.encodeBase64(content));


            Map<String, String> headers = new HashMap<>();
            // 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
            headers.put("Authorization", "APPCODE " + APP_CODE);
            // 根据API的要求，定义相对应的Content-Type
            headers.put("Content-Type", "application/json; charset=UTF-8");
            Map<String, String> querys = new HashMap<>();
            // String bodys = "{\"image\":\"base64_image_string\"#图片以base64编码的string}";
            String bodys = "{" + "\"image\"" + ":" + "\"" + imgBase64 + "\"" + "}";
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
            String passportJson = EntityUtils.toString(response.getEntity());
            // 获取response的body
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("PassportEntity", file.toString()))
                .body(passportJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

/*
护照返回 Json 数据字段
{
     "authority": "公安部出入境管理局*",  #签发机关
     "birth_date": "19861030",                 #生日
     "birth_day": "861030",                    #生日(即将弃用)
     "birth_place": "广西",                    #出生地
     "country": "CHN",                         #国籍
     "expiry_date": "20230501",                #到期日期
     "expiry_day": "230501",                   #到期日期(即将弃用)
     "issue_date": "20130502",                 #发证日期
     "issue_place": "广西",                     #发证地址
     "line0": "P0CHNWANG<<JING<<<<<<<<<<<<<<<<<<<<<<<<<<",
     "line1": "E203545580CHN8610304M2305019MNPELOLIOKLPA938",
     "name": "WANG.JING",                   #姓名英文
     "name_cn": "汪婧",                         #姓名中文
     "passport_no": "E20354xxxx",               #护照号码
     "person_id": "MNPELOLIOKLPA9",            #持照人身份ID
     "request_id": "20171120113612_813974f02a16b81ab911292d181b0b42",  #请求唯一标识，用于错误追踪
1
     "sex": "M",                               #性别
     "src_country": "CHN",                     #国籍
     "success": true,
     "type": "P0"                               #护照类型
}
 */

