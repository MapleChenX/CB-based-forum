package com.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public RabbitMQUtil(@Lazy RabbitTemplate rabbitTemplate, @Lazy RabbitAdmin rabbitAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * 发送消息到指定的队列
     * @param queueName 队列名称
     * @param msg 消息对象
     */
    public void sendMessage(String queueName, Object msg) {
        String jsonMessage = JSONObject.toJSONString(msg);
        sendMessage(queueName, jsonMessage);
    }

    /**
     * 发送消息到指定的队列
     * @param queueName 队列名称
     * @param msg 消息字符串
     */
    public void sendMessage(String queueName, String msg) {
        try {
            rabbitAdmin.declareQueue(new Queue(queueName, true, false, false));
            rabbitTemplate.convertAndSend(queueName, msg);
        } catch (Exception e) {
            log.error("发送消息失败: {}, 错误: {}", queueName, e.getMessage());
        }
    }
}
