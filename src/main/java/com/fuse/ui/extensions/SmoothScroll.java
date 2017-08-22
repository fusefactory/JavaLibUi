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
  private float dampingFactor = 0.9f;
  private final float minVelocityMag = 0.001f; // when velocity reaches this value (or lower), we finalize the movement

  @Override
  public void update(float dt){
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

    if(velocity.mag() < minVelocityMag)
      velocity = null; // isDamping() = false
  }

  @Override
  public void drawDebug(){
    PVector offset = new PVector(0.0f, 0.0f, 0.0f);

    if(originalNodePositionGlobal != null){
      offset.x = (scrollableNode.getGlobalPosition().x - originalNodePositionGlobal.x) % 100.0f;
      offset.x = (scrollableNode.getGlobalPosition().y - originalNodePositionGlobal.y) % 100.0f;
      offset.z = 0.0f;

      PGraphics pg = Node.getPGraphics();
      pg.stroke(255,0,0);
      pg.strokeWeight(1.0f);

      for(float x = offset.x; x < node.getSize().x; x += node.getSize().x * 0.2f)
        pg.line(x, 0, x, 5);

      for(float y = offset.y; y < node.getSize().y; y += node.getSize().y * 0.2f){
        pg.line(0, y, 5, y);
      }
    }
  }

  public void enable(){
    super.enable();

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(scrollableNode == null || event.node != this.node)
        return; // touch didn't start on our node

      // just started dragging?
      if(!isDragging()){
        this.velocity = null; // this makes isDamping() false
        originalNodePosition = scrollableNode.getPosition(); // this makes isDragging true
        originalNodePositionGlobal = scrollableNode.getGlobalPosition();
        // startDragEvent.trigger(this);
      }

      apply(event.offset());
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!isDragging())
        return;

      apply(event.offset());
      originalNodePosition = null; // this makes isDragging() false
      // endDragEvent.trigger(this);
      this.velocity = event.velocity; // this makes isDamping() true
      // startDampEvent.trigger(this);
    }, this);
  }

  public void disable(){
    super.disable();

    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);

    velocity = null; // isDamping() = false
    originalNodePosition = null; // isDragging() = false
  }

  private void apply(PVector dragOffset){
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
