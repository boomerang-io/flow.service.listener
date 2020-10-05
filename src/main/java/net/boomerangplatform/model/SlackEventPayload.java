package net.boomerangplatform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
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
}
