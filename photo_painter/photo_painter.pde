void setup(){
  size(100,100);
  surface.setResizable(true);

  frameRate(60);
  background(245, 245, 220); // beige (TODO: tile a canvas texture image)
  smooth();
  randomSeed(10);
  noFill();

  PICTURE = loadImage("pic7.jpg");
  
  updateGlobalsForResize();
}

void draw(){
  if (!DID_RESIZE_SURFACE) {
    DID_RESIZE_SURFACE = true;
    surface.setSize(PICTURE.width, PICTURE.height);
  }
  
  if (NUM_RESIZES_REMAINING > 0) {
    TIME++;
    if (TIME > TIME_TO_NEXT_RESIZE) {
      updateGlobalsForResize();
    }
    updateBrushes();
  }
}