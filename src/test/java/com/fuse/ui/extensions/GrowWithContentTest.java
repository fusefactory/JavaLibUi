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

public class GrowWithContentTest {
  @Test public void test(){
    Node n = new Node();
    // default size
    assertEquals(n.getSize(), new PVector(0, 0, 0));
    // explicit size
    n.setSize(100, 100);
    assertEquals(n.getSize(), new PVector(100, 100, 0));

    Node child = new Node();
    child.setPosition(10,10);
    child.setSize(300,200);
    n.addChild(child);
    // does NOT automatically grow with content
    assertEquals(n.getSize(), new PVector(100, 100, 0));

    GrowWithContent ext = GrowWithContent.enableFor(n);
    assertEquals(n.getSize(), new PVector(310, 210, 0));
    // child repositions
    child.setPosition(20,20);
    assertEquals(n.getSize(), new PVector(320, 220, 0));
    // child resizes
    child.setSize(400,300);
    assertEquals(n.getSize(), new PVector(420, 320, 0));
    // child scales
    child.setScale(2);
    assertEquals(n.getSize(), new PVector(820, 620, 0));
    // grand-children are not considered
    Node baby = new Node();
    baby.setPosition(5000,5000).setSize(3000,3000);
    assertEquals(n.getSize(), new PVector(820, 620, 0));
  }
}
