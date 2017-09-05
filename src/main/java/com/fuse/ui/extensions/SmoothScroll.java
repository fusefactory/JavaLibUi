package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class SmoothScroll extends ExtensionBase {
  // attributes
  private Node scrollableNode = null;
  // original values
  private PVector originalNodePosition = null;
  private PVector dragStartNodePositionGlobal = null;
  // velocity
  private PVector velocity = null;
  private PVector smoothedVelocity = null;
  private final static float velocitySmoothCoeff = 0.1f;
  private float dampingFactor = 0.001f;
  private final static float minVelocityMag = 1.0f; // when velocity reaches this value (or lower), we finalize the movement
  private float velocityReductionFactor = 0.2f; // factor to multipy the (already smoother) smoothedVelocity when setting the main velocity
  // snapping (falling back into place)
  private PVector snapInterval = null;
  private float snapVelocityMag = 75.0f; // when velocity reaches this value (or lower), we start snapping
  private PVector snapPosition = null;
  private float snapFactor = 0.95f;
  private final static float snapDoneDist = 0.9f;
  // offset limits
  private PVector minOffset = null;
  private PVector maxOffset = null;

  // events
  public Event<PVector> newSnapPositionEvent;
  public Event<PVector> newSnapIntervalPageEvent;
  public Event<Node> restEvent;

  public SmoothScroll(){
    smoothedVelocity = new PVector(0.0f, 0.0f, 0.0f);
    newSnapPositionEvent = new Event<>();
    newSnapIntervalPageEvent = new Event<>();
    restEvent = new Event<>();

    // newSnapIntervalPageEvent triggers everytime newSnapPositionEvent is triggered
    newSnapPositionEvent.addListener((PVector snapPos) -> {
      PVector value = this.calcSnapIntervalPage(snapPos);
      if(value != null)
        newSnapIntervalPageEvent.trigger(value);
    }, this);
  }

  @Override public void destroy(){
    super.destroy();
    scrollableNode = null;
  }

  @Override
  public void update(float dt){
    if(isSnapping()){
      PVector curPos = scrollableNode.getPosition();
      float local_dt = dt;

      while(local_dt > 1.0f){
        PVector delta = snapPosition.get();
        delta.sub(curPos);
        delta.mult(snapFactor);
        curPos.add(delta);
        local_dt -= 1.0f;
      }

      PVector delta = snapPosition.get();
      delta.sub(curPos);
      delta.mult(snapFactor);
      delta.lerp(new PVector(0,0,0), 1.0f-local_dt);
      curPos.add(delta);

      scrollableNode.setPosition(curPos);

      if(curPos.dist(snapPosition) <= snapDoneDist){
        scrollableNode.setPosition(snapPosition);
        // after snapping to desired position, snap again if beyonf offset limits
        this.snapPosition = this.getOffsetLimitSnapPosition();
        if(this.snapPosition != null)
          newSnapPositionEvent.trigger(this.snapPosition.get());
        else
          restEvent.trigger(scrollableNode);
      }

      return;
    }

    if(!isDamping())
      return;

    // apply velocity
    PVector deltaPos = velocity.get();
    deltaPos.mult(dt);
    PVector pos = scrollableNode.getPosition();
    pos.add(deltaPos);
    scrollableNode.setPosition(pos);

    // "snap back" when damping beyond limits
    this.snapPosition = getOffsetLimitSnapPosition();
    if(this.snapPosition != null){ // isSnapping() == true
      this.velocity = null; // isDamping() = false
      newSnapPositionEvent.trigger(this.snapPosition.get());
      return;
    }

    // apply damping
    PVector dampedVelocity = velocity.get();
    dampedVelocity.mult(dampingFactor);
    velocity.lerp(dampedVelocity, dt);

    float mag = velocity.mag();

    if(snapInterval != null && mag <= snapVelocityMag){
      this.startSnapping();
      return;
    }

    if(velocity.mag() < minVelocityMag){
      velocity = null; // isDamping() = false
      restEvent.trigger(scrollableNode);
    }
  }

  @Override
  public void drawDebug(){
    float deltaX = node.getSize().x * 0.1f;
    float deltaY = node.getSize().y * 0.1f;
    float offsetX = 0.0f;
    float offsetY = 0.0f;

    if(originalNodePosition != null){
      PVector curOffset = this.getCurrentOffset();
      offsetX = curOffset.x % deltaX;
      offsetY = curOffset.y % deltaY;
    }

    PGraphics pg = Node.getPGraphics();
    pg.stroke(255,0,0, 150);
    pg.strokeWeight(1.0f);

    for(float x = offsetX; x < node.getSize().x; x += deltaX)
      pg.line(x, 0, x, node.getSize().y);

    for(float y = offsetY; y < node.getSize().y; y += deltaY)
      pg.line(0, y, node.getSize().x, y);
  }

  public void enable(){
    super.enable();

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(scrollableNode == null || event.node != this.node)
        return; // touch didn't start on our node

      // just started dragging?
      if(!isDragging()){
        this.velocity = null; // this makes isDamping() false
        this.snapPosition = null; // isSnapping() = false
        dragStartNodePositionGlobal = scrollableNode.getGlobalPosition(); // this makes isDragging true
        smoothedVelocity = new PVector(0.0f, 0.0f, 0.0f);
      }

      if(event.velocitySmoothed != null)
        smoothedVelocity.lerp(event.velocitySmoothed, velocitySmoothCoeff);

      applyDragOffset(event.offset());
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!isDragging())
        return;

      applyDragOffset(event.offset());
      dragStartNodePositionGlobal = null; // this makes isDragging() false

      snapPosition = getOffsetLimitSnapPosition();
      if(snapPosition != null){ // isSnapping() = true
        newSnapPositionEvent.trigger(this.snapPosition.get());
        return;
      }

      // endDragEvent.trigger(this);
      if(event.velocitySmoothed != null)
        smoothedVelocity.lerp(event.velocitySmoothed, velocitySmoothCoeff);

      if(event.velocitySmoothed != null)
        this.velocity = event.velocitySmoothed.get(); //smoothedVelocity; // this makes isDamping() true
      else
        this.velocity = smoothedVelocity.get();

      this.velocity.mult(velocityReductionFactor);
    }, this);
  }

  public void disable(){
    super.disable();

    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);

    velocity = null; // isDamping() = false
    dragStartNodePositionGlobal = null; // isDragging() = false
  }

  private void applyDragOffset(PVector dragOffset){
    if(dragStartNodePositionGlobal == null) // should already be set at first processed touchMoveEvent, but just to be sure
      dragStartNodePositionGlobal = scrollableNode.getGlobalPosition();

    PVector globPos = dragStartNodePositionGlobal.get();
    globPos.add(dragOffset);

    scrollableNode.setGlobalPosition(globPos);
  }

  public boolean isDragging(){
    return dragStartNodePositionGlobal != null;
  }

  public boolean isDamping(){
    return this.velocity != null;
  }

  private void startSnapping(){
    this.velocity = null; // isDamping() == false

    if(this.snapInterval == null)
      return;

    PVector curOffset = this.getCurrentOffset();

    PVector targetOffset = new PVector(curOffset.x - curOffset.x % snapInterval.x,
                                        curOffset.y - curOffset.y % snapInterval.y,
                                        0.0f);

    if(Math.abs(targetOffset.x - curOffset.x) > snapInterval.x * 0.5f)
      targetOffset.x += curOffset.x < targetOffset.x ? -snapInterval.x : snapInterval.x;
    if(Math.abs(targetOffset.y - curOffset.y) > snapInterval.y * 0.5f)
      targetOffset.y += curOffset.y < targetOffset.y ? -snapInterval.y : snapInterval.y;

    snapPosition = originalNodePosition.get();
    snapPosition.add(targetOffset); // isSnapping() = true
    newSnapPositionEvent.trigger(this.snapPosition.get());
  }

  public boolean isSnapping(){
    return snapPosition != null;
  }

  public PVector getVelocity(){
    return this.velocity == null ? new PVector(0,0,0) : velocity.get();
  }

  public Node getScrollableNode(){
    return scrollableNode;
  }

  public void setScrollableNode(Node newScrollableNode){
    boolean wasEnabled = isEnabled();

    if(wasEnabled)
      disable();

    scrollableNode = newScrollableNode;
    originalNodePosition = scrollableNode.getPosition();


    if(wasEnabled)
      enable();
  }

  public float getDampingFactor(){
    return dampingFactor;
  }

  public void setDampingFactor(float newDampingFactor){
    dampingFactor = newDampingFactor;
  }

  public float getVelocityReductionFactor(){
    return velocityReductionFactor;
  }

  public void setVelocityReductionFactor(float factor){
    velocityReductionFactor = factor;
  }

  /**
   * Enables/disabled snapping, when enabling sets the snap interval to the extended node's size
   * @param enable Flag that specifies if snapping should be enabled or disabled
   */
  public void setSnapEnabled(boolean enable){
    if(!enable){
      snapInterval = null;
      return;
    }

    snapInterval = node.getSize();
  }

  public PVector getSnapInterval(){
    return snapInterval;
  }

  public void setSnapInterval(PVector interval){
    snapInterval = interval == null ? null : interval.get();
  }

  public void setSnapFactor(float newFactor){
    snapFactor = newFactor;
  }

  public void setSnapVelocity(float newSnapVelocity){
    snapVelocityMag = newSnapVelocity;
  }

  public void setMinOffset(float x, float y){
    this.setMinOffset(new PVector(x,y,0.0f));
  }

  public void setMinOffset(PVector offset){
    this.minOffset = offset.get();

    PVector p = getOffsetLimitSnapPosition();
    if(p != null){
      this.snapPosition = p;
      newSnapPositionEvent.trigger(this.snapPosition.get());
    }
  }

  public void setMaxOffset(float x, float y){
    this.setMaxOffset(new PVector(x,y,0.0f));
  }

  public void setMaxOffset(PVector offset){
    this.maxOffset = offset;

    PVector p = getOffsetLimitSnapPosition();
    if(p != null){
      this.snapPosition = p;
      newSnapPositionEvent.trigger(this.snapPosition.get());
    }
  }

  public void setScrollPosition(float x, float y){
    this.setScrollPosition(new PVector(x,y,0.0f));
  }

  public void setScrollPosition(PVector pos){
    this.scrollableNode.setPosition(pos);

    if(this.snapInterval != null){
      this.startSnapping();
    }
  }

  public PVector getOffsetLimitSnapPosition(){
    PVector curOffset = getCurrentOffset();
    PVector snapPos = scrollableNode.getPosition();

    boolean snap = false;

    if(minOffset!=null){
      if(curOffset.x < minOffset.x){
        snapPos.x = minOffset.x;
        snap = true;
      }

      if(curOffset.y < minOffset.y){
        snapPos.y = minOffset.y;
        snap = true;
      }
    }

    if(maxOffset!=null){
      if(curOffset.x > maxOffset.x){
        snapPos.x = maxOffset.x;
        snap = true;
      }

      if(curOffset.y > maxOffset.y){
        snapPos.y = maxOffset.y;
        snap = true;
      }
    }

    if(snap)
      return snapPos;

    return null;
  }

  public PVector getCurrentOffset(){
    if(originalNodePosition == null || scrollableNode == null)
      return new PVector(0,0,0);

    PVector vec = scrollableNode.getPosition();
    vec.sub(originalNodePosition);
    return vec;
  }

  public PVector getSnapPosition(){
      return snapPosition;
  }

  public void setSnapPosition(float x, float y){
    this.setSnapPosition(new PVector(x,y));
  }

  public void setSnapPosition(PVector pos){
    if(pos == null){ // abort snapping?
      this.snapPosition = null;
      return;
    }

    this.snapPosition = pos.get();
    this.velocity = null; // isDamping() = false
    newSnapPositionEvent.trigger(this.snapPosition.get());
  }

  public PVector calcSnapIntervalPage(PVector pos){
    if(this.snapInterval == null) return null;
    // offset of the position
    PVector delta = pos.get();
    delta.sub(this.originalNodePosition);
    // divide by the interval
    delta.x = -delta.x / this.snapInterval.x;
    delta.y = -delta.y / this.snapInterval.y;
    return delta;
  }

  public void step(float x, float y){
    this.step(new PVector(x,y,0.0f));
  }

  public void step(PVector offset){
    if(this.snapInterval == null) return;
    PVector current = this.calcSnapIntervalPage(this.scrollableNode.getPosition());
    PVector delta = offset.get();
    delta.mult(-1.0f); // invert; step left means offset to right
    delta.x = delta.x * this.snapInterval.x;
    delta.y = delta.y * this.snapInterval.y;
    if(this.snapPosition != null){
      delta.add(this.snapPosition);
      this.setSnapPosition(delta);
    } else {
      delta.add(this.scrollableNode.getPosition());
      this.setSnapPosition(delta);
    }
  }

  //
  // Static methods
  //

  public static SmoothScroll enableFor(Node touchAreaNode, Node scrollableNode){
    SmoothScroll ext = getFor(touchAreaNode, scrollableNode);

    if(ext == null){
      ext = new SmoothScroll();
      ext.setScrollableNode(scrollableNode);
      touchAreaNode.use(ext);
    }

    return ext;
  }

  public static SmoothScroll disableFor(Node touchAreaNode, Node scrollableNode){
    SmoothScroll ext = getFor(touchAreaNode, scrollableNode);

    if(ext != null)
      touchAreaNode.stopUsing(ext);

    return ext;
  }

  public static SmoothScroll getFor(Node touchAreaNode, Node scrollableNode){
    for(ExtensionBase ext : touchAreaNode.getExtensions())
      if(SmoothScroll.class.isInstance(ext))
        return (SmoothScroll)ext;

    return null;
  }
}
