package piecelist;

public class UpdateEvent {  // [from..to[ was replaced by text
    int from;
    int to;
    String text;
    String font;
    int fontSize;
    int fontStyle;
    boolean fontChanged;

    public UpdateEvent(int a, int b, String t, String font, int fontSize, int fontStyle, boolean fontChanged) {
        from = a;
        to = b;
        text = t;
        this.font = font;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.fontChanged = fontChanged;
    }

    //getters and setters
    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getText() {
        return text;
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

    public boolean getFontChanged() {
        return fontChanged;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

    public void setFontChanged(boolean fontChanged) {
        this.fontChanged = fontChanged;
    }
}