package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Draggable extends ExtensionBase {
  private PVector originalNodePosition = null;

  @Override public void enable(Node newNode){
    super.enable(newNode);

    node.touchDownEvent.addListener((TouchEvent event) -> {
      originalNodePosition = getNode().getPosition();
    }, this);

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(originalNodePosition == null) return; // happens sometimes with race conditions
      // logger.warning("offset: "+event.offset().toString()+", orig pos: "+originalNodePosition.toString());
      getNode().setPosition(PVector.add(event.offset(), originalNodePosition));
    }, this);

    node.touchUpEvent.addListener((TouchEvent event) -> {
      if(originalNodePosition == null) return; // happens sometimes with race conditions
      getNode().setPosition(PVector.add(event.offset(), originalNodePosition));
      originalNodePosition = null;
    }, this);
  }

  @Override public void disable(){
    super.disable();
    node.touchDownEvent.removeListeners(this);
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
}
