# JavaLibUi
_Java package that provides classes for building interactive User Interfaces_

The code in this package is heavily inspired by the [ofxInterface OpenFrameworks addon](https://github.com/galsasson/ofxInterface) and the [poScene cinder block](https://github.com/Potion/Cinder-poScene).

## Installation

Use as maven/gradle/sbt/leiningen dependency with [JitPack](https://github.com/jitpack/maven-modular)
* https://jitpack.io/#fusefactory/JavaLibUi

For more info on jitpack see;
* https://github.com/jitpack/maven-simple
* https://jitpack.io/docs/?#building-with-jitpack

## JavaDocs
* https://fusefactory.github.io/JavaLibUi/site/apidocs/index.html

## Main Classes
* com.fuse.ui.Node
 * com.fuse.ui.ImageNode
 * com.fuse.ui.LineNode
 * com.fuse.ui.RectNode
 * com.fuse.ui.TextNode
 * com.fuse.ui.LambdaNode
 * com.fuse.ui.EventNode
* com.fuse.ui.TouchManager
* com.fuse.ui.TouchEvent


### Dependencies
This repo uses [maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) for dependency management (see the pom.xml file).

Runtime Dependencies are:
* [Processing](https://processing.org/) core [(mvn)](https://mvnrepository.com/artifact/org.processing/core)
* [fusefactory](http://fuseinteractive.it/)'s [JavaLibEvent package](https://github.com/fusefactory/JavaLibEvent) [(jitpack)](https://jitpack.io/#fusefactory/event/1.0)

### Creating and rendering a simple scene
_see also the example application in the example/ folder_
```Java
import processing.core.*;
import com.fuse.ui.*;

PGraphics pg;
Node sceneNode;

void setup(){
    // ...
    papplet.frameRate(30.0f);

    // currently, Node requires a PGraphics instance for all rendering...
    pg = papplet.createGraphics(papplet.width, papplet.height, P3D);
    Node.setPGraphics(pg);

    // create our scene's root node
    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    // create a button
    RectNode buttonNode = new RectNode();
    buttonNode.setPosition(100, 100);
    buttonNode.setSize(200,100);
    sceneNode.addChild(buttonNode);

    // add a text-label to the button
    TextNode textNode = new TextNode();
    textNode.setText("RectNode");
    textNode.setSize(100,100);
    textNode.setTextSize(14);
    textNode.setTextColor(pg.color(0,0,0));
    buttonNode.addChild(textNode);
}

void update(float dt){
    // this performs a full recursive UI update by executing the update
    // method of all Nodes that are part of the sceneNode's subtree
    sceneNode.updateSubtree(dt);
}

void draw(){
    // perform updates before rendering
    this.update(1.0f / papplet.frameRate);

    // draw UI to framebuffer
    pg.beginDraw();
    {
        // clear framebuffer
        pg.clear();

        // render scene; this takes al Nodes in the sceneNode's subtree,
        // re-orders them according to the plane ('z-level') attribute
        // and executes their draw methods after applying their global transform matrix
        sceneNode.render();

        // renderDebug does the same render, but instead of the draw method, it invokes
        // every Node's drawDebug method. For debug/development purposes only.
        if(bDrawDebug)
            sceneNode.renderDebug();
    }
    pg.endDraw();

    // clear screen
    papplet.background(0);
    // draw framebuffer to screen
    papplet.image(pg, 0f, 0f);
}
```


### Usage - TouchManager
_TODO_ (see TouchManagerTest unit tests for now)
