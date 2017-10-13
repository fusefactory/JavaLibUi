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
  private Float duration = null;
  private Float dtAfterEveryTouch = null;


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

  /* Specify the duration of the gesture in seconds */
  public TouchGenerator duration(float timeInSeconds){
    duration = timeInSeconds;
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

  public TouchGenerator updateSceneAfterEveryTouch(){
    return this.updateSceneAfterEveryTouch(1.0f/30.0f);
  }

  public TouchGenerator updateSceneAfterEveryTouch(Float dt){
    dtAfterEveryTouch = dt;
    return this;
  }

  public void doubleclick(float x, float y){
    TouchManager touchManager = new TouchManager();
    touchManager.setNode(rootNode);

    touchManager.touchDown(1, new PVector(x, y, 0.0f));
    touchManager.touchUp(1, new PVector(x, y, 0.0f));
    touchManager.update(touchManager.getDoubleClickMaxInterval() / 2);
    touchManager.touchDown(1, new PVector(x, y, 0.0f));
    touchManager.touchUp(1, new PVector(x, y, 0.0f));
  }

  /** Gets ordered list of events and executes them all */
  public void go(){
    TouchManager touchManager = new TouchManager();
    touchManager.setNode(rootNode);

    List<TouchEvent> touchEvents = getTouchEvents();

    Float deltaTime = null;
    if(duration != null && touchEvents.size() > 1){
      deltaTime = duration / (touchEvents.size()-1);
      touchManager.update(0.0f); // this enabled "controlled time" inside TouchManager
    }

    for(TouchEvent te : touchEvents){
      touchManager.submitTouchEvent(te);
      if(deltaTime != null)
        touchManager.update(deltaTime);
      if(dtAfterEveryTouch != null)
        touchManager.getNode().updateSubtree(dtAfterEveryTouch);
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
      if(delta != null && fromPos != null){
        toPos = fromPos.get();
        toPos.add(delta);
      } else
        return events;
    }

    events.add(TouchManager.createTouchDownEvent(touchId, fromPos));

    if(moveCount > 0){
      // if no touch up; then the last move event should end at the toPost
      int divAmount = bUpTouch ? (moveCount+1) : moveCount;
      PVector stepDelta = toPos.get();
        stepDelta.sub(fromPos);
        stepDelta.div(divAmount);
      PVector currentPos = fromPos.get();

      for(int i=0; i<moveCount; i++){
        currentPos.add(stepDelta);
        events.add(TouchManager.createTouchMoveEvent(touchId, currentPos));
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
