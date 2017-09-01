package com.fuse.ui.extensions;

import java.util.List;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class PinchZoom extends ExtensionBase {

  // attributes
  private PinchMath math = null;
  private PVector originalScale, originalPosition;
  private PVector targetScale, targetPosition;
  // configurables
  private boolean bRestore = false;
  private boolean bSmoothRestore = true;
  private float smoothing = 7.0f;

  public Event<Node> startPinchEvent, endPinchEvent;

  public PinchZoom(){
    startPinchEvent = new Event<>();
    endPinchEvent = new Event<>();
  }

  @Override
  public void destroy(){
    super.destroy();
    startPinchEvent.destroy();
    endPinchEvent.destroy();
  }

  @Override
  public void update(float dt){
    if(targetScale != null){
      PVector scale = targetScale.get();
      // delta
      scale.sub(this.node.getScale());
      // smoothed delta
      scale.mult(1.0f / this.smoothing);
      // applied delta
      scale.add(this.node.getScale());
      // apply
      this.node.setScale(scale);
    }

    if(targetPosition != null){
      PVector pos = targetPosition.get();
      // delta
      pos.sub(this.node.getPosition());
      // smoothed delta
      pos.mult(1.0f / this.smoothing);
      // applied delta
      pos.add(this.node.getPosition());
      // apply
      this.node.setPosition(pos);
    }
  }

  private void start(TouchEvent[] events){
    this.math = new PinchMath(events);
    this.originalScale = this.node.getScale();
    this.originalPosition = this.node.getPosition();
    this.startPinchEvent.trigger(this.node);
  }

  private void stop(){
    this.endPinchEvent.trigger(this.node);

    if(this.bRestore)
      this.restore(this.bSmoothRestore);

    if(!bSmoothRestore){
      this.targetPosition = null;
      this.targetScale = null;
    }

    this.math = null;
  }

  public void restore(boolean smooth){
    if(this.originalScale != null){
      if(smooth)
        this.targetScale = this.originalScale;
      else
        this.node.setScale(this.originalScale);
    }

    if(this.originalPosition != null){
      if(smooth)
        this.targetPosition = this.originalPosition;
      else
        this.node.setPosition(this.originalPosition);
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

  private void touchUpdate(){
    PVector scale = this.originalScale.get();
    float pinchScale = this.math.getPinchScale();
    scale.mult(pinchScale);
    //this.node.setScale(scale);
    this.targetScale = scale;

    // current "dragged" position of the pinch-center
    PVector p = math.getParentSpaceCurrentPinchCenter();
    // offset of pinch-center to origin of pinched-node
    PVector offset = math.getLocalStartPinchCenter();
    // scale offset
    p.x = p.x - offset.x * pinchScale;
    p.y = p.y - offset.y * pinchScale;
    // update position
    // this.node.setPosition(p);
    this.targetPosition = p;
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

  public boolean getRestore(){
    return bRestore;
  }

  public void setRestore(boolean enableRestore){
    bRestore = enableRestore;
  }

  public float getSmoothing(){
    return smoothing;
  }

  public void setSmoothing(float smoothing){
    this.smoothing = smoothing;
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


    float scaler = math.getPinchScale();
    localPos = node.toLocal(math.getGlobalCurrentPinchCenter());
    pg.fill(pg.color(255,100,100,80));
    pg.ellipse(localPos.x, localPos.y, 10 * scaler, 10 * scaler);

    pg.stroke(pg.color(100,255,100,200));
    pg.strokeWeight(3.0f);
    PVector vec = math.getLocalStartPinchCenter();
    pg.line(0.0f, 0.0f, vec.x, vec.y);
  }
}

//
// Separate class for the math and cache
//

class PinchMath {
  private TouchEvent[] events;
  private Node node;
  private PVector localStartPinchCenter;

  /** MUST be initialized with an array of exactly TWO touch event instances */
  public PinchMath(TouchEvent[] events){
    this.events = events;
    this.node = events[0].node;
    this.localStartPinchCenter = this.node.toLocal(this.calcGlobalStartPinchCenter());
  }

  public TouchEvent[] getEvents(){
    return events;
  }

  public void resetActiveCaches(){
    getGlobalCurrentPinchCenterCache = null;
    getGlobalCurrentDeltaCache = null;
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
