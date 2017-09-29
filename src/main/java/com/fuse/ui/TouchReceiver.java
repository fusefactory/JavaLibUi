package com.fuse.ui;

import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import com.fuse.utils.Event;

/** Base class for both Node and TouchManager; it provides touch events and method for receiving them */
public class TouchReceiver {

  protected final static long IDLE_DURATION = 500l; // seconds after which an event if considered IDLE_DURATION

  public Event<TouchEvent>
    touchEvent,
    touchDownEvent,
    touchUpEvent,
    touchMoveEvent,
    touchEnterEvent,
    touchExitEvent,
    touchClickEvent,
    touchDoubleClickEvent;

  protected Logger logger;
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
  
  public void destroy() {
	  touchEvent.destroy();
	  touchDownEvent.destroy();
	  touchUpEvent.destroy();
	  touchMoveEvent.destroy();
	  touchEnterEvent.destroy();
	  touchExitEvent.destroy();
	  touchClickEvent.destroy();
	  touchDoubleClickEvent.destroy();
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
          if(this != event.node) { // for original touch target enter/exit doesn't affect the touch activeness
            addActiveTouchEvent(event);
          }

          touchEnterEvent.trigger(event);
          break;
        }

        case TOUCH_EXIT: {
          if(this != event.node) { // for original touch target enter/exit doesn't affect the touch activeness
        	  removeActiveTouchEvent(event);
          }

          touchExitEvent.trigger(event);
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

  public void addActiveTouchEvent(TouchEvent evt){
    // lazy initialize; a lot of Nodes will never receive events and thus don't need this
    if(activeTouchEvents == null)
      activeTouchEvents = new ArrayList<>();

    if(!this.activeTouchEvents.contains(evt)){
      for(int idx=this.activeTouchEvents.size()-1; idx>=0; idx--){
        if(this.activeTouchEvents.get(idx).touchId == evt.touchId)
          this.activeTouchEvents.remove(idx);
      }

      activeTouchEvents.add(evt);
    }

    //if(evt.time != null)
    //  removeActiveTouchEventsBefore(evt.time - IDLE_DURATION);
  }

  public void removeActiveTouchEvent(TouchEvent evt){
    if(activeTouchEvents == null)
      return;

    activeTouchEvents.remove(evt);

    //if(evt.time != null)
    //  removeActiveTouchEventsBefore(evt.time - IDLE_DURATION);
  }

  /*private void removeActiveTouchEventsBefore(long time){
    for(int i=activeTouchEvents.size()-1; i>=0; i--){
      Long eventTime = activeTouchEvents.get(i).time;
      if(eventTime != null && eventTime < time)
        activeTouchEvents.remove(i);
    }
  }*/

  public List<TouchEvent> getActiveTouchEvents(){
    List<TouchEvent> result = new ArrayList<>();
    if(activeTouchEvents != null)
      result.addAll(activeTouchEvents); // TODO; make copies of Event instances to not allow modification?

    return result;
  }

  public boolean isTouched(){
    return !(activeTouchEvents == null || activeTouchEvents.isEmpty());
  }
}
