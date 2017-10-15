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
  private PVector snapInterval = null; // the size of a single "cell" in the snapping grid
  private float snapVelocityMag = 75.0f; // when velocity reaches this value (or lower), we start snapping
  private PVector snapPosition = null; // scroll position to "snap to"
  private float snapFactor = (1.0f/7.0f); // multiplication factor for smoothed snapping motion
  private float snapThrowFactor = 0.5f; // multiplier for 'throwing' after dragging
  private final static float snapDoneDist = 0.9f; // distance at which snapping is considered finished (whebn smoothing motion is finalized)
  // offset limits
  private PVector minOffset = null;
  private PVector maxOffset = null;
  private float offsetLimitSlack = 70.0f;  // how much beyond the offset limit can be scrolled
  private float offsetLimitSlackDistance = 700.0f; // how much beyond the scroll limit needs to be dragged to reach max slack

  // events
  public Event<PVector> newSnapPositionEvent;
  public Event<PVector> newStepPositionEvent;
  public Event<Node> restEvent;

  public SmoothScroll(){
    smoothedVelocity = new PVector(0.0f, 0.0f, 0.0f);
    newSnapPositionEvent = new Event<>();
    newStepPositionEvent = new Event<>();
    restEvent = new Event<>();

    // newStepPositionEvent triggers everytime newSnapPositionEvent is triggered
    newSnapPositionEvent.addListener((PVector snapPos) -> {
      PVector value = this.toStepPosition(snapPos);
      if(value != null)
        newStepPositionEvent.trigger(value);
    }, this);
  }

  @Override public void destroy(){
    newSnapPositionEvent.destroy();
    newStepPositionEvent.destroy();
    restEvent.destroy();
    super.destroy();
    // scrollableNode = null;
  }

  @Override
  public void setup(){
    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(scrollableNode == null || event.node != this.node)
        return; // touch didn't start on our node

      TouchEvent localEvent = this.node.toLocal(event);

      // just started dragging?
      if(!isDragging()){
        this.velocity = null; // this makes isDamping() false
        this.snapPosition = null; // isSnapping() = false
        dragStartNodePositionGlobal = scrollableNode.getGlobalPosition(); // this makes isDragging true
        smoothedVelocity = new PVector(0.0f, 0.0f, 0.0f);
      }

      if(localEvent.velocitySmoothed != null)
        this.smoothedVelocity = localEvent.velocitySmoothed; // use TouchEvent's velocity smoothing
      else
        smoothedVelocity.lerp(localEvent.velocity, velocitySmoothCoeff); // apply our own smoothing


      applyDragOffset(event.offset()); // not localized event because dragging is global
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!isDragging())
        return;

      TouchEvent localEvent = this.node.toLocal(event);

      applyDragOffset(event.offset());  // not localized event because dragging is global
      dragStartNodePositionGlobal = null; // this makes isDragging() false

      // check offset limits; snap-back if necessary
      snapPosition = getOffsetLimitSnapPosition();
      if(snapPosition != null){ // isSnapping() = true
        newSnapPositionEvent.trigger(this.snapPosition.get());
        return;
      }

      // update our smoothed velocity
      if(localEvent.velocitySmoothed != null)
        this.smoothedVelocity = localEvent.velocitySmoothed; // use TouchEvent's velocity smoothing
      else
        smoothedVelocity.lerp(localEvent.velocity, velocitySmoothCoeff); // apply our own smoothing

      // when snapping-behaviour is enabled we don't use velocity/damping;
      // instead, we calculate a target position to snap to
      if(this.isSnapEnabled()){
        //PVector throwTarget = this.smoothedVelocity.get();
        PVector throwTarget = localEvent.offset();
        throwTarget.mult(this.snapThrowFactor);
        throwTarget.add(this.scrollableNode.getPosition());
        this.setSnapPosition(this.toClosestSnapPosition(throwTarget));
        return;
      }

      // initialize a velocity and start damping
      this.velocity = this.smoothedVelocity.get();
      this.velocity.mult(velocityReductionFactor);
    }, this);
  }

  @Override
  public void teardown(){
    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);

    velocity = null; // isDamping() = false
    dragStartNodePositionGlobal = null; // isDragging() = false
  }

  @Override
  public void update(float dt){
    if(isSnapping()){
      this.updateSnapping(dt);
      return;
    }

    if(isDamping())
      this.updateDamping(dt);
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

  // dragging methods // // // // //

  private void applyDragOffset(PVector globalDragOffset){
    if(dragStartNodePositionGlobal == null) // should already be set at first processed touchMoveEvent, but just to be sure
      dragStartNodePositionGlobal = scrollableNode.getGlobalPosition();

    PVector localPosBefore = scrollableNode.getPosition();

    PVector globPos = dragStartNodePositionGlobal.get();
    globPos.add(globalDragOffset);
    scrollableNode.setGlobalPosition(globPos);
    scrollableNode.setY(localPosBefore.y); // Y-axis locked HACK

   PVector offset = this.getCurrentOffset();
   if(this.minOffset != null && offset.x < this.minOffset.x) {
	   float diff = offset.x - this.minOffset.x;
	   float f = (float)Math.sin( Math.max(-1.0f, Math.min(0.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
	   offset.x = this.minOffset.x + this.offsetLimitSlack * f;
	   offset.add(originalNodePosition.get());
	   this.scrollableNode.setPosition(offset);
   } else if(this.maxOffset != null && offset.x > this.maxOffset.x) {
	   float diff = offset.x - this.maxOffset.x;
	   float f = (float)Math.sin( Math.max(0.0f, Math.min(1.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
	   offset.x = this.maxOffset.x + this.offsetLimitSlack * f;
   	   offset.add(originalNodePosition.get());
   	   this.scrollableNode.setPosition(offset);
   }
  }

  public boolean isDragging(){
    return dragStartNodePositionGlobal != null;
  }

  // velocity/damping methods // // // // //

  private void updateDamping(float dt){
    // apply current velocity
    PVector deltaPos = velocity.get();
    deltaPos.mult(dt);
    PVector pos = scrollableNode.getPosition();
    pos.add(deltaPos);
    scrollableNode.setPosition(pos);

    // start "snap back" when damping beyond limits
    this.snapPosition = getOffsetLimitSnapPosition();
    if(this.snapPosition != null){ // isSnapping() == true
      this.velocity = null; // isDamping() = false
      newSnapPositionEvent.trigger(this.snapPosition.get());
      return;
    }

    // apply damping (velocity reducation due to "friction")
    PVector dampedVelocity = velocity.get();
    dampedVelocity.mult(dampingFactor);
    velocity.lerp(dampedVelocity, dt);

    float mag = velocity.mag();

    // start snap-back (if enabled) when velocity has dropped low enough
    if(this.isSnapEnabled() && mag <= snapVelocityMag){
      this.startSnapping();
      return;
    }

    // no snapping; finalize damping
    if(mag < minVelocityMag){
      velocity = null; // isDamping() = false
      restEvent.trigger(scrollableNode);
    }
  }

  public boolean isDamping(){
    return this.velocity != null;
  }

  public PVector getVelocity(){
    return this.velocity == null ? new PVector(0,0,0) : velocity.get();
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

  // 'snapping' methods // // // // //

  private void updateSnapping(float dt){
    PVector curPos = scrollableNode.getPosition();
    // float local_dt = dt;

    // while(local_dt > 1.0f){
    //   PVector delta = snapPosition.get();
    //   delta.sub(curPos);
    //   delta.mult(snapFactor);
    //   curPos.add(delta);
    //   local_dt -= 1.0f;
    // }
    //
    // PVector delta = snapPosition.get();
    // delta.sub(curPos);
    // delta.mult(snapFactor);
    // delta.lerp(new PVector(0,0,0), 1.0f-local_dt);
    // curPos.add(delta);
    PVector delta = snapPosition.get();
    delta.sub(curPos);
    delta.mult(snapFactor);
    curPos.add(delta);

    scrollableNode.setPosition(curPos);

    // current snap finished?
    if(curPos.dist(snapPosition) <= snapDoneDist){
      // apply target snap-position (unsmoothed)
      scrollableNode.setPosition(snapPosition);
      // after snapping to desired position, snap again if beyond offset limits
      this.snapPosition = this.getOffsetLimitSnapPosition();
      // trigger appropriate events
      if(this.snapPosition != null)
        newSnapPositionEvent.trigger(this.snapPosition.get());
      else
        restEvent.trigger(scrollableNode);
    }

    return;
  }

  /**
   * Enables/disables snapping. When enabling, it sets the snap interval to the extended node's size
   * @param enable when true enables snapping, when false disables snapping.
   */
  public void setSnapEnabled(boolean enable){
    setSnapInterval(enable ? node.getSize() : null);
  }

  public PVector getSnapInterval(){
    return snapInterval;
  }

  /**
   * Enables/disables/configures snapping bahaviour.
   * @param interval specifies the two-dimensional (z-attribute is ignored) snap interval. When null, disables snapping behaviour.
   */
  public void setSnapInterval(PVector interval){
    snapInterval = interval == null ? null : interval.get();
  }

  /** @return true if snapping behaviour is enabled */
  public boolean isSnapEnabled(){
    return this.snapInterval != null;
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

  /** @return boolean true only if currently snapping to a target position */
  public boolean isSnapping(){
    return snapPosition != null;
  }

  /**
   * Configures snapping behaviour smoothness
   * @param newFactor offset multiplier; higher value means faster snapping.
   */
  public void setSnapFactor(float newFactor){
    snapFactor = newFactor;
  }

  /**
   * Configures snapping behaviour responsiveness
   * @param newSnapVelocity specifies the velocity at which we'll start snapping into place
   */
  public void setSnapVelocity(float snapVelocity){
    snapVelocityMag = snapVelocity;
  }

  /** @return PVector current target position for snap-back behaviour. Returns null if not currently snapping */
  public PVector getSnapPosition(){
      return snapPosition;
  }

  /**
   * Configures the current snapping-target-position (starts snap-back)
   * @param x the horizontal target-position
   * @param y the vertical target-position
   */
  public void setSnapPosition(float x, float y){
    this.setSnapPosition(new PVector(x,y));
  }

  /**
   * Configures the current snapping-target-position (starts snap-back)
   * @param pos the snapping target-position
   */
  public void setSnapPosition(PVector pos){
    if(pos == null){ // abort snapping?
      // abort current snapping operation (if any) and apply
      // offset-limit exceeded snap position if necessary
      this.snapPosition = this.getOffsetLimitSnapPosition();
      return;
    }

    // apply offset limit correction (if necessary). TODO: too rigid? make this optional?
    PVector correctedPosition = this.getOffsetLimitsCorrection(pos);
    this.snapPosition = correctedPosition == null ? pos.get() : correctedPosition;
    // reset damping (isDamping() = false)
    this.velocity = null;
    // trigger notification
    newSnapPositionEvent.trigger(this.snapPosition.get());
  }

  public float getSnapThrowFactor(){
    return snapThrowFactor;
  }

  /**
   * Configures snap-back behaviour after dragging.
   * @param multiplier multiplies the -smoothed- velocity when calculating
   * the snapping-target-position. A higher value makes it "further" throwable.
   */
  public void setSnapThrowFactor(float multiplier){
    snapThrowFactor = multiplier;
  }

  private PVector toClosestSnapPosition(PVector pos){
    PVector stepPos = this.toStepPosition(pos);
    // round to nearest stepPosition
    stepPos.x = Math.round(stepPos.x);
    stepPos.y = Math.round(stepPos.y);
    // convert back to scrollPosition
    return this.stepPositionToNodePosition(stepPos);
  }

  // 'stepping' methods - 'paginated' extension of snapping // // // // //

  /**
   * Converts a given PVector position into a "step-position"
   * (the offset of the given position to the original scrollable node's position,
   * divided by the snapInterval).
   * @param pos the position to convert
   * @return PVector calculated step-position
   */
  private PVector toStepPosition(PVector pos){
    if(this.snapInterval == null) return null;

    // offset of the position
    PVector delta = pos.get();
    delta.sub(this.originalNodePosition);
    // divide by the interval
    delta.x = -delta.x / this.snapInterval.x;
    delta.y = -delta.y / this.snapInterval.y;
    return delta;
  }

  private PVector stepPositionToNodePosition(PVector pos){
    PVector p = pos.get();
    p.x = -p.x * this.snapInterval.x;
    p.y = -p.y * this.snapInterval.y;
    return p;
  }

  public PVector getStepPosition(){
    return this.toStepPosition(this.scrollableNode.getPosition());
  }

  public void step(float x, float y){
    this.step(new PVector(x,y,0.0f));
  }

  public void step(PVector offset){
    if(this.snapInterval == null) return;
    PVector current = this.toStepPosition(this.scrollableNode.getPosition());
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

  // offset limits methods // // // // //

  public boolean hasOffsetLimits(){
    return this.minOffset != null || this.maxOffset != null;
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

  /** @return PVector target position for snap-back after offset limit is exceeded. Returns null if offset limit is not exceeded */
  private PVector getOffsetLimitSnapPosition(){
	  Node n = this.scrollableNode;
	  if(n == null) return null;
    return this.getOffsetLimitsCorrection(n.getPosition());
  }

  private PVector getOffsetLimitsCorrection(PVector pos){
    if(originalNodePosition == null) return pos.get();

    PVector offset = pos.get();
    offset.sub(originalNodePosition);

    PVector snapPos = pos.get();
    boolean snap = false;

    if(minOffset!=null){
      if(offset.x < minOffset.x){
        snapPos.x = minOffset.x;
        snap = true;
      }

      if(offset.y < minOffset.y){
        snapPos.y = minOffset.y;
        snap = true;
      }
    }

    if(maxOffset!=null){
      if(offset.x > maxOffset.x){
        snapPos.x = maxOffset.x;
        snap = true;
      }

      if(offset.y > maxOffset.y){
        snapPos.y = maxOffset.y;
        snap = true;
      }
    }

    // return null if nothing to correct
    return snap ? snapPos : null;
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

  public PVector getCurrentOffset(){
    if(originalNodePosition == null || scrollableNode == null)
      return new PVector(0,0,0);

    PVector vec = scrollableNode.getPosition();
    vec.sub(originalNodePosition);
    return vec;
  }

  // Static factory methods // // // // //

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
