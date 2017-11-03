package com.fuse.ui.example;

import java.util.concurrent.ConcurrentLinkedQueue;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.example.utils.TuioInput;
import com.fuse.ui.TouchManager;
import com.fuse.ui.TouchEvent;
import com.fuse.ui.Node;
import com.fuse.ui.TextNode;
import com.fuse.ui.extensions.Swiper;

public class App extends PApplet {
  private static int TUIO_PORT = 3333;
  private float timeBetweenFrames;
  private boolean bDrawDebug = true;

  private ConcurrentLinkedQueue<Float> hist1 = new ConcurrentLinkedQueue<Float>();
  private ConcurrentLinkedQueue<Float> hist2 = new ConcurrentLinkedQueue<Float>();
  private ConcurrentLinkedQueue<Float> hist3 = new ConcurrentLinkedQueue<Float>();
  private ConcurrentLinkedQueue<Float> hist4 = new ConcurrentLinkedQueue<Float>();
  private ConcurrentLinkedQueue<Float> hist5 = new ConcurrentLinkedQueue<Float>();
  private int MAXHISTSIZE = 800;
  private TouchEvent activeTouchEvent;
  private PApplet papplet;
  private PGraphics pg;
  private Node sceneNode,subjectNode;

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
    papplet.frameRate(60.0f);
    timeBetweenFrames = 1.0f / papplet.frameRate;

    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    this.subjectNode = new Node();
    subjectNode.setPosition(-1000,-1000);
    subjectNode.touchEvent.addListener((TouchEvent e) -> this.activeTouchEvent = e);
    subjectNode.setSize(sceneNode.getSize().sub(subjectNode.getPosition()));
    sceneNode.addChild(subjectNode);

    // we're not swiping anything, but the swiper shows a nice reference grid in debug mode
    Swiper
      .enableFor(subjectNode)
      .setDampThrowFactor(2.0f)
      .setSnapEnabled(false)
      .setSmoothValue(10.0f);

    {
      HistNode histNode = new HistNode(this.hist1);
      subjectNode.addChild(histNode);
      histNode.setGlobalPosition(new PVector(0,sceneNode.getSize().y));
      histNode.setFillColor(pg.color(255,100,100,150));
    }

    {
      HistNode histNode = new HistNode(this.hist2);
      subjectNode.addChild(histNode);
      histNode.setGlobalPosition(new PVector(0,sceneNode.getSize().y));
      histNode.setFillColor(pg.color(0,255,100,150));
    }

    {
      HistNode histNode = new HistNode(this.hist3);
      subjectNode.addChild(histNode);
      histNode.setGlobalPosition(new PVector(0,sceneNode.getSize().y*0.25f));
      histNode.setScale(new PVector(1.0f, 0.3f, 1.0f));
      histNode.setFillColor(pg.color(255,0,0,200));
    }

    {
      HistNode histNode = new HistNode(this.hist4);
      subjectNode.addChild(histNode);
      histNode.setGlobalPosition(new PVector(0,sceneNode.getSize().y*0.5f));
      histNode.setScale(new PVector(1.0f, 0.3f, 1.0f));
      histNode.setFillColor(pg.color(0,255,0,200));
    }

    {
      HistNode histNode = new HistNode(this.hist5);
      subjectNode.addChild(histNode);
      histNode.setGlobalPosition(new PVector(0,sceneNode.getSize().y*0.75f));
      histNode.setScale(new PVector(1.0f, 0.3f, 1.0f));
      histNode.setFillColor(pg.color(0,0,255,200));
    }

    {
      sceneNode.addChild(
        new TextNode()
        .setText("drag to see velocity information...")
        .setAlignX(CENTER)
        .noCrop()
        .setPosition(sceneNode.getSize().mult(0.5f)));
    }

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);
    touchManager.setDispatchOnUpdate(true);

    tuioInput = new TuioInput();
    tuioInput.setPapplet(this);
    tuioInput.setup(touchManager, TUIO_PORT);
  }

  private void update(float dt){
    if(this.activeTouchEvent != null){ //} && !this.activeTouchEvent.isFinished()){
      TouchEvent e = this.subjectNode.toLocal(this.activeTouchEvent);

      this.hist1.add(e.velocity.mag());
      while(this.hist1.size() > MAXHISTSIZE){
        hist1.poll();
      }

      this.hist2.add(e.velocitySmoothed.mag());
      while(this.hist2.size() > MAXHISTSIZE){
        hist2.poll();
      }

      this.hist3.add(e.velocitySmoothed.x);
      while(this.hist3.size() > MAXHISTSIZE){
        hist3.poll();
      }

      this.hist4.add(e.velocitySmoothed.y);
      while(this.hist4.size() > MAXHISTSIZE){
        hist4.poll();
      }

      this.hist5.add(e.velocitySmoothed.z);
      while(this.hist5.size() > MAXHISTSIZE){
        hist5.poll();
      }
    }

    touchManager.update(dt);
    // run update on all UI nodes
    sceneNode.updateSubtree(dt);
  }

  public void draw(){
    // OF-style; first update all "data" before rendering
    update(timeBetweenFrames);
    this.papplet.frame.setTitle(Integer.toString((int)this.papplet.frameRate)+" fps");
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
