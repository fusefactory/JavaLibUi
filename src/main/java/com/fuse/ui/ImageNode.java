package com.fuse.ui;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.Texture;

public class ImageNode extends Node {

  public enum Mode {
    NORMAL, // image rendered at original size at Node's origin (0,0) position
    CENTER, // image rendered at original size centered inside the node
    FIT,    // image is stretched/squeezed into exactly this node's dimensions
    FIT_CENTERED, // image is stretched/squeezed to fit inside the node, but aspect ratio is respected
    FILL // image is strechted/squeed to exactly fill the node, but aspect ratio is respected
  }

  protected PImage image;
  protected Mode mode;
  private boolean autoResizeToImage = false;
  private boolean bClearImageOnDestroy = false;
  protected Integer tintColor = null, tintColorAlpha = null;

  // instance lifecycle methods // // // // //

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public ImageNode(){
    image = null;
    mode = Mode.NORMAL;
    this.alphaState.push((Float v) -> this.updateAlpha());
  }

  public ImageNode(String nodeName){
    this();
    super.setName(nodeName);
  }

  @Override
  public void destroy(){
    if(this.bClearImageOnDestroy && this.image!=null){
      // only processing2?
      Object cache = pg.getCache(image);
      if(cache instanceof Texture) {
    	  Texture tex = (Texture)cache;
    	  tex.unbind();
    	  tex.disposeSourceBuffer();
      }
      pg.removeCache(image);
      this.image = null;
    }
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(image == null)
      return;

    if(this.tintColorAlpha != null)
      pg.tint(this.tintColorAlpha);

    switch(mode){
      case NORMAL : {
        pg.image(image, 0.0f, 0.0f);
        break;
      }
      case CENTER : {
        PVector pos = PVector.mult(getSize(), 0.5f);
        pg.imageMode(PApplet.CENTER);
        pg.image(image, pos.x, pos.y);
        pg.imageMode(PApplet.CORNERS); // restore default
        break;
      }
      case FIT : {
        pg.imageMode(PApplet.CORNERS);
        pg.image(image, 0.0f, 0.0f, getSize().x, getSize().y);
        break;
      }
      case FIT_CENTERED : {
        PVector fitCenteredSize = calculateFitCenteredSize();
        PVector pos = PVector.mult(getSize(), 0.5f);
        pg.imageMode(PApplet.CENTER);
        pg.image(image, pos.x, pos.y, fitCenteredSize.x, fitCenteredSize.y);
        pg.imageMode(PApplet.CORNERS); // restore default
        break;
      }
      case FILL : {
        PVector fillSize = calculateFillSize();
        PVector pos = PVector.mult(getSize(), 0.5f);
        pg.imageMode(PApplet.CENTER);
        pg.image(image, pos.x, pos.y, fillSize.x, fillSize.y);
        pg.imageMode(PApplet.CORNERS); // restore default
      }
    }

    if(tintColor != null)
      pg.noTint();
  }

  // config getter/setter methods // // // // //

  /**
   * Set/change the image of this node.
   * @param newImage The image that should from now on be rendered by this node
   */
  public void setImage(PImage newImage){
    image = newImage;

    if(autoResizeToImage && image != null)
      setSize(image.width, image.height);
  }

  /** @return PImage The image that this node is rendering */
  public PImage getImage(){ return image; }

  public Mode getMode(){ return mode; }
  public void setMode(Mode newMode){ if(newMode != null) mode = newMode; }

  public boolean getAutoResizeToImage(){ return autoResizeToImage; }
  public void setAutoResizeToImage(boolean enable){
    autoResizeToImage = enable;
    if(autoResizeToImage && image != null){
      setSize(image.width, image.height);
    }
  }

  public ImageNode setTint(Integer clr){ tintColor = clr; this.updateAlpha(); return this; }
  public Integer getTint(){ return tintColor; }

  public ImageNode setClearImageOnDestroy(boolean enable){ this.bClearImageOnDestroy = enable; return this; }
  public boolean getClearImageOnDestroy(){ return this.bClearImageOnDestroy; }

  // private helper methods // // // // //

  private PVector calculateFitCenteredSize(){
    if(image == null) return new PVector(0.0f,0.0f,0.0f);
    float w = getSize().x / image.width;
    float h = getSize().y / image.height;
    float factor = Math.min(w,h);
    return new PVector(factor * image.width, factor * image.height, 0.0f);
  }

  private PVector calculateFillSize(){
    if(image == null) return new PVector(0.0f,0.0f,0.0f);
    float w = getSize().x / image.width;
    float h = getSize().y / image.height;
    float factor = Math.max(w,h);
    return new PVector(factor * image.width, factor * image.height, 0.0f);
  }

  private void updateAlpha(){
    if(this.tintColor == null)
        this.tintColorAlpha = pg.color(255,255,255, this.alphaState.get() * 255.0f);
    else
      this.tintColorAlpha = Node.alphaColor(this.tintColor, this.alphaState.get());
  }
}
