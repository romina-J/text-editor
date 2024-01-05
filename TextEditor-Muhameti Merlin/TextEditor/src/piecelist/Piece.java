package piecelist;

import java.io.File;

public class Piece { // descriptor specifies the text pieces and their order
    int len; // length of this piece
    File file; // file containing this piece
    int filePos; // offset from beginning of file
    Piece next;
    String font;
    int fontSize;
    int fontStyle;


    public Piece(int len, File file, int offset, String font, int fontSize, int fontStyle) {
        this.len = len;
        this.filePos = offset;
        this.file = file;
        this.font = font;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
    }

    public int getLen() {
        return len;
    }

    public File getFile() {
        return file;
    }

    public int getFilePos() {
        return filePos;
    }

    public Piece getNext() {
        return next;
    }

    public String getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    //Possible format of storing fonts and styles in a file:
    //File = textOffset {Piece} Text.
    //Piece = length font style.
}