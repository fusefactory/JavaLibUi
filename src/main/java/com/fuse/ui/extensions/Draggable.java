package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Draggable extends ExtensionBase {
  private PVector originalNodePosition = null;
  private boolean bDragging = false;

  public Event<Draggable> startEvent;
  public Event<Draggable> endEvent;

  public Draggable(){
    startEvent = new Event<>();
    endEvent = new Event<>();
  }

  @Override public void enable(Node newNode){
    super.enable(newNode);

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(originalNodePosition == null || !bDragging){
        if(event.node != this.getNode())
          return; // touch didn't start on our node

        originalNodePosition = getNode().getPosition();
        bDragging = true;
        // TODO; min offset before officialy start dragging?
        startEvent.trigger(this);
      }

      // logger.warning("offset: "+event.offset().toString()+", orig pos: "+originalNodePosition.toString());
      getNode().setPosition(PVector.add(event.offset(), originalNodePosition));
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(!bDragging)
        return;

      if(originalNodePosition != null)
        getNode().setPosition(PVector.add(event.offset(), originalNodePosition));

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

  public PVector getOffset(){
    if(originalNodePosition == null || node == null)
      return new PVector(0.0f,0.0f,0.0f);

    return node.getPosition().sub(originalNodePosition);
  }

  public PVector getOriginalPosition(){
    return originalNodePosition == null ? null : originalNodePosition.copy();
  }

  public boolean isDragging(){
    return bDragging;
  }
}
