package com.fuse.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

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
  protected Integer tintColor = null;
  protected PVector fitCenteredSize = null;
  protected PVector fillSize = null;

  private void _init(){
    image = null;
    mode = Mode.NORMAL;
  }

  /** Default constructor; intialized with default values: image=null and mode=NORMAL */
  public ImageNode(){
    _init();
  }

  public ImageNode(String nodeName){
    super(nodeName);
    _init();
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(image == null)
      return;

    if(tintColor != null)
      pg.tint(tintColor);

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
        // "cache" the centered fit size
        if(fitCenteredSize == null)
          fitCenteredSize = calculateFitCenteredSize();
        PVector pos = PVector.mult(getSize(), 0.5f);
        pg.imageMode(PApplet.CENTER);
        pg.image(image, pos.x, pos.y, fitCenteredSize.x, fitCenteredSize.y);
        pg.imageMode(PApplet.CORNERS); // restore default
        break;
      }
      case FILL : {
        // "cache" the centered fit size
        //if(fillSize == null)
        fillSize = calculateFillSize();
        PVector pos = PVector.mult(getSize(), 0.5f);
        pg.imageMode(PApplet.CENTER);
        pg.image(image, pos.x, pos.y, fillSize.x, fillSize.y);
        pg.imageMode(PApplet.CORNERS); // restore default
      }
    }

    if(tintColor != null)
      pg.noTint();
  }

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

  public void setTint(Integer clr){ tintColor = clr; }
  public Integer getTint(){ return tintColor; }

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
}
