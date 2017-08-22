package com.hll.push.queue;

import com.hll.push.core.consts.MQConstant;
import com.hll.push.core.model.Message;
import com.hll.push.core.enums.MessageStatus;
import com.hll.push.core.model.SingleMessage;
import com.hll.push.mq.RabbitMQClientManager;
import com.hll.push.service.MessageService;
import com.hll.push.utils.JsonUtil;
import com.hll.push.websocket.ClientIdToChannelMap;
import com.hll.push.websocket.Packet;
import com.lmax.disruptor.EventHandler;
import com.rabbitmq.client.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 消息的事件处理器
 * Author: huangll
 * Written on 17/8/4.
 */
@Component
public class MessageEventHandler implements EventHandler<Message> {

  private static final Logger logger = LoggerFactory.getLogger(MessageEventHandler.class);

  @Autowired
  private MessageService messageService;

  @Autowired
  ClientIdToChannelMap clientIdToChannelMap;

  @Autowired
  RabbitMQClientManager rabbitMQClientManager;

  @Override
  public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception {

    //将消息保存到数据库
    String messageId = messageService.save(message);

    //推送消息
    message.getIds().stream().forEach(receiveId -> {

      if (clientIdToChannelMap.contain(receiveId)) {
        //找到连接,推送消息
        pushMessage(message, messageId, receiveId);
      } else {
        //找不到连接,将消息发到mq进行广播
        sendToMQ(messageId, message, receiveId);
      }

    });
  }

  private void sendToMQ(String messageId, Message message, String receiveId) {
    Channel channel = rabbitMQClientManager.getChannel();
    try {
      channel.exchangeDeclare(MQConstant.MESSAGE_EXCHANGE, "fanout");

      SingleMessage singleMessage = new SingleMessage(messageId, message.getFrom(), receiveId, message.getContent());

      channel.basicPublish(MQConstant.MESSAGE_EXCHANGE, "", null, JsonUtil.toJson(singleMessage).getBytes(StandardCharsets.UTF_8));
      channel.close();
    } catch (Exception e) {
      throw new RuntimeException("send message to mq fail ", e);
    }
  }

  private void pushMessage(Message message, String messageId, String receiveId) {
    SingleMessage singleMessage = new SingleMessage(messageId, message.getFrom(), receiveId, message.getContent());

    Packet response = Packet.newInstance();
    response.setType(Packet.Type.PUSH);
    response.setMsg(singleMessage);
    TextWebSocketFrame frame = new TextWebSocketFrame(response.toJson());

    clientIdToChannelMap.getChannel(receiveId).writeAndFlush(frame);

    logger.info("推送消息: {}", JsonUtil.toJson(singleMessage));

    //更新消息的数据库状态
    messageService.updateStatus(messageId, receiveId, MessageStatus.Pushed.value());
  }
}
