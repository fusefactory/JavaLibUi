package com.fuse.ui;

import processing.core.PVector;

public class RectNode extends Node {

  private Integer rectFillColor = null;
  private Integer rectStrokeColor = null;
  private Float rectStrokeWeight = null;

  private void _init(){
    if(pg != null){
      pg.colorMode(pg.RGB, 255);
      rectFillColor = pg.color(255);
    }
  }

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public RectNode(){
    _init();
  }

  public RectNode(String nodeName){
    super(nodeName);
    _init();
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(rectStrokeColor == null)
      pg.noStroke();
    else
      pg.stroke(rectStrokeColor);

    if(rectFillColor == null)
      pg.noFill();
    else
      pg.fill(rectFillColor);

    if(rectStrokeWeight != null)
      pg.strokeWeight(rectStrokeWeight);

    pg.rect(0,0,getSize().x,getSize().y);
  }

  public Integer getRectColor(){ return rectFillColor; }
  public void setRectColor(Integer newRectColor){ rectFillColor = newRectColor; }

  public Integer getRectStrokeColor(){ return rectStrokeColor; }
  public void setRectStrokeColor(Integer newColor){ rectStrokeColor = newColor; }

  public Float getStrokeWeight(){ return rectStrokeWeight; }
  public void setStrokeWeight(Float newWeight){ rectStrokeWeight = newWeight; }

}
