package com.aliyunservice.www.web.rest.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by bonismo@hotmail.com on 2018/4/14 上午12:22
 *
 * @Version: V1.0.0
 * <p>
 * Description: File 转换工具
 */
public class FileUtils {

    /**
     * 转换图片格式
     *
     * @description: MultipartFile 转换为 File
     * @param: file 图片
     * @return: java.io.File
     * @author: Bonismo
     * @version: V1.0.0
     * @date: 2018/4/13 下午11:46
     */
    public static File convert(MultipartFile file) {
        try {
            File conFile = new File(file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(conFile);
            fos.write(file.getBytes());
            fos.close();
            return conFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


