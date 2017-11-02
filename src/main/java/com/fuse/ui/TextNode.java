package com.fuse.ui;

import processing.core.PVector;
import processing.core.PApplet;
import processing.core.PFont;

public class TextNode extends Node {
  private String text;
  private int textColor, textColorAlpha;
  private float textSize;
  private PVector textOffset;
  private PFont font;
  private int alignX, alignY;
  private Integer frameColor = null, frameColorAlpha = null;
  private PVector framePadding = new PVector(3.0f, 3.0f, 0.0f);

  public TextNode(){
    text = "";
    if(pg != null){
      pg.colorMode(pg.RGB, 255);
      this.setTextColor(pg.color(255));
    }
    textSize = 20f;
    textOffset = new PVector(0.0f, 0.0f, 0.0f);
    alignX = PApplet.LEFT;
    alignY = PApplet.BASELINE;

    this.alphaState.push((Float v) -> this.updateAlpha());
  }

  public TextNode(String nodeName){
    this();
    super.setName(nodeName);
  }

  public TextNode setText(String txt){ text = txt == null ? "" : txt; return this; }
  public String getText(){ return text; }

  public TextNode setTextColor(int newColor){ this.textColor = newColor; this.updateAlpha(); return this; }
  public int getTextColor(){ return textColor; }

  public TextNode setTextSize(float newSize){ textSize = newSize; return this; }
  public float getTextSize(){ return textSize; }

  public TextNode setFont(PFont newFont){ this.font = newFont; return this; }
  public PFont getFont(){ return this.font; }

  public PVector getTextOffset(){ return textOffset.get(); }
  public TextNode setTextOffset(PVector offset){ textOffset = offset.get(); return this; }

  public TextNode setAlignX(int align){ alignX = align; return this; }
  public int getAlignX(int align){ return alignX; }

  public TextNode setAlignY(int align){ alignY = align; return this; }
  public int getAlignY(int align){ return alignY; }

  public void setFrameColor(Integer clr) { this.frameColor = clr; this.updateAlpha(); }
  public Integer getFrameColor() { return this.frameColor; }

  public void setFramePadding(PVector vec) { this.framePadding = vec == null ? new PVector(3.0f,3.0f,0.0f) : vec; }
  public PVector getFramePadding() { return this.framePadding.get(); }

  @Override
  public void draw(){
    super.draw();

    if(text.equals(""))
      return;

    if(this.font != null)
      pg.textFont(this.font);

    pg.textSize(textSize);
    pg.textAlign(alignX, alignY);
    pg.noStroke();

    if(this.frameColorAlpha != null) {
    		float x=textOffset.x, y=textOffset.y, w = pg.textWidth(text);
    		pg.fill(this.frameColorAlpha);

        switch(this.alignX){
          case PApplet.LEFT: x -= this.framePadding.x; break;
          case PApplet.RIGHT: x += this.getSize().x - w - this.framePadding.x; break;
          case PApplet.CENTER: x += this.getSize().x/2.0f - w/2.0f - this.framePadding.x;
        }

        // TODO
        switch(this.alignY){
          case PApplet.BASELINE:
          case PApplet.TOP: y -= this.framePadding.y; break;
          case PApplet.BOTTOM: y += this.getSize().y - this.textSize - this.framePadding.y; break;
          case PApplet.CENTER: y += this.getSize().y/2.0f - this.textSize/2.0f - this.framePadding.y; break;
        }

    		pg.rect(x, y,
    				Math.min(w+this.framePadding.x*2, this.getSize().x),
    				Math.min(this.textSize+this.framePadding.y*2, this.getSize().y));
    }

    pg.fill(textColorAlpha);
    pg.text(text, textOffset.x, textOffset.y, getSize().x, getSize().y);
  }

  public float getDrawWidth(){
	  if(text.equals(""))
		  return 0.0f;

	  if(this.font != null)
	      pg.textFont(this.font);

	  pg.noStroke();
	  //pg.fill(textColor);
	  pg.textSize(textSize);
	  pg.textAlign(alignX, alignY);
	  return pg.textWidth(text);
  }

  private void updateAlpha(){
    this.textColorAlpha = Node.alphaColor(this.textColor, this.alphaState.get());
    if(this.frameColor != null)
      this.frameColorAlpha = Node.alphaColor(this.frameColor, this.alphaState.get());
  }
}
