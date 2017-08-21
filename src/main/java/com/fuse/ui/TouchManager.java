package com.fuse.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.*;
import java.util.function.Consumer;

import com.fuse.utils.Event;

import processing.core.PGraphics;
import processing.core.PVector;

class TouchLog {
  public TouchEvent touchEvent;
  public float time;
}

class TouchMirror {
  private TouchReceiver receiver;
  private List<TouchEvent> currentMirrorEvents;
  private PVector mirrorOffset;

  public TouchMirror(TouchReceiver receiver){
    this.receiver = receiver;
    currentMirrorEvents = new ArrayList<>();
    mirrorOffset = new PVector(10,10,0);
    enable();
  }

  public void enable(){
    Consumer<TouchEvent> func = (TouchEvent event) -> {
      for(TouchEvent activeEvent : this.currentMirrorEvents)
        if(activeEvent.touchId == event.touchId)
          return;

      TouchEvent mirror = event.copy();
      mirror.touchId = event.touchId + 1;

      PVector offset = event.offset();
      offset.mult(-1.0f);
      mirror.position = offset;
      mirror.position.add(event.startPosition);
      mirror.position.add(mirrorOffset);

      this.currentMirrorEvents.add(mirror);
      receiver.submitTouchEvent(mirror);
      this.currentMirrorEvents.remove(mirror);
    };

    receiver.touchDownEvent.addListener(func, this);
    receiver.touchMoveEvent.addListener(func, this);
    receiver.touchUpEvent.addListener(func, this);
  }

  public void disable(){
    receiver.touchDownEvent.removeListeners(this);
    receiver.touchUpEvent.removeListeners(this);
    receiver.touchMoveEvent.removeListeners(this);
  }
}

public class TouchManager extends TouchReceiver {
  private Logger logger;
  private Node node;
  private boolean dispatchOnUpdate;
  private boolean controlledTime;
  private float time;
  /// the maximum amount of time (in seconds) between a touch-down and a touch-up for it to be considered a click
  private float clickMaxInterval;
  /// the maximum distance (in pixels) between the position of touch-down and the position of touch-up for it to be considered a click
  private float clickMaxDistance;

  private List<TouchEvent> touchEventQueue;
  private Map<Integer, TouchEvent> activeTouchEvents;
  private Map<Integer, TouchLog> activeTouchLogs;

  private void _init(){
    logger = Logger.getLogger(TouchManager.class.getName());
    dispatchOnUpdate = false;
    controlledTime = false;
    clickMaxInterval = 0.2f; // seconds
    clickMaxDistance = 15; // pixels
    touchEventQueue = new ArrayList<TouchEvent>();
    activeTouchEvents = new HashMap<Integer, TouchEvent>();
    activeTouchLogs = new HashMap<Integer, TouchLog>();
  }
  public TouchManager(){
    _init();
  }

  public TouchManager(Node sceneNode){
    _init();
    setNode(sceneNode);
  }

  public void update(){
    if(dispatchOnUpdate){
      List<TouchEvent> queueCopy = new ArrayList<>();
      queueCopy.addAll(touchEventQueue);

      for(TouchEvent e : queueCopy){
        processTouchEvent(e);
        touchEventQueue.remove(e);
      }
    }
  }

  public void update(float dt){
    controlledTime = true;
    time += dt;
    update();
  }

  /**
   * this method can be called to process a new touch-down event
   */
  public void touchDown(int id, PVector p){
    logger.finer("touchDown (" + Integer.toString(id) + "): "+Float.toString(p.x)+", "+Float.toString(p.y));
    submitTouchEvent(createTouchDownEvent(id, p));
  };

  /**
   * this method can be called to process a new touch-up event
   */
  public void touchUp(int id, PVector p){
    logger.finer("touchUp (" + Integer.toString(id) + "): "+Float.toString(p.x)+", "+Float.toString(p.y));
    submitTouchEvent(createTouchUpEvent(id,p));
  };

  /**
   * this method can be called to process a new touch-move event
   */
  public void touchMove(int id, PVector p){
    logger.finer("touchMove (" + Integer.toString(id) + "): "+Float.toString(p.x)+", "+Float.toString(p.y));
    submitTouchEvent(createTouchMoveEvent(id, p));
  };

  /**
   * takes a touch event and, depending on the dispatchOnUpdate setting, will immediately process or queue for processing during the next call to update()
   */
  @Override public void submitTouchEvent(TouchEvent event){
    if(dispatchOnUpdate){
      touchEventQueue.add(event);
      return;
    }

    processTouchEvent(event);
  }

  /// finds the targeted node and triggers events
  private void processTouchEvent(TouchEvent event){
    switch(event.eventType){
      case TOUCH_DOWN:
        event.node = getNodeForTouchPosition(event.position);
        event.startPosition = event.position;
        activeTouchEvents.put(event.touchId, event);

        { // create touch log for later calculations
          TouchLog tlog = new TouchLog();
          tlog.touchEvent = event.copy();
          if(controlledTime)
            tlog.time = this.time;
          else
            tlog.time = (System.currentTimeMillis() / 1000.0f);

          activeTouchLogs.put(event.touchId, tlog);
        }

        break;

      case TOUCH_MOVE:
        { // find and update existing active TouchEvent
          TouchEvent existing = activeTouchEvents.get(event.touchId);
          if(existing != null){
            existing.position = event.position;
            existing.eventType = event.eventType;
            event = existing;
          } else {
            logger.warning("no existing touch event for touch move event");
            activeTouchEvents.put(event.touchId, event);
          }
        }

        { // Check if the touch moved to another element
          Node n = getNodeForTouchPosition(event.position);
          Node prev = event.mostRecentNode == null ? event.node : event.mostRecentNode;

          // changed to other node since previous event?
          if(n != prev){
            // update event
            event.mostRecentNode = n;

            // prepare touch exit event
            TouchEvent tmpEvent = event.copy();
            tmpEvent.eventType = TouchEvent.EventType.TOUCH_EXIT;

            // trigger touch exit events
            this.receiveTouchEvent(tmpEvent);
            if(prev != null)
              prev.receiveTouchEvent(tmpEvent);

            // prepare touch enter event
            tmpEvent = event.copy();
            tmpEvent.eventType = TouchEvent.EventType.TOUCH_ENTER;

            // trigger touch enter events
            this.receiveTouchEvent(tmpEvent);
            if(n != null)
              n.receiveTouchEvent(tmpEvent);
          }
        }

        break;

      case TOUCH_UP:
        { // find and update existing active TouchEvent
          TouchEvent existing = activeTouchEvents.get(event.touchId);
          if(existing != null){
            existing.position = event.position;
            existing.eventType = event.eventType;
            event = existing;
          } else {
            logger.warning("no existing touch event for touch up event");
            activeTouchEvents.put(event.touchId, event);
          }
        }

        { // Check if the touch moved to another element
          Node n = getNodeForTouchPosition(event.position);

          if(n != event.node)
            event.mostRecentNode = n;
        }

        { // check for click
          TouchLog tlog = activeTouchLogs.get(event.touchId);
          if(tlog == null){
            logger.warning("could not find touch log for touch up event: " + event.toString());
          } else {
            float t;
            if(controlledTime)
              t = this.time;
            else
              t = (System.currentTimeMillis() / 1000.0f);

            // check if time and distance between touch-down and -up aren't too big
            if(t - tlog.time <= clickMaxInterval && PVector.dist(tlog.touchEvent.position, event.position) <= clickMaxDistance){
              TouchEvent tmpEvent = event.copy();
              tmpEvent.eventType = TouchEvent.EventType.TOUCH_CLICK;

              // logger.warning("CLICK: dist=" + Float.toString(PVector.dist(tlog.touchEvent.position, event.position)));
              // logger.warning("pos1=" + tlog.touchEvent.position.toString());
              // logger.warning("pos2=" + event.position.toString());

              // trigger touch click events
              this.receiveTouchEvent(tmpEvent);
              if(event.node != null){
                logger.finest("TouchManager triggering TOUCH_CLICK event on Node: "+event.node.getName());
                event.node.receiveTouchEvent(tmpEvent);
              }
            }
          }
        }

        // this is the end of this touch
        activeTouchEvents.remove(event.touchId);
        activeTouchLogs.remove(event.touchId);
        break;
    }

    // trigger appropriate events on event's original node
    if(event.node != null){
      event.node.receiveTouchEvent(event);
    }

    // trigger appropriate events on event's most recent node
    if(event.mostRecentNode != null && event.mostRecentNode != event.node)
      event.mostRecentNode.receiveTouchEvent(event);

    // trigger appropriate events on this
    this.receiveTouchEvent(event);
  }

  public void setNode(Node newNode){
    this.node = newNode;
  }

  public Node getNodeForTouchPosition(PVector pos){
    if(this.node == null)
      return null;

    List<Node> nodes = new ArrayList<Node>();
    if(node != null)
      loadNodesUnderPosition(nodes, node, pos);

    if(nodes.isEmpty()){
      this.logger.fine("no node found for touch event");
      return null;
    }

    Collections.sort(nodes, Node.topPlaneFirst);
    // logger.warning("touch event for: "+nodes.get(0).getName());
    return nodes.get(0);
  }

  private void loadNodesUnderPosition(List<Node> targetList, Node root, PVector pos){
    // if (root == null)
    //   return;
    boolean rootContains = root.isInside(pos);

    // // handle touch clip (only contained child nodes will respond to touch events)
    // if(/*root->bClipTouch && */!rootContains) {
    //   return;
    // }

    // recursive; also check child's children and add them before the current root node (so they appear before it even if they are on the same plane)
    for(Node childNode : root.getChildNodes()) {
      loadNodesUnderPosition(targetList, childNode, pos);
    }

    if(root.isInteractive() && rootContains) {
      // if this node has a clipping node then the touch
      // only applies to this node if it's within the clipping area
      Node clipNode = root.getClippingNode();
      if(clipNode == null || clipNode.isInside(pos))
        targetList.add(root);
    }
  }

  public boolean getDispatchOnUpdate(){
    return dispatchOnUpdate;
  }

  public void setDispatchOnUpdate(boolean newVal){
    dispatchOnUpdate = newVal;
  }

  public void setClickMaxInterval(float interval){
    clickMaxInterval = interval;
  }

  public void setClickMaxDistance(float distance){
    clickMaxDistance = distance;
  }

  public static TouchEvent createTouchDownEvent(int id, PVector p){
    TouchEvent e = new TouchEvent();
    e.touchId = id;
    e.eventType = TouchEvent.EventType.TOUCH_DOWN;
    e.position = p;
    return e;
  }

  public static TouchEvent createTouchMoveEvent(int id, PVector p){
    TouchEvent e = new TouchEvent();
    e.touchId = id;
    e.eventType = TouchEvent.EventType.TOUCH_MOVE;
    e.position = p;
    return e;
  }

  public static TouchEvent createTouchUpEvent(int id, PVector p){
    TouchEvent e = new TouchEvent();
    e.touchId = id;
    e.eventType = TouchEvent.EventType.TOUCH_UP;
    e.position = p;
    return e;
  }


  /** For debugging! (TODO: remove?) */
  private TouchMirror touchMirror = null;

  /** DEBUG FEATURE for pinch-zoom without touchscreen! (TODO: remove?) */
  public void setMirrorNodeEventsEnabled(boolean enable){
    if(enable){
      if(touchMirror != null)
        return;
      touchMirror = new TouchMirror(this);
      return;
    }

    if(touchMirror != null){
      touchMirror.disable();
      touchMirror = null;
    }
  }

  public boolean getMirrorNodeEventsEnabled(){ return touchMirror != null; }

  public void drawActiveTouches(){
    PGraphics pg = Node.getPGraphics();

    pg.colorMode(pg.RGB, 255);
    pg.fill(pg.color(255,100,100,150));
    pg.noStroke();
    pg.ellipseMode(pg.CENTER);

    for(TouchEvent event : activeTouchEvents.values())
      pg.ellipse(event.position.x, event.position.y, 25, 25);
  }
}
