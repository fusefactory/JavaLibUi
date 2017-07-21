package com.fuse.ui.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import processing.core.PVector;
import com.fuse.ui.Node;
import com.fuse.ui.TouchManager;
import com.fuse.ui.TouchGenerator;

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
    assertEquals(Draggable.enableFor(n).getClass(), Draggable.class);

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

  @Test public void getOffset(){
    // create scene
    Node scene = new Node();
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // create touch manager that acts on this scene
    TouchManager tm = new TouchManager(scene);
    // enable draggable on node
    Draggable d = Draggable.enableFor(n);

    assertEquals(d.getOffset(), new PVector(0.0f, 0.0f, 0.0f));
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(d.getOffset(), new PVector(0.0f, 0.0f, 0.0f));
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(d.getOffset(), new PVector(10.0f, 10.0f, 0.0f));
    tm.touchMove(0, new PVector(15,25,0));
    assertEquals(d.getOffset(), new PVector(-5.0f, 5.0f, 0.0f));
  }

  @Test public void events(){
    // create scene
    Node scene = new Node();
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // create touch manager that acts on this scene
    TouchManager tm = new TouchManager(scene);
    // enable draggable on node
    Draggable d = Draggable.enableFor(n);

    d.startEvent.enableHistory();
    d.endEvent.enableHistory();

    assertEquals(d.startEvent.getHistory().size(), 0);
    assertEquals(d.endEvent.getHistory().size(), 0);

    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(d.startEvent.getHistory().size(), 0);
    assertEquals(d.endEvent.getHistory().size(), 0);
    tm.touchMove(0, new PVector(30,30,0));
    assertEquals(d.startEvent.getHistory().size(), 1);
    assertEquals(d.endEvent.getHistory().size(), 0);
    tm.touchMove(0, new PVector(50,30,0));
    assertEquals(d.startEvent.getHistory().size(), 1);
    assertEquals(d.endEvent.getHistory().size(), 0);
    tm.touchUp(0, new PVector(15,25,0));
    assertEquals(d.startEvent.getHistory().size(), 1);
    assertEquals(d.endEvent.getHistory().size(), 1);
  }

  @Test public void only_when_touch_starts_on_node(){
    // create scene
    Node scene = new Node();
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // redundant copy for clarity
    PVector originalPosition = n.getPosition().copy();
    Draggable.enableFor(n);

    // drag horizontally (mostly) to the right
    // starting left (outside) from n
    // dragging OVER n
    // using 5 intermediate "move" events, between the down and the up events
    TouchGenerator.on(scene).from(5, 5).move(200, 10).moves(5).go();

    // didn't move
    assertEquals(n.getPosition(), originalPosition);
  }
}
