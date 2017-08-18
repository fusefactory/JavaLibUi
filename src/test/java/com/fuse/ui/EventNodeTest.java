package com.fuse.ui;

import processing.core.PGraphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class EventNodeTest {
    public EventNodeTest(){
        Node.setPGraphics(new PGraphics());
    }

    @Test public void draw(){
        EventNode subject = new EventNode();
        subject.drawEvent.enableHistory(); // for easy assertions

        Node scene = new Node();
        scene.addChild(subject);

        assertEquals(subject.drawEvent.getHistory().size(), 0);
        scene.render();
        assertEquals(subject.drawEvent.getHistory().size(), 1);
        assertEquals(subject.drawEvent.getHistory().get(0), subject);
        scene.render();
        assertEquals(subject.drawEvent.getHistory().size(), 2);
        assertEquals(subject.drawEvent.getHistory().get(1), subject);
    }

    @Test public void update(){
        EventNode subject = new EventNode();
        subject.updateEvent.enableHistory(); // for easy assertions

        Node scene = new Node();
        scene.addChild(subject);

        assertEquals(subject.updateEvent.getHistory().size(), 0);
        scene.updateSubtree(1.0f);
        assertEquals(subject.updateEvent.getHistory().size(), 1);
        assertEquals((float)subject.updateEvent.getHistory().get(0), 1.0f, 0.000001f);
        scene.updateSubtree(100.0f);
        assertEquals(subject.updateEvent.getHistory().size(), 2);
        assertEquals((float)subject.updateEvent.getHistory().get(1), 100.0f, 0.0000001f);
    }
}
