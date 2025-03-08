package com.example.utils;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQUtil(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(String queueName, String message) {
        Queue queue = new Queue(queueName, true);
        rabbitTemplate.convertAndSend(queue.getName(), message);
    }
}
