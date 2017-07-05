package com.fuse.ui;

import processing.core.PVector;

public class TouchEvent {
  public enum EventType {
    TOUCH_DOWN, // "finger down"
    TOUCH_MOVE, // "finger dragged"
    TOUCH_UP, // "finger up"
    TOUCH_ENTER, // "finger dragged into element"
    TOUCH_EXIT, // "finger dragged out of element"
    TOUCH_CLICK, // "finger tapped an element"
    TOUCH_DOUBLECLICK // "finger double-tapped an element"
  }

  public int touchId;
  public EventType eventType;
  public PVector position;
  public PVector startPosition;
  public Node node;
  public Node mostRecentNode;

  public String toString(){
    String result = "#"+Integer.toString(touchId)+" ";
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
    result += " on "+(n == null ? "<NO NODE>" : n.getName()) + " at position: " + Float.toString(position.x)+", "+Float.toString(position.y);
    return result;
  }

  public TouchEvent copy(){
    TouchEvent touchEvent = new TouchEvent();
    touchEvent.touchId = touchId;
    touchEvent.eventType = eventType;
    touchEvent.position = position;
    touchEvent.startPosition = startPosition;
    touchEvent.node = node;
    touchEvent.mostRecentNode = mostRecentNode;
    return touchEvent;
  }

  public float distance(){
    if(startPosition == null || position == null)
      return 0.0f;
    return PVector.dist(startPosition, position);
  }

  public PVector offset(){
    return PVector.sub(position, startPosition);
  }
}
