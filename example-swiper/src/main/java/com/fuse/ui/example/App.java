package com.fuse.ui.example;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.example.utils.TuioInput;
import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.RectNode;
import com.fuse.ui.ImageNode;
import com.fuse.ui.extensions.Swiper;

public class App extends PApplet {
  private static int TUIO_PORT = 3333;
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug = true;

  // input for processing Tuio (OSC-based touch protocol) events;
  // all TUIO events are converted and passed on to the TouchManager
  private TuioInput tuioInput;
  private TouchManager touchManager;
  private Node sceneNode;
  private Node touchAreaNode;
  private Node scrollableNode;
  private Swiper swiper;

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

    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);
    touchManager.setDispatchOnUpdate(true);

    tuioInput = new TuioInput();
    tuioInput.setPapplet(this);
    tuioInput.setup(touchManager, TUIO_PORT);

    populateScene(sceneNode);
  }

  private void update(float dt){
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

  public void populateScene(Node scene){
    // Container
    this.touchAreaNode = new Node();
    this.touchAreaNode.setName("container");
    this.touchAreaNode.setPosition(100,100);
    this.touchAreaNode.setSize(600,400);
    this.touchAreaNode.setClipContent(true); // toggle-able with the 'c' key
    this.sceneNode.addChild(this.touchAreaNode);

    // Scrollable content sub-container
    this.scrollableNode = new Node();
    this.touchAreaNode.addChild(
      this.scrollableNode
        .setName("scrollable")
        .setInteractive(false));


    // Create/enable swiper extensions
    this.swiper = Swiper.enableFor(touchAreaNode, scrollableNode);

    // Create some dummy content so we actually see some stuff happening

    for(int x=0; x<100; x++){
      for(int y=0; y<4; y++){
        this.scrollableNode.addChild(
          (new RectNode())
            .setRectColor(pg.color(150+(float)Math.random()*105, 0, (float)Math.random()*105))
            .setSize(98,98)
            .setPosition(x*100, y*100)
            .setInteractive(false));
      }
    }
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

      case 'c':{
        this.touchAreaNode.setClipContent(!touchAreaNode.isClippingContent());
        System.out.println("clipping: "+Boolean.toString(touchAreaNode.isClippingContent()));
        return;
      }

      case 'r': {
        scrollableNode.setPosition(0,0);
        return;
      }
    }
    switch (keyCode){
      case UP:
        this.swiper.setSmoothValue(this.swiper.getSmoothValue()+0.5f);
        System.out.println("smoothing: "+Float.toString(this.swiper.getSmoothValue()));
        return;
      case DOWN:
        this.swiper.setSmoothValue(this.swiper.getSmoothValue()-0.5f);
        System.out.println("smoothing: "+Float.toString(this.swiper.getSmoothValue()));
        return;
    }
  }
}
