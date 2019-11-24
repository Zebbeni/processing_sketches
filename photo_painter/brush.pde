class Brush {
  ArrayList<Point> pathPoints;
  ArrayList<Bristle> bristles;
  color strokeColor;
  int pointIndexToDraw;
  
  Brush() {
    this.strokeColor = color(random(255), random(255),random(255));
    initializeBristles();
    this.createStrokePath();
  }
 
  void initializeBristles() {
    bristles = new ArrayList<Bristle>(MAX_BRUSH_BRISTLES);
    for (int b = 0; b < MAX_BRUSH_BRISTLES; b++) {
      float dist = random(1.0) * BRUSH_RADIUS;
      this.bristles.add(new Bristle(random(2*PI), dist));
    }
  }
  
  void update() {
    if (pointIndexToDraw >= pathPoints.size() - 1) {
      createStrokePath();
    } else {
      drawPath();
    }
  }
 
  void createStrokePath() {
    int x = round(random(PIC_FOR_SAMPLING.width));
    int y = round(random(PIC_FOR_SAMPLING.height));

    boolean findSimilar = true;
    if (random(1.0) > 0.90) {
      findSimilar = false;
    }

    if (random(1) < 0.9) {
      // 90% of the time, try to keep drawing a similar color
      int NUM_CANDIDATES = 50;
      float biggestDifference = -9999999.999;
      float smallestDifference = 999999999.99;
      for (int c = 0; c < NUM_CANDIDATES; c++) {
        int candidateX = round(random(PIC_FOR_SAMPLING.width));
        int candidateY = round(random(PIC_FOR_SAMPLING.height));
        color candidateColor = PIC_FOR_SAMPLING.get(candidateX, candidateY);
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
    color colorToMatch = strokeColor;
    
    pathPoints = new ArrayList<Point>();
    pathPoints.add(new Point(x, y));
    pointIndexToDraw = 0;
    
    int nextX = x;
    int nextY = y;
    while (true) {
      color nextColor = colorToMatch;
      float colorDist = 0.0;
      float smallestDist = 999999999.99;
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
      pathPoints.set(p, new Point((point.x + 0.5) / SIZE_RATIO, (point.y + 0.5) / SIZE_RATIO));
    }
  }
  
  void drawPath() {
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