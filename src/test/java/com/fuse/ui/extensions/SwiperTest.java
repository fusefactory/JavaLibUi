package com.fuse.ui.extensions;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Ignore;

import processing.core.PVector;
import com.fuse.utils.Event;
import com.fuse.ui.Node;
import com.fuse.ui.TouchManager;
import com.fuse.ui.TouchEvent;

public class SwiperTest extends ExtensionBase {
  /*@Ignore @Test public void swipeLeft(){
    // create scene
    Node scene = new Node();
    scene.setSize(300, 200);
    Node n = new Node();
    n.setPosition(10, 10);
    n.setSize(100, 100);
    scene.addChild(n);

    // create touch manager that acts on this scene
    TouchManager tm = new TouchManager(scene);
    Swiper s = Swiper.enableFor(n);
    assertEquals(s.getMaxOffsetLeft(), -100.0f, 0.000001);
    s.setMaxOffsetLeft(-150.0f);
    s.swipeLeftEvent.enableHistory();

    // try to interact with node (no draggable extension added yet), verify it doesn't move
    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(-90,30,0));
    assertEquals(n.getPosition(), new PVector(-100,10,0));

    assertFalse(s.willSwipeLeft());
    assertFalse(s.willSwipe());
    assertFalse(s.willSwipeRight());
    assertEquals(s.isSnapBackEnabled(), false);
    s.setSnapBackEnabled(true);

    tm.touchUp(0, new PVector(-80,55,0));
    assertEquals(s.swipeLeftEvent.getHistory().size(), 0);
    assertEquals(n.getPosition(), new PVector(10, 10, 0));

    tm.touchDown(0, new PVector(20,20,0));
    assertEquals(n.getPosition(), new PVector(10,10,0));
    tm.touchMove(0, new PVector(-200,20,0));
    assertTrue(s.willSwipeLeft());
    assertTrue(s.willSwipe());
    assertFalse(s.willSwipeRight());

    assertEquals(s.isInstantSwipeEnabled(), false);
    s.setInstantSwipeEnabled(true);

    assertEquals(s.swipeLeftEvent.getHistory().size(), 0);
    assertTrue(s.willSwipeLeft());
    tm.touchUp(0, new PVector(-190,20,0));
    assertEquals(s.swipeLeftEvent.getHistory().size(), 1);
    assertEquals(n.getPosition(), new PVector(-290,10,0));
  }*/
}
