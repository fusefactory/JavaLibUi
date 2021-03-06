package com.fuse.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PFont;
import processing.core.PVector;

public class TextNode extends ShapeNode {
  private String text;

  private float textSize;
  private PVector textOffset;
  private PFont font;
  private int alignX, alignY;
  private Integer frameColor = null, frameColorAlpha = null;
  private PVector framePadding = new PVector(3.0f, 3.0f, 0.0f);
  private boolean bCropEnabled = true; // TODO: change default to false

  public TextNode(){
    text = "";
    if(pg != null){
      pg.colorMode(PGraphics.RGB, 255);
      this.setFillColor(pg.color(255));
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

  /**
   * @deprecated use {@link #setFillColor(int)}
   */
  @Deprecated
  public TextNode setTextColor(int newColor){ this.setFillColor(newColor); return this; }

  /**
   * @deprecated use {@link #getFillColor()}
   */
  @Deprecated
  public int getTextColor(){ return this.getFillColor(); }

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

  public TextNode setFrameColor(Integer clr) { this.frameColor = clr; this.updateAlpha(); return this; }
  public Integer getFrameColor() { return this.frameColor; }

  public TextNode setFramePadding(PVector vec) { this.framePadding = vec == null ? new PVector(3.0f,3.0f,0.0f) : vec; return this; }
  public PVector getFramePadding() { return this.framePadding.get(); }

  public TextNode setCropEnabled(boolean enabled){ this.bCropEnabled = enabled; return this; }
  public boolean getCropEnabled(){ return this.bCropEnabled; }
  public TextNode noCrop(){ return this.setCropEnabled(false); }

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

    super.beforeDraw(); // prepare color settings
    this.drawText(textOffset, bCropEnabled ? getSize() : null);

    super.afterDraw();
  }

  protected void drawText(PVector textpos, PVector cropSize) {
    if(cropSize != null)
      pg.text(this.text, textpos.x, textpos.y, cropSize.x, cropSize.y);
    else
      pg.text(this.text, textpos.x, textpos.y);
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
    if(this.frameColor != null)
      this.frameColorAlpha = Node.alphaColor(this.frameColor, this.alphaState.get());
  }
}
