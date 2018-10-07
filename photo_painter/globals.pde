// Time and image sample size ratio dictate most other global values.
int TIME = 0;
int TOTAL_TIMES_TO_RESIZE = 100;
int NUM_RESIZES_REMAINING = TOTAL_TIMES_TO_RESIZE;
int TIME_TO_NEXT_RESIZE = 60; // doubles with each resize. 20, 40, 80, 160... 
float START_SIZE_RATIO = 0;
float END_SIZE_RATIO = 0.5;
float SIZE_RATIO;
float SIZE_RATIO_INC;
float PERCENT_REMAINING;

// Brush / Bristle constants
int NUM_BRUSHES = 1;
ArrayList<Brush> BRUSHES;
float STROKE_OPACITY = 0.0;
float STROKE_HARDNESS = 0.0; // controls opacity of brush stroke at end points
float NUM_BRUSH_POINTS_TO_SOFTEN = 20;
float STROKE_WEIGHT = 2.0;
float BRUSH_RADIUS;
float BRISTLE_NOISE; // controls frequency of random gaps in bristle stroke
int MAX_BRUSH_BRISTLES;
int MAX_BRUSH_PATH_POINTS;
int MIN_BRUSH_PATH_POINTS = 10;
int POINTS_TO_DRAW_PER_FRAME = 6; // # of tail points to draw when animating stroke

// Color compare constants
float MAX_COLOR_THRESHOLD = 100;
float MIN_COLOR_THRESHOLD = 50;
float COLOR_DIST_THRESHOLD;

// Image constants
PImage PICTURE, PIC_FOR_SAMPLING;
boolean DID_RESIZE_SURFACE = false;

// Update all global variables based on t and our new size ratio
void updateGlobalsForResize() {  
  // SIZE_RATIO grows from START_SIZE_RATIO to END_SIZE_RATIO (linear)
  if (START_SIZE_RATIO == 0){
    START_SIZE_RATIO = 8.0 / min(PICTURE.width, PICTURE.height);
    SIZE_RATIO = START_SIZE_RATIO;
    SIZE_RATIO_INC = (END_SIZE_RATIO - START_SIZE_RATIO) / TOTAL_TIMES_TO_RESIZE;
  } else {
    SIZE_RATIO += SIZE_RATIO_INC;
  }

  PERCENT_REMAINING = (float)NUM_RESIZES_REMAINING / (float)TOTAL_TIMES_TO_RESIZE;
  COLOR_DIST_THRESHOLD = PERCENT_REMAINING * (MAX_COLOR_THRESHOLD - MIN_COLOR_THRESHOLD) + MIN_COLOR_THRESHOLD;

  // update the sampling pic
  PIC_FOR_SAMPLING = PICTURE.copy();
  PIC_FOR_SAMPLING.resize(
    ceil(PICTURE.width * SIZE_RATIO),
    ceil(PICTURE.height * SIZE_RATIO)
    );

  // update brush constants
  NUM_BRUSHES = round(0.3 * (TOTAL_TIMES_TO_RESIZE - NUM_RESIZES_REMAINING)) + 1; // 1 --> 10    (linear)
  STROKE_WEIGHT = 1.0 * PERCENT_REMAINING + 1.0;
  STROKE_HARDNESS = 0.5 * (1.0 - PERCENT_REMAINING);                                      // 100 --> 255  (linear)
  STROKE_OPACITY = 50.0 + 55.0 * (1.0 - PERCENT_REMAINING);
  MAX_BRUSH_PATH_POINTS = round((10 * MIN_BRUSH_PATH_POINTS) * (1 - PERCENT_REMAINING) + MIN_BRUSH_PATH_POINTS); // 10 --> 20     (linear)
  BRUSH_RADIUS = min(min(PICTURE.width, PICTURE.height) / 2.0, 2.0 / (SIZE_RATIO));                           // 100 --> 1   (linear)
  MAX_BRUSH_BRISTLES = min(1000, round(1.0 * pow(BRUSH_RADIUS, 1.7)));                                // 1000 --> 10 (linear)
  BRISTLE_NOISE = (0.4 * PERCENT_REMAINING) + 0.6;

  // update brushes
  initializeBrushes();

  println(100 - PERCENT_REMAINING * 100 + "% done");
  println("sample width: " + PIC_FOR_SAMPLING.width + " height: " + PIC_FOR_SAMPLING.height);
  println("bristles: " + MAX_BRUSH_BRISTLES * NUM_BRUSHES);

  // update time and resize constants
  TIME = 0;                                           // reset time
  NUM_RESIZES_REMAINING--;                            // decrement num times to resize
}

void initializeBrushes() {
  BRUSHES = new ArrayList<Brush>(NUM_BRUSHES);
  for (int n = 0; n < NUM_BRUSHES; n++) {
    BRUSHES.add(new Brush());
  }
}

void updateBrushes() {
  for (int n = 0; n < NUM_BRUSHES; n++) {
    BRUSHES.get(n).update();
  }
}