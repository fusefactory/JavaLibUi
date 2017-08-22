package com.fuse.ui.extensions;

import processing.core.PApplet;
import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class SmoothScroll extends ExtensionBase {
  private Node scrollableNode = null;

  private PVector originalNodePosition = null;
  private PVector originalNodePositionGlobal = null;
  private PVector velocity = null;
  private float dampingFactor = 0.9f;

  public void enable(){
    super.enable();

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(scrollableNode == null || event.node != this.node)
        return; // touch didn't start on our node

      // just started dragging?
      if(!isDragging()){
        originalNodePosition = scrollableNode.getPosition(); // this makes isDragging true
        originalNodePositionGlobal = scrollableNode.getGlobalPosition();
        // startEvent.trigger(this);
      }

      apply(event.offset());
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!isDragging())
        return;

      apply(event.offset());
      originalNodePosition = null; // this makes isDragging() false
      // endEvent.trigger(this);
    }, this);
  }

  public void disable(){
    super.disable();

    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);
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
