package com.fuse.ui;

import processing.core.PVector;

public class ShadowImageNode extends ImageNode {

  private PVector shadowOffset = new PVector(2,2,0);
  private int shadowColor;
  private boolean drawShadow = true;

  public ShadowImageNode(){
    this.shadowColor = pg.color(0,0,0,150);
  }

  @Override
  protected void drawImage(float x, float y, PVector siz){
    if(this.drawShadow) {
      // backup any tint that should be applied to main image
      int existingTintClr = pg.tintColor;
      // apply shadow tint
      pg.tint(shadowColor);

      // draw shadow
      if(siz!=null)
        pg.image(image, x+shadowOffset.x, y+shadowOffset.y, siz.x, siz.y);
      else
        pg.image(image, x+shadowOffset.x, y+shadowOffset.y);

      // restore image tint
      pg.tint(existingTintClr);
    }

    // draw main image
    if(siz!=null)
      pg.image(image, x, y, siz.x, siz.y);
    else
      pg.image(image, x, y);
  }

  public ShadowImageNode setShadowColor(int clr) { this.shadowColor = clr; return this; }
  public int getShadowColor() { return this.shadowColor; }

  public ShadowImageNode setShadowOffset(PVector offset) { this.shadowOffset = offset; return this; }
  public PVector getShadowOffset() { return this.shadowOffset.get(); }

  public ShadowImageNode setDrawShadow(boolean enable) { this.drawShadow = enable; return this; }
  public boolean getDrawShadow() { return this.drawShadow; }
}
