package com.fuse.ui;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
// import org.junit.Ignore;

import processing.video.Movie;
import ddf.minim.AudioPlayer;

public class MovieNodeTest {
  @Test public void autoStartAppliesToNewlyReceivedMovie(){
    MovieNode mn = new MovieNode();
    mn.setAutoStart(true);

    // TODO
    // these require a papplet instance
    // Movie m = new Movie(null, ""); // required a papplet instance
    // AudioPlayer p = new AudioPlayer(null, null);
    // mn.setMovie(m, p);

    // assert is playing: m
    // assert is playing: p
    // m.stop()
    // p.stop()
  }
}
