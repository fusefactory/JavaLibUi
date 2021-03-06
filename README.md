# JavaLibUi

[![Build Status](https://travis-ci.org/fusefactory/JavaLibUi.svg?branch=master)](https://travis-ci.org/fusefactory/JavaLibUi)

_Java package that provides a framework, on top of processing, for building (touch-screen) user interfaces._

The code in this package is heavily inspired by the [ofxInterface OpenFrameworks addon](https://github.com/galsasson/ofxInterface) and the [poScene cinder block](https://github.com/Potion/Cinder-poScene).

## Installation

Use as maven/gradle/sbt/leiningen dependency with [JitPack](https://github.com/jitpack/maven-modular)
* https://jitpack.io/#fusefactory/JavaLibUi

For more info on jitpack see;
* https://github.com/jitpack/maven-simple
* https://jitpack.io/docs/?#building-with-jitpack

## Documentation
* javadocs: https://fusefactory.github.io/JavaLibUi/site/apidocs/index.html
* to run unit tests: ```mvn test```

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
* com.fuse.ui.extensions.ExtensionBase
 * com.fuse.ui.extensions.Draggable
 * com.fuse.ui.extensions.PinchZoom
 * com.fuse.ui.extensions.DoubleClickZoom
 * com.fuse.ui.extensions.Swiper
 * com.fuse.ui.extensions.TouchEventForwarder

### Dependencies
This repo uses [maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) for dependency management (see the pom.xml file).

Compile Dependencies are:
* [Processing](https://processing.org/) core (2.2.1 currently) [(mvn)](https://mvnrepository.com/artifact/org.processing/core)
* [fusefactory](http://fuseinteractive.it/)'s [JavaLibEvent package](https://github.com/fusefactory/JavaLibEvent) [(jitpack)](https://jitpack.io/#fusefactory/event/1.0)

### USAGE: Creating and rendering a simple scene
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


### USAGE: Interactivity using the TouchManager
_see also the example application in the example/ folder_

```Java

import processing.core.*;
import com.fuse.ui.*;

Node sceneNode;
TouchManager touchManager;

public void setup(){
    // ...

    // create our scene's root node
    sceneNode = new Node();
    sceneNode.setSize(papplet.width, papplet.height);

    // initialize our touch manager
    touchManager = new TouchManager();
    touchManager.setNode(sceneNode);

    // create a button node
    Node exitButtonNode = new Node();
    exitButtonNode.setName("exit");
    exitButtonNode.setPosition(10, 50);
    exitButtonNode.setSize(300, 200);
    sceneNode.addChild(exitButtonNode);

    // create a touch-click handler for the exit button
    exitButtonNode.touchClickEvent.addListener((TouchEvent event) -> {
        System.out.println("Exit clicked, shutting down!");
        exit();
    }, this);
}

// simulate touch events using the mouse
public void mousePressed(){
    touchManager.touchDown(0, new PVector(mouseX, mouseY, 0f));
}

public void mouseDragged(){
    touchManager.touchMove(0, new PVector(mouseX, mouseY, 0f));
}

public void mouseReleased(){
    touchManager.touchUp(0, new PVector(mouseX, mouseY, 0f));
}
```


### USAGE: Using extensions for more advanced behaviours
_See also the example applications, like [example-pinchzoom](https://github.com/fusefactory/JavaLibUi/tree/master/example-pinchzoom) and [example-swiper](https://github.com/fusefactory/JavaLibUi/tree/master/example-swiper)_

The Node class has a "plugin system" which lets you add extensions to Nodes. This package includes a bunch of Extensions for common behaviours, but you can also create you own extensions by simply inheriting from the ```com.fuse.ui.extensions.ExtensionBase``` class.

Extensions can be attached to a node using the ```use``` method, and removed with the ```stopUsing``` method. Once attached to a Node, this Node will be responsible for updating all of its extensions (which by default all Nodes should do in their update method).

```java
    MyCustomExtension ext = new MyCustomExtension();
    Node someNode = new Node();
    someNode.use(ext);
    // some time later
    someNode.stopUsing(ext);
```

Most included extensions have static factory method which will take care of this for you though;
```java
    Node myNode = new Node();

    // returns null when this behaviour is not yet activated
    Draggable existingDrag = Draggable.getFor(myNode);

    // Activates the behaviour, only when not already active. always returns an active instance
    Draggable drag = Draggable.enableFor(myNode);

    // Disables the draggable behaviour on this node (if active)
    Draggable.disableFor(myNode);
```
