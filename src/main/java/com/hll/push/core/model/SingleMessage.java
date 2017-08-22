package com.hll.push.core.model;

/**
 * Author: huangll
 * Written on 17/8/15.
 */
public class SingleMessage {

  private String messageId;

  private String from;

  private String to;

  private String content;

  public SingleMessage() {
  }

  public SingleMessage(String messageId, String from, String to, String content) {
    this.messageId = messageId;
    this.from = from;
    this.to = to;
    this.content = content;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }
}
