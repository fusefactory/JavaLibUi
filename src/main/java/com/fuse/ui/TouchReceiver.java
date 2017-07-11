package com.fuse.ui;

import java.util.logging.*;
import java.util.Map;
import java.util.HashMap;
import com.fuse.utils.Event;

/** Base class for both Node and TouchManager; it provides touch events and method for receiving them */
public class TouchReceiver {

  public Event<TouchEvent>
    touchEvent,
    touchDownEvent,
    touchUpEvent,
    touchMoveEvent,
    touchEnterEvent,
    touchExitEvent,
    touchClickEvent,
    touchDoubleClickEvent;

  private Map<TouchEvent.EventType, Event<TouchEvent>> eventMap;

  private Logger logger;

  public TouchReceiver(){
    logger = Logger.getLogger(TouchReceiver.class.getName());

    touchEvent = new Event<TouchEvent>();
    touchDownEvent = new Event<TouchEvent>();
    touchUpEvent = new Event<TouchEvent>();
    touchMoveEvent = new Event<TouchEvent>();
    touchEnterEvent = new Event<TouchEvent>();
    touchExitEvent = new Event<TouchEvent>();
    touchClickEvent = new Event<TouchEvent>();
    touchDoubleClickEvent = new Event<TouchEvent>();

    eventMap = new HashMap<TouchEvent.EventType, Event<TouchEvent>>();
    eventMap.put(TouchEvent.EventType.TOUCH_DOWN, touchDownEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_UP, touchUpEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_MOVE, touchMoveEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_ENTER, touchEnterEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_EXIT, touchExitEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_CLICK, touchClickEvent);
    eventMap.put(TouchEvent.EventType.TOUCH_DOUBLECLICK, touchDoubleClickEvent);
  }

  public void receiveTouchEvent(TouchEvent event){
    // general touchEvent
    touchEvent.trigger(event);

    // type-specific touch event
    Event<TouchEvent> specificEvent = eventMap.get(event.eventType);
    if(specificEvent != null){
      // logger.warning("triggering specific event for: " + event.toString());
      specificEvent.trigger(event);
      return;
    }

    logger.warning("could not find touch-type specific event to trigger for touch event: " + event.toString());

    switch(event.eventType){
      case TOUCH_DOWN: this.onTouchDown(event); break;
      case TOUCH_MOVE: this.onTouchMove(event); break;
      case TOUCH_UP: this.onTouchUp(event); break;
      // case TOUCH_ENTER: this.onTouchDown(event); break;
      // case TOUCH_EXIT: this.onTouchDown(event); break;
      // case TOUCH_CLICK: this.onTouchDown(event); break;
      // case TOUCH_DOUBLECLICK: this.onTouchDown(event); break;
    }
  }

  public void onTouchDown(TouchEvent event){}
  public void onTouchMove(TouchEvent event){}
  public void onTouchUp(TouchEvent event){}
}
