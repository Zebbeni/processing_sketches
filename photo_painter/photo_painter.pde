void setup(){
  size(100,100);
  surface.setResizable(true);

  frameRate(100);
  background(245, 245, 220); // beige (TODO: tile a canvas texture image)
  smooth();
  randomSeed(10);
  noFill();

  PICTURE = loadImage("sunset.jpg");
  
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

void mouseClicked() {
  FOCUS_X = mouseX;
  FOCUS_Y = mouseY;
}