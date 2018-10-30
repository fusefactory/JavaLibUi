package com.fuse.ui.example;

import java.util.logging.*;
import java.util.LinkedList;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.TextNode;

public class App extends PApplet {
  Logger logger;
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug;

  private TouchManager touchManager;
  private Node sceneNode;
  private TextNode logNode;
  private LinkedList<String> logLines = new LinkedList<String>();

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

    this.logNode = new TextNode();
    this.logNode.setPosition(5, 5);
    this.logNode.setSize(PVector.sub(this.sceneNode.getSize(), new PVector(10,10)));
    this.logNode.setTextSize(14);
    this.sceneNode.addChild(this.logNode);

    touchManager.touchDownEvent.addListener((evt) ->this.log("[touchDownEvent] " + evt.toString()));
    touchManager.touchMoveEvent.addListener((evt) ->this.log("[touchMoveEvent] " + evt.toString()));
    touchManager.touchUpEvent.addListener((evt) ->this.log("[touchUpEvent] " + evt.toString()));
    // touchManager.touchEvent.addListener((evt) -> this.log("[touchEvent] " + evt.toString()));
  }

  private void log(String line) {
    // System.out.println("touchevent: "+evt.toString());
    while (this.logLines.size() >= 25) this.logLines.removeFirst();
    this.logLines.add(line);
    this.logNode.setText(String.join("\n", this.logLines));
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

  @Override
  public void mousePressed(MouseEvent event) {
    // touchManager.touchDown(0, new PVector(mouseX, mouseY, 0f));
    if (event.getNative() instanceof com.jogamp.newt.event.MouseEvent) {
      com.jogamp.newt.event.MouseEvent nativeEvent = (com.jogamp.newt.event.MouseEvent) event.getNative();
      touchManager.touchDown(nativeEvent.getPointerId(0), new PVector(mouseX, mouseY, 0f));
    }
  }

  @Override
  public void mouseDragged(MouseEvent event) {
    // touchManager.touchMove(0, new PVector(mouseX, mouseY, 0f));
    if (event.getNative() instanceof com.jogamp.newt.event.MouseEvent) {
      com.jogamp.newt.event.MouseEvent nativeEvent = (com.jogamp.newt.event.MouseEvent) event.getNative();
      touchManager.touchMove(nativeEvent.getPointerId(0), new PVector(mouseX, mouseY, 0f));
    }
  }

  @Override
  public void mouseReleased(MouseEvent event) {
    // touchManager.touchUp(0, new PVector(mouseX, mouseY, 0f));
    if (event.getNative() instanceof com.jogamp.newt.event.MouseEvent) {
      com.jogamp.newt.event.MouseEvent nativeEvent = (com.jogamp.newt.event.MouseEvent) event.getNative();
      touchManager.touchUp(nativeEvent.getPointerId(0), new PVector(mouseX, mouseY, 0f));
    }
  }

//  public void mousePressed(){
//    // logger.finest("mousePressed: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
//    touchManager.touchDown(0, new PVector(mouseX, mouseY, 0f));
//  }
//
//  public void mouseDragged(){
//    // logger.finest("mouseDragged: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
//    touchManager.touchMove(0, new PVector(mouseX, mouseY, 0f));
//  }
//
//  public void mouseReleased(){
//    // logger.finest("mouseReleased: " + Integer.toString(mouseX) + ", " + Integer.toString(mouseY));
//    touchManager.touchUp(0, new PVector(mouseX, mouseY, 0f));
//  }

  public void keyPressed(){
    switch(key){
      case 'd': {
        bDrawDebug = !bDrawDebug;
        return;
      }
    }
  }
}
