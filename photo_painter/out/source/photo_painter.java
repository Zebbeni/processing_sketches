import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class photo_painter extends PApplet {

public void setup(){
  
  surface.setResizable(true);

  frameRate(100);
  background(245, 245, 220); // beige (TODO: tile a canvas texture image)
  
  randomSeed(10);
  noFill();

  PICTURE = loadImage("pic12.jpg");
  
  updateGlobalsForResize();
}

public void draw(){
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

public void mouseClicked() {
  FOCUS_X = mouseX;
  FOCUS_Y = mouseY;
}
class Bristle {
  float angle, dist;
  Bristle(float angle, float dist) {
    this.angle = angle;
    this.dist = dist;
  }
  
  public void drawPath(ArrayList<Point> pathPoints, int pointIndexToDraw) {
    float targetX, targetY;
    int startIndex = max(0, pointIndexToDraw - POINTS_TO_DRAW_PER_FRAME);
    Point point = pathPoints.get(startIndex);

    beginShape();
    for (int p = startIndex; p < pointIndexToDraw; p++) {
      if (random(1.0f) < BRISTLE_NOISE) {
        point = pathPoints.get(p);
        targetX = point.x + (sin(angle) * (float)dist);
        targetY = point.y + (cos(angle) * (float)dist);
        curveVertex(targetX, targetY);
      } else {
        endShape();
        beginShape();
      }
    }
    endShape();
  }
}
class Brush {
  ArrayList<Point> pathPoints;
  ArrayList<Bristle> bristles;
  int strokeColor;
  int pointIndexToDraw;
  
  Brush() {
    this.strokeColor = color(random(255), random(255),random(255));
    initializeBristles();
    this.createStrokePath();
  }
 
  public void initializeBristles() {
    bristles = new ArrayList<Bristle>(MAX_BRUSH_BRISTLES);
    for (int b = 0; b < MAX_BRUSH_BRISTLES; b++) {
      float dist = random(1.0f) * BRUSH_RADIUS;
      this.bristles.add(new Bristle(random(2*PI), dist));
    }
  }
  
  public void update() {
    if (pointIndexToDraw >= pathPoints.size() - 1) {
      createStrokePath();
    } else {
      drawPath();
    }
  }
 
  public void createStrokePath() {
    int x = round(random(PIC_FOR_SAMPLING.width));
    int y = round(random(PIC_FOR_SAMPLING.height));

    boolean findSimilar = true;
    if (random(1.0f) > 0.90f) {
      findSimilar = false;
    }

    if (random(1) < 0.9f) {
      // 90% of the time, try to keep drawing a similar color
      int NUM_CANDIDATES = 50;
      float biggestDifference = -9999999.999f;
      float smallestDifference = 999999999.99f;
      for (int c = 0; c < NUM_CANDIDATES; c++) {
        int candidateX = round(random(PIC_FOR_SAMPLING.width));
        int candidateY = round(random(PIC_FOR_SAMPLING.height));
        int candidateColor = PIC_FOR_SAMPLING.get(candidateX, candidateY);
        float thisDistance = colorDist(candidateColor, strokeColor);
        if (findSimilar && thisDistance < smallestDifference) {
          smallestDifference = thisDistance;
          x = candidateX;
          y = candidateY;
        }
        if (!findSimilar && thisDistance > biggestDifference) {
          biggestDifference = thisDistance;
          x = candidateX;
          y = candidateY;
        }
      }
    }

    strokeColor = PIC_FOR_SAMPLING.get(x, y);
    int colorToMatch = strokeColor;
    
    pathPoints = new ArrayList<Point>();
    pathPoints.add(new Point(x, y));
    pointIndexToDraw = 0;
    
    int nextX = x;
    int nextY = y;
    while (true) {
      int nextColor = colorToMatch;
      float colorDist = 0.0f;
      float smallestDist = 999999999.99f;
      for (int x2 = nextX + 2; x2 >= nextX - 2; x2--) {
        if (x2 >= 0 && x2 < PIC_FOR_SAMPLING.width + 2) {
          for (int y2 = nextY - 2; y2 <= nextY + 2; y2++) {
            if (y2 >= 0 && y2 < PIC_FOR_SAMPLING.height + 2) {
              boolean alreadyUsed = false;
              for (int p = 0; p < pathPoints.size(); p++) {
                if (pathPoints.get(p).x == x2 && pathPoints.get(p).y == y2) {
                  alreadyUsed = true;
                  break;
                }
              }
              if (!alreadyUsed) {
                nextColor = PIC_FOR_SAMPLING.get(x2, y2);
                colorDist = colorDist(colorToMatch, nextColor);
                if (colorDist < smallestDist) {
                  smallestDist = colorDist;
                  nextX = x2;
                  nextY = y2;
                }
              }
            }
          }
        }
      }
      
      if (smallestDist > COLOR_DIST_THRESHOLD) {
        break;
      }
      if (pathPoints.size() > MAX_BRUSH_PATH_POINTS) {
        break;
      }
      pathPoints.add(new Point(nextX, nextY));
    }

    // scale all points to actual size of picture
    Point point;
    for (int p = 0; p < pathPoints.size(); p++) {
      point = pathPoints.get(p);
      pathPoints.set(p, new Point((point.x + 0.5f) / SIZE_RATIO, (point.y + 0.5f) / SIZE_RATIO));
    }
  }
  
  public void drawPath() {
    strokeWeight(STROKE_WEIGHT);
    int distFromEnd = pathPoints.size() - pointIndexToDraw;
    float opacity = (STROKE_OPACITY * STROKE_HARDNESS) + distFromEnd * (STROKE_OPACITY - (STROKE_OPACITY * STROKE_HARDNESS)) / NUM_BRUSH_POINTS_TO_SOFTEN;
    opacity = min(255, opacity);
    stroke(strokeColor, opacity);
    for (int b = 0; b < MAX_BRUSH_BRISTLES; b++) {
      bristles.get(b).drawPath(pathPoints, pointIndexToDraw);
    }
    pointIndexToDraw++;
  }
}
// calculate color distance between two colors
public float colorDist(int c1, int c2)
{
  float rmean =(red(c1) + red(c2)) / 2;
  float r = red(c1) - red(c2);
  float g = green(c1) - green(c2);
  float b = blue(c1) - blue(c2);
  return sqrt((PApplet.parseInt(((512+rmean)*r*r))>>8)+(4*g*g)+(PApplet.parseInt(((767-rmean)*b*b))>>8));
}
// Time and image sample size ratio dictate most other global values.
int TIME = 0;
int TOTAL_TIMES_TO_RESIZE = 100;
int NUM_RESIZES_REMAINING = TOTAL_TIMES_TO_RESIZE;
int TIME_TO_NEXT_RESIZE = 50; // doubles with each resize. 20, 40, 80, 160... 
float START_SIZE_RATIO = 0;
float END_SIZE_RATIO = 0.4f;
float SIZE_RATIO;
float SIZE_RATIO_INC;
float PERCENT_REMAINING;

// Brush / Bristle values
int NUM_BRUSHES = 1;
ArrayList<Brush> BRUSHES;
float STROKE_OPACITY = 0.0f;
float STROKE_HARDNESS = 0.0f; // controls opacity of brush stroke at end points
float NUM_BRUSH_POINTS_TO_SOFTEN = 20;
float STROKE_WEIGHT = 2.0f;
float BRUSH_RADIUS;
float BRISTLE_NOISE; // controls frequency of random gaps in bristle stroke
int MAX_BRUSH_BRISTLES;
int MAX_BRUSH_PATH_POINTS;
int MIN_BRUSH_PATH_POINTS = 20;
int POINTS_TO_DRAW_PER_FRAME = 6; // # of tail points to draw when animating stroke

// Color compare values
float MAX_COLOR_THRESHOLD = 40;
float MIN_COLOR_THRESHOLD = 1;
float COLOR_DIST_THRESHOLD;

// Focus constants
int FOCUS_X = 0;
int FOCUS_Y = 0;
float FOCUS_SIZE;
float CHANCE_FOCUS;

// Image constants
PImage PICTURE, PIC_FOR_SAMPLING;
boolean DID_RESIZE_SURFACE = false;

// Update all global variables based on t and our new size ratio
public void updateGlobalsForResize() {
  // SIZE_RATIO grows from START_SIZE_RATIO to END_SIZE_RATIO (linear)
  int min_picture_dimension = min(PICTURE.width, PICTURE.height);
  if (START_SIZE_RATIO == 0){
    START_SIZE_RATIO = 4.0f / min_picture_dimension;
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
  
  FOCUS_SIZE = ceil(0.2f * min_picture_dimension * SIZE_RATIO);
  CHANCE_FOCUS = 0.5f * (1.0f - PERCENT_REMAINING);

  // update brush constants
  NUM_BRUSHES = round(0.5f * (TOTAL_TIMES_TO_RESIZE - NUM_RESIZES_REMAINING)) + 1; // 1 --> 10    (linear)
  STROKE_WEIGHT = 1.0f * PERCENT_REMAINING + 1.0f;
  STROKE_HARDNESS = 0.5f * (1.0f - PERCENT_REMAINING);                                      // 100 --> 255  (linear)
  STROKE_OPACITY = 35.0f + 25.0f * (1.0f - PERCENT_REMAINING);
  MAX_BRUSH_PATH_POINTS = round((20 * MIN_BRUSH_PATH_POINTS) * (1 - PERCENT_REMAINING) + MIN_BRUSH_PATH_POINTS); // 10 --> 20     (linear)
  BRUSH_RADIUS = (1.0f + random(2.0f)) / (SIZE_RATIO);                           // 100 --> 1   (linear)
  MAX_BRUSH_BRISTLES = min(1000, round(1.0f * pow(BRUSH_RADIUS, 1.7f)));                                // 1000 --> 10 (linear)
  BRISTLE_NOISE = (0.2f * PERCENT_REMAINING) + 0.6f;

  // update brushes
  initializeBrushes();

  println(100 - PERCENT_REMAINING * 100 + "% done");
  println("sample width: " + PIC_FOR_SAMPLING.width + " height: " + PIC_FOR_SAMPLING.height);
  println("bristles: " + MAX_BRUSH_BRISTLES * NUM_BRUSHES);

  // update time and resize constants
  TIME_TO_NEXT_RESIZE *= 1.02f;
  TIME = 0;                                           // reset time
  NUM_RESIZES_REMAINING--;                            // decrement num times to resize
}

public void initializeBrushes() {
  BRUSHES = new ArrayList<Brush>(NUM_BRUSHES);
  for (int n = 0; n < NUM_BRUSHES; n++) {
    BRUSHES.add(new Brush());
  }
}

public void updateBrushes() {
  for (int n = 0; n < NUM_BRUSHES; n++) {
    BRUSHES.get(n).update();
  }
}
class Point {
  float x, y;
  Point(float x, float y) {
    this.x = x;
    this.y = y;
  }
}
  public void settings() {  size(100,100);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "photo_painter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
