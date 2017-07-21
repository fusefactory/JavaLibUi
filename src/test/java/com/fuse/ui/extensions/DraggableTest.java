package com.fuse.ui.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Ignore;

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

  @Ignore @Test public void rotatedParent(){
    Node scene = new Node();
    Node rotator = new Node();
    rotator.rotate((float)Math.PI * 0.5f); // turned 90 degrees; our so when we move our subject node sideway, globally it's moving vertically
    //rotator.setPosition(100, 0);
    scene.addChild(rotator);
    Node subject = new Node();
    subject.setSize(100, 100); // default position: 0,0
    rotator.addChild(subject);

    Draggable.enableFor(subject);
    // drag from global position -5,5  200 pixels DOWN (start at -5 because the rotator rotated around 0,0, putting it outside the scree :/)
    // with 5 -eqaully spaced- touchMove events in between touchDown and touchUp
    TouchGenerator.on(scene).from(-5, 5).move(0, 200).moves(5).go();

    // in local coordinates the node got dragged 200 pixel RIGHT
    assertEquals(subject.getPosition(), new PVector(200, 0, 0));
  }
}
