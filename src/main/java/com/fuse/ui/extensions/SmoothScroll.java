package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PGraphics;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class SmoothScroll extends ExtensionBase {
  private Node scrollableNode = null;

  private PVector originalNodePosition = null;
  private PVector originalNodePositionGlobal = null;
  private PVector velocity = null;
  private PVector smoothedVelocity = null;

  private final static float velocitySmoothCoeff = 0.1f;
  private float dampingFactor = 0.001f;
  private final static float minVelocityMag = 1.0f; // when velocity reaches this value (or lower), we finalize the movement
  private final static float velocityReductionFactor = 0.05f; // factor to multipy the (already smoother) smoothedVelocity when setting the main velocity

  // snapping (falling back into place)
  private PVector snapOffset = null;
  private float snapVelocityMag = 75.0f; // when velocity reaches this value (or lower), we start snapping
  private PVector snapGlobalPosition = null;
  private float snapFactor = 0.3f;

  @Override
  public void update(float dt){
    if(isSnapping()){
      PVector delta = snapGlobalPosition.get();
      PVector curGlobalPos = scrollableNode.getGlobalPosition();
      delta.sub(curGlobalPos);
      delta.mult(snapFactor);
      curGlobalPos.add(delta);
      scrollableNode.setGlobalPosition(curGlobalPos);
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

    // apply damping
    PVector dampedVelocity = velocity.get();
    dampedVelocity.mult(dampingFactor);
    velocity.lerp(dampedVelocity, dt);

    float mag = velocity.mag();

    if(snapOffset != null && mag <= snapVelocityMag){
      this.velocity = null; // isDamping() == false

      PVector globPos = scrollableNode.getGlobalPosition();
      PVector curOffset = globPos.get();
      curOffset.sub(originalNodePositionGlobal);

      PVector targetOffset = new PVector(curOffset.x - curOffset.x % snapOffset.x,
                                          curOffset.y - curOffset.y % snapOffset.y,
                                          0.0f);
      if(Math.abs(targetOffset.x - curOffset.x) > snapOffset.x * 0.5f)
        targetOffset.x += curOffset.x < targetOffset.x ? -snapOffset.x : snapOffset.x;
      if(Math.abs(targetOffset.y - curOffset.y) > snapOffset.y * 0.5f)
        targetOffset.y += curOffset.y < targetOffset.y ? -snapOffset.y : snapOffset.y;

      snapGlobalPosition = originalNodePositionGlobal.get();
      snapGlobalPosition.add(targetOffset); // isSnapping() = true
      return;
    }

    if(velocity.mag() < minVelocityMag)
      velocity = null; // isDamping() = false
  }

  @Override
  public void drawDebug(){
    float deltaX = node.getSize().x * 0.1f;
    float deltaY = node.getSize().y * 0.1f;
    float offsetX = 0.0f;
    float offsetY = 0.0f;

    if(originalNodePositionGlobal != null){
      offsetX = (scrollableNode.getGlobalPosition().x - originalNodePositionGlobal.x) % deltaX;
      offsetY = (scrollableNode.getGlobalPosition().y - originalNodePositionGlobal.y) % deltaY;
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
        this.snapGlobalPosition = null; // isSnapping() = false
        originalNodePosition = scrollableNode.getPosition(); // this makes isDragging true
        originalNodePositionGlobal = scrollableNode.getGlobalPosition();
        smoothedVelocity = new PVector(0.0f, 0.0f, 0.0f);
      }

      smoothedVelocity.lerp(event.velocitySmoothed, velocitySmoothCoeff);
      applyDragOffset(event.offset());
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!isDragging())
        return;

      applyDragOffset(event.offset());
      originalNodePosition = null; // this makes isDragging() false
      // endDragEvent.trigger(this);
      smoothedVelocity.lerp(event.velocitySmoothed, velocitySmoothCoeff);
      this.velocity = smoothedVelocity; // this makes isDamping() true
      this.velocity.mult(velocityReductionFactor);
    }, this);
  }

  public void disable(){
    super.disable();

    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);

    velocity = null; // isDamping() = false
    originalNodePosition = null; // isDragging() = false
  }

  private void applyDragOffset(PVector dragOffset){
    if(originalNodePositionGlobal == null) // should already be set at first processed touchMoveEvent, but just to be sure
      originalNodePositionGlobal = scrollableNode.getGlobalPosition();

    PVector globPos = originalNodePositionGlobal.get();
    globPos.add(dragOffset);

    scrollableNode.setGlobalPosition(globPos);
  }

  public boolean isDragging(){
    return originalNodePosition != null;
  }

  public boolean isDamping(){
    return this.velocity != null;
  }

  public boolean isSnapping(){
    return snapGlobalPosition != null;
  }

  public PVector getVelocity(){
    return this.velocity == null ? new PVector(0,0,0) : velocity.get();
  }

  public Node getScrollableNode(){
    return scrollableNode;
  }

  public void setScrollableNode(Node newScrollableNode){
    if(!isEnabled()){
      scrollableNode = newScrollableNode;
      return;
    }

    disable();
    scrollableNode = newScrollableNode;
    enable();
  }

  public float getDampingFactor(){
    return dampingFactor;
  }

  public void setDampingFactor(float newDampingFactor){
    dampingFactor = newDampingFactor;
  }

  public void setSnapEnabled(boolean enable){
    if(!enable){
      snapOffset = null;
      return;
    }

    snapOffset = node.getSize();
  }

  public void setSnapFactor(float newFactor){
    snapFactor = newFactor;
  }

  public void setSnapVelocity(float newSnapVelocity){
    snapVelocityMag = newSnapVelocity;
  }

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
