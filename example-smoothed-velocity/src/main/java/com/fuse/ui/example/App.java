package com.fuse.ui.example;

import java.util.concurrent.ConcurrentLinkedQueue;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.example.utils.TuioInput;
import com.fuse.ui.TouchManager;
import com.fuse.ui.TouchEvent;
import com.fuse.ui.Node;

public class App extends PApplet {
  private static int TUIO_PORT = 3333;
  private float timeBetweenFrames;
  private boolean bDrawDebug = true;

  private ConcurrentLinkedQueue<PVector> hist1 = new ConcurrentLinkedQueue<PVector>();
  private ConcurrentLinkedQueue<PVector> hist2 = new ConcurrentLinkedQueue<PVector>();
  private int MAXHISTSIZE = 800;
  private TouchEvent activeTouchEvent;
  private PApplet papplet;
  private PGraphics pg;
  private Node sceneNode;

  // input for processing Tuio (OSC-based touch protocol) events;
  // all TUIO events are converted and passed on to the TouchManager
  private TuioInput tuioInput;
  private TouchManager touchManager;

  public static void main( String[] args ){
    PApplet.main("com.fuse.ui.example.App");
  }

  public App(){
    super();
    // always a good idea to refer to a dedicated papplet variable instead of 'this'
    this.papplet = this;
  }

  public void settings(){
    size(800, 600, P3D);
    //fullScreen(P3D, 2);
  }

  public void setup(){
    papplet.frameRate(30.0f);
    timeBetweenFrames = 1.0f / papplet.frameRate;

    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);
    sceneNode.touchEvent.addListener((TouchEvent e) -> this.activeTouchEvent = e);

    {
      HistNode histNode = new HistNode(this.hist1);
      histNode.setPosition(0,sceneNode.getSize().y);
      histNode.setFillColor(pg.color(255,100,100,100));
      sceneNode.addChild(histNode);
    }

    {
      HistNode histNode = new HistNode(this.hist2);
      histNode.setPosition(0,sceneNode.getSize().y);
      histNode.setFillColor(pg.color(100,255,100,100));
      sceneNode.addChild(histNode);
    }

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);
    touchManager.setDispatchOnUpdate(true);

    tuioInput = new TuioInput();
    tuioInput.setPapplet(this);
    tuioInput.setup(touchManager, TUIO_PORT);
  }

  private void update(float dt){
    if(this.activeTouchEvent != null && !this.activeTouchEvent.isFinished()){
      this.hist1.add(this.activeTouchEvent.velocity.copy());
      while(this.hist1.size() > MAXHISTSIZE){
        hist1.poll();
      }

      this.hist2.add(this.activeTouchEvent.velocitySmoothed.copy());
      while(this.hist2.size() > MAXHISTSIZE){
        hist2.poll();
      }
    }

    touchManager.update(dt);
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
        System.out.println("draw debug: "+Boolean.toString(bDrawDebug));
        return;
      }
    }
    switch (keyCode){
      // case UP:
      // case DOWN:
    }
  }
}
