package com.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
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
        Channel channel = null;
        try {
            // 获取连接并创建channel
            channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(false);

            // 判断队列是否存在
            try {
                channel.queueDeclarePassive(queueName);  // 检查队列是否存在
            } catch (Exception e) {
                // 如果队列不存在，则声明队列
                channel.queueDeclare(queueName, true, false, false, null);  // 随用随创建
            }

            // 发送消息
            rabbitTemplate.convertAndSend(queueName, msg);
        } catch (Exception e) {
            log.error("创建队列失败:{}", queueName);
        }

        rabbitTemplate.convertAndSend(queueName, msg);
    }
}
