package com.fuse.ui;

import processing.core.PVector;
import processing.core.PApplet;
import processing.core.PFont;

public class TextNode extends Node {
  private String text;
  private int textColor;
  private float textSize;
  private PVector textOffset;
  private PFont font;
  private int alignX, alignY;

  private void _init(){
    text = "";
    if(pg != null){
      pg.colorMode(pg.RGB, 255);
      textColor = pg.color(255);
    }
    textSize = 20f;
    textOffset = new PVector(0.0f, 0.0f, 0.0f);
    alignX = PApplet.LEFT;
    alignY = PApplet.BASELINE;
  }

  public TextNode(){
    _init();
  }

  public TextNode(String nodeName){
    super(nodeName);
    _init();
  }

  public void setText(String txt){ text = txt == null ? "" : txt; }
  public String getText(){ return text; }

  public void setTextColor(int newColor){ this.textColor = newColor; }
  public int getTextColor(){ return textColor; }

  public void setTextSize(float newSize){ textSize = newSize; }
  public float getTextSize(){ return textSize; }

  public void setFont(PFont newFont){ this.font = newFont; }
  public PFont getFont(){ return this.font; }

  public PVector getTextOffset(){ return textOffset.get(); }
  public void setTextOffset(PVector offset){ textOffset = offset.get(); }

  public void setAlignX(int align){ alignX = align; }
  public int getAlignX(int align){ return alignX; }

  public void setAlignY(int align){ alignY = align; }
  public int getAlignY(int align){ return alignY; }

  @Override
  public void draw(){
    super.draw();

    if(text.equals(""))
      return;

    if(this.font != null)
      pg.textFont(this.font);

    pg.noStroke();
    pg.fill(textColor);
    pg.textSize(textSize);
    pg.textAlign(alignX, alignY);
    pg.text(text, textOffset.x, textOffset.y, getSize().x, getSize().y);
  }
}
