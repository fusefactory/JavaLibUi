package com.fuse.ui;

import java.util.function.Consumer;
/**
 * Custom Node type that extends the Node class with a
 * setDrawFunc and setUpdateFunc which allows the user to
 * use this node for any kind of custom behaviour.
 */
public class LambdaNode extends Node {

  private Consumer<Float> updateFunc = null;
  private Runnable drawFunc = null;

  public LambdaNode(){
  }

  public LambdaNode(String nodeName){
    super(nodeName);
  }

  /** Draw this node's image at this Node's position */
  @Override public void draw(){
    if(drawFunc!=null) drawFunc.run();
  }

  @Override public void update(float dt){
    super.update(dt);
    if(updateFunc!=null) updateFunc.accept(dt);
  }

  public void setDrawFunc(Runnable func){
    this.drawFunc = func;
  }

  public void setUpdateFunc(Consumer<Float> func){
    this.updateFunc = func;
  }
}
