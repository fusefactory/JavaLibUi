package com.fuse.ui;

import processing.core.PVector;

public class RectNode extends Node {

  private Integer rectFillColor = null;

  private Integer rectStrokeColor = null;
  private Float rectStrokeWeight = null;

  private Float fillAlpha = null;
  private Integer fillAlphaColor = null;

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public RectNode(){
    if(pg != null){
      pg.colorMode(pg.RGB, 255);
      rectFillColor = pg.color(255);
    }

    this.alphaState.push((Float v) -> this.updateAlpha());
  }

  public RectNode(String nodeName){
    this();
    super.setName(nodeName);
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(rectFillColor == null && this.fillAlphaColor == null)
      pg.noFill();
    else
      pg.fill(this.fillAlphaColor == null ? this.rectFillColor : this.fillAlphaColor);

    if(rectStrokeWeight == null)
      pg.noStroke();
    else
      pg.strokeWeight(rectStrokeWeight);

    if(rectStrokeColor == null)
      pg.noStroke();
    else
      pg.stroke(rectStrokeColor);

    pg.rect(0,0,getSize().x,getSize().y);
  }

  public Integer getRectColor(){ return rectFillColor; }
  public RectNode setRectColor(Integer newRectColor){ rectFillColor = newRectColor; this.updateAlpha(); return this; }

  public Integer getRectStrokeColor(){ return rectStrokeColor; }
  public RectNode setRectStrokeColor(Integer newColor){ rectStrokeColor = newColor; return this; }

  public Float getStrokeWeight(){ return rectStrokeWeight; }
  public RectNode setStrokeWeight(Float newWeight){ rectStrokeWeight = newWeight; return this; }

  public Float getFillAlpha() { return this.fillAlpha; }
  public RectNode setFillAlpha(Float alpha) { this.fillAlpha = alpha; this.updateAlpha(); return this; }

  private void updateAlpha(){
    Integer baseColor = this.rectFillColor;
    if(baseColor == null){
      this.fillAlphaColor = null;
      return;
    }

    float alpha = this.alphaState.get();

    if(this.fillAlpha != null)
      alpha *= this.fillAlpha;

    this.fillAlphaColor = Node.alphaColor(baseColor, alpha);
  }
}
