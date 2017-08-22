package com.hll.push.mq;

import com.hll.push.core.consts.MQConstant;
import com.hll.push.core.enums.MessageStatus;
import com.hll.push.core.model.SingleMessage;
import com.hll.push.service.MessageService;
import com.hll.push.utils.JsonUtil;
import com.hll.push.websocket.ClientIdToChannelMap;
import com.hll.push.websocket.Packet;
import com.rabbitmq.client.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Author: huangll
 * Written on 17/8/15.
 */
@Component("messageConsumer")
public class MessageConsumer {

  @Autowired
  RabbitMQClientManager rabbitMQClientManager;

  @Autowired
  ClientIdToChannelMap clientIdToChannelMap;

  @Autowired
  private MessageService messageService;

  private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);


  public void startConsume() {
    Channel channel = rabbitMQClientManager.getChannel();
    try {
      channel.exchangeDeclare(MQConstant.MESSAGE_EXCHANGE, "fanout");
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, MQConstant.MESSAGE_EXCHANGE, "");

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
          String message = new String(body, "UTF-8");
          SingleMessage singleMessage = JsonUtil.getObj(message, SingleMessage.class);

          if (clientIdToChannelMap.contain(singleMessage.getTo())) {
            //找到连接,推送消息
            pushMessage(singleMessage);
          }
        }
      };
      channel.basicConsume(queueName, true, consumer);
      System.out.println("begin cunsume");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void pushMessage(SingleMessage singleMessage) {

    Packet response = Packet.newInstance();
    response.setType(Packet.Type.PUSH);
    response.setMsg(singleMessage);
    TextWebSocketFrame frame = new TextWebSocketFrame(response.toJson());

    clientIdToChannelMap.getChannel(singleMessage.getTo()).writeAndFlush(frame);

    logger.info("推送消息: {}", JsonUtil.toJson(singleMessage));

    //更新消息的数据库状态
    messageService.updateStatus(singleMessage.getMessageId(), singleMessage.getTo(), MessageStatus.Pushed.value());
  }

}
