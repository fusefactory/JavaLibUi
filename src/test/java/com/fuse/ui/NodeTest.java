package com.fuse.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import processing.core.*;

public class NodeTest {

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
        Node c1 = new Node();
        c1.setPosition(100f, 30f);
        node.addChild(c1);
        assertEquals(c1.toGlobal(new PVector(10f, 10f, 0f)), new PVector(210f, 90f));
        node.rotateZ(PGraphics.PI);
        assertEquals(node.toGlobal(new PVector(10, 10, 0)), new PVector(90, 40, 0));
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

    @Test public void forAllOffspring(){
        Node parent = new Node();
        Node c1 = new Node();
        c1.setName("c1");
        Node d1 = new Node();
        d1.setName("d1");
        c1.addChild(d1);
        parent.addChild(c1);

        List<String> strings = new ArrayList<>();
        parent.forAllOffspring((Node n) -> { strings.add("New kid: "+n.getName()); });

        assertEquals(strings.get(0), "New kid: c1");
        assertEquals(strings.get(1), "New kid: d1");

        Node c2 = new Node();
        c2.setName("c2");
        Node d2 = new Node();
        d2.setName("d2");
        c2.addChild(d2);
        parent.addChild(c2);

        assertEquals(strings.get(2), "New kid: c2");
        assertEquals(strings.get(3), "New kid: d2");

        parent.removeChild(c2);

        Node e2 = new Node();
        e2.setName("e2");
        c2.addChild(e2);

        assertEquals(strings.size(), 4);
    }

    @Test public void setClipContent(){
        // create parent node with children and verify children don't have clipping nodes
        Node parent = new Node();
        Node c1 = new Node();
        parent.addChild(c1);
        Node c1_1 = new Node();
        c1.addChild(c1_1);
        assertEquals(c1.getClippingNode(), null);
        assertEquals(c1_1.getClippingNode(), null);

        // enable clipping on parent
        // verify all children got the parent as clipping node
        parent.setClipContent(true);
        assertEquals(c1.getClippingNode(), parent);
        assertEquals(c1_1.getClippingNode(), parent);

        // add new child to parent
        // verify this also get that parent as clipping node
        Node c2 = new Node();
        assertEquals(c2.getClippingNode(), null);
        parent.addChild(c2);
        assertEquals(c2.getClippingNode(), parent);

        // disable clipping on parent and verify all child
        // nodes have no clipping node anymore
        parent.setClipContent(false);
        assertEquals(c1.getClippingNode(), null);
        assertEquals(c1_1.getClippingNode(), null);
        assertEquals(c2.getClippingNode(), null);

        // add new child to parent
        // verify it gets no clipping node
        Node c3 = new Node();
        parent.addChild(c3);
        assertEquals(c3.getClippingNode(), null);
    }

    @Test public void setClipContent_nested(){
        // create parent node with children and verify children don't have clipping nodes
        Node parent = new Node();
        Node c1 = new Node();
        parent.addChild(c1);
        Node c1_1 = new Node();
        c1.addChild(c1_1);
        assertEquals(c1.getClippingNode(), null);
        assertEquals(c1_1.getClippingNode(), null);

        // enable clipping on parent
        // verify all children got the parent as clipping node
        parent.setClipContent(true);
        assertEquals(c1.getClippingNode(), parent);
        assertEquals(c1_1.getClippingNode(), parent);

        // enable clipping on c1
        // veify c1_1 (child of c1) get c1 as clipping node but c1's clipping node stays parent
        c1.setClipContent(true);
        assertEquals(c1.getClippingNode(), parent);
        assertEquals(c1_1.getClippingNode(), c1);

        // add new child to parent
        // verify this also get that parent as clipping node
        Node c2 = new Node();
        assertEquals(c2.getClippingNode(), null);
        parent.addChild(c2);
        assertEquals(c2.getClippingNode(), parent);
        // now add c2 to c1 and verify c1 becomes clipping node
        c1.addChild(c2);
        assertEquals(c2.getClippingNode(), c1);

        // disable clipping on c1
        // verify all children now have parent as clippingNode again
        c1.setClipContent(false);
        assertEquals(c1.getClippingNode(), parent);
        assertEquals(c1_1.getClippingNode(), parent);
        assertEquals(c2.getClippingNode(), parent);
    }

    @Test public void getGlobalPosition(){
        Node root = new Node();
        root.setPosition(10.0f, 0.0f);
        // assertEquals(root.getGlobalPosition().x, 10.0f, 0.00001f);

        Node c1 = new Node();
        c1.setPosition(10.0f, 0.0f);
        assertEquals(c1.getGlobalPosition(), new PVector(10.0f, 0.0f, 0.0f));
        root.addChild(c1);
        assertEquals(c1.getGlobalPosition(), new PVector(20.0f, 0.0f, 0.0f));

        Node c2 = new Node();
        c2.setPosition(15.0f, 20.0f);
        c1.addChild(c2);
        assertEquals(c2.getGlobalPosition(), new PVector(35.0f, 20.0f, 0.0f));
    }

    @Test public void getGlobalBottomRight(){
        Node n = new Node();
        n.setSize(100, 50);
        assertEquals(n.getGlobalBottomRight(), new PVector(100,50,0));
        n.rotate(PGraphics.PI * 0.5f);
        assertEquals(n.getGlobalBottomRight().dist(new PVector(-50,100,0)) < 0.00001f, true);
    }

    @Test public void setGlobalPosition(){
        Node scene = new Node();
        scene.setPosition(100, 0);
        scene.rotate((float)Math.PI * 0.5f); // 90 degrees clockwise
        Node subject = new Node();
        scene.addChild(subject);
        subject.setGlobalPosition(new PVector(20, 0, 0));
        assertEquals(subject.getGlobalPosition().dist(new PVector(20, 0, 0)) < 0.00001f, true);
        assertEquals(subject.getPosition().dist(new PVector(0, 80, 0)), 0.0000001f, 0.0001f);
    }

    @Test public void whenClicked(){
        Node n = new Node();
        List<String> strings = new ArrayList<>();
        n.whenClicked(() -> {
            strings.add("clikked");
        });

        assertEquals(strings.size(), 0);
        n.touchClickEvent.trigger(null);
        assertEquals(strings.size(), 1);
    }

    @Test public void getChildNodes(){
        Node a = new Node();
        a.addChild(new Node());
        a.addChild(new Node());
        a.addChild(new Node());
        List<Node> cc = a.getChildNodes();
        assertEquals(cc.size(), 3);
        cc.remove(1);
        assertEquals(cc.size(), 2);
        assertEquals(a.getChildNodes().size(), 3);
    }


    @Test public void withChild(){
        Node n = new Node();

        List<String> strings = new ArrayList<>();

        n.withChildren("bambino", (Node bambinoNode) -> {
            strings.add("1: "+bambinoNode.getName());
        });

        assertEquals(strings.size(), 0);
        n.addChild(new Node("bambino"));

        n.withChild("bambino", (Node bambinoNode) -> {
            strings.add("2: "+bambinoNode.getName());
        });

        assertEquals(strings.get(0), "2: bambino");
        assertEquals(strings.size(), 1);

        n.addChild(new Node("bambino"));

        n.withChild("bambino", (Node bambinoNode) -> {
            strings.add("3: "+bambinoNode.getName());
        });

        assertEquals(strings.get(1), "3: bambino");
        assertEquals(strings.size(), 2);
    }

    @Test public void withChildren(){
        Node n = new Node();

        List<String> strings = new ArrayList<>();

        n.withChildren("bambino", (Node bambinoNode) -> {
            strings.add("1: "+bambinoNode.getName());
        });

        assertEquals(strings.size(), 0);
        n.addChild(new Node("bambino"));
        assertEquals(strings.size(), 0);

        n.withChildren("bambino", (Node bambinoNode) -> {
            strings.add("2: "+bambinoNode.getName());
        });

        assertEquals(strings.get(0), "2: bambino");
        assertEquals(strings.size(), 1);

        // and bambino grandchild
        n.getChildNodes().get(0).addChild(new Node("bambino"));

        n.withChildren("bambino", (Node bambinoNode) -> {
            strings.add("3: "+bambinoNode.getName());
        });

        assertEquals(strings.get(1), "3: bambino");
        assertEquals(strings.get(2), "3: bambino");
        assertEquals(strings.size(), 3);

        // only direct children (0 levels-deep)
        n.withChildren("bambino", 0, (Node bambinoNode) -> {
            strings.add("4: "+bambinoNode.getName());
        });

        assertEquals(strings.get(3), "4: bambino");
        assertEquals(strings.size(), 4);
    }

    @Test public void getPosition_safety(){
        Node n = new Node();
        n.setPosition(10, 10);
        assertEquals(n.getPosition(), new PVector(10,10,0));
        n.getPosition().add(new PVector(10, 0, 0));
        assertEquals(n.getPosition(), new PVector(10,10,0));
    }

    @Test public void getSize_safety(){
        Node n = new Node();
        n.setSize(10, 10);
        assertEquals(n.getSize(), new PVector(10,10,0));
        PVector tmp = n.getSize();
        tmp.add(new PVector(10, 0, 0));
        assertEquals(n.getSize(), new PVector(10,10,0));
    }

    // @Test public void getRotation_safety(){
    //     Node n = new Node();
    //     n.rotateZ(10);
    //     assertEquals(n.getRotation(), new PVector(0,0,10));
    //     n.getRotation().add(new PVector(10, 0, 0));
    //     assertEquals(n.getRotation(), new PVector(0,0,10));
    // }

    @Test public void positionChangeEvent(){
        Node n = new Node();
        n.setPosition(10, 20);

        List<String> strs = new ArrayList<>();
        n.positionChangeEvent.whenTriggered(() -> strs.add("change"));
        assertEquals(strs.size(), 0);
        n.setPosition(20, 20);
        assertEquals(strs.size(), 1);
        n.setPosition(20, 20); // no change
        assertEquals(strs.size(), 1);
    }

    @Test public void scale(){
        Node n = new Node();
        n.setScale(new PVector(0.5f, 2.0f, 1.0f));
        assertEquals(n.toLocal(new PVector(1.0f, 1.0f, 0.0f)), new PVector(2.0f, 0.5f, 0.0f));
        assertEquals(n.toGlobal(new PVector(10,10,0)), new PVector(5, 20, 0));

        Node child = new Node();
        child.setPosition(10, 10);
        assertEquals(child.getGlobalPosition(), new PVector(10.0f, 10.0f, 0.0f));
        child.setParent(n);
        assertEquals(child.getGlobalPosition(), new PVector(5.0f, 20.0f, 0.0f));

        // scaling back to 1.0, though child has 10.0,10.0 position offset
        child.setScale(new PVector(2.0f, 0.5f, 1.0f));
        assertEquals(child.toLocal(new PVector(100.0f, 100.0f, 0.0f)), new PVector(95.0f, 80.0f, 0.0f));

        n.setPosition(10, 10);
        assertEquals(n.toGlobal(new PVector(0f, 0f, 0f)), new PVector(5,20,0));
    }

    @Test public void enable(){
        Node n = new Node();
        assertTrue(n.isVisible());
        assertTrue(n.isInteractive());
        n.disable();
        assertFalse(n.isVisible());
        assertFalse(n.isInteractive());
        n.enable();
        assertTrue(n.isVisible());
        assertTrue(n.isInteractive());
        n.enable(false);
        assertFalse(n.isVisible());
        assertFalse(n.isInteractive());
        n.enable(true);
        assertTrue(n.isVisible());
        assertTrue(n.isInteractive());
    }

    @Test public void copyAllTouchEventsFrom(){
        Node a = new Node();
        Node b = new Node();

        a.touchEvent.enableHistory();
        b.touchEvent.enableHistory();

        assertEquals(b.touchEvent.getHistory().size(), 0);

        a.receiveTouchEvent(new TouchEvent());
        assertEquals(b.touchEvent.getHistory().size(), 0);

        b.copyAllTouchEventsFrom(a);

        a.receiveTouchEvent(new TouchEvent());
        assertEquals(b.touchEvent.getHistory().size(), 1);
        a.receiveTouchEvent(new TouchEvent());
        assertEquals(b.touchEvent.getHistory().size(), 2);
        assertEquals(b.touchEvent.getHistory().get(1).node, null);

        TouchEvent evt = new TouchEvent();
        evt.node = a;
        a.receiveTouchEvent(evt);
        assertEquals(b.touchEvent.getHistory().size(), 3);
        assertEquals(b.touchEvent.getHistory().get(2).node, b); // node attribute was transformed to b

        b.stopCopyingAllTouchEventsFrom(a);

        a.receiveTouchEvent(new TouchEvent());
        assertEquals(b.touchEvent.getHistory().size(), 3);



    }

    @Test public void setRotation(){
        Node n = new Node();
        PVector vec = new PVector();
        float radFactor = 1.0f / 180.0f * (float)Math.PI;

        vec.z = 180.0f * radFactor;
        n.setRotation(vec);
        //assertEquals(n.getRotation().z, (float)Math.PI, 0.00001f);
        assertEquals(n.toGlobal(new PVector(10,30,0)).x, -10.0f, 0.0001f);
        assertEquals(n.toGlobal(new PVector(10,30,0)).y, -30.0f, 0.0001f);

        vec.z = 45.0f * radFactor;
        n.setRotation(vec);
        assertEquals(n.getRotation(), vec);
        // assertEquals(n.toGlobal(new PVector(10,30,0)).x, -10.0f, 0.0001f);
        // assertEquals(n.toGlobal(new PVector(10,30,0)).y, 10.0f, 0.0001f);

        vec.z = 0.0f * radFactor;
        n.setRotation(vec);
        assertEquals(n.getRotation(), vec);
        assertEquals(n.toGlobal(new PVector(10,10,0)).x, 10.0f, 0.0001f);
        assertEquals(n.toGlobal(new PVector(10,10,0)).y, 10.0f, 0.0001f);
    }
}
