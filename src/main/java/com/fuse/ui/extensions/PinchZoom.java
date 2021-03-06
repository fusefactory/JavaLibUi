package com.fuse.ui.extensions;

import java.util.List;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;
import com.fuse.utils.Event;

import processing.core.PGraphics;
import processing.core.PVector;

public class PinchZoom extends TransformerExtension {
  // attributes
  private PinchMath math = null;
  private PVector originalScale, originalPosition;
  // configurables
  private boolean bRestore = false;

  // events
  public Event<Node> startPinchEvent, endPinchEvent;

  public PinchZoom(){
    startPinchEvent = new Event<>();
    endPinchEvent = new Event<>();
  }

  @Override
  public void destroy(){
    startPinchEvent.destroy();
    endPinchEvent.destroy();
    super.destroy();
  }

  @Override public void update(float dt){
      if(isPinching()){
        this.updatePinching();
      }

      super.update(dt);
  }

  @Override protected void setup(){
    this.node.touchDownEvent.addListener((TouchEvent event) -> {
      if(!this.isPinching()){
        TouchEvent[] events = this.getPinchZoomTouchEvents();

        if(events != null)
          this.startPinching(events); // creates a this.math object, with the given events, making this.isPinching() == true

        return;
      }
    }, this);

    this.node.touchUpEvent.addListener((TouchEvent event) -> {
      if(this.isPinching()){
        TouchEvent[] events = this.math.getEvents();
        // one of ours pinch events?
        if(events[0] == event || events[1] == event)
          stopPinching(); // remove this.math, making this.isPinching() == true
      }
    }, this);
  }

  @Override protected void teardown(){
    if(this.node == null) return;
    this.node.touchDownEvent.removeListeners(this);
    this.node.touchUpEvent.removeListeners(this);
  }

  private void startPinching(TouchEvent[] events){
    this.math = new PinchMath(events);
    this.originalScale = this.node.getScale();
    this.originalPosition = this.node.getPosition();
    this.startPinchEvent.trigger(this.node);
  }

  private void stopPinching(){
    this.endPinchEvent.trigger(this.node);

    if(this.bRestore){
      this.restore();
    } else {
      super.stopActiveTransformations();
    }

    this.math = null;
  }

  private void updatePinching(){
    this.math.update();

    // SCALING
    PVector scale = this.originalScale.get();
    float pinchScale = this.math.getPinchScale();
    scale.mult(pinchScale);

    // TRANSLATE
    // current "dragged" position of the pinch-center
    PVector p = math.getParentSpaceCurrentPinchCenter();
    // offset of pinch-center to origin of pinched-node
    PVector offset = math.getLocalStartPinchCenter();
    // scale offset; have to multiply by pinchScale, because
    // the pinchscale is not yet applied to the node (because of smoothing)
    //offset.mult(pinchScale); // update offset for new pinchScale
    //p.sub(offset); // pinch-center-to-scaled-node-origin
    p.x = p.x - offset.x * scale.x;
    p.y = p.y - offset.y * scale.y;

    super.transformScale(scale);
    super.transformPosition(p);
  }

  public void restore(){
    if(this.originalScale != null){
      super.transformScale(this.originalScale);
    }

    if(this.originalPosition != null){
      super.transformPosition(this.originalPosition);
    }
  }

  public boolean isPinching(){
    return this.math != null;
  }

  // state reader methods // // // // //

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

  // configure methods // // // // //

  public boolean getRestore(){
    return bRestore;
  }

  public void setRestore(boolean enableRestore){
    bRestore = enableRestore;
  }

  // static factory methods // // // // //

  public static PinchZoom enableFor(Node n){
    PinchZoom d = getFor(n);

    if(d == null){
      d = new PinchZoom();
      n.use(d);
    }

    return d;
  }

  public static PinchZoom getFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(PinchZoom.class.isInstance(ext))
        return (PinchZoom)ext;
    return null;
  }

  public static void disableFor(Node n){
    for(ExtensionBase ext : n.getExtensions()) {
      if(PinchZoom.class.isInstance(ext))
          n.stopUsing(ext);
    }
  }

  // dev-only methods // // // // //

  @Override
  public void drawDebug(){
	super.drawDebug();

    PinchMath math = this.math; // copy reference to local scope; had some issues of "this.math" becoming null DURING this draw method
    if(math == null) return;

    PGraphics pg = Node.getPGraphics();
    pg.colorMode(PGraphics.RGB, 255);
    pg.fill(pg.color(100,100,255,150));
    pg.noStroke();
    pg.ellipseMode(PGraphics.CENTER);

    PVector localPos = node.toLocal(math.getEvents()[0].position);
    pg.ellipse(localPos.x, localPos.y, 30, 30);
    localPos = node.toLocal(math.getEvents()[1].position);
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

  /** Really only resets some cache variable, but should still be called every update ;) */
  public void update(){
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
    if(getGlobalCurrentDeltaCache != null)
      return getGlobalCurrentDeltaCache;

    float result = events[0].position.dist(events[1].position);
    getGlobalCurrentDeltaCache = result;
    return result;
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
