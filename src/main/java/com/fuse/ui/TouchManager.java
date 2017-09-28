package com.fuse.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

import java.util.logging.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

import processing.core.PGraphics;
import processing.core.PVector;

public class TouchManager extends TouchReceiver {
  private Logger logger;
  private Node node;
  private boolean dispatchOnUpdate = false;
  private long time = 0;
  private Long previousFrameMillis = null;
  /// the maximum amount of time (in seconds) between a touch-down and a touch-up for it to be considered a click
  private long clickMaxInterval = 200l;
  private float clickMaxDistance = 15.0f;
  //private long doubleClickMaxInterval = 600l;
  // private long doubleClickMinDelay = 500l;
  /// the maximum distance (in pixels) between the position of touch-down and the position of touch-up for it to be considered a click

  // velocity smoothing logic based on ofxInterface OpenFrameworks addon implementation
  // see: https://github.com/galsasson/ofxInterface/blob/master/src/TouchManager.cpp
  private final static float velocitySmoothCoeff = 0.25f;
  private final static float velocityDump = 0.6f;

  
  private ConcurrentLinkedQueue<TouchEvent> touchEventQueue;
  private Map<Integer, TouchEvent> activeTouchEvents;
  private final static int MAX_CLICK_HISTORY_SIZE = 10; // no need to remember more; only remembering for double-click events, but have to consider multiple simultanous users
  private ConcurrentLinkedDeque<TouchEvent> clickHistory;

  private void _init(){
    logger = Logger.getLogger(TouchManager.class.getName());
    touchEventQueue = new ConcurrentLinkedQueue<TouchEvent>();
    activeTouchEvents = new HashMap<Integer, TouchEvent>();
    //clickHistory = new ConcurrentLinkedDeque<>();
  }

  public TouchManager(){
    _init();
  }

  public TouchManager(Node sceneNode){
    _init();
    setNode(sceneNode);
  }

  public void update(){
    if(previousFrameMillis == null)
    previousFrameMillis = System.currentTimeMillis();
    Long newMillis = System.currentTimeMillis();
    this.update(newMillis - previousFrameMillis);
    previousFrameMillis = newMillis;
  }

  public void update(long dtMs){
    time += dtMs;

    for(TouchEvent evt : activeTouchEvents.values()){
      evt.velocitySmoothed.mult(velocityDump);
    }

    if(dispatchOnUpdate){
      while(!this.touchEventQueue.isEmpty()) {
    	  processTouchEvent(this.touchEventQueue.poll());
      }
    }

    //finalizeIdleTouchEvents();
  }
  
  @Deprecated
  public void update(float dt){
	  this.update((long)(1000l * dt));
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
    if(event.time == null)
      event.time = this.getTime();

    if(dispatchOnUpdate){
      this.touchEventQueue.add(event);
      return;
    }

    processTouchEvent(event);
  }

  private TouchEvent updateExistingEvent(TouchEvent existing, TouchEvent event){
    /*if(existing.position == null && event.position != null
    || (existing.position != null && event.position != null && !existing.position.equals(event.position))){
      existing.lastChangeTime = event.time;
    }*/

    // if(existing.touchId != event.touchId) logger.warning("updating existing event with different touchId");
    if(event.velocity == null){
      // calculate velocity
      long dt = event.time-existing.time;
      if(dt > 0){
        float deltaTime = ((float)dt)/1000.0f;
        // update velocity
        existing.velocity = event.position.get();
        existing.velocity.sub(existing.position);
        existing.velocity.div(deltaTime);
      }
    } else {
      existing.velocity = event.velocity;
    }

    if(event.velocitySmoothed == null)
      existing.velocitySmoothed.lerp(existing.velocity, velocitySmoothCoeff);
    else
      existing.velocitySmoothed = event.velocitySmoothed;

    existing.time = event.time;
    existing.position = event.position;
    existing.eventType = event.eventType;

    return existing;
  }

  /// finds the targeted node and triggers events
  private void processTouchEvent(TouchEvent event){
    if(event == null || event.eventType == null){
      logger.warning("TouchManager.processTouchEvent got null event or eventType");
      return;
    }

    switch(event.eventType){
      case TOUCH_DOWN:
        // init time
        //if(event.startTime == null)
          event.startTime = event.time;
        // init velocity
        if(event.velocity == null)
          event.velocity = new PVector(0.0f, 0.0f, 0.0f);
        if(event.velocitySmoothed == null)
          event.velocitySmoothed = new PVector(0.0f, 0.0f, 0.0f);
        // init target
        event.node = getNodeForTouchPosition(event.position);
        event.startPosition = event.position;
        // store
        activeTouchEvents.put(event.touchId, event);
        break;

      case TOUCH_MOVE:
        { // find and update existing active TouchEvent
          TouchEvent existing = activeTouchEvents.get(event.touchId);
          if(existing != null){
            event = updateExistingEvent(existing, event);
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
            //TouchEvent tmpEvent = event.copy();
            //tmpEvent.eventType = TouchEvent.EventType.TOUCH_EXIT;
            event.eventType = TouchEvent.EventType.TOUCH_EXIT;

            // trigger touch exit events
            this.receiveTouchEvent(event);
            if(prev != null)
              prev.receiveTouchEvent(event);

            // prepare touch enter event
            //tmpEvent = event.copy();
            //tmpEvent.eventType = TouchEvent.EventType.TOUCH_ENTER;
            event.eventType = TouchEvent.EventType.TOUCH_ENTER;

            // trigger touch enter events
            this.receiveTouchEvent(event);
            if(n != null){
              n.receiveTouchEvent(event);
            }

            // restore TOUCH_MOVE type for further processing
            event.eventType = TouchEvent.EventType.TOUCH_MOVE;
          }
        }

        break;

      case TOUCH_UP:
        { // find and update existing active TouchEvent
          TouchEvent existing = activeTouchEvents.get(event.touchId);

          if(existing != null){
            event = updateExistingEvent(existing, event);
          } else {
            logger.warning("no existing touch event for touch up event");
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
            //TouchEvent tmpEvent = event.copy();
            //tmpEvent.eventType = TouchEvent.EventType.TOUCH_EXIT;
            event.eventType = TouchEvent.EventType.TOUCH_EXIT;

            // trigger touch exit events
            this.receiveTouchEvent(event);
            if(prev != null)
              prev.receiveTouchEvent(event);

            // prepare touch enter event
            //tmpEvent = event.copy();
            //tmpEvent.eventType = TouchEvent.EventType.TOUCH_ENTER;
            event.eventType = TouchEvent.EventType.TOUCH_ENTER;

            // trigger touch enter events
            this.receiveTouchEvent(event);
            if(n != null){
              n.receiveTouchEvent(event);
            }

            // restore TOUCH_MOVE type for further processing
            event.eventType = TouchEvent.EventType.TOUCH_UP;
          }
        }

        { // check for click
          Long dur = event.getDuration();

          if(dur != null && dur <= clickMaxInterval && event.distance() <= clickMaxDistance){
            /*
        	  // find previous click and double click times
        	  Long previousClickTime = null;
        	  Long previousDoubleClickTime = null;

            Iterator<TouchEvent> iter = this.clickHistory.iterator();
            while(iter.hasNext()) {
              TouchEvent previous = iter.next();

              //logger.info("prev click ti: "+Long.toString(previous.time));
              if(previous.node == event.node && previous.mostRecentNode == event.mostRecentNode) {
            	  if(previous.eventType == TouchEvent.EventType.TOUCH_CLICK) {
            		  if(previousClickTime == null || previousClickTime < previous.time) {
            			  previousClickTime = previous.time;
            		  }
            	  } else if(previous.eventType == TouchEvent.EventType.TOUCH_DOUBLECLICK) {
            		  if(previousDoubleClickTime == null || previousDoubleClickTime < previous.time) {
            			  previousDoubleClickTime = previous.time;
            		  }
            	  }
              }
            }

            System.out.println("prev click:" + Long.toString(previousClickTime == null ? 0 : previousClickTime));
            // process and trigger possible DOUBLE_CLICK first
            if((previousDoubleClickTime == null || previousDoubleClickTime < (event.time - this.doubleClickMinDelay))
            		&& previousClickTime != null
            		&& (event.time - previousClickTime) < this.doubleClickMaxInterval) {
            	// logger.info("doubledur: "+Long.toString((event.time - previousClickTime)));
            	event.eventType = TouchEvent.EventType.TOUCH_DOUBLECLICK;

            	if(event.node != null) {
            	  event.node.receiveTouchEvent(event);
            	}
              this.receiveTouchEvent(event);

            	// record double click event in click history
            	this.clickHistory.add(event.copy());
            }*/
            //TouchEvent tmpEvent = event.copy();
            //tmpEvent.eventType = TouchEvent.EventType.TOUCH_CLICK;
            event.eventType = TouchEvent.EventType.TOUCH_CLICK;
            // logger.warning("CLICK: dist=" + Float.toString(PVector.dist(tlog.touchEvent.position, event.position)));
            // logger.warning("pos1=" + tlog.touchEvent.position.toString());
            // logger.warning("pos2=" + event.position.toString());

            // trigger touch click events
            this.receiveTouchEvent(event);

            if(event.node != null){
              // logger.finest("TouchManager triggering TOUCH_CLICK event on Node: "+event.node.getName());
              event.node.receiveTouchEvent(event);
            }

            if(event.mostRecentNode != null && event.mostRecentNode != event.node){
              event.mostRecentNode.receiveTouchEvent(event);
            }

         	/*this.clickHistory.add(event.copy());
            while(this.clickHistory.size() > MAX_CLICK_HISTORY_SIZE) {
              this.clickHistory.poll();
            }*/

            event.eventType = TouchEvent.EventType.TOUCH_UP; // restore
          }
        }

        // this is the end of this touch
        activeTouchEvents.remove(event.touchId);
        break;

      default:
        logger.warning("TouchManager.processTouchEvent got unknown eventType");
        return;
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

  public Node getNode(){
    return this.node;
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

  public long getClickMaxInterval() {
	  return clickMaxInterval;
  }

  public void setClickMaxInterval(long interval){
    clickMaxInterval = interval;
  }

  // public long getDoubleClickMaxInterval() {
	//   return doubleClickMaxInterval;
  // }
  //
  // public void setDoubleClickMaxInterval(long interval){
	// this.doubleClickMaxInterval = interval;
  // }

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

  public void drawActiveTouches(){
    PGraphics pg = Node.getPGraphics();

    pg.colorMode(pg.RGB, 255);
    pg.fill(pg.color(255,100,100,150));
    pg.noStroke();
    pg.ellipseMode(pg.CENTER);

    for(TouchEvent event : activeTouchEvents.values())
      pg.ellipse(event.position.x, event.position.y, 25, 25);
  }

  private long getTime(){
      return this.time;
  }

  // TouchManager doesn't keep active touch events (like this)
  @Override public void addActiveTouchEvent(TouchEvent evt){}

  /*private void finalizeIdleTouchEvents(){
    List<TouchEvent> events = super.getActiveTouchEvents();
    long limit = this.getTime() - super.IDLE_DURATION;

    for(int i=events.size()-1; i>=0; i--){
      TouchEvent event = activeTouchEvents.get(i);
      if(event == null)
        continue;

      if((event.time != null && event.time < limit)
      || (event.lastChangeTime == null && event.getDuration() > super.IDLE_DURATION)
      || (event.lastChangeTime != null && event.lastChangeTime < limit)){
        logger.info("TouchManager removing event");
        if(event.node != null){
            logger.info("ON NODE");
          event.node.removeActiveTouchEvent(event);
        }

        if(event.mostRecentNode != null && event.mostRecentNode != event.node){
            logger.info("ON MOST RECENT");
          event.mostRecentNode.removeActiveTouchEvent(event);
        }

        this.removeActiveTouchEvent(event);
      }
    }
  }*/
}
