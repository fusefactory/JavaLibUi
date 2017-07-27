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
  }

  // virtual
  public void submitTouchEvent(TouchEvent event){}

  public void receiveTouchEvent(TouchEvent event){
    // general touchEvent
    touchEvent.trigger(event);

    if(event.eventType != null){
      switch(event.eventType){
        case TOUCH_DOWN: touchDownEvent.trigger(event); this.onTouchDown(event); break;
        case TOUCH_MOVE: touchMoveEvent.trigger(event); this.onTouchMove(event); break;
        case TOUCH_UP: touchUpEvent.trigger(event); this.onTouchUp(event); break;
        case TOUCH_ENTER: touchEnterEvent.trigger(event); break;
        case TOUCH_EXIT: touchExitEvent.trigger(event); break;
        case TOUCH_CLICK: touchClickEvent.trigger(event); break;
        case TOUCH_DOUBLECLICK: touchDoubleClickEvent.trigger(event); break;
        default: logger.warning("could not find touch-type specific event to trigger for touch event: " + event.toString());
      }
    }
  }

  public void onTouchDown(TouchEvent event){}
  public void onTouchMove(TouchEvent event){}
  public void onTouchUp(TouchEvent event){}

  /**
   * Convenience method that wraps the given function in a lambda so the calle doesn't need to "know about" TouchEvent
   * @param func The parameter-less function to invoke when this node is clicked
   */
  public void whenClicked(Runnable func){
    touchClickEvent.addListener( (TouchEvent evt) -> func.run() );
  }
}
