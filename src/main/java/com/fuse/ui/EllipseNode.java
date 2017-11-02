package com.fuse.ui;

import processing.core.PGraphics;

public class EllipseNode extends ShapeNode {

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    // this take care of applying all styles (colors/stroke)
    super.draw();
    // draw our shape
    pg.ellipseMode(PGraphics.CORNER); // TODO: make configurable
    pg.ellipse(0,0,getSize().x,getSize().y);
  }
}
