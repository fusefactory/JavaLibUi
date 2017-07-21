package com.fuse.ui;

import processing.core.PVector;

public class TouchGenerator {

  private Node rootNode = null;
  private PVector fromPos = null;
  private PVector toPos = null;
  private PVector delta = null;
  private int moveCount = 0;

  public static TouchGenerator on(Node rootNode){
    TouchGenerator gen = new TouchGenerator();
    gen.rootNode = rootNode;
    return gen;
  }

  public TouchGenerator from(float x, float y){ return from(new PVector(x,y,0.0f)); }
  public TouchGenerator from(PVector pos){
    this.fromPos = pos;
    return this;
  }

  public TouchGenerator to(float x, float y){ return to(new PVector(x,y,0.0f)); }
  public TouchGenerator to(PVector pos){
    this.toPos = pos;
    return this;
  }

  public TouchGenerator move(float deltaX, float deltaY){ return move(new PVector(deltaX, deltaY, 0)); }
  public TouchGenerator move(PVector delta){
    this.delta = delta;
    return this;
  }

  public TouchGenerator moves(int amount){
    this.moveCount = amount;
    return this;
  }

  public void go(){
    TouchManager touchManager = new TouchManager();
    touchManager.setNode(rootNode);

    if(fromPos == null)
      fromPos = new PVector(0,0,0);

    if(toPos == null){
      if(delta != null && fromPos != null)
        toPos = fromPos.copy().add(delta);
    }

    touchManager.touchDown(0, fromPos);

    if(moveCount > 0){
      PVector stepDelta = toPos.copy().sub(fromPos).div(moveCount+1);
      PVector currentPos = fromPos.copy();

      for(int i=0; i<moveCount; i++){
        touchManager.touchMove(0, currentPos.add(stepDelta));
      }
    }

    touchManager.touchUp(0, toPos);
  }
}
