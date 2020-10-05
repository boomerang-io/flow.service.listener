package io.boomerang.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class SlackEventPayload {
  
/*
 * This model services dual purpose as both the Challenge Payload and the Events Payload.
 * https://api.slack.com/events-api#the-events-api__subscribing-to-event-types__events-api-request-urls
 * https://api.slack.com/events-api#receiving_events
 */

  private String token;

  private String challenge;

  private String type;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getChallenge() {
    return challenge;
  }

  public void setChallenge(String challenge) {
    this.challenge = challenge;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  Map<String, Object> details = new LinkedHashMap<>();
  
  @JsonAnySetter
  void setDetail(String key, Object value) {
      details.put(key, value);
  }
}
