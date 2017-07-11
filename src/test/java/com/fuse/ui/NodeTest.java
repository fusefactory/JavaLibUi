package com.fuse.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import processing.core.*;

/**
* Unit test for com.fuse.utils.Event.
*/
public class NodeTest {

    private void _start(String name){
        // System.out.println("TEST: "+name);
    }

    @Test public void setSize(){
        Node node = new Node();
        node.setSize(100,200);
        assertEquals(node.getSize(), new PVector(100, 200, 0));
    }

    @Test public void setVisible(){

        Node node = new Node();
        assertEquals(node.isVisible(), true);
        node.setVisible(false);
        assertEquals(node.isVisible(), false);
        node.setVisible(true);
        assertEquals(node.isVisible(), true);
    }

    @Test public void loadSubtreeList(){

        Node node = new Node();
        node.setName("scene");
        Node c1 = new Node();
        c1.setName("c1");
        Node c2 = new Node();
        c2.setName("c2");
        Node c3 = new Node();
        c3.setName("c3");
        Node c1_1 = new Node();
        c1_1.setName("c1_1");
        Node c1_2 = new Node();
        c1_2.setName("c1_2");
        Node c2_1 = new Node();
        c2_1.setName("c2_1");
        node.addChild(c1);
        node.addChild(c2);
        node.addChild(c3);
        c1.addChild(c1_1);
        c1.addChild(c1_2);
        c2.addChild(c2_1);
        List<Node> list = new ArrayList<Node>();
        node.loadSubtreeList(list, true /* only visible */);
        assertEquals(list.size(), 7);
        assertEquals(list.get(0), node);
        assertEquals(list.get(1), c1);
        assertEquals(list.get(2), c1_1);
        assertEquals(list.get(3), c1_2);
        assertEquals(list.get(4), c2);
        assertEquals(list.get(5), c2_1);
        assertEquals(list.get(6), c3);

        //_start("loadSubtreeList with hidden child");
        c1.setVisible(false);

        // reload
        list.clear();
        node.loadSubtreeList(list, true /* only visible */);

        assertEquals(list.size(), 4);
        assertEquals(list.get(0), node);
        assertEquals(list.get(1), c2);
        assertEquals(list.get(2), c2_1);
        assertEquals(list.get(3), c3);

        //_start("loadSubtreeList with forceAll=true");
        list.clear();
        node.loadSubtreeList(list, false /* also invisible */);
        assertEquals(list.size(), 7);
        assertEquals(list.get(0), node);
        assertEquals(list.get(1), c1);
        assertEquals(list.get(2), c1_1);
        assertEquals(list.get(3), c1_2);
        assertEquals(list.get(4), c2);
        assertEquals(list.get(5), c2_1);
        assertEquals(list.get(6), c3);

        //_start("loadSubtreeList unhidden child and removed child");
        c1.setVisible(true);
        c2.removeChild(c2_1);

        list.clear();
        node.loadSubtreeList(list, false /* also invisible */);
        assertEquals(list.size(), 6);
        assertEquals(list.get(0).getName(), "scene");
        assertEquals(list.get(1).getName(), "c1");
        assertEquals(list.get(2).getName(), "c1_1");
        assertEquals(list.get(3).getName(), "c1_2");
        assertEquals(list.get(4).getName(), "c2");
        assertEquals(list.get(5).getName(), "c3");

        //_start("loadSubtreeList with custom plane value");
        c1_2.setPlane(1.0f);
        c1_1.setPlane(-5.0f);

        list = node.getOrderedSubtreeList(false /* only visible */);
        assertEquals(list.size(), 6);
        assertEquals(list.get(0).getName(), "c1_1");
        assertEquals(list.get(1).getName(), "scene");
        assertEquals(list.get(2).getName(), "c1");
        assertEquals(list.get(3).getName(), "c2");
        assertEquals(list.get(4).getName(), "c3");
        assertEquals(list.get(5).getName(), "c1_2");

    }

    @Test public void isInside(){

        Node node = new Node();
        node.setPosition(10f,10f);
        node.setSize(100f, 100f);
        assertTrue(node.isInside(new PVector(10f,10f,0f)));
        assertTrue(node.isInside(new PVector(50f,50f,0f)));
        assertFalse(node.isInside(new PVector(0f,50f,0f)));
        assertFalse(node.isInside(new PVector(50f,00f,0f)));
        assertFalse(node.isInside(new PVector(0f,0f,0f)));
        assertFalse(node.isInside(new PVector(110f,110f,0f)));

    }

    @Test public void toLocal(){

        Node node = new Node();
        node.setPosition(100f,0f);
        node.setSize(100f, 50f);
        node.rotate(90f / 180.0f * (float)Math.PI); // 90 degrees clockwise around origin (top left corner)
        assertEquals(node.toLocal(new PVector(100f, 0f, 0f)), new PVector(0f, 0f, 0f));
        assertEquals(node.toLocal(new PVector(90f, 120f, 0f)).dist(new PVector(120f, 10f,0f)) < 0.0001f, true);

    }

    @Test public void isInside_with_rotation(){
        Node node = new Node();
        node.setPosition(100f,0f);
        node.setSize(100f, 50f);
        node.rotate(90f / 180.0f * (float)Math.PI); // 90 degrees clockwise around origin (top left corner)
        assertEquals(node.isInside(new PVector(100f, 0f, 0f)), true);
        assertEquals(node.isInside(new PVector(101f, 0f, 0f)), false);
        assertEquals(node.isInside(new PVector(60f, 20f, 0f)), true);
        assertEquals(node.isInside(new PVector(60f, 99f, 0f)), true);
        assertEquals(node.isInside(new PVector(60f, 101f, 0f)), false);
    }

    @Test public void toGlobal(){
        Node node = new Node();
        node.setPosition(100f, 50f);
        assertEquals(node.toGlobal(new PVector(10f, 10f, 0f)), new PVector(110f, 60f));
    }

    @Test public void getChildWithName(){
        Node node = new Node();
        Node c1 = new Node();
        c1.setName("foofoo #123");
        assertEquals(node.getChildWithName("foofoo #123"), null);
        node.addChild(c1);
        assertEquals(node.getChildWithName("foofoo #123"), c1);
    }

    @Test public void getChildrenWithName(){
        Node node = new Node();
        assertEquals(node.getChildrenWithName("aa").size(), 0);

        Node c = new Node();
        c.setName("aa");
        node.addChild(c);
        assertEquals(node.getChildrenWithName("aa").size(), 1);

        c = new Node();
        c.setName("aa");
        node.addChild(c);
        assertEquals(node.getChildrenWithName("aa").size(), 2);

        c = new Node();
        c.setName("ac");
        node.addChild(c);
        assertEquals(node.getChildrenWithName("aa").size(), 2);

        Node c2 = new Node();
        c2.setName("aa");
        c.addChild(c2);
        assertEquals(node.getChildrenWithName("aa").size(), 3);
    }

    @Test public void getParent(){
        Node parent = new Node();
        Node child = new Node();
        assertEquals(child.getParent(), null);
        parent.addChild(child);
        assertEquals(child.getParent(), parent);
    }

    @Test public void toLocal_with_parent_and_child_translation(){
        Node parent = new Node();
        parent.setPosition(10f, 10f);
        Node child = new Node();
        child.setPosition(10f, 10f);
        parent.addChild(child);
        assertEquals(child.toLocal(new PVector(30.0f, 30.0f, 0.0f)), new PVector(10.0f, 10.0f, 0.0f));
    }

    @Test public void forAllChildren(){
        Node parent = new Node();
        Node c1 = new Node();
        c1.setName("c1");
        parent.addChild(c1);

        List<String> strings = new ArrayList<>();
        parent.forAllChildren((Node n) -> strings.add("New kid: "+n.getName()));

        assertEquals(strings.get(0), "New kid: c1");

        Node c2 = new Node();
        c2.setName("c2");
        parent.addChild(c2);

        assertEquals(strings.get(1), "New kid: c2");
    }
}
