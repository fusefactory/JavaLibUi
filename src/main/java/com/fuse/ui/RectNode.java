package com.fuse.ui;

import java.lang.Deprecated;

public class RectNode extends ShapeNode {

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    // this take care of applying all styles (colors/stroke)
    super.draw();
    // draw our shape
    pg.rect(0,0,getSize().x,getSize().y);
  }

  @Deprecated
  public Integer getRectColor(){ return this.getFillColor(); }
  @Deprecated
  public RectNode setRectColor(Integer newRectColor){ return (RectNode)this.setFillColor(newRectColor); }
  @Deprecated
  public Integer getRectStrokeColor(){ return this.getStrokeColor(); }
  @Deprecated
  public RectNode setRectStrokeColor(Integer newColor){ return (RectNode)this.setStrokeColor(newColor); }
}
