package viewer;

import java.util.LinkedList;

public class Line {
    //The text positions of lines are not stored so that they do not have be updated during editing
    //String text;    // text of this line
    LinkedList<CharFont> text = new LinkedList<CharFont>();
    int len;        // length of this line (including CRLF)
    int x, y, w, h; // top left corner, width, height
    int base;       // base line
    Line prev, next;

}