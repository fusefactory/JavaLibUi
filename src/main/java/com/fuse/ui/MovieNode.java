package com.fuse.ui;

import com.fuse.ui.Node;
import com.fuse.utils.Event;

import ddf.minim.AudioPlayer; // for separate audio tracks
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;
import com.fuse.vlcplayer.VLCPlayer;

public class MovieNode extends Node {

  public enum Mode {
    NORMAL, // image rendered at original size at Node's origin (0,0) position
    CENTER, // image rendered at original size centered inside the node
    FIT, // image stretched into the dimensions of the Node
    FIT_CENTERED // image stretched within original aspect ratio and centered
  }

  private Movie movie = null;
  private AudioPlayer audioPlayer = null;

  private PApplet papplet;
  private VLCPlayer vlcplayer = null;
  private PImage videoImage = null;

  private Mode mode = Mode.NORMAL;
  private boolean autoResizeToMovie = false;
  private Integer tintColor = null;
  private boolean bAutoStart = false;
  private boolean bAutoStarted = false;
  private boolean bPaused = false;

  public Event<MovieNode> autoStartEvent = new Event<>();

  /** Default constructor; intialized with default values: movie=null and mode=NORMAL */
  public MovieNode(){
    super();
  }

  public MovieNode(String nodeName){
    super(nodeName);
  }

  @Override
  public void destroy(){
    super.destroy();
    autoStartEvent.destroy();
    this.papplet = null;

    if(this.movie!=null){
      if(this.movie.playbin != null) {
      // if(this.movie.isLoaded()) {
        this.movie.stop();
        this.movie.dispose();
      }
      this.movie = null;
    }

    if (this.vlcplayer != null) {
      this.vlcplayer.close();
      this.vlcplayer = null;
    }

    if(this.audioPlayer!=null){
      this.audioPlayer.pause();
      this.audioPlayer.close();
      this.audioPlayer = null;
    }
  }

  @Override public void update(float dt){
    super.update(dt);
    // this actually causes _some_ videos to stay black...
    // if(movie != null) && movie.available())
    //    movie.read();

    // don't keep (auto-)starting; user might want to pause it at some point
    if(bAutoStart && (!bAutoStarted) ) {
      this.autoStart();
    }

    if(bPaused) {
      if(this.movie != null) this.movie.pause(); // no way to read paused state from movie, we'll just keep setting it to paused?
      if (this.vlcplayer != null) this.vlcplayer.pause();
      if(this.audioPlayer!=null) this.audioPlayer.pause();
    }

    if (this.vlcplayer != null && this.vlcplayer.available()) {

      if (this.videoImage != null && (
          this.videoImage.width != this.vlcplayer.width()
          || this.videoImage.height != this.vlcplayer.height())) {
        this.videoImage = null;
      }

      // lazy-initialize video image which to which we'll copy individual frames for rendering
      if (this.videoImage == null && this.papplet != null) {
        this.videoImage = this.papplet.createImage(this.vlcplayer.width(), this.vlcplayer.height(), PApplet.ARGB);
      }

      if (this.videoImage != null) {
        System.arraycopy(vlcplayer.textureBuffer(), 0, videoImage.pixels, 0, vlcplayer.textureBuffer().length);
        videoImage.updatePixels();
      }
    }
  }

  /** Draw this node's movie at this Node's position */
  @Override public void draw(){
    if (this.vlcplayer != null && this.videoImage != null) {
      this.drawImg(this.videoImage);
      return;
    }

    if(movie == null)
    return;

    if(tintColor != null)
    pg.tint(tintColor);

    if(mode == Mode.NORMAL){
      pg.image(movie, 0.0f, 0.0f);
    } else if(mode == Mode.CENTER){
      PVector pos = PVector.mult(getSize(), 0.5f);
      pg.imageMode(PApplet.CENTER);
      pg.image(movie, pos.x, pos.y);
      pg.imageMode(PApplet.CORNERS); // restore default
    } else if(mode == Mode.FIT){
      pg.imageMode(PApplet.CORNERS);
      pg.image(movie, 0.0f, 0.0f, getSize().x, getSize().y);
    } else if(mode == Mode.FIT_CENTERED){
      //if(fitCenteredSize == null)
      PVector fitCenteredSize = calculateFitCenteredSize();
      PVector pos = PVector.mult(getSize(), 0.5f);
      pg.imageMode(PApplet.CENTER);
      pg.image(movie, pos.x, pos.y, fitCenteredSize.x, fitCenteredSize.y);
      pg.imageMode(PApplet.CORNERS); // restore default
    }

    if(tintColor != null) pg.noTint();
  }

  public void drawImg(PImage img){
    if(tintColor != null)
    pg.tint(tintColor);

    if(mode == Mode.NORMAL){
      pg.image(img, 0.0f, 0.0f);
    } else if(mode == Mode.CENTER){
      PVector pos = PVector.mult(getSize(), 0.5f);
      pg.imageMode(PApplet.CENTER);
      pg.image(img, pos.x, pos.y);
      pg.imageMode(PApplet.CORNERS); // restore default
    } else if(mode == Mode.FIT){
      pg.imageMode(PApplet.CORNERS);
      pg.image(img, 0.0f, 0.0f, getSize().x, getSize().y);
    } else if(mode == Mode.FIT_CENTERED){
      //if(fitCenteredSize == null)
      PVector fitCenteredSize = calculateFitCenteredSize();
      PVector pos = PVector.mult(getSize(), 0.5f);
      pg.imageMode(PApplet.CENTER);
      pg.image(img, pos.x, pos.y, fitCenteredSize.x, fitCenteredSize.y);
      pg.imageMode(PApplet.CORNERS); // restore default
    }

    if(tintColor != null) pg.noTint();
  }


  /**
  * Set/change the movie of this node.
  * @param newMovie The movie that should from now on be rendered by this node
  */
  public void setMovie(Movie newMovie){
    this.setMovie(newMovie, null);
  }

  public void setMovie(Movie newMovie, AudioPlayer audioPlayer){
    this.movie = newMovie;
    this.audioPlayer = audioPlayer;

    if(this.movie == null)
      return; // done

    if(this.audioPlayer != null) {
      this.movie.volume(0.0f);
    }

    if(autoResizeToMovie) {
      if (this.vlcplayer != null) setSize(this.vlcplayer.width(), this.vlcplayer.height());
      else if (this.movie != null) setSize(movie.width, movie.height);
    }

    if(bAutoStart) {
      this.autoStart();
    }
  }

  public void setMovie(VLCPlayer player, PApplet applet){
    this.vlcplayer = player;
    this.papplet = applet;
  }

  private void autoStart() {
    // TODO: maybe video is started but audio isn't?
    if(bAutoStarted) return;

    boolean startedSomething = false;
    if (movie != null && movie.playbin != null) {

      // Processing.Movie
      if (!movie.playbin.isPlaying()) movie.play();

      if(this.audioPlayer != null) {
        this.movie.volume(0.0f);
        this.audioPlayer.play();
      }

      startedSomething =true;
    }

    // VLC-player
    if (this.vlcplayer != null) {
      this.vlcplayer.play();

      if(this.audioPlayer != null) { // never happens
        this.vlcplayer.mute();
        this.audioPlayer.play();
      }

      startedSomething = true;
    }

    if (startedSomething) {
      bAutoStarted = true;
      autoStartEvent.trigger(this);
    }
  }

  /** @return PImage The movie that this node is rendering */
  public Movie getMovie(){ return movie; }
  public VLCPlayer getVlcPlayer() { return this.vlcplayer; }

  public Mode getMode(){ return mode; }
  public void setMode(Mode newMode){ mode = newMode; }

  public boolean getAutoResizeToMovie(){ return autoResizeToMovie; }
  public void setAutoResizeToMovie(boolean enable){
    autoResizeToMovie = enable;
    if(autoResizeToMovie && vlcplayer != null){
      setSize(this.vlcplayer.width(), this.vlcplayer.height());
    }
    else if(autoResizeToMovie && movie != null){
      setSize(movie.width, movie.height);
    }
  }

  public void setTint(Integer clr){ tintColor = clr; }
  public Integer getTint(){ return tintColor; }

  public boolean getAutoStart(){ return bAutoStart; }
  public void setAutoStart(boolean enable){
    if (enable == this.bAutoStart) return; // no change
    bAutoStart = enable;
    bAutoStarted = false;

    if(bAutoStart) {
      this.autoStart();
    }
  }

  private PVector calculateFitCenteredSize(){
    if (this.vlcplayer != null) {
      float w = getSize().x / this.vlcplayer.width();
      float h = getSize().y / this.vlcplayer.height();
      if(w > h){
        w = h * this.vlcplayer.width();
        h = h * this.vlcplayer.height();
      } else {
        h = w * this.vlcplayer.height();
        w = w * this.vlcplayer.width();
      }

      return new PVector(w,h,0.0f);
    }

    if(movie != null) {
      float w = getSize().x / movie.width;
      float h = getSize().y / movie.height;
      if(w > h){
        w = h * movie.width;
        h = h * movie.height;
      } else {
        h = w * movie.height;
        w = w * movie.width;
      }

      return new PVector(w,h,0.0f);
    }

    return new PVector(0.0f,0.0f,0.0f);
  }

  public void setPaused(boolean paused) {

    boolean somethingChanged = false;

    if(this.movie != null){
      if(paused) {
        this.movie.pause();
        if(this.audioPlayer != null) this.audioPlayer.pause();
      } else {
        try {
          //if(this.movie.available() || this.moviei) {
          this.movie.play();
          if(this.audioPlayer != null) {
            this.movie.volume(0.0f);
            this.audioPlayer.play();
          }
          // }
        } catch(java.lang.NullPointerException exc) { // in case movie isn't fully initialized yet
      	  	System.err.println("movie exc: "+exc.toString());
      	  	exc.printStackTrace();
        }
      }

      somethingChanged = true;
    }

    if (this.vlcplayer != null) {
      if (paused) {
        this.vlcplayer.pause();
        if(this.audioPlayer != null) this.audioPlayer.pause();
      } else {
        try {
          //if(this.movie.available() || this.moviei) {
          this.vlcplayer.play();
          if(this.audioPlayer != null) {
            this.vlcplayer.mute();
            this.audioPlayer.play();
          }
        } catch(java.lang.NullPointerException exc) { // in case movie isn't fully initialized yet
            System.err.println("vlcplayer exc: "+exc.toString());
            exc.printStackTrace();
        }
      }

      somethingChanged = true;
    }

    if (somethingChanged) this.bPaused = paused;
  }

  public void jumpTo(float percentage) {
    this.jumpTo(percentage, true);
  }

  public void jumpTo(float percentage, boolean andPlay) {
    if(this.audioPlayer != null){
      this.audioPlayer.cue((int)(((float)this.audioPlayer.length())*percentage));
      this.audioPlayer.pause();
    }

    if (this.movie != null) {
      try{
        movie.jump(percentage * movie.duration());

        if(andPlay) {
          if(this.audioPlayer != null){
            movie.volume(0.0f);
            this.audioPlayer.play();
          }
        } else {
          movie.pause();
        }
      } catch(java.lang.NullPointerException exc) { // in case movie isn't fully initialized yet
        System.err.println("movie exc: "+exc.toString());
        exc.printStackTrace();
      }
    }

    if (this.vlcplayer != null) {
      this.vlcplayer.seek(percentage);

      if(andPlay) {
        if(this.audioPlayer != null){
          this.vlcplayer.mute();
          this.audioPlayer.play();
        }
      } else {
        this.vlcplayer.pause();
      }
    }
  }
}
