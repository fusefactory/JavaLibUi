package com.fuse.ui.extensions;

import processing.core.PVector;

import com.fuse.ui.Node;
import com.fuse.ui.TouchEvent;

public class Draggable extends ExtensionBase {
  private PVector originalNodePosition = null;

  @Override public void setNode(Node newNode){
    super.setNode(newNode);

    node.touchDownEvent.addListener((TouchEvent event) -> {
      originalNodePosition = getNode().getPosition();
    });

    node.touchMoveEvent.addListener((TouchEvent event) -> {
      if(originalNodePosition == null)
        return; // sohuld not happen

      logger.warning("offset: "+event.offset().toString()+", orig pos: "+originalNodePosition.toString());
      getNode().setPosition(PVector.add(event.offset(), originalNodePosition));
    });

    node.touchUpEvent.addListener((TouchEvent event) -> {
      getNode().setPosition(PVector.add(event.offset(), originalNodePosition));
      originalNodePosition = null;
    });
  }
}
