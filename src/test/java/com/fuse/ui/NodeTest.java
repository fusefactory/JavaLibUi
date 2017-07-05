package com.fuse.ui;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.ArrayList;
import processing.core.*;

/**
 * Unit test for com.fuse.utils.Event.
 */
public class NodeTest extends TestCase
{
    private void _start(String name){
      System.out.println("TEST: "+name);
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NodeTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NodeTest.class );
    }

    /**
     * Test Logic
     */
    public void testApp()
    {
      { _start("setSize");
        Node node = new Node();
        node.setSize(100,200);
        assertEquals(node.getSize(), new PVector(100, 200, 0));
      }

      { _start("setVisible");
         Node node = new Node();
         assertEquals(node.isVisible(), true);
         node.setVisible(false);
         assertEquals(node.isVisible(), false);
         node.setVisible(true);
         assertEquals(node.isVisible(), true);
       }

      { _start("loadSubtreeList");
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

        _start("loadSubtreeList with hidden child");
        c1.setVisible(false);

        // reload
        list.clear();
        node.loadSubtreeList(list, true /* only visible */);

        assertEquals(list.size(), 4);
        assertEquals(list.get(0), node);
        assertEquals(list.get(1), c2);
        assertEquals(list.get(2), c2_1);
        assertEquals(list.get(3), c3);

        _start("loadSubtreeList with forceAll=true");
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

        _start("loadSubtreeList unhidden child and removed child");
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

        _start("loadSubtreeList with custom plane value");
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

      { _start("isInside");
        Node node = new Node();
        node.setPosition(10f,10f);
        node.setSize(100f, 100f);
        this.assertTrue(node.isInside(new PVector(10f,10f,0f)));
        this.assertTrue(node.isInside(new PVector(50f,50f,0f)));
        this.assertFalse(node.isInside(new PVector(0f,50f,0f)));
        this.assertFalse(node.isInside(new PVector(50f,00f,0f)));
        this.assertFalse(node.isInside(new PVector(0f,0f,0f)));
        this.assertFalse(node.isInside(new PVector(110f,110f,0f)));
      }

      { _start("toLocal");
        Node node = new Node();
        node.setPosition(100f,0f);
        node.setSize(100f, 50f);
        node.rotate(90f / 180.0f * (float)Math.PI); // 90 degrees clockwise around origin (top left corner)
        assertEquals(node.toLocal(new PVector(100f, 0f, 0f)), new PVector(0f, 0f, 0f));
        assertEquals(node.toLocal(new PVector(90f, 120f, 0f)).dist(new PVector(120f, 10f,0f)) < 0.0001f, true);
      }

      { _start("isInside with rotation");
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

      { _start("toGlobal");
        Node node = new Node();
        node.setPosition(100f, 50f);
        assertEquals(node.toGlobal(new PVector(10f, 10f, 0f)), new PVector(110f, 60f));
      }

      { _start("getChildWithName");
        Node node = new Node();
        Node c1 = new Node();
        c1.setName("foofoo #123");
        assertEquals(node.getChildWithName("foofoo #123"), null);
        node.addChild(c1);
        assertEquals(node.getChildWithName("foofoo #123"), c1);
      }

      { _start("getParent");
        Node parent = new Node();
        Node child = new Node();
        assertEquals(child.getParent(), null);
        parent.addChild(child);
        assertEquals(child.getParent(), parent);
      }

      { _start("toLocal with parent and child translation");
        Node parent = new Node();
        parent.setPosition(10f, 10f);
        Node child = new Node();
        child.setPosition(10f, 10f);
        parent.addChild(child);
        assertEquals(child.toLocal(new PVector(30.0f, 30.0f, 0.0f)), new PVector(10.0f, 10.0f, 0.0f));
      }

    }
}
