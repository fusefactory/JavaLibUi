package com.fuse.ui;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class ImageNode extends Node {

  public enum Mode {
    NORMAL, // image rendered at original size at Node's origin (0,0) position
    CENTER, // image rendered at original size centered inside the node
    FIT
  }

  private PImage image;
  private Mode mode;
  private boolean autoResizeToImage = false;

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

    if(mode == Mode.NORMAL){
      pg.image(image, 0.0f, 0.0f);
      return;
    }

    if(mode == Mode.CENTER){
      PVector pos = PVector.mult(getSize(), 0.5f);
      pg.imageMode(PApplet.CENTER);
      pg.image(image, pos.x, pos.y);
      pg.imageMode(PApplet.CORNERS); // restore default
      return;
    }

    if(mode == Mode.FIT){
      pg.imageMode(PApplet.CORNERS);
      pg.image(image, 0.0f, 0.0f, getSize().x, getSize().y);
    }
  }

  /**
   * Set/change the image of this node.
   * @param newImage The image that should from now on be rendered by this node
   */
  public void setImage(PImage newImage){
    image = newImage;

    if(autoResizeToImage && image != null)
      setSize(image.pixelWidth, image.pixelHeight);
  }

  /** @return PImage The image that this node is rendering */
  public PImage getImage(){ return image; }

  public Mode getMode(){ return mode; }
  public void setMode(Mode newMode){ mode = newMode; }

  public boolean getAutoResizeToImage(){ return autoResizeToImage; }
  public void setAutoResizeToImage(boolean enable){
    autoResizeToImage = enable;
    if(autoResizeToImage && image != null){
      setSize(image.width, image.height);
    }
  }
}
