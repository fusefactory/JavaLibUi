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

        assertFalse(pz.isActive());
        gen.go();
        assertTrue(pz.isActive());

        assertEquals(pz.getGlobalStartDelta(), new PVector(10,10,0));
        // assertEquals(pz.getGlobalCurrentDelta(), new PVector(30,30,0));
        assertEquals(pz.getGlobalPinchScale(), new PVector(3.0f, 3.0f, 1.0f));

        //assertEquals(pz.getGlobalStartCenter(), new PVector(50,50,0));
        // assertEquals(pz.getGlobalCurrentCenter(), new PVector(50,50,0));

        assertEquals(pz.getGlobalPinchTranslate(), new PVector(-10.0f*3.0f,-10.0f*3.0f,0));

        // assertEquals(n.getPosition(), new PVector(-10, -10, 0));
    }
}
