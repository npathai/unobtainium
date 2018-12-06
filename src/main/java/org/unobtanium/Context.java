package org.unobtanium;

import java.util.concurrent.ConcurrentHashMap;

public class Context {
  private ConcurrentHashMap<String, Object> contextInformation = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) contextInformation.get(key);
  }

  public void set(String key, Object value) {
    this.contextInformation.put(key, value);
  }
}
