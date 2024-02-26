package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ObjectController {

    @Resource
    ImageService service;

    @GetMapping("/images/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "image/jpg");
        this.fetchImage(request, response);
    }

    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 获取图片路径
        String imagePath = request.getServletPath().substring(7);
        // 获取输出流
        ServletOutputStream stream = response.getOutputStream();
        // 如果图片路径小于13位，则返回404
        if(imagePath.length() <= 13) {
            response.setStatus(404);
            stream.println(RestBean.failure(404, "Not found").toString());
        } else {
            try {
                // 从Minio获取图片
                service.fetchImageFromMinio(stream, imagePath);
                // 设置缓存控制头
                // 表示资源会在2592000秒（约30天）后过期。在这段时间内，如果有相同的请求，浏览器会直接从缓存中获取资源，而不是向服务器发送请求。
                response.setHeader("Cache-Control", "max-age=2592000");
            } catch (ErrorResponseException e) {
                // 如果获取图片出现异常，则返回404
                if(e.response().code() == 404) {
                    response.setStatus(404);
                    stream.println(RestBean.failure(404, "Not found").toString());
                } else {
                    log.error("从Minio获取图片出现异常: "+e.getMessage(), e);
                }
            }
        }
    }
}
