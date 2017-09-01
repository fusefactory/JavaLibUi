package com.fuse.ui.extensions;

import java.util.List;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

class PinchMath {
  private TouchEvent[] events;
  private Node node;
  private PVector localStartPinchCenter;

  /** MUST be initialized with an array of exactly TWO touch event instances */
  public PinchMath(TouchEvent[] events){
    this.events = events;
    this.node = events[0].node;
    this.localStartPinchCenter = this.node.toLocal(this.getGlobalStartPinchCenter());
  }

  public TouchEvent[] getEvents(){
    return events;
  }

  public void resetActiveCaches(){
    getGlobalCurrentPinchCenterCache = null;
    getGlobalCurrentDeltaCache = null;
  }

  public PVector getLocalCurrentPinchCenter(){
    PVector p = getGlobalCurrentPinchCenter();
    return this.node.toLocal(p);
  }

  private PVector getGlobalStartPinchCenterCache = null;

  public PVector getGlobalStartPinchCenter(){
    if(getGlobalStartPinchCenterCache == null)
      getGlobalStartPinchCenterCache = calcGlobalStartPinchCenter();
    return getGlobalStartPinchCenterCache;
  }

  private PVector calcGlobalStartPinchCenter(){
    PVector p1 = events[0].startPosition.get();
    PVector p2 = events[1].startPosition.get();
    // delta
    p2.sub(p1);
    // half-delta
    p2.mult(0.5f);
    // center
    p1.add(p2);
    return p1;
  }

  public PVector getGlobalCurrentPinchCenter(){
    if(getGlobalCurrentPinchCenterCache == null)
      getGlobalCurrentPinchCenterCache = calcGlobalCurrentPinchCenter();
    return getGlobalCurrentPinchCenterCache;
  }

  private PVector getGlobalCurrentPinchCenterCache = null;

  private PVector calcGlobalCurrentPinchCenter(){
    PVector p1 = events[0].position.get();
    PVector p2 = events[1].position.get();
    // delta
    p2.sub(p1);
    // half-delta
    p2.mult(0.5f);
    // center
    p1.add(p2);
    return p1;
  }

  public float getGlobalStartDelta(){
    if(getGlobalStartDeltaCache == null){
      getGlobalStartDeltaCache = events[0].startPosition.dist(events[1].startPosition);
    }

    return getGlobalStartDeltaCache;
  }

  private Float getGlobalStartDeltaCache = null;

  public float getGlobalCurrentDelta(){
    if(getGlobalCurrentDeltaCache == null){
      getGlobalCurrentDeltaCache = events[0].position.dist(events[1].position);
    }

    return getGlobalCurrentDeltaCache;
  }

  private Float getGlobalCurrentDeltaCache = null;

  public float getPinchScale(){
    return this.getGlobalCurrentDelta() / this.getGlobalStartDelta();
  }

  public PVector getLocalStartPinchCenter(){
    return localStartPinchCenter;
  }

  public PVector getCurrentGlobalStartPinchCenter(){
    return node.toGlobal(getLocalStartPinchCenter());
  }

  public PVector getParentSpaceCurrentPinchCenter(){
    PVector p = this.getGlobalCurrentPinchCenter();
    Node n = node.getParent();
    return (n == null) ? p : n.toLocal(p);
  }
}

public class PinchZoom extends ExtensionBase {

  private PinchMath math = null;
  private PVector originalScale, originalPosition;

  private void start(TouchEvent[] events){
    this.math = new PinchMath(events);
    this.originalScale = this.node.getScale();
    this.originalPosition = this.node.getPosition();
  }

  private void stop(){
    this.math = null;

    if(this.originalScale != null){
      this.node.setScale(this.originalScale);
      this.originalScale = null;
    }

    if(this.originalPosition != null){
      this.node.setPosition(this.originalPosition);
      this.originalPosition = null;
    }
  }

  @Override public void enable(){
    if(this.isEnabled() || this.node == null) return;
    super.enable();

    this.node.touchDownEvent.addListener((TouchEvent event) -> {
      if(this.math == null){
        TouchEvent[] events = this.getPinchZoomTouchEvents();

        if(events != null)
          this.start(events);

        return;
      }
    });

    this.node.touchUpEvent.addListener((TouchEvent event) -> {
      if(this.math != null && this.getPinchZoomTouchEvents() == null){
        this.stop();
      }
    });

    this.node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(this.math != null && this.originalScale != null){
        this.math.resetActiveCaches();
        this.touchUpdate();
      }
    }, this);
  }

  @Override public void disable(){
    super.disable();
    if(this.node == null) return;
    this.node.touchMoveEvent.removeListeners(this);
  }

//  boolean isActive(){
//    return this.math != null;
//  }

  private void touchUpdate(){
    PVector scale = this.originalScale.get();
    float pinchScale = this.math.getPinchScale();
    scale.mult(pinchScale);
    this.node.setScale(scale);

    // PVector p = math.getLocalStartPinchCenter();
    // p.mult(-1.0f * pinchScale);
    // p.add(this.originalPosition);
    PVector p = math.getParentSpaceCurrentPinchCenter();
    //PVector offset = math.getCurrentGlobalStartPinchCenter();
    p.sub(math.getLocalStartPinchCenter());
    //p.sub(offset);
    this.node.setPosition(p);



    // if(zoomMode == ZoomMode.SIZE){
    //   PVector newSize = new PVector(
    //     originalNodeSize.x * scaler.x,
    //     originalNodeSize.y * scaler.y,
    //     originalNodeSize.z * scaler.z);
    //   // getNode().setSize(newSize);
    // }
    //
    // if(zoomMode == ZoomMode.SCALE){
    //   // getNode().setScale(scaler);
    // }
  }

  /** returns null when not actively pinch-zooming, otherwise returns an array of exactly two touch-events */
  private TouchEvent[] getPinchZoomTouchEvents(){
    // the node should have two active touch events
    List<TouchEvent> activeTouchEvent = node.getActiveTouchEvents();
    if(activeTouchEvent.size() != 2)
      return null;

    TouchEvent[] events = new TouchEvent[2];
    events[0] = activeTouchEvent.get(0);
    events[1] = activeTouchEvent.get(1);

    // both active touch events must also have started on this node
    if(events[0].node != this.node || events[1].node != this.node)
      return null;

    return events;
  }

//  /** @return PVector Distance between the start-points of the two touches */
//  public PVector getGlobalStartDelta(){
//    if(!isActive()) return new PVector();
//    PVector result = touchEvent2.startPosition.get();
//    result.sub(touchEvent1.startPosition);
//    return result;
//  }
//
//  /** @return PVector Distance between the current positions of the two touches */
//  public PVector getGlobalCurrentDelta(){
//    if(!isActive()) return new PVector();
//    PVector result = touchEvent2.position.get();
//    result.sub(touchEvent1.position);
//    return result;
//  }

//  public PVector getGlobalPinchScale(){
//    if(!isActive()) return new PVector();
//    PVector start = getGlobalStartDelta();
//    PVector current = getGlobalCurrentDelta();
//    return new PVector(
//      Math.abs(start.x == 0.0f ? 1.0f : current.x / start.x),
//      Math.abs(start.y == 0.0f ? 1.0f : current.y / start.y),
//      Math.abs(start.z == 0.0f ? 1.0f : current.z / start.z));
//  }

//  public PVector getGlobalPinchTranslate(){
//    if(!isActive()) return new PVector();
//
//
//    // start center of two touches
//    PVector delta = touchEvent2.startPosition.get();
//    delta.sub(touchEvent1.startPosition);
//    delta.mult(0.5f);
//    PVector startCenter = PVector.add(delta, touchEvent1.startPosition);
//
//    PVector currentDelta = touchEvent2.position.get();
//    currentDelta.sub(touchEvent1.position);
//    currentDelta.mult(0.5f);
//    PVector currentCenter = PVector.add(currentDelta, touchEvent1.position);
//
//    PVector scale = getGlobalPinchScale();
//
//    PVector touchOffset = currentCenter;
//    touchOffset.sub(startCenter);
//    touchOffset.add(touchEvent1.offset());
//
//    return new PVector(
//      -Math.abs(scale.x*touchOffset.x),
//      -Math.abs(scale.y*touchOffset.y),
//      -Math.abs(scale.z*touchOffset.z));
//  }

  public static PinchZoom enableFor(Node n){
    PinchZoom d = getFor(n);

    if(d == null){
      d = new PinchZoom();
      n.use(d);
    }

    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(PinchZoom.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }

  public static PinchZoom getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(PinchZoom.class.isInstance(ext))
        return (PinchZoom)ext;
    return null;
  }

  @Override
  public void drawDebug(){
    if(this.math == null) return;

    PGraphics pg = Node.getPGraphics();
    pg.colorMode(pg.RGB, 255);
    pg.fill(pg.color(100,100,255,150));
    pg.noStroke();
    pg.ellipseMode(pg.CENTER);

    PVector localPos = node.toLocal(this.math.getEvents()[0].position);
    pg.ellipse(localPos.x, localPos.y, 30, 30);
    localPos = node.toLocal(this.math.getEvents()[1].position);
    pg.ellipse(localPos.x, localPos.y, 30, 30);

    localPos = node.toLocal(math.getGlobalStartPinchCenter());
    pg.ellipse(localPos.x, localPos.y, 15, 15);

    float scaler = math.getPinchScale();
    localPos = node.toLocal(math.getGlobalCurrentPinchCenter());
    pg.fill(pg.color(255,100,100,80));
    pg.ellipse(localPos.x, localPos.y, 15 * scaler, 15 * scaler);

    pg.stroke(pg.color(100,255,100,200));
    pg.strokeWeight(3.0f);
    PVector vec = math.getLocalStartPinchCenter();
    pg.line(0.0f, 0.0f, vec.x, vec.y);
    // pg.stroke(pg.color(0,200,0));
    // pg.line(0.0f, 0.0f, vec.x, vec.y);

    // PVector p = math.getParentSpaceCurrentPinchCenter();
    // PVector offset = math.getCurrentGlobalStartPinchCenter();
    // p.sub(offset);
    // this.node.setPosition(p);

  }
}
