package com.fuse.ui.example;

import java.util.logging.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import com.fuse.vlcplayer.VLCPlayer;

import com.fuse.ui.TouchManager;
import com.fuse.ui.Node;
import com.fuse.ui.MovieNode;



public class App extends PApplet {
  private final String RUNTIME_LIBS_PATH = "../modules/VLCPlayer/lib";

  Logger logger;
  private PApplet papplet;
  private PGraphics pg;
  private float timeBetweenFrames;
  private boolean bDrawDebug;

  private TouchManager touchManager;
  private Node sceneNode;
  private MovieNode movieNode;

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
    String existing = System.getProperty("java.library.path");
    String updated = existing + ":" + RUNTIME_LIBS_PATH;
    System.out.println("Updating System property 'java.library.path' from "+existing+" to "+updated);
    System.setProperty("java.library.path", updated);

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
      MovieNode n = new MovieNode();
      n.setPosition(0, 0);
      n.setSize(sceneNode.getSize());
      n.setAutoStart(true);
      sceneNode.addChild(n);

      // save to attach video later
      this.movieNode = n;

      // TextNode tx = new TextNode();
      // tx.setText("RectNode");
      // tx.setSize(100,100);
      // tx.setTextSize(14);
      // tx.setTextColor(pg.color(0,0,0));
      // n.addChild(tx);
    }

    // load movie
    VLCPlayer player = new VLCPlayer(dataPath("vid.mp4"));
    player = new VLCPlayer(dataPath("vid.mp4"));
    player = new VLCPlayer(dataPath("vid.mp4"));
    player = new VLCPlayer(dataPath("vid.mp4"));
    this.movieNode.setMovie(player, (PApplet)this);
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
