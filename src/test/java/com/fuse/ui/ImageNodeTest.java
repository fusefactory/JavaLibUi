package com.fuse.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Ignore;

import processing.core.*;

public class ImageNodeTest {

  @Test public void setShaderFragPath(){
    Node.pg = new PGraphics();

    ImageNode node = new ImageNode();
    //node.getShader
    assertEquals(node.getShader(), null);
    node.setShaderPath("testdata/gray.glsl");
    // assertEquals(node.getShader() == null, false); our dummy pg cannot load shaders
    assertEquals(node.getShaderFragPath(), "testdata/gray.glsl");
    assertEquals(node.getShaderVertPath(), null);
  }
}
