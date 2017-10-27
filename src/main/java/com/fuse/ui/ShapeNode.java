package com.fuse.ui;

import processing.core.PVector;

public class ShapeNode extends Node {

  private Integer fillColor = null;
  private Float fillAlpha = null;
  private Integer fillAlphaColor = null;

  private Integer strokeColor = null;
  private Float strokeWeight = null;

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public ShapeNode(){
    if(pg != null){
      pg.colorMode(pg.RGB, 255);
      fillColor = pg.color(255);
    }

    this.alphaState.push((Float v) -> this.updateAlpha());
  }

  public ShapeNode(String nodeName){
    this();
    super.setName(nodeName);
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(fillColor == null && this.fillAlphaColor == null)
      pg.noFill();
    else
      pg.fill(this.fillAlphaColor == null ? this.fillColor : this.fillAlphaColor);

    if(strokeWeight == null)
      pg.noStroke();
    else
      pg.strokeWeight(strokeWeight);

    if(strokeColor == null)
      pg.noStroke();
    else
      pg.stroke(strokeColor);

    // draw shape
    // pg.rect(0,0,getSize().x,getSize().y);
  }

  public Integer getFillColor(){ return fillColor; }
  public ShapeNode setFillColor(Integer clr){ fillColor = clr; this.updateAlpha(); return this; }

  public Float getFillAlpha() { return this.fillAlpha; }
  public ShapeNode setFillAlpha(Float alpha) { this.fillAlpha = alpha; this.updateAlpha(); return this; }

  public Integer getStrokeColor(){ return strokeColor; }
  public ShapeNode setStrokeColor(Integer newColor){ strokeColor = newColor; return this; }

  public Float getStrokeWeight(){ return strokeWeight; }
  public ShapeNode setStrokeWeight(Float newWeight){ strokeWeight = newWeight; return this; }

  private void updateAlpha(){
    Integer baseColor = this.fillColor;
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
