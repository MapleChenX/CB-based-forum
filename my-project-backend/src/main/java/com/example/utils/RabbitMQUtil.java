package com.example.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQUtil(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送消息到指定的队列
     * @param queueName 队列名称
     * @param msg 消息对象
     * @throws Exception
     */
    public void sendMessage(String queueName, Object msg)  {
        String jsonMessage = JSONObject.toJSONString(msg);
        sendMessage(queueName, jsonMessage);
    }

    public void sendMessage(String queueName, String msg)  {
        try {
            rabbitTemplate.getConnectionFactory().createConnection().createChannel(false)
                    .queueDeclare(queueName, true, false, false, null);
        } catch (Exception e) {
            log.error("创建队列失败:{}", queueName);
        }

        rabbitTemplate.convertAndSend(queueName, msg);
    }
}
