package com.hll.push;

import com.hll.push.mq.MessageConsumer;
import com.hll.push.mq.RabbitMQClientManager;
import com.hll.push.queue.RealtimeMessageQueue;
import com.hll.push.websocket.PushServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Author: huangll
 * Written on 17/6/28.
 */
@SpringBootApplication
public class Bootstrap {

  public static void main(String[] args) {
    //启动web容器
    SpringApplication app = new SpringApplication(Bootstrap.class);
    ConfigurableApplicationContext context = app.run();

    //启动push server
    PushServer pushServer = context.getBean("PushServer", PushServer.class);
    pushServer.start();

    //初始化disruptor队列
    RealtimeMessageQueue realtimeMessageQueue = context.getBean("realtimeMessageQueue", RealtimeMessageQueue.class);
    realtimeMessageQueue.start();

    //初始化RabbitMQ连接
    RabbitMQClientManager rabbitMQClientManager = context.getBean("rabbitMQClientManager", RabbitMQClientManager.class);
    rabbitMQClientManager.init();

    MessageConsumer messageConsumer = context.getBean("messageConsumer", MessageConsumer.class);
    messageConsumer.startConsume();

  }
}
