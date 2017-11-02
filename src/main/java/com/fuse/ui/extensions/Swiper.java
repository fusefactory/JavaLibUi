package com.fuse.ui.extensions;

import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Swiper extends TransformerExtension {
  // attributes
  private Node scrollableNode;
  private Node touchAreaNode;
  private PVector originalNodePosition = null;
  // dragging
  private boolean bDragging = false;
  private PVector dragStartNodePositionGlobal = null;
  private TouchEvent draggingTouchEvent = null;
  // velocity / damping
  private boolean bDamping = false;
  private long minTouchDurationToDamp = 50l;
  private float maxDampVelocity = 1000.0f;
  private float dampThrowFactor = 2.0f;
  // snapping (falling back into place)
  private boolean bSnapping = false;
  private PVector snapInterval = null; // size of a single "cell" in the snapping grid
  private float snapVelocityMag = 75.0f; // when velocity reaches this value (or lower), we start snapping
  private float snapThrowFactor = 2.0f; // multiplier for the smoothed 'throwing' velocity after dragging
  // offset limits
  private PVector minOffset = null;
  private PVector maxOffset = null;
  private float offsetLimitSlack = 70.0f;  // how much beyond the offset limit can be scrolled
  private float offsetLimitSlackDistance = 700.0f; // how much beyond the scroll limit needs to be dragged to reach max slack

  // events
  public Event<TouchEvent> startDraggingEvent;
  public Event<TouchEvent> endDraggingEvent;
  public Event<PVector> newSnapPositionEvent;
  public Event<PVector> newStepPositionEvent;
  public Event<PVector> throwEvent = new Event<>();
  public Event<Node> restEvent;


  // lifecycle methods

  public Swiper(){
    startDraggingEvent = new Event<>();
    endDraggingEvent = new Event<>();
    newSnapPositionEvent = new Event<>();
    newStepPositionEvent = new Event<>();
    restEvent = new Event<>();

    super.setMaxTransformationTime(6.0f);
    super.setSmoothValue(10.0f);

  }

  @Override public void destroy(){
    startDraggingEvent.destroy();
    endDraggingEvent.destroy();
    newSnapPositionEvent.destroy();
    newStepPositionEvent.destroy();
    restEvent.destroy();
    super.destroy();
    // scrollableNode = null;
  }

  @Override protected void setup(){
    this.touchAreaNode.touchDownEvent.addListener((TouchEvent event) -> {
      if(!bDragging || this.draggingTouchEvent.isFinished())
        this.startDragging(event);
    }, this);

    this.touchAreaNode.touchUpEvent.addListener((TouchEvent event) -> {
      TouchEvent dragEvent = this.draggingTouchEvent;
      if(bDragging) {
    	// threading issue double check
    	if(dragEvent == null) return;
    	if(dragEvent == event || dragEvent.isFinished())
    		this.endDragging();
      }
    }, this);

    super.enable();
  }

  @Override protected void teardown(){
    super.disable();

    this.touchAreaNode.touchDownEvent.removeListeners(this);
    this.touchAreaNode.touchUpEvent.removeListeners(this);

    bSnapping = false;
    bDamping = false;
    bDragging = false;
  }

  @Override public void update(float dt){
    // progress all smoothed transformations
    super.update(dt);

    if(bDragging){
      this.updateDragging();
      return;
    }

    if(isSnapping()){
      this.updateSnapping(dt);
      return;
    }

    if(isDamping()){
      this.updateDamping(dt);
    }
  }

  @Override public void drawDebug(){
    float deltaX = this.touchAreaNode.getSize().x * 0.1f;
    float deltaY = this.touchAreaNode.getSize().y * 0.1f;
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

    for(float x = offsetX; x < this.touchAreaNode.getSize().x; x += deltaX){
      pg.line(x, 0, x, this.touchAreaNode.getSize().y);
    }

    for(float y = offsetY; y < this.touchAreaNode.getSize().y; y += deltaY){
      pg.line(0, y, this.touchAreaNode.getSize().x, y);
    }
  }

  // configuration methods // // // // //

  @Override public void setNode(Node n){
    super.setNode(n);
    this.setScrollableNode(n);
  }

  public Node getScrollableNode(){
    return this.scrollableNode;
  }

  public Swiper setScrollableNode(Node newScrollableNode){
    boolean wasEnabled = isEnabled();

    if(wasEnabled)
      disable();

    scrollableNode = newScrollableNode;
    originalNodePosition = scrollableNode.getPosition();


    if(wasEnabled)
      enable();

    return this;
  }

  public Swiper setTouchAreaNode(Node n){
    boolean wasEnabled = isEnabled();

    if(wasEnabled)
      disable();

    touchAreaNode = n;
    if(touchAreaNode != null && this.snapInterval == null) {
      this.snapInterval = touchAreaNode.getSize();
    }

    if(wasEnabled)
      enable();

    return this;
  }

  public Node getTouchAreaNode(){
    return this.touchAreaNode;
  }

  // dragging methods // // // // //

  private void startDragging(TouchEvent event){
    this.dragStartNodePositionGlobal = scrollableNode.getGlobalPosition();
    this.draggingTouchEvent = event;
    bDragging = true;
    startDraggingEvent.trigger(event);
  }

  private void endDragging(){
    bDragging = false;
    endDraggingEvent.trigger(this.draggingTouchEvent);

    // check offset limits; snap-back if necessary
    PVector pos = getOffsetLimitSnapPosition();
    if(pos != null){ // isSnapping() = true
      this.setSnapPosition(pos);
      return;
    }

    TouchEvent localEvent = this.touchAreaNode.toLocal(this.draggingTouchEvent);

    PVector throwTarget = localEvent.getSmoothedVelocity();
    // PVector throwTarget = localEvent.offset();
    throwTarget.mult(this.snapThrowFactor);
    throwTarget.add(this.scrollableNode.getPosition());

    // when snapping-behaviour is enabled we don't use velocity/damping;
    // instead, we calculate a target position to snap to
    if(this.isSnapEnabled()){
      throwTarget = this.toClosestSnapPosition(throwTarget);
      this.setSnapPosition(throwTarget);
      return;
    }

    if(localEvent.getDuration() > minTouchDurationToDamp){
      // TODO damping doesn't work (well) yet...
      PVector vel = localEvent.offset();
      // vel.mult(1.0f / ((float)localEvent.getDuration()/1000.f));
      this.startDamping(vel);
    }

    this.throwEvent.trigger(throwTarget);
  }

  /** should only be called when it is already verified that we're dragging (this.draggingTouchEvent can't be null) */
  private void updateDragging(){
    TouchEvent localEvent = this.touchAreaNode.toLocal(this.draggingTouchEvent);

    PVector globPos = dragStartNodePositionGlobal.get();
    globPos.add(this.draggingTouchEvent.offset());
    // use TransformationExtension's smoothing options
    super.transformPositionGlobal(globPos);

    PVector localpos = this.getTargetPosition();
    if(localpos == null) localpos = this.node.getPosition();

    // apply offset restrictins with slack
    PVector offset = localpos.get();
    offset.sub(this.originalNodePosition);
    boolean needCorrection = false;

    if(this.minOffset != null){
      if(offset.x < this.minOffset.x){
        float diff = offset.x - this.minOffset.x;
        float f = (float)Math.sin( Math.max(-1.0f, Math.min(0.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
        localpos.x = this.minOffset.x + this.offsetLimitSlack * f;
        localpos.x += originalNodePosition.x;
        needCorrection = true;
      }

      if(offset.y < this.minOffset.y){
        float diff = offset.y - this.minOffset.y;
        float f = (float)Math.sin( Math.max(-1.0f, Math.min(0.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
        localpos.y = this.minOffset.y + this.offsetLimitSlack * f;
        localpos.y += originalNodePosition.y;
        needCorrection = true;
      }
    }

    if(this.maxOffset != null){
      if(offset.x > this.maxOffset.x) {
        float diff = offset.x - this.maxOffset.x;
        float f = (float)Math.sin( Math.max(0.0f, Math.min(1.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
        localpos.x = this.maxOffset.x + this.offsetLimitSlack * f;
        localpos.x += originalNodePosition.x;
        needCorrection = true;
      }

      if(offset.y > this.maxOffset.y) {
        float diff = offset.y - this.maxOffset.y;
        float f = (float)Math.sin( Math.max(0.0f, Math.min(1.0f, diff / this.offsetLimitSlackDistance)) * (float)Math.PI * 0.5f );
        localpos.y = this.maxOffset.y + this.offsetLimitSlack * f;
        localpos.y += originalNodePosition.y;
        needCorrection = true;
      }
    }

    if(needCorrection){
      this.transformPosition(localpos);
    }
  }

  public boolean isDragging(){
    return bDragging;
  }

  // velocity/damping methods // // // // //

  private void startDamping(PVector velocity){
    velocity = velocity.get();
    velocity.mult(dampThrowFactor);
    if(velocity.mag() > this.maxDampVelocity)
      velocity.mult(this.maxDampVelocity / velocity.mag());

    velocity.add(this.scrollableNode.getPosition());
    super.transformPosition(velocity);
    this.bDamping = true;
  }

  private void updateDamping(float dt){
    if(super.isTransformingPosition()){
      PVector vel = super.getPositionTransformationVelocity();
      float mag = vel.mag();

      // start snap-back (if enabled) when velocity has dropped low enough
      if(this.isSnapEnabled() && mag <= snapVelocityMag){
        this.endDamping();
        this.startSnapping();
        return;
      }
    } else {
      this.endDamping();
      restEvent.trigger(scrollableNode);
    }
  }

  private void endDamping(){
    this.stopPositionTransformation(); // necessary?
    bDamping = false;
    // TODO trigger event
  }

  private void stopDamping(){
    this.endDamping();
  }

  public boolean isDamping(){
    return bDamping;
  }

  public float getDampThrowFactor(){ return dampThrowFactor; }
  public Swiper setDampThrowFactor(float factor){ dampThrowFactor = factor; return this; }

  @Deprecated
  public float getVelocityReductionFactor(){ return dampThrowFactor; }
  @Deprecated
  public Swiper setVelocityReductionFactor(float factor){ dampThrowFactor = factor; return this; }

  public float getMinTouchDurationToDamp(){ return this.minTouchDurationToDamp; }
  public Swiper setMinTouchDurationToDamp(long ms){ this.minTouchDurationToDamp = ms; return this; }
  public Swiper setMinTouchDurationToDamp(float seconds){ this.minTouchDurationToDamp = (long)seconds * 1000l; return this; }

  public float getMaxDampVelocity(){ return this.maxDampVelocity; }
  public Swiper setMaxDampVelocity(float vel){ this.maxDampVelocity = vel; return this; }

  // 'snapping' methods // // // // //

  private void updateSnapping(float dt){
    if(!super.isTransformingPosition()){ // done?
      // chec if we've snapped beyond offset limit bounds
      PVector p = this.getOffsetLimitSnapPosition();
      if(p != null){
        // snap to closest position within offset limit bounds
        this.setSnapPosition(p);
      } else {
        // done snapping, we've finally found some peace
        restEvent.trigger(scrollableNode);
      }

      // TODO: only when we're not doing an offset-limit follow-up snap?
      bSnapping = false;
    }
  }

  /**
   * Enables/disables snapping. When enabling, it sets the snap interval to the extended node's size
   * @param enable when true enables snapping, when false disables snapping.
   */
  public Swiper setSnapEnabled(boolean enable){
    return setSnapInterval(enable ? this.touchAreaNode.getSize() : null);
  }

  public PVector getSnapInterval(){
    return snapInterval;
  }

  /**
   * Enables/disables/configures snapping bahaviour.
   * @param interval specifies the two-dimensional (z-attribute is ignored) snap interval. When null, disables snapping behaviour.
   */
  public Swiper setSnapInterval(PVector interval){
    this.snapInterval = interval;
    //snapInterval = interval != null ? interval.get() : (this.touchAreaNode == null ? null : this.touchAreaNode.getSize());
    return this;
  }

  /** @return true if snapping behaviour is enabled */
  public boolean isSnapEnabled(){
    return this.snapInterval != null;
  }

  private void startSnapping(){
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

    PVector p = originalNodePosition.get();
    p.add(targetOffset);
    this.setSnapPosition(p);
  }

  /** @return boolean true only if currently snapping to a target position */
  public boolean isSnapping(){
    return bSnapping;
  }

  /**
   * Configures snapping behaviour responsiveness
   * @param newSnapVelocity specifies the velocity at which we'll start snapping into place
   */
  public Swiper setSnapVelocity(float snapVelocity){
    snapVelocityMag = snapVelocity;
    return this;
  }

  /** @return PVector current target position for snap-back behaviour. Returns null if not currently snapping */
  public PVector getSnapPosition(){
    return bSnapping && super.getTargetPosition() != null ? super.getTargetPosition() : this.getCurrentOffset();
  }

  /**
   * Configures the current snapping-target-position (starts snap-back)
   * @param x the horizontal target-position
   * @param y the vertical target-position
   */
  public void setSnapPosition(float x, float y){
    this.setSnapPosition(new PVector(x,y));
  }

  public void setSnapPosition(PVector pos) {
	  this.setSnapPosition(pos, false);
  }

  /**
   * Configures the current snapping-target-position (starts snap-back)
   * @param pos the snapping target-position
   */
  public void setSnapPosition(PVector pos, boolean instant){
    if(pos == null){ // abort snapping?
      // abort current snapping operation (if any) and apply
      // offset-limit exceeded snap position if necessary
    	  if(instant)
    		  this.node.setPosition(this.getOffsetLimitSnapPosition());
    	  else
    		  super.transformPosition(this.getOffsetLimitSnapPosition());
      return;
    }

    this.stopDamping();

    // apply offset limit correction (if necessary). TODO: too rigid? make this optional?
    PVector correctedPos = this.getOffsetLimitsCorrection(pos);
    correctedPos = correctedPos == null ? pos.get() : correctedPos;

    if(instant) {
      this.node.setPosition(pos);
    } else {
      this.transformPosition(correctedPos);
      this.bSnapping = true;
    }

    // trigger notifications
    newSnapPositionEvent.trigger(correctedPos);

    PVector stepValue = this.toStepPosition(correctedPos);
    if(stepValue != null)
      newStepPositionEvent.trigger(stepValue);
  }

  public float getSnapThrowFactor(){
    return snapThrowFactor;
  }

  /**
   * Configures snap-back behaviour after dragging.
   * @param multiplier multiplies the -smoothed- velocity when calculating
   * the snapping-target-position. A higher value makes it "further" throwable.
   */
  public Swiper setSnapThrowFactor(float multiplier){
    snapThrowFactor = multiplier;
    return this;
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
    PVector interval = this.snapInterval;
    if(interval == null) return this.originalNodePosition.get();
    // get offset; multiply step-position by interval size and invert direction
    PVector delta = pos.get();
    delta.x = -delta.x * interval.x;
    delta.y = -delta.y * interval.y;
    // add offset of the original node position
    delta.add(this.originalNodePosition);
    return delta;
  }

  public PVector getStepPosition(){
    return this.toStepPosition(this.scrollableNode.getPosition());
  }

  public void setStepPosition(float x, float y){
	  this.setStepPosition(x,y,false);
  }

  public void setStepPosition(float x, float y, boolean instant){
    this.setStepPosition(new PVector(x,y,0), instant);
  }

  public void setStepPosition(PVector pos) {
	  this.setStepPosition(pos, false);
  }

  public void setStepPosition(PVector pos, boolean instant){
    pos = this.stepPositionToNodePosition(pos);
    pos = this.toClosestSnapPosition(pos);
    this.setSnapPosition(pos, instant);
  }

  public Swiper step(float x, float y){
    return this.step(new PVector(x,y,0.0f));
  }

  public Swiper step(PVector offset){
    if(this.snapInterval == null) return this;
    PVector current = this.toStepPosition(this.scrollableNode.getPosition());
    PVector delta = offset.get();
    delta.mult(-1.0f); // invert; step left means offset to right
    delta.x = delta.x * this.snapInterval.x;
    delta.y = delta.y * this.snapInterval.y;
    if(bSnapping){
      delta.add(this.getSnapPosition());
      this.setSnapPosition(delta);
    } else {
      delta.add(this.scrollableNode.getPosition());
      this.setSnapPosition(delta);
    }

    return this;
  }

  public Swiper setMinStep(float x, float y){
    return this.setMinStep(new PVector(x,y,0));
  }

  public Swiper setMinStep(PVector step){
    // max offset, not min offset; higher 'step' means lower (negative) offset
    return this.setMaxOffset(this.stepPositionToNodePosition(step));
  }

  public Swiper setMaxStep(float x, float y){
    return this.setMaxStep(new PVector(x,y,0));
  }

  public Swiper setMaxStep(PVector step){
    // max offset, not min offset; higher 'step' means lower (negative) offset
    return this.setMinOffset(this.stepPositionToNodePosition(step));
  }

  // offset/limits methods // // // // //

  public PVector getCurrentOffset(){
    if(originalNodePosition == null || scrollableNode == null)
      return new PVector(0,0,0);

    PVector vec = scrollableNode.getPosition();
    vec.sub(originalNodePosition);
    return vec;
  }

  public boolean hasOffsetLimits(){
    return this.minOffset != null || this.maxOffset != null;
  }

  public Swiper setMinOffset(float x, float y){
    return this.setMinOffset(new PVector(x,y,0.0f));
  }

  public Swiper setMinOffset(PVector offset){
    this.minOffset = offset.get();

    PVector p = getOffsetLimitSnapPosition();
    if(p != null)
      this.setSnapPosition(p);

    return this;
  }

  public Swiper setMaxOffset(float x, float y){
    return this.setMaxOffset(new PVector(x,y,0.0f));
  }

  public Swiper setMaxOffset(PVector offset){
    this.maxOffset = offset;

    PVector p = getOffsetLimitSnapPosition();
    if(p != null)
      this.setSnapPosition(p);

    return this;
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

  // Static factory methods // // // // //

  public static Swiper enableFor(Node touchAreaNode){
    // find existing
    Swiper ext = getFirstFor(touchAreaNode);

    if(ext == null){
      Node scrollerNode = new Node();
      scrollerNode.setInteractive(false); // we don't want it to take our touch events
      touchAreaNode.addChild(scrollerNode);
      ext = enableFor(touchAreaNode, scrollerNode);
    }

    return ext;
  }

  public static Swiper enableFor(Node touchAreaNode, Node scrollableNode){
    // find existing
    Swiper ext = getFor(touchAreaNode, scrollableNode);

    // create new
    if(ext == null){
      ext = new Swiper();
      ext.setNode(scrollableNode);
      ext.setTouchAreaNode(touchAreaNode);
      touchAreaNode.addExtension(ext);
      ext.enable();
    }

    return ext;
  }

  public static Swiper disableFor(Node touchAreaNode, Node scrollableNode){
    // find existing
    Swiper ext = getFor(touchAreaNode, scrollableNode);

    // disable
    if(ext != null)
      touchAreaNode.stopUsing(ext);

    return ext;
  }

  public static Swiper getFor(Node touchAreaNode, Node scrollableNode){
    // find first extension of this type
    for(ExtensionBase ext : touchAreaNode.getExtensions())
      if(Swiper.class.isInstance(ext))
        if(((Swiper)ext).getScrollableNode() == scrollableNode)
          return (Swiper)ext;

    return null;
  }

  public static Swiper getFirstFor(Node touchAreaNode){
    // find first extension of this type
    for(ExtensionBase ext : touchAreaNode.getExtensions())
      if(Swiper.class.isInstance(ext))
        return (Swiper)ext;

    return null;
  }
}
