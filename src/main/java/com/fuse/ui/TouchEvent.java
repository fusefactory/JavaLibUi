package com.fuse.ui;

import processing.core.PVector;

/** TouchEvent class represents a single touch event (like touch-down, touch-up, touch-click, touch-double-click, touch-enter, etc.) */
public class TouchEvent {
  /** Types of touches that can be represented by the TouchEvent class */
  public enum EventType {
    TOUCH_DOWN, // "finger down"
    TOUCH_MOVE, // "finger dragged"
    TOUCH_UP, // "finger up"
    TOUCH_ENTER, // "finger dragged into element"
    TOUCH_EXIT, // "finger dragged out of element"
    TOUCH_CLICK, // "finger tapped an element"
    TOUCH_DOUBLECLICK // "finger double-tapped an element"
  }

  /** Id of this touch (for distinguishing between simltanous touches on multi-touch interfaces) */
  public int touchId;
  /** Type of touch event (up/down/click/etc.) */
  public EventType eventType;
  /** Current position of this touch */
  public PVector position;
  /** First position of this touch */
  public PVector startPosition;
  /** First node that was touched (and which this touch is basically "assigned to") */
  public Node node;
  /** Latest node that was touched */
  public Node mostRecentNode;
  /** The time in millis */
  public Long time;
  public Long startTime;
  //public Long lastChangeTime;
  /** The velocity of the touch movement */
  public PVector velocity;
  public PVector velocitySmoothed;


  /** @return String A string-based representation of this TouchEvent instance (mainly for debugging) */
  public String toString(){
    String result = "#"+Integer.toString(touchId)+" ";

    if(eventType == null)
      result += "????";
    else
      switch(eventType){
        case TOUCH_DOWN:
          result += "DOWN";
          break;
        case TOUCH_MOVE:
          result += "MOVE";
          break;
        case TOUCH_UP:
          result += "UP";
          break;
        case TOUCH_EXIT:
          result += "EXIT";
          break;
        case TOUCH_ENTER:
          result += "ENTER";
          break;
        case TOUCH_CLICK:
          result += "CLICK";
          break;
        case TOUCH_DOUBLECLICK:
          result += "DOUBLECLICK";
          break;
      }

    Node n = mostRecentNode == null ? node : mostRecentNode;
    result += " on "+(n == null ? "<NO NODE>" : n.getName());
    result += " at position: ";
    result += (position == null ? "???" : (Float.toString(position.x)+", "+Float.toString(position.y)));

    return result;
  }

  /**
   * Creates a shallow copy of this instance, mainly used for creating follow-up event,
   * like a touch-move after a touch-down, or a touch-click after a touch-up
   * @return TouchEvent The created copy of this event
   */
  public TouchEvent copy(){
    TouchEvent touchEvent = new TouchEvent();
    touchEvent.touchId = touchId;
    touchEvent.eventType = eventType;
    touchEvent.position = position == null ? null :position.get();
    touchEvent.startPosition = startPosition == null ? null : startPosition.get();
    touchEvent.node = node;
    touchEvent.mostRecentNode = mostRecentNode;
    touchEvent.time = time == null ? null : (long) time;
    touchEvent.startTime = startTime == null ? null : (long) startTime;
    touchEvent.velocity = velocity == null ? null : velocity.get();
    touchEvent.velocitySmoothed = velocitySmoothed == null ? null : velocitySmoothed.get();
    return touchEvent;
  }

  /** @return float The distance between the original position (the position at which the touch started) and the current position */
  public float distance(){
    return startPosition == null || position == null ? 0.0f : PVector.dist(startPosition, position);
  }

  /** @return PVector The vector form the original position (the position at which the touch started) and the current position */
  public PVector offset(){
    if(startPosition == null || position == null)
      return new PVector(0.0f, 0.0f, 0.0f);
    PVector result = position.get();
    result.sub(startPosition);
    return result;
  }

  public Long getDuration(){
    return time == null || startTime == null ? null : time-startTime;
  }

  public boolean isFinished(){
    return this.eventType == EventType.TOUCH_UP;
  }

  public PVector getSmoothedVelocity() {
    return this.velocitySmoothed == null ? this.velocity.get() : this.velocitySmoothed.get();
  }
}
