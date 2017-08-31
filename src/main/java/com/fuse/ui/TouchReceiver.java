package com.fuse.ui;

import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import com.fuse.utils.Event;

/** Base class for both Node and TouchManager; it provides touch events and method for receiving them */
public class TouchReceiver {

  private final static float IDLE_DURATION = 500.0f; // seconds after which an event if considered IDLE_DURATION

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
  private List<TouchEvent> activeTouchEvents = null;

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
        case TOUCH_DOWN: {
          addActiveTouchEvent(event);
          touchDownEvent.trigger(event);
          this.onTouchDown(event);
          break;
        }

        case TOUCH_MOVE: {
          touchMoveEvent.trigger(event);
          this.onTouchMove(event);
          break;
        }

        case TOUCH_UP: {
          touchUpEvent.trigger(event);
          this.onTouchUp(event);
          removeActiveTouchEvent(event);
          break;
        }

        case TOUCH_ENTER: {
          addActiveTouchEvent(event);
          touchEnterEvent.trigger(event);
          break;
        }

        case TOUCH_EXIT: {
          touchExitEvent.trigger(event);
          removeActiveTouchEvent(event);
          break;
        }

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

  private void addActiveTouchEvent(TouchEvent evt){
    // lazy initialize; a lot of Nodes will never receive events and thus don't need this
    if(activeTouchEvents == null)
      activeTouchEvents = new ArrayList<>();

    if(!activeTouchEvents.contains(evt))
      activeTouchEvents.add(evt);

    if(evt.time != null)
      removeActiveTouchEventsBefore(evt.time - IDLE_DURATION);
  }

  private void removeActiveTouchEvent(TouchEvent evt){
    if(activeTouchEvents == null)
      return;

    activeTouchEvents.remove(evt);

    if(evt.time != null)
      removeActiveTouchEventsBefore(evt.time - IDLE_DURATION);
  }

  private void removeActiveTouchEventsBefore(float time){
    for(int i=activeTouchEvents.size()-1; i>=0; i--){
      Float eventTime = activeTouchEvents.get(i).time;
      if(eventTime != null && eventTime < time)
        activeTouchEvents.remove(i);
    }
  }

  public List<TouchEvent> getActiveTouchEvents(){
    List<TouchEvent> result = new ArrayList<>();
    if(activeTouchEvents != null)
      result.addAll(activeTouchEvents); // TODO; make copies of Event instances to not allow modification?

    return result;
  }
}
