package com.fuse.ui.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

import processing.core.PVector;
import com.fuse.ui.Node;
import com.fuse.ui.TouchManager;

public class ConstrainTest {
  @Test public void allAxisByDefault(){
    // create node
    Node n = new Node();
    // move and verify moved
    n.setPosition(10, 20);
    assertEquals(n.getPosition(), new PVector(10,20,0));
    // move and verify moved
    n.setPosition(20, 20);
    assertEquals(n.getPosition(), new PVector(20,20,0));
    // apply constrain (all axis by default) and verify it won't move anymore
    Constrain c = Constrain.enableFor(n, true);
    n.setPosition(30, 30);
    assertEquals(n.getPosition(), new PVector(20,20,0));
    // disable constrain and verify moving works again
    Constrain.disableFor(n);
    n.setPosition(12, 34);
    assertEquals(n.getPosition(), new PVector(12,34,0));
  }

  @Test public void offByDefault(){
    // create node
    Node n = new Node();
    // move and verify moved
    n.setPosition(10, 20);
    assertEquals(n.getPosition(), new PVector(10,20,0));
    // move and verify moved
    n.setPosition(20, 20);
    assertEquals(n.getPosition(), new PVector(20,20,0));
    // apply constrain (no axis by default) and verify it won't move anymore
    Constrain c = Constrain.enableFor(n);
    n.setPosition(30, 30);
    assertEquals(n.getPosition(), new PVector(30,30,0));

    c.setFixX();
    n.setPosition(40, 40);
    assertEquals(n.getPosition(), new PVector(30,40,0));
    c.setFixY();
    n.setPosition(50, 50, 50);
    assertEquals(n.getPosition(), new PVector(30,40,50));
    c.setFixX(false);
    c.setFixZ();
    n.setPosition(60, 60, 60);
    assertEquals(n.getPosition(), new PVector(60,40,50));
    // disable constrain and verify moving works again
    Constrain.disableFor(n);
    n.setPosition(12, 34, 0);
    assertEquals(n.getPosition(), new PVector(12,34,0));
  }

  @Test public void setMinX(){
    // create node and constrain extension
    Node n = new Node();
    Constrain c = Constrain.enableFor(n, false);
    // move and verify moved
    n.setPosition(10f, 10f);
    assertEquals(n.getPosition(), new PVector(10,10,0));
    c.setMinX(20.0f);
    assertEquals(n.getPosition(), new PVector(20,10,0));
    n.setPosition(10.0f, -10.0f);
    assertEquals(n.getPosition(), new PVector(20,-10,0));
    c.setMinX(-20.0f);
    n.setPosition(-100f, -1000f);
    assertEquals(n.getPosition(), new PVector(-20,-1000,0));
    c.setMinX(null);
    n.setPosition(-1000f, -2000f);
    assertEquals(n.getPosition(), new PVector(-1000,-2000,0));
  }

  @Test public void xPercentageEvent(){
    Node n = new Node();
    Constrain c = Constrain.enableFor(n, false);
    List<Float> floats = new ArrayList<>();
    c.xPercentageEvent.addListener((Float xperc) -> floats.add(xperc));

    n.setPosition(20, 0);
    assertEquals(floats.size(), 0);
    c.setMaxX(100f);
    assertEquals(floats.size(), 0);
    c.setMinX(50f); // now we have a min and a max on the X axi which means it will start triggering the xPercentageEvent
    assertEquals(floats.size(), 1);
    assertEquals((float)floats.get(0), 0.0f, 0.00001f);
    n.setPosition(75, 20);
    assertEquals(floats.size(), 2);
    assertEquals((float)floats.get(1), 0.5f, 0.00001f); // 50%
    n.setPosition(60, 20);
    assertEquals(floats.size(), 3);
    assertEquals((float)floats.get(2), 0.2f, 0.00001f); // 20%
  }

  @Test public void setPercentageX(){
    Node n = new Node();
    Constrain c = Constrain.enableFor(n, false);
    c.setMinX(20f);
    c.setMaxX(400f);
    c.setPercentageX(0.0f);
    assertEquals(n.getPosition(), new PVector(20,0,0));
    c.setPercentageX(0.9f); // 90%
    assertEquals(n.getPosition(), new PVector(362,0,0));
  }

  @Test public void setFillParent(){
    Node parent = new Node();
    parent.setSize(100, 100);

    Node child = new Node();
    child.setSize(200, 200);
    parent.addChild(child);

    Constrain c = Constrain.enableFor(child);
    c.setFillParent(true);

    // test horizontal restriction
    child.setX(10);
    assertEquals(child.getPosition(), new PVector(0,0,0));
    child.setX(-10);
    assertEquals(child.getPosition(), new PVector(-10,0,0));
    child.setX(-100);
    assertEquals(child.getPosition(), new PVector(-100,0,0));
    child.setX(-110);
    assertEquals(child.getPosition(), new PVector(-100,0,0));

    // test vertical restriction
    child.setY(10);
    assertEquals(child.getPosition(), new PVector(-100,0,0));
    child.setY(-10);
    assertEquals(child.getPosition(), new PVector(-100,-10,0));
    child.setY(-100);
    assertEquals(child.getPosition(), new PVector(-100,-100,0));
    child.setY(-110);
    assertEquals(child.getPosition(), new PVector(-100,-100,0));

    // if child is smaller than parent it can't fill the parent;
    // constrain won't work on axis where the child is smaller

    // make child's height smaller than parent height; no y-axis constrain
    child.setSize(150, 50);
    child.setPosition(300, 300);
    assertEquals(child.getPosition(), new PVector(0,300,0));
    // also make width smaller; no x-axis constrain
    child.setSize(50, 50);
    child.setPosition(-500, -500);
    assertEquals(child.getPosition(), new PVector(-500,-500,0));

    // make child big than parent again; constrain becomes active again
    child.setSize(150, 150);
    assertEquals(child.getPosition(), new PVector(-50,-50,0));
  }
}
