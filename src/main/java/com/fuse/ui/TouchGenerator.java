package com.fuse.ui;

import java.util.List;
import java.util.ArrayList;
import processing.core.PVector;

public class TouchGenerator {

  private Node rootNode = null;
  private PVector fromPos = null;
  private PVector toPos = null;
  private PVector delta = null;
  private int moveCount = 0;
  private TouchGenerator[] mixSources = null;
  private boolean bUpTouch = true;
  private int touchId = 0;


  public static TouchGenerator on(Node rootNode){
    TouchGenerator gen = new TouchGenerator();
    gen.rootNode = rootNode;
    return gen;
  }

  /**
   * Create a TouchGenerator instance which tries extracts touch events from both given sources
   * and performs them in sequence; alternating between single events from each source.
   */
  public TouchGenerator mix(TouchGenerator mixSource1, TouchGenerator mixSource2){
    mixSources = new TouchGenerator[2];
    mixSources[0] = mixSource1;
    mixSources[1] = mixSource2;
    return this;
  }

  public TouchGenerator from(float x, float y){ return from(new PVector(x,y,0.0f)); }
  public TouchGenerator from(PVector pos){
    this.fromPos = pos;
    return this;
  }

  public TouchGenerator to(float x, float y){ return to(new PVector(x,y,0.0f)); }
  public TouchGenerator to(PVector pos){
    this.toPos = pos;
    return this;
  }

  public TouchGenerator move(float deltaX, float deltaY){ return move(new PVector(deltaX, deltaY, 0)); }
  public TouchGenerator move(PVector delta){
    this.delta = delta;
    return this;
  }

  public TouchGenerator moves(int amount){
    this.moveCount = amount;
    return this;
  }

  public TouchGenerator noUp(){
    bUpTouch = false;
    return this;
  }
  public TouchGenerator setTouchId(int touchId){
    this.touchId = touchId;
    return this;
  }

  /** Gets ordered list of events and executes them all */
  public void go(){
    TouchManager touchManager = new TouchManager();
    touchManager.setNode(rootNode);

    for(TouchEvent te : getTouchEvents()){
      touchManager.submitTouchEvent(te);
    }
  }

  private List<TouchEvent> _getTouchEvents_cache = null;
  public List<TouchEvent> getTouchEvents(){
    if(_getTouchEvents_cache != null) return _getTouchEvents_cache;

    if(mixSources != null){
      _getTouchEvents_cache = eventsForMixedSources();
    } else {
      _getTouchEvents_cache = eventsForlinearFromToInSteps();
    }

    return _getTouchEvents_cache;
  }

  private List<TouchEvent> eventsForlinearFromToInSteps(){
    List<TouchEvent> events = new ArrayList();

    if(fromPos == null)
      fromPos = new PVector(0,0,0);

    if(toPos == null){
      if(delta != null && fromPos != null)
        toPos = fromPos.copy().add(delta);
      else
        return events;
    }

    events.add(TouchManager.createTouchDownEvent(touchId, fromPos));

    if(moveCount > 0){
      // if no touch up; then the last move event should end at the toPost
      int divAmount = bUpTouch ? (moveCount+1) : moveCount;
      PVector stepDelta = toPos.copy().sub(fromPos).div(divAmount);
      PVector currentPos = fromPos.copy();

      for(int i=0; i<moveCount; i++){
        events.add(TouchManager.createTouchMoveEvent(touchId, currentPos.add(stepDelta)));
      }
    }

    if(bUpTouch)
      events.add(TouchManager.createTouchUpEvent(touchId, toPos));

    return events;
  }

  private List<TouchEvent> eventsForMixedSources(){
    List<TouchEvent> events = new ArrayList();

    // find longest list size
    int max = 0;
    for(int i=0; i<mixSources.length; i++)
      if(mixSources[i].getTouchEvents().size() > max)
        max = mixSources[i].getTouchEvents().size();

    // mix events into our events list
    for(int i=0; i<max; i++)
      for(int j=0; j<mixSources.length; j++)
        if(mixSources[j].getTouchEvents().size() > i)
          events.add(mixSources[j].getTouchEvents().get(i));

    return events;
  }
}