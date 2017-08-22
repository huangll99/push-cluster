package com.hll.push.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Author: huangll
 * Written on 17/8/15.
 */
@Component("rabbitMQClientManager")
public class RabbitMQClientManager {

  private static Logger logger = LoggerFactory.getLogger(RabbitMQClientManager.class);

  @Value("${rabbitmq.host}")
  private String host;

  @Value("${rabbitmq.port}")
  private Integer port;

  //只需建立一个connection,从connection中获取channel,操作rabbitmq
  private Connection connection;

  //启动的时候初始化
  public void init() {
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory();

      connectionFactory.setHost(host);
      connectionFactory.setPort(port);

      connection = connectionFactory.newConnection();

      logger.info("connect rabbitmq successfully...");
    } catch (Exception e) {
      throw new RuntimeException("connect rabbitmq fail", e);
    }
  }

  public Channel getChannel() {
    try {
      return connection.createChannel();
    } catch (IOException e) {
      throw new RuntimeException("get rabbitmq channel fail ", e);
    }
  }

}
