package com.fuse.ui.example;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.RectNode;
import com.fuse.ui.TextNode;
import com.fuse.ui.extensions.Draggable;
import com.fuse.ui.extensions.PinchZoom;
import com.fuse.ui.extensions.Constrain;

public class App extends PApplet {
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug;

  private TouchManager touchManager;
  private Node sceneNode;
  private RectNode containerNode, zoomableNode;

  public static void main( String[] args )
  {
    PApplet.main("com.fuse.ui.example.App");
  }

  public App(){
    super();
    papplet = this;
  }

  public void settings(){
    size(800, 600, P3D);
  }

  public void setup(){
    papplet.frameRate(30.0f);
    timeBetweenFrames = 1.0f / papplet.frameRate;
    bDrawDebug = false;

    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);

    populateScene(sceneNode);
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

  public void populateScene(Node scene){
    // Container
    this.containerNode = new RectNode();
    this.containerNode.setPosition(100,100);
    this.containerNode.setSize(600,400);
    this.containerNode.setRectColor(pg.color(100,100,100));
    this.containerNode.setClipContent(true); // toggle-able with the 'c' key

    // zoomable
    this.zoomableNode = new RectNode();
    this.zoomableNode.setPosition(100,100);
    this.zoomableNode.setSize(200,133);
    this.zoomableNode.setRectColor(pg.color(200,150,150));
    this.containerNode.addChild(this.zoomableNode);
  }

  public void mousePressed(){
    touchManager.touchDown(0, new PVector(mouseX, mouseY, 0f));
  }

  public void mouseDragged(){
    touchManager.touchMove(0, new PVector(mouseX, mouseY, 0f));
  }

  public void mouseReleased(){
    touchManager.touchUp(0, new PVector(mouseX, mouseY, 0f));
  }

  public void keyPressed(){
    switch(key){
      case 'd': {
        bDrawDebug = !bDrawDebug;
        return;
      }

      case 'c':{
        this.containerNode.setClipContent(!containerNode.isClippingContent());
      }
    }
  }
}
