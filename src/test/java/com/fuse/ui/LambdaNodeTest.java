package com.fuse.ui;

import processing.core.PGraphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class LambdaNodeTest {
    float counter;

    public LambdaNodeTest(){
        Node.setPGraphics(new PGraphics());
    }

    @Test public void draw(){
        LambdaNode subject = new LambdaNode();
        counter = 0.0f;
        subject.setDrawFunc(() -> { counter += 10.0f; });

        Node scene = new Node();
        scene.addChild(subject);

        assertEquals(counter, 0.0f, 0.0000001f);
        scene.render();
        assertEquals(counter, 10.0f, 0.0000001f);
        scene.render();
        scene.render();
        assertEquals(counter, 30.0f, 0.0000001f);
    }

    @Test public void update(){
        LambdaNode subject = new LambdaNode();
        subject.setUpdateFunc((Float dt) -> { counter += dt * 20.0f; } );

        Node scene = new Node();
        scene.addChild(subject);

        assertEquals(counter, 0.0f, 0.0000001f);
        scene.updateSubtree(1.0f);
        assertEquals(counter, 20.0f, 0.0000001f);
        scene.updateSubtree(100.0f);
        assertEquals(counter, 2020.0f, 0.0000001f);
    }
}
