package net.boomerangplatform.service;

import net.boomerangplatform.model.Event;

public interface EventProcessor {

  void routeEvent(String target, Event event);
}
