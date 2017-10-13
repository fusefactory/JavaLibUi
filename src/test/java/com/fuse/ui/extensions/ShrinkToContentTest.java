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

public class ShrinkToContentTest {
  @Test public void test(){
    Node n = new Node();
    // default size
    assertEquals(n.getSize(), new PVector(0, 0, 0));
    // explicit size
    n.setSize(1000, 1000);
    assertEquals(n.getSize(), new PVector(1000, 1000, 0));

    Node child = new Node();
    child.setPosition(10,10);
    child.setSize(300,200);
    n.addChild(child);
    // does NOT automatically shrink to content
    assertEquals(n.getSize(), new PVector(1000, 1000, 0));

    ShrinkToContent ext = ShrinkToContent.enableFor(n);
    assertEquals(n.getSize(), new PVector(310, 210, 0));
    // child repositions
    child.setPosition(20,20);
    assertEquals(n.getSize(), new PVector(310, 210, 0));
    // child resizes
    child.setSize(50,60);
    assertEquals(n.getSize(), new PVector(70, 80, 0));
    // child scales
    child.setScale(0.5f);
    assertEquals(n.getSize(), new PVector(45, 50, 0));

    // now n should stop adepting its size to child
    n.removeChild(child);
    child.setPosition(5,5);
    assertEquals(n.getSize(), new PVector(45, 50, 0));
    assertEquals(child.positionChangeEvent.size(), 0);
    assertEquals(child.sizeChangeEvent.size(), 0);
    assertEquals(child.scaleChangeEvent.size(), 0);
  }
}
