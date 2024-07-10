package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.vo.response.WeatherVO;
import com.example.service.WeatherService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate rest;

    @Resource
    StringRedisTemplate template;

    @Value("${spring.weather.key}")
    String key;

    public WeatherVO fetchWeather(double longitude, double latitude) {
        return fetchFromCache(longitude, latitude);
    }

    private WeatherVO fetchFromCache(double longitude, double latitude) {
        // 获取位置
        // 获取到的数据是String类型，设置转化为byte[]类型，再解压byte[]为JSONObject类型
        JSONObject geo = this.decompressStingToJson(rest.getForObject(
                "https://geoapi.qweather.com/v2/city/lookup?location=" + longitude + "," + latitude + "&key=" + key, byte[].class));
        if (geo == null) return null;
        log.info("geo: {}", geo);
        if (geo.getJSONArray("location") == null) {
            return null;
        }
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);
        // 从cache中获取数据，id作为地区的唯一标识
        int id = location.getInteger("id");
        String key = Const.FORUM_WEATHER_CACHE + id;
        String cache = template.opsForValue().get(key);
        if (cache != null)
            return JSONObject.parseObject(cache).to(WeatherVO.class);
        // 缓存中没有就从API获取数据
        WeatherVO vo = this.fetchFromAPI(id, location);
        if (vo == null) return null;
        template.opsForValue().set(key, JSONObject.from(vo).toJSONString(), 1, TimeUnit.HOURS);
        return vo;
    }

    private WeatherVO fetchFromAPI(int id, JSONObject location) {
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);
        // 获取实时天气
        JSONObject now = this.decompressStingToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/now?location=" + id + "&key=" + key, byte[].class));
        if (now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        // 获取逐小时天气
        JSONObject hourly = this.decompressStingToJson(rest.getForObject(
                "https://devapi.qweather.com/v7/weather/24h?location=" + id + "&key=" + key, byte[].class));
        if (hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;
    }

    private JSONObject decompressStingToJson(byte[] data) {
        //创建一个字节数组输出流
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            //创建一个GZIP输入流，将字节数组作为输入
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            //创建一个字节数组缓冲区
            byte[] buffer = new byte[1024];
            //定义一个变量，用于记录读取的字节数
            int read;
            //循环读取字节数，并将字节数写入字节数组输出流
            while ((read = gzip.read(buffer)) != -1)
                stream.write(buffer, 0, read);
            //关闭GZIP输入流
            gzip.close();
            //关闭字节数组输出流
            stream.close();
            //将字节数组输出流转换为JSONObject
            return JSONObject.parseObject(stream.toString());
        } catch (IOException e) {
            //如果发生异常，返回null
            return null;
        }
    }
}
