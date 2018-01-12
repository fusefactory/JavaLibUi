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

public class SwiperTest {
  @Ignore @Test public void testSwiper(){
    assertEquals("TODO", "test, dragging, snapping, step-offsets, etc.");
  }

  @Test public void setSnapPosition(){
    Node areaNode = new Node();
    Node scrollerNode = new Node();
    Swiper s = Swiper.enableFor(areaNode, scrollerNode);

    s.setSnapPosition(new PVector(-100, 0, 0));
    assertEquals(scrollerNode.getPosition(), new PVector(0, 0, 0));

    s.update(0.1f);
    assertEquals(scrollerNode.getPosition(), new PVector(-10.0f, 0, 0));

    for(int i=0; i<100; i+=1) {
      s.update(0.1f);
    }

    assertEquals(scrollerNode.getPosition(), new PVector(-100, 0, 0));
  }

  @Test public void setSnapPosition_with_supplier_arg(){
    Node areaNode = new Node();
    Swiper s = Swiper.enableFor(areaNode);

    Event<Integer> dummy = new Event<>();
    dummy.enableHistory();

    s.setSnapPosition(() -> {
      dummy.trigger(0);
      return new PVector(-100.0f - dummy.getHistory().size()*0.5f, 0.0f, 0.0f);
    });

    for(int i=0; i<1000; i+=1)
      s.update(0.1f);

    assertEquals(s.getScrollableNode().getPosition(), new PVector(-595.49994f, 0, 0));
  }

  @Test public void setSnapPosition_with_supplier_arg_interrupted(){
    Node areaNode = new Node();
    Swiper s = Swiper.enableFor(areaNode);

    Event<Integer> dummy = new Event<>();
    dummy.enableHistory();

    s.setSnapPosition(() -> {
      dummy.trigger(0);
      return new PVector(-100.0f - dummy.getHistory().size()*0.5f, 0.0f, 0.0f);
    });

    for(int i=0; i<300; i+=1)
      s.update(0.1f);

    s.setSnapPosition(10, 0);

    for(int i=0; i<700; i+=1)
      s.update(0.1f);

    assertEquals(s.getScrollableNode().getPosition(), new PVector(10, 0, 0));
  }
}
