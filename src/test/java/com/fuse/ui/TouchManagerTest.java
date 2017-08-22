package com.fuse.ui;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import processing.core.*;

/**
 * Unit test for com.fuse.utils.Event.
 */
public class TouchManagerTest {
  private List<TouchEvent> touchEvents;
  private List<String> strings;

  @Test public void touchEvent()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    TouchManager man = new TouchManager();
    touchEvents.clear();

    // trigger touch, nothing happens
    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(1,0,0));
    assertEquals(touchEvents.size(), 0);

    // register touch event listener
    man.touchEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(100,50,0)); // some distance from touch down event to avoid click
    assertEquals(touchEvents.size(), 3);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_DOWN);
    assertEquals(touchEvents.get(1).eventType, TouchEvent.EventType.TOUCH_MOVE);
    assertEquals(touchEvents.get(2).eventType, TouchEvent.EventType.TOUCH_UP);
    assertEquals(touchEvents.get(0).position, new PVector(0,0,0));
    assertEquals(touchEvents.get(1).position, new PVector(1,0,0));
    assertEquals(touchEvents.get(2).position, new PVector(100,50,0));
  }

  @Test public void touchDownEvent()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    TouchManager man = new TouchManager();
    touchEvents.clear();

    // trigger touch, nothing happens
    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(1,0,0));
    assertEquals(touchEvents.size(), 0);

    // register touch event listener
    man.touchDownEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(1,0,0));
    assertEquals(touchEvents.size(), 1);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_DOWN);
    assertEquals(touchEvents.get(0).position, new PVector(0,0,0));
    assertEquals(touchEvents.get(0).velocity, null);
  }

  @Test public void touchUpEvent()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();


    TouchManager man = new TouchManager();
    touchEvents.clear();

    // trigger touch, nothing happens
    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(1,0,0));
    assertEquals(touchEvents.size(), 0);

    // register touch event listener
    man.touchUpEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(2,0,0));
    assertEquals(touchEvents.size(), 1);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_UP);
    assertEquals(touchEvents.get(0).position, new PVector(2,0,0));
    assertEquals(touchEvents.get(0).velocity, new PVector(1000000, 0, 0)); // these touch events weren't performed in human speed but in computer speed, 9999 is the limit
  }

  @Test public void touchMoveEvent()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    TouchManager man = new TouchManager();
    touchEvents.clear();

    // trigger touch, nothing happens
    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(1,0,0));
    assertEquals(touchEvents.size(), 0);

    // register touch event listener
    man.touchMoveEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(0,0,0));
    man.touchMove(0, new PVector(1,0,0));
    man.touchUp(0, new PVector(2,0,0));
    assertEquals(touchEvents.size(), 1);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_MOVE);
    assertEquals(touchEvents.get(0).position, new PVector(1,0,0));
    assertEquals(touchEvents.get(0).velocity, new PVector(1000000, 0, 0)); // these touch events weren't performed in human speed but in computer speed, 9999 is the limit
  }

  @Test public void NodeTouchEvent()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    Node sceneNode = new Node();
    sceneNode.setPosition(100,100);
    sceneNode.setSize(100,100);
    TouchManager man = new TouchManager();
    man.setNode(sceneNode);
    touchEvents.clear();

    sceneNode.touchEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(1, new PVector(10, 10, 0));
    assertEquals(touchEvents.size(), 0);
    man.touchDown(1, new PVector(110, 111, 0));
    assertEquals(touchEvents.size(), 1);
    assertEquals(touchEvents.get(0).touchId, 1);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_DOWN);
    assertEquals(touchEvents.get(0).position, new PVector(110, 111, 0));
    assertEquals(touchEvents.get(0).node, sceneNode);

    // add child (on top of) sceneNode
    Node childNode = new Node();
    childNode.setPosition(new PVector(20, 20, 0));
    childNode.setSize(new PVector(20, 20, 0));
    childNode.touchEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });
    sceneNode.addChild(childNode);
    man.touchDown(2, new PVector(130, 135, 0));
    assertEquals(touchEvents.size(), 2);
    assertEquals(touchEvents.get(1).touchId, 2);
    assertEquals(touchEvents.get(1).position, new PVector(130, 135, 0));
    assertEquals(touchEvents.get(1).node, childNode);

    man.touchDown(3, new PVector(130, 145, 0));
    assertEquals(touchEvents.size(), 3);
    assertEquals(touchEvents.get(2).touchId, 3);
    assertEquals(touchEvents.get(2).node, sceneNode);
  }

  @Test public void dispatchOnUpdate()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    TouchManager man = new TouchManager();
    man.setDispatchOnUpdate(true);
    touchEvents.clear();

    man.touchEvent.addListener((TouchEvent e) -> {
      touchEvents.add(e.copy());
    });

    assertEquals(touchEvents.size(), 0);
    man.touchDown(0, new PVector(10, 10, 0));
    man.touchMove(0, new PVector(12, 10, 0));
    man.touchUp(0, new PVector(12, 80, 0));
    assertEquals(touchEvents.size(), 0);
    man.update();
    assertEquals(touchEvents.size(), 3);
    assertEquals(touchEvents.get(0).touchId, 0);
    assertEquals(touchEvents.get(0).eventType, TouchEvent.EventType.TOUCH_DOWN);
    assertEquals(touchEvents.get(0).position, new PVector(10, 10, 0));
    assertEquals(touchEvents.get(0).startPosition, new PVector(10, 10, 0));
    assertEquals(touchEvents.get(1).touchId, 0);
    assertEquals(touchEvents.get(1).eventType, TouchEvent.EventType.TOUCH_MOVE);
    assertEquals(touchEvents.get(1).position, new PVector(12, 10, 0));
    assertEquals(touchEvents.get(1).startPosition, new PVector(10, 10, 0));
    assertEquals(touchEvents.get(2).touchId, 0);
    assertEquals(touchEvents.get(2).eventType, TouchEvent.EventType.TOUCH_UP);
    assertEquals(touchEvents.get(2).position, new PVector(12, 80, 0));
    assertEquals(touchEvents.get(2).startPosition, new PVector(10, 10, 0));
  }

  @Test public void touchEnterExit()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    Node sceneNode = new Node();
    sceneNode.setSize(100,100);
    TouchManager man = new TouchManager();
    man.setNode(sceneNode);
    touchEvents.clear();

    Node c1 = new Node();
    c1.setPosition(10,10);
    c1.setSize(10,10);
    c1.setName("c1");
    sceneNode.addChild(c1);
    Node c2 = new Node();
    c2.setPosition(20,10);
    c2.setSize(100,100);
    c2.setName("c2");
    sceneNode.addChild(c2);

    c1.touchEvent.addListener((TouchEvent e) -> {
      strings.add("c1: "+e.toString());
    });
    c2.touchEvent.addListener((TouchEvent e) -> {
      strings.add("c2: "+e.toString());
    });

    man.touchDown(0, new PVector(11f,11f,0f));
    man.touchMove(0, new PVector(18f,11f,0f));
    man.touchMove(0, new PVector(25f,11f,0f));
    man.touchUp(0, new PVector(50f,11f,0f));

    assertEquals(strings.size(), 8);
    assertEquals(strings.get(0), "c1: #0 DOWN on c1 at position: 11.0, 11.0");
    assertEquals(strings.get(1), "c1: #0 MOVE on c1 at position: 18.0, 11.0");
    assertEquals(strings.get(2), "c1: #0 EXIT on c2 at position: 25.0, 11.0");
    assertEquals(strings.get(3), "c2: #0 ENTER on c2 at position: 25.0, 11.0");
    assertEquals(strings.get(4), "c1: #0 MOVE on c2 at position: 25.0, 11.0");
    assertEquals(strings.get(5), "c2: #0 MOVE on c2 at position: 25.0, 11.0");
    assertEquals(strings.get(6), "c1: #0 UP on c2 at position: 50.0, 11.0");
    assertEquals(strings.get(7), "c2: #0 UP on c2 at position: 50.0, 11.0");
  }

  @Test public void click()
  {
    touchEvents = new ArrayList<TouchEvent>();
    strings = new ArrayList<String>();

    TouchManager man = new TouchManager();
    man.touchClickEvent.addListener((TouchEvent e) -> {
      strings.add(e.toString());
    });

    man.update(0); // call update with delta-time value to enable "controlledTime" (TODO: explicit enable/disable of controlled time)
    strings.clear();

    // touch too long for a click
    man.touchDown(0, new PVector(10f, 10f, 0f));
    man.update(5); // move 5 seconds into the future
    man.touchUp(0, new PVector(10f, 10f, 0f));

    // click
    assertEquals(strings.size(), 0);
    man.touchDown(0, new PVector(10f, 10f, 0f));
    man.update(0.2f); // move 0.2 seconds into the future
    man.touchUp(0, new PVector(10f, 10f, 0f));

    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "#0 CLICK on <NO NODE> at position: 10.0, 10.0");

    // change max interval
    strings.clear();
    man.setClickMaxInterval(0.05f);
    man.touchDown(0, new PVector(10f, 10f, 0f));
    man.update(0.2f); // move 0.2 seconds into the future
    man.touchUp(0, new PVector(10f, 10f, 0f));
    assertEquals(strings.size(), 0);

    man.touchDown(4, new PVector(10f, 10f, 0f));
    man.update(0.045f); // move 0.2 seconds into the future
    man.touchUp(4, new PVector(10f, 10f, 0f));
    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "#4 CLICK on <NO NODE> at position: 10.0, 10.0");

    // change max distance
    strings.clear();
    man.setClickMaxDistance(5);
    man.touchDown(0, new PVector(10f, 10f, 0f));
    man.update(0.02f);
    man.touchUp(0, new PVector(16f, 10f, 0f));
    assertEquals(strings.size(), 0);

    man.touchDown(0, new PVector(10f, 10f, 0f));
    man.update(0.02f);
    man.touchUp(0, new PVector(14f, 10f, 0f));
    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "#0 CLICK on <NO NODE> at position: 14.0, 10.0");
  }

  @Test public void not_interactive_node(){
    strings = new ArrayList<>();

    Node n1 = new Node();
    n1.setPosition(10, 10);
    n1.setSize(100, 100);
    n1.whenClicked(() -> strings.add("n1 clicked"));

    TouchManager man = new TouchManager();
    man.setNode(n1);
    assertEquals(strings.size(), 0);

    // click
    man.touchDown(0, new PVector(20,20));
    man.touchUp(0, new PVector(20,20));
    // verify
    assertEquals(strings.size(), 1);
    assertEquals(strings.get(0), "n1 clicked");

    // click 2nd time
    man.touchDown(0, new PVector(20,20));
    man.touchUp(0, new PVector(20,20));
    // verify
    assertEquals(strings.size(), 2);
    assertEquals(strings.get(1), "n1 clicked");

    // disable
    n1.setInteractive(false);
    // click 3rd time
    man.touchDown(0, new PVector(20,20));
    man.touchUp(0, new PVector(20,20));
    // verify ignored
    assertEquals(strings.size(), 2);
  }

  @Test public void click_on_clipped_node(){
    // setup scene
    Node scene = new Node();
    Node clipper = new Node();
    clipper.setPosition(100, 100); // global position: 100, 100
    clipper.setSize(200, 100);  // global bottom right: 300, 200
    scene.addChild(clipper);
    Node subject = new Node();
    subject.setPosition(10, 10); // global position: 110,110
    subject.setSize(300,50);   // global bottom right:  410, 160
    clipper.addChild(subject);

    // init TouchManager
    TouchManager man = new TouchManager();
    man.setNode(scene);

    // enable history on subject's touchDownEvent for easy assertions
    subject.touchDownEvent.enableHistory();
    assertEquals(subject.touchDownEvent.getHistory().size(), 0);

    // clipper not yet clipping; click outside clipper but on subject position
    man.touchDown(0, new PVector(350, 150));
    assertEquals(subject.touchDownEvent.getHistory().size(), 1); // touch event triggered

    // enable clipping and try again
    clipper.setClipContent(true);
    man.touchDown(0, new PVector(350, 150));
    assertEquals(subject.touchDownEvent.getHistory().size(), 1); // touch event NOT triggered

    // with clipping enabled try INSIDE clipper
    man.touchDown(0, new PVector(150, 150));
    assertEquals(subject.touchDownEvent.getHistory().size(), 2); // touch event triggered

    // disable clipping and try outside clipper again
    clipper.setClipContent(false);
    man.touchDown(0, new PVector(350, 150));
    assertEquals(subject.touchDownEvent.getHistory().size(), 3); // touch event triggered

  }

  @Test public void setMirrorNodeEventsEnabled(){
    Node n = new Node();
    n.setSize(100, 100);
    n.touchMoveEvent.enableHistory();

    TouchManager man = new TouchManager(n);
    man.touchDown(0, new PVector(9, 9, 0));
    man.touchMove(0, new PVector(8, 8, 0));

    assertEquals(n.touchMoveEvent.getHistory().size(), 1);
    assertEquals(n.touchMoveEvent.getHistory().get(0).touchId, 0);

    man.setMirrorNodeEventsEnabled(true);
    man.touchDown(1, new PVector(9, 9, 0)); // needed for the move events to be "accepted"
    man.touchMove(1, new PVector(9, 9, 0));
    man.touchMove(1, new PVector(7, 7, 0));

    assertEquals(n.touchMoveEvent.getHistory().size(), 5); // 4 new events triggered, not 2 new
    assertEquals(n.touchMoveEvent.getHistory().get(1).touchId, 1);
    assertEquals(n.touchMoveEvent.getHistory().get(2).touchId, 2);
    assertEquals(n.touchMoveEvent.getHistory().get(3).touchId, 1);
    assertEquals(n.touchMoveEvent.getHistory().get(3).position, new PVector(7, 7, 0));
    assertEquals(n.touchMoveEvent.getHistory().get(4).touchId, 2);
    assertEquals(n.touchMoveEvent.getHistory().get(4).position, new PVector(21, 21, 0));
  }


}
