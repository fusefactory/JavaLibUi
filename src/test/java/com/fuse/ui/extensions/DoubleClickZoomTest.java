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

public class DoubleClickZoomTest {
  @Test public void enable_test_disable(){
    Node scene = new Node();
    scene.setSize(800,600);
    Node subject = new Node();
    subject.setSize(100,100);
    scene.addChild(subject);
    subject.setScale(1.5f);

    // extension not applied yet; double click does nothing
    TouchGenerator.on(scene).doubleclick(120,120);
    assertEquals(subject.getScale(), new PVector(1.5f,1.5f,1));

    // apply extension
    DoubleClickZoom dcz = DoubleClickZoom.enableFor(subject);
    dcz.setSmoothValue(0.0f); // disable smoothing for instant transform for easier testing

    // double click zoom to 200% by default
    TouchGenerator.on(scene).doubleclick(120, 120);
    assertEquals(subject.getScale(), new PVector(3.0f,3.0f,1));

    // second double click returns to original scale
    TouchGenerator.on(scene).doubleclick(120,120);
    assertEquals(subject.getScale(), new PVector(1.5f,1.5f,1));

    // disable extension
    DoubleClickZoom.disableFor(subject);

    // extension no longer applied; double click does nothing
    TouchGenerator.on(scene).doubleclick(120,120);
    assertEquals(subject.getScale(), new PVector(1.5f,1.5f,1));
  }
}
