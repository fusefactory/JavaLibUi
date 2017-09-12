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
    Draggable d = new Draggable();
    n.use(d);
    d.disableSmoothing();

    // try same interaction, verify node moves
    tm.touchDown(0, new PVector(20,20,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(20,20,0));
    tm.touchMove(0, new PVector(50,50,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(40,40,0));
    tm.touchUp(0, new PVector(55,55,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(40,40,0)); // touch up is ignored
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
    Draggable d = Draggable.enableFor(n);
    d.disableSmoothing();

    // try interaction, verify node moves
    tm.touchDown(0, new PVector(20,20,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(20,20,0));
    tm.touchMove(0, new PVector(50,50,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(40,40,0));
    tm.touchUp(0, new PVector(55,55,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(40,40,0));

    // disable draggable on node and reset to original position
    Draggable.disableFor(n);
    n.setPosition(10,10);

    // try same interaction,verify node doesn't move
    tm.touchDown(0, new PVector(20,20,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(30,30,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(50,50,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchUp(0, new PVector(55,55,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
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
    d.disableSmoothing(); // smoothing causes less easy to predict/pre-calculate values

    assertEquals(d.getOffset(), new PVector(0.0f, 0.0f, 0.0f));
    tm.touchDown(0, new PVector(20,20,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(d.getOffset(), new PVector(0.0f, 0.0f, 0.0f));
    tm.touchMove(0, new PVector(30,30,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
    assertEquals(d.getOffset(), new PVector(10.0f, 10.0f, 0.0f));
    tm.touchMove(0, new PVector(15,25,0));
    d.update(0.0f); // no smoothing, so amount delta-time value doesn't matter
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
    assertEquals(d.startEvent.getHistory().size(), 1);
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
    PVector originalPosition = n.getPosition().get();
    Draggable.enableFor(n);

    // drag horizontally (mostly) to the right
    // starting left (outside) from n
    // dragging OVER n
    // using 5 intermediate "move" events, between the down and the up events
    TouchGenerator.on(scene).from(5, 5).move(200, 10).moves(5).go();

    // didn't move
    assertEquals(n.getPosition(), originalPosition);
  }

  @Test public void rotatedParent(){
    Node scene = new Node();
    Node rotator = new Node();
      rotator.rotate((float)Math.PI); // turned 180 degrees;
      rotator.setPosition(100,100);
      //rotator.setPosition(100, 0);
      scene.addChild(rotator);
    Node subject = new Node();
      subject.setSize(100, 100); // default position: 0,0
      rotator.addChild(subject);


    Draggable.enableFor(subject).disableSmoothing();

    assertEquals(subject.getPosition(), new PVector(0, 0, 0));
    // drag from global position 10,10 and move 100 pixels to the right and 20 pixels down
    // in 5 -eqaully spaced- touchMove events after the touchDown (we're not sending a touch up)
    TouchGenerator.on(scene)
      .from(10, 10).move(100, 20)
      .moves(5).updateSceneAfterEveryTouch(0.0f).noUp()
      .go();

    // in local coordinates the node got dragged 200 pixel RIGHT
    assertEquals(subject.getPosition().x, -100.0f, 0.0001f);
    assertEquals(subject.getPosition().y, -20.0f, 0.0001f);
  }
}
