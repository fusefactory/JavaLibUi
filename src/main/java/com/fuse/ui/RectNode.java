package com.fuse.ui;

import processing.core.PVector;

public class RectNode extends Node {

  private Integer rectFillColor = null;
 
  private Integer rectStrokeColor = null;
  private Float rectStrokeWeight = null;

  private Float fillAlpha = null;
  private Integer fillAlphaColor = null;

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
  public RectNode setRectColor(Integer newRectColor){
	  rectFillColor = newRectColor;
	  if(this.fillAlpha != null) this.setFillAlpha(this.fillAlpha); // apply fill alpha
	  return this;
  }

  public Integer getRectStrokeColor(){ return rectStrokeColor; }
  public RectNode setRectStrokeColor(Integer newColor){ rectStrokeColor = newColor; return this; }

  public Float getStrokeWeight(){ return rectStrokeWeight; }
  public RectNode setStrokeWeight(Float newWeight){ rectStrokeWeight = newWeight; return this; }
  
  public Float getFillAlpha() { return this.fillAlpha; }
  public RectNode setFillAlpha(Float alpha) {
	  this.fillAlpha = alpha;

	  if(this.fillAlpha == null) {
		  this.fillAlphaColor = null;
		  return this;
	  }

	  pg.colorMode(pg.RGB, 255);
	  this.fillAlphaColor = pg.color(
			  pg.red(this.rectFillColor),
			  pg.green(this.rectFillColor),
			  pg.blue(this.rectFillColor),
			  alpha * 255.0f);

	  return this;
  }
}
