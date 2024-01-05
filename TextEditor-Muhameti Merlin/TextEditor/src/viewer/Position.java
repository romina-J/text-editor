package viewer;

public class Position {
    Line line; // line containing this position
    int x, y;  // base line point corresponding to this position
    int tpos;  // text position (relative to start of text)
    int org;   // origin (text position of first character in this line)
    int off;   // text offset from org
}