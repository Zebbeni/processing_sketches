class Bristle {
  float angle, dist;
  Bristle(float angle, float dist) {
    this.angle = angle;
    this.dist = dist;
  }
  
  void drawPath(ArrayList<Point> pathPoints, int pointIndexToDraw) {
    float targetX, targetY;
    int startIndex = max(0, pointIndexToDraw - POINTS_TO_DRAW_PER_FRAME);
    Point point = pathPoints.get(startIndex);

    beginShape();
    for (int p = startIndex; p < pointIndexToDraw; p++) {
      if (random(1.0) < BRISTLE_NOISE) {
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
