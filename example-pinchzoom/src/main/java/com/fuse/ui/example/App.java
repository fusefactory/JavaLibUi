package com.fuse.ui.example;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import com.fuse.ui.example.utils.TuioInput;
import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.RectNode;
import com.fuse.ui.ImageNode;
import com.fuse.ui.extensions.Draggable;
import com.fuse.ui.extensions.PinchZoom;
import com.fuse.ui.extensions.Constrain;

public class App extends PApplet {
  private static int TUIO_PORT = 3333;
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug;

  // input for processing Tuio (OSC-based touch protocol) events;
  // all TUIO events are converted and passed on to the TouchManager
  private TuioInput tuioInput;
  private TouchManager touchManager;
  private Node sceneNode;
  private RectNode containerNode;
  private ImageNode zoomableNode;
  private Draggable draggable;
  private Constrain constrain;
  private PinchZoom pinchZoom;

  public static void main( String[] args )
  {
    PApplet.main("com.fuse.ui.example.App");
  }

  public App(){
    super();
    papplet = this;
  }

  public void settings(){
    //size(800, 600, P3D);
    fullScreen(P3D);
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

    tuioInput = new TuioInput();
    tuioInput.setPapplet(this);
    tuioInput.setup(touchManager, TUIO_PORT);

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
    this.containerNode.setName("container");
    this.containerNode.setPosition(100,100);
    this.containerNode.setSize(600,400);
    this.containerNode.setRectColor(pg.color(100,100,100));
    //this.containerNode.setClipContent(true); // toggle-able with the 'c' key
    this.sceneNode.addChild(this.containerNode);

    // zoomable
    this.zoomableNode = new ImageNode();
    this.zoomableNode.setName("zoomable");
    this.zoomableNode.setPosition(100,100);
    this.zoomableNode.setImage(papplet.loadImage("alpacas.jpg"));
    this.zoomableNode.setAutoResizeToImage(true);
    this.containerNode.addChild(this.zoomableNode);

    this.draggable = Draggable.enableFor(this.zoomableNode);
    this.draggable.disable();

    this.constrain = Constrain.enableFor(this.zoomableNode);
    /*this.constrain.setMinX(-100.0f); //.setFillParent(true);
    this.constrain.setMaxX(0.0f); //.setFillParent(true);
    this.constrain.setMinY(-100.0f); //.setFillParent(true);
    this.constrain.setMaxY(0.0f); //.setFillParent(true);
    this.constrain.disable();*/
    this.constrain.setMaxScale(2.5f);
    this.constrain.setMinScale(0.5f);
    this.constrain.setFillParent(true);
    this.constrain.setCenterWhenFitting(true);

    /*
      PinchZoom only works when there are two active touches on a node
      min and max zoom/position are applied after release
    */
    this.pinchZoom = PinchZoom.enableFor(this.zoomableNode);
    // this.pinchZoom.disable();
    //this.pinchZoom.setRestore(true);
    //this.pinchZoom.setMaxScale(2.5f);
    //this.pinchZoom.setMinScale(0.5f);
    //this.pinchZoom.setFillParent(false);

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
        this.containerNode.setClipContent(!containerNode.isClippingContent());
        System.out.println("clipping: "+Boolean.toString(containerNode.isClippingContent()));
        return;
      }

      case 'r': {
        //this.pinchZoom.setRestore(!this.pinchZoom.getRestore());
        //System.out.println("restore: "+Boolean.toString(this.pinchZoom.getRestore()));
        zoomableNode.setScale(1.0f);
        zoomableNode.setPosition(0,0);
        return;
      }

      case 'g': {
        if(this.draggable.isEnabled()) this.draggable.disable(); else this.draggable.enable();
        System.out.println("draggable: "+Boolean.toString(this.draggable.isEnabled()));
        return;
      }

      case 'p': {
        if(this.pinchZoom.isEnabled()) this.pinchZoom.disable(); else this.pinchZoom.enable();
        System.out.println("pinchzoom: "+Boolean.toString(this.pinchZoom.isEnabled()));
        return;
      }

      case 'o': {
        if(this.constrain.isEnabled()) this.constrain.disable(); else this.constrain.enable();
        System.out.println("constrain: "+Boolean.toString(this.constrain.isEnabled()));
        return;
      }
    }
    switch (keyCode){
      case UP:
        this.pinchZoom.setSmoothValue(this.pinchZoom.getSmoothValue()+0.5f);
        System.out.println("smoothing: "+Float.toString(this.pinchZoom.getSmoothValue()));
        return;
      case DOWN:
        this.pinchZoom.setSmoothValue(this.pinchZoom.getSmoothValue()-0.5f);
        System.out.println("smoothing: "+Float.toString(this.pinchZoom.getSmoothValue()));
        return;
    }
  }
}
