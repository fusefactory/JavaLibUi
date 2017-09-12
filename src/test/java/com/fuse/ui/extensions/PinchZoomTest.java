package com.fuse.ui.extensions;

import processing.core.PVector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Ignore;

import com.fuse.ui.Node;
import com.fuse.ui.TouchGenerator;

public class PinchZoomTest {
    @Ignore @Test public void pinchZoom(){
        Node scene = new Node();
        Node n = new Node();
        n.setSize(100, 100);
        n.setPosition(10, 0);
        scene.addChild(n);

        PinchZoom pz = PinchZoom.enableFor(n);

        // create the two touch gestures, to be mixed together
        TouchGenerator gen = TouchGenerator.on(scene).mix(
            (new TouchGenerator()).from(45, 45).move(-10, -10).moves(5).noUp(),
            (new TouchGenerator()).from(55, 55).move(10, 10).moves(5).noUp().setTouchId(1));

        assertEquals(gen.getTouchEvents().size(), 12); // 2down, 10 move, 0 up

        assertFalse(pz.isPinching());
        gen.go();
        assertTrue(pz.isPinching());
    }
}
