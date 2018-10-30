package com.fuse.ui.example;

import java.util.logging.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.RectNode;
import com.fuse.ui.TextNode;
import com.fuse.ui.extensions.Draggable;
import com.fuse.ui.extensions.Swiper;
import com.fuse.ui.extensions.PinchZoom;
import com.fuse.ui.extensions.SmoothScroll;
import com.fuse.ui.extensions.Constrain;

public class App extends PApplet {
  Logger logger;
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug;

  private TouchManager touchManager;
  private Node sceneNode;

  public static void main( String[] args )
  {
    PApplet.main("com.fuse.ui.example.App");
  }

  public App(){
    super();
    logger = Logger.getLogger("");//App.class.getName());
    logger.setLevel(Level.ALL);
    papplet = this;
  }

  public void settings(){
    size(800, 600, P3D);
  }

  public void setup(){
    papplet.frameRate(30.0f);
    timeBetweenFrames = 1.0f / papplet.frameRate;
    bDrawDebug = true;

    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);
  }

  private void update(float dt){
    // run update on all UI nodes
    sceneNode.updateSubtree(dt);
  }

  public void draw(){
    // OF-style; first update all "data" before rendering
    update(timeBetweenFrames);

    papplet.background(0);
    papplet.clear();

    // UI (2D)
    pg.beginDraw();
    {
      pg.clear();
      sceneNode.render();

      if(bDrawDebug){
        sceneNode.renderDebug();
        touchManager.drawActiveTouches();
      }
    }
    pg.endDraw();

    papplet.image(pg, 0f,0f);
  }

  public void mousePressed(){
    // logger.finest("mousePressed: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
    touchManager.touchDown(0, new PVector(mouseX, mouseY, 0f));
  }

  public void mouseDragged(){
    // logger.finest("mouseDragged: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
    touchManager.touchMove(0, new PVector(mouseX, mouseY, 0f));
  }

  public void mouseReleased(){
    // logger.finest("mouseReleased: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
    touchManager.touchUp(0, new PVector(mouseX, mouseY, 0f));
  }

  public void keyPressed(){
    switch(key){
      case 'd': {
        bDrawDebug = !bDrawDebug;
        return;
      }
    }
  }
}
