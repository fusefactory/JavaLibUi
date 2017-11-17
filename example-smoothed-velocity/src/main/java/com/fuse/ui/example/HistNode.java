package com.fuse.ui.example;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import processing.core.PVector;
import com.fuse.ui.ShapeNode;

public class HistNode extends ShapeNode {
  private ConcurrentLinkedQueue<Float> queue;
  private float stepX = 1.0f;

  public HistNode(ConcurrentLinkedQueue<Float> queue){
    this.queue = queue;
  }

  @Override public void draw(){
    super.draw(); // configured stroke and fill color for our draw actions

    Iterator<Float> it = this.queue.iterator();

    for(float x = 0.0f; it.hasNext(); x+=this.stepX){
      float val = it.next();
      pg.rect(x, -val, stepX, val);
    }
  }
}
