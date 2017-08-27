package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Draggable extends ExtensionBase {
  private PVector originalNodePosition = null;
  private PVector originalNodePositionGlobal = null;
  private boolean bDragging = false;

  public Event<Draggable> startEvent;
  public Event<Draggable> endEvent;

  public Draggable(){
    startEvent = new Event<>();
    endEvent = new Event<>();
  }

  @Override public void enable(){
    super.enable();

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(originalNodePosition == null || !bDragging){
        Node ourNode = this.getNode();

        if(event.node != ourNode)
          return; // touch didn't start on our node

        originalNodePosition = ourNode.getPosition();
        originalNodePositionGlobal = ourNode.getGlobalPosition();

        bDragging = true;

        // TODO; min offset before officialy start dragging?
        startEvent.trigger(this);
      }

      apply(event.offset());
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!bDragging)
        return;

      apply(event.offset());

      bDragging = false;
      endEvent.trigger(this);
      // originalNodePosition = null;
    }, this);
  }

  @Override public void disable(){
    super.disable();
    node.touchMoveEvent.removeListeners(this);
    node.touchUpEvent.removeListeners(this);
  }

  public void apply(PVector dragOffset){
    if(originalNodePositionGlobal == null) // should already be set at first processed touchMoveEvent, but just to be sure
      originalNodePositionGlobal = this.getNode().getGlobalPosition();

    PVector globPos = dragOffset.get();
    globPos.add(originalNodePositionGlobal);
    this.getNode().setGlobalPosition(globPos);
  }

  public PVector getOffset(){
    if(originalNodePosition == null || node == null)
      return new PVector(0.0f,0.0f,0.0f);

    PVector result = node.getPosition();
    result.sub(originalNodePosition);
    return result;
  }

  public PVector getOriginalPosition(){
    return originalNodePosition == null ? null : originalNodePosition.get();
  }

  public boolean isDragging(){
    return bDragging;
  }

  public static Draggable enableFor(Node n){
    for(ExtensionBase ext : n.getExtensions())
      if(Draggable.class.isInstance(ext))
        return (Draggable)ext;
    Draggable d = new Draggable();
    n.use(d);
    return d;
  }

  public static void disableFor(Node n){
    for(int i=n.getExtensions().size()-1; i>=0; i--)
      if(Draggable.class.isInstance(n.getExtensions().get(i))){
        n.stopUsing(n.getExtensions().get(i));
      }
  }


}
