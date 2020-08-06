package net.boomerangplatform.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Event {
  
  private Map<String, Object> details = new LinkedHashMap<>();
  
  @JsonAnySetter
  public void setDetail(String key, Object value) {
      details.put(key, value);
  }
}
