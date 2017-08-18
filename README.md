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

### Usage - SceneGraph
_TODO_ (see NodeTest unit tests for now)

### Usage - TouchManager
_TODO_ (see TouchManagerTest unit tests for now)
