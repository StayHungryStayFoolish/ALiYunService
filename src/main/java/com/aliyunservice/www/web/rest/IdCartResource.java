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
 * Created by bonismo@hotmail.com on 2018/4/14 上午12:18
 *
 * @Version: V1.0.0
 * <p>
 * Description: 身份证识别接口
 */
@RestController
@RequestMapping("/api")
public class IdCartResource {

    private final Logger log = LoggerFactory.getLogger(IdCartResource.class);

    // 护照 OCR URL
    private static final String HOST = "https://dm-51.data.aliyun.com";

    // 请求的 json 数据路径
    private static final String PATH = "/rest/160601/ocr/ocr_idcard.json";

    // 请求方式
    private static final String METHOD = "POST";

    // 阿里云申请后的 app_code
    private static final String APP_CODE = "372da5885c954fce885e1fbc5a540226";

    /**
     * Post  /scan : get face idCard info
     *
     * @description: 根据传入的身份证图片正面返回扫描后的 Json 数据
     * @param: imgFile 正面身份证图片
     * @return: 身份证正面 json 数据
     * @author: Bonismo
     * @version: V1.0.0
     * @date: 2018/4/14 上午12:59
     */
    @PostMapping("/get-ocr-face-idCard")
    @Timed
    public ResponseEntity<String> getFaceOcrIdCard(@Valid @RequestParam MultipartFile imgFile) {
        log.debug("Rest request to get IdCart MultipartFile : {} ", imgFile.toString());
        String side = "face";
        // 获取response的body
        String faceIdCardJson = getIdCard(imgFile, side);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("FaceIdCardEntity", imgFile.toString()))
            .body(faceIdCardJson);
    }

    /**
     * Post  /scan : get back idCard info
     *
     * @description: 根据传入的身份证图片反面返回扫描后的 Json 数据
     * @param: imgFile 反面身份证图片
     * @return: 身份证反面 json 数据
     * @author: Bonismo
     * @version: V1.0.0
     * @date: 2018/4/14 上午1:36
     */
    @PostMapping("/get-ocr-back-idCard")
    @Timed
    public ResponseEntity<String> getBackOcrIdCard(@Valid @RequestParam MultipartFile imgFile) {
        log.debug("Rest request to get IdCart MultipartFile : {} ", imgFile.toString());
        String side = "back";
        // 获取response的body
        String backIdCardJson = getIdCard(imgFile, side);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("BackIdCardEntity", imgFile.toString()))
            .body(backIdCardJson);
    }

    /**
     * @description: 根据传入图片的正反面获取信息
     * @param: imgFile 图片
     * @param: side 正反面
     * @return: json 数据
     * @author: Bonismo
     * @version: V1.0.0
     * @date: 2018/4/14 上午1:32
     */
    private static String getIdCard(MultipartFile imgFile, String side) {
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
            //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
            headers.put("Authorization", "APPCODE " + APP_CODE);
            //根据API的要求，定义相对应的Content-Type
            headers.put("Content-Type", "application/json; charset=UTF-8");
            Map<String, String> querys = new HashMap<>();
            // String bodys = "{\"image\":\"图片二进制数据的base64编码\",\"configure\":\"{\\\"side\\\":\\\"face\\\"}\"#身份证正反面类型:face/back}";
            String bodys = "{" + "\"image\"" + ":" + "\"" + imgBase64 + "\"" + "," + "\"configure\":\"{\\\"side\\\":\\\"" + side + "\\\"" + "}\"" + "}";
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
            // 获取response的body
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


/*
正面返回结果：
{

	"address"    : "浙江省杭州市余杭区文一西路969号",   #地址信息
	"config_str" : "{\\\"side\\\":\\\"face\\\"}",                #配置信息，同输入configure
	"face_rect":{
		"angle": -90,
		"center":{
			"x" : 952,
			"y" : 325.5
		},
		"size":{
			"height":181.99,
			"width":164.99
			}
	},                          #人脸位置，center表示人脸矩形中心坐标，size表示人脸矩形长宽，angle表示矩形顺时针旋转的度数。
	"name" : "张三",                                  #姓名
	"nationality": "汉"， #民族
	"num" : "1234567890",                             #身份证号
	"sex" : "男",                                     #性别
	"birth" : "20000101",                             #出生日期
	"nationality" : "汉",                             #民族
	"success" : true                                  #识别结果，true表示成功，false表示失败
}


反面返回结果:
{
    "config_str" : "{\\\"side\\\":\\\"back\\\"}",#配置信息，同输入configure
    "start_date" : "19700101",       #有效期起始时间
    "end_date" : "19800101",         #有效期结束时间
    "issue" : "杭州市公安局",         #签发机关
    "success" : true                   #识别结果，true表示成功，false表示失败
}
 */


