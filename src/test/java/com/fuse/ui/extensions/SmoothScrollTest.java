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

public class SmoothScrollTest {
  @Test public void use(){
    // create scene
    Node scene = new Node();
    Node touchAreaNode = new Node();
    touchAreaNode.setPosition(10, 10);
    touchAreaNode.setSize(300, 100);
    scene.addChild(touchAreaNode);

    Node scrollableNode = new Node();
    touchAreaNode.addChild(scrollableNode);

    SmoothScroll extension = SmoothScroll.enableFor(touchAreaNode, scrollableNode);

    // touch down on global position 15,15 and drag 100 pixels to the RIGHT
    // divide the gesture up into 15 touch-events
    TouchGenerator.on(scene).from(15, 15).move(100, 0).moves(10).duration(0.2f).go();

    // moved 100 to right
    assertEquals(scrollableNode.getPosition(), new PVector(100, 0, 0));
    assertEquals(extension.getVelocity().x, 500, 0.111f);

    // same gesture
    TouchGenerator.on(scene).from(15, 15).move(100, 0).moves(10).duration(0.2f).go();

    // again moved 100 more to right
    assertEquals(scrollableNode.getPosition(), new PVector(200, 0, 0));
    assertEquals(extension.getVelocity().x, 500, 0.111f);

    // move 1 second "into the future"
    scene.updateSubtree(1.0f);
    // check damping movement
    assertEquals(scrollableNode.getPosition().x, 700.0f, 0.001f);
    assertEquals(extension.getVelocity().x, 450.0f, 0.111f);

    // disable smooth scroll
    SmoothScroll.disableFor(touchAreaNode, scrollableNode);

    // move 1 second "into the future"
    scene.updateSubtree(1.0f);
    // check damping movement disabled
    assertEquals(scrollableNode.getPosition().x, 700.0f, 0.001f);
    assertEquals(extension.getVelocity(), new PVector(0.0f, 0.0f, 0.0f));

    // same gesture once more
    TouchGenerator.on(scene).from(15, 15).move(100, 0).moves(10).duration(0.2f).go();

    // didn't move because was disabled
    assertEquals(scrollableNode.getPosition().x, 700.0f, 0.001f);
  }
}
