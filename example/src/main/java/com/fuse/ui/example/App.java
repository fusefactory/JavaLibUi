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
    bDrawDebug = false;

    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);

    // create scene content

    {
      RectNode n = new RectNode();
      n.setPosition(100, 100);
      n.setSize(100,100);
      sceneNode.addChild(n);

      TextNode tx = new TextNode();
      tx.setText("RectNode");
      tx.setSize(100,100);
      tx.setTextSize(14);
      tx.setTextColor(pg.color(0,0,0));
      n.addChild(tx);
    }

    {
      RectNode n = new RectNode();
      n.setPosition(250, 100);
      n.setSize(100,100);
      sceneNode.addChild(n);
      // add draggable extension
      Draggable.enableFor(n);

      TextNode tx = new TextNode();
      tx.setText("Draggable");
      tx.setSize(100,100);
      tx.setTextSize(14);
      tx.setTextColor(pg.color(0,0,0));
      n.addChild(tx);
      // disable textnode, otherwise it will take all touchevents,
      // because it covers its entire parent node
      tx.setInteractive(false);
    }

    {
      RectNode n = new RectNode();
      n.setPosition(400, 100);
      n.setSize(100,100);
      sceneNode.addChild(n);
      // add draggable extension
      PinchZoom.enableFor(n);

      TextNode tx = new TextNode();
      tx.setText("PinchZoom (WIP)");
      tx.setSize(100,100);
      tx.setTextSize(14);
      tx.setTextColor(pg.color(0,0,0));
      n.addChild(tx);
      // disable textnode, otherwise it will take all touchevents,
      // because it covers its entire parent node
      tx.setInteractive(false);
    }

    { // SmoothScroll extension demo
      RectNode n = new RectNode();
      n.setPosition(50, 250);
      n.setSize(700,300);
      n.setRectColor(pg.color(80,80,80));
      n.setClipContent(true);
      sceneNode.addChild(n);

      Node scrollableNode = new Node();
      scrollableNode.setInteractive(false);
      n.addChild(scrollableNode);



      for(int i=-20; i<=20; i++){
        RectNode black = new RectNode();
        float height = 10.0f + (float)Math.cos(0.1f * (float)i) * 100.0f;
        black.setPosition(i*700+10, 150.0f-height*0.5f);
        black.setSize(680, height);
        black.setRectColor(pg.color(0));
        black.setInteractive(false);
        scrollableNode.addChild(black);
      }

      // configure smooth scroll
      SmoothScroll ext = SmoothScroll.enableFor(n, scrollableNode);
      ext.setSnapEnabled(true);
      ext.setMinOffset(-20.0f*700.0f, 0.0f);
      ext.setMaxOffset(20.0f*700.0f, 0.0f);
      ext.setSnapFactor(0.95f);

      // smooth scroll lets the user scroll in two dimensions, so we add a constrain to fix the vertical dimension,
      // allowing only horizontal scrolling
      Constrain.enableFor(scrollableNode).setFixY();
    }
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
