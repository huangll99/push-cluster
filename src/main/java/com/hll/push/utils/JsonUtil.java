package com.hll.push.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hll.push.core.model.SingleMessage;

import java.io.IOException;

/**
 * Author: huangll
 * Written on 17/7/16.
 */
public class JsonUtil {

  static private ObjectMapper objectMapper = new ObjectMapper();

  public static String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T getObj(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
