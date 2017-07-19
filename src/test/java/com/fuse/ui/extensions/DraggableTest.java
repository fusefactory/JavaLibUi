package com.fuse.ui.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import processing.core.PVector;
import com.fuse.ui.Node;
import com.fuse.ui.TouchManager;

public class DraggableTest {
  @Test public void use(){
    // create scene
    Node scene = new Node();
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // create touch manager that acts on this scene
    TouchManager tm = new TouchManager(scene);

    // try to interact with node (no draggable extension added yet), verify it doesn't move
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(50,50,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchUp(0, new PVector(55,55,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));

    // add draggable extension to Node
    n.use(new Draggable());

    // try same interaction, verify node moves
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(n.getPosition(), new PVector(20,20,0));
    tm.touchMove(0, new PVector(50,50,0));
    assertEquals(n.getPosition(), new PVector(40,40,0));
    tm.touchUp(0, new PVector(55,55,0));
    assertEquals(n.getPosition(), new PVector(45,45,0));
  }

  @Test public void enable_disable(){
    // create scene
    Node scene = new Node();
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // create touch manager that acts on this scene
    TouchManager tm = new TouchManager(scene);
    // enable draggable on node
    Draggable.enableFor(n);

    // try interaction, verify node moves
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(n.getPosition(), new PVector(20,20,0));
    tm.touchMove(0, new PVector(50,50,0));
    assertEquals(n.getPosition(), new PVector(40,40,0));
    tm.touchUp(0, new PVector(55,55,0));
    assertEquals(n.getPosition(), new PVector(45,45,0));

    // disable draggable on node and reset to original position
    Draggable.disableFor(n);
    n.setPosition(10,10);

    // try same interaction,verify node doesn't move
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(50,50,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchUp(0, new PVector(55,55,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
  }
}
