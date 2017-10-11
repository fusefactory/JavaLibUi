package com.fuse.ui;

import com.fuse.anim.Timeline;

/**
 * SceneNode is a simple Node with an embedded Timeline instant, so animations can be
 * attached to it and they will automatically update.
 */
public class SceneNode extends Node {

  public Timeline timeline;

  public SceneNode(){
    timeline = new Timeline();
  }

  @Override
  public void destroy(){
    timeline.destroy();
    super.destroy();
  }

  @Override
  public void update(float dt){
    timeline.update(dt);
    super.update(dt);
  }
}
