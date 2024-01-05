package viewer;

import piecelist.Piece;
import piecelist.PieceListText;
import piecelist.UpdateEvent;
import piecelist.UpdateEventListener;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.swing.JScrollBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**********************************************************************
 *  Text Window
 **********************************************************************/

public class Viewer extends Canvas implements AdjustmentListener, UpdateEventListener {
    static final int TOP = 5;    // top margin
    static final int BOTTOM = 5; // bottom margin
    static final int LEFT = 5;   // left margin
    static final int EOF = '\0';
    static final String CRLF = "\r\n";
    static final int LF = '\n';
    static final int tabSize = 25;

    public static Piece clipboardFrom;
    public static Piece clipboardTo;

    PieceListText text;
    Line firstLine = null; // the lines in this viewer
    int firstTpos = 0;     // first text position in this viewer
    int lastTpos;          // last text position in this viewer
    JScrollBar scrollBar;
    Selection sel = null;
    Position caret;
    Position lastPos;   // last mouse position: used during mouse dragging
    Graphics g;
    private String _font;
    private int fontSize;
    private int fontStyle;

    public void setFont(String font) {
        _font = font;
        if (sel != null) {
            text.setFont(sel.beg.tpos, sel.end.tpos, font);
        }
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        if (sel != null) {
            text.setSize(sel.beg.tpos, sel.end.tpos, fontSize);
        }
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
        if (sel != null) {
            text.setStyle(sel.beg.tpos, sel.end.tpos, fontStyle);
        }
    }

    public int getFontStyle() {
        return this.fontStyle;
    }

    public void cut() {
        if (sel == null) {
            return;
        }
        clipboardFrom = text.delete(sel.beg.tpos, sel.end.tpos, _font, fontSize, fontStyle);
        clipboardTo = null;
        removeSelection();
    }

    public void copy() {
        //setCaret();
        int from = sel.beg.tpos;
        int to = sel.end.tpos;
        removeSelection();
        clipboardFrom = text.split(from).getNext();
        clipboardTo = text.split(to).getNext();
    }

    public void paste() {
        if (caret == null) {
            text.delete(sel.beg.tpos, sel.end.tpos, _font, fontSize, fontStyle);
        }

        Piece buff = clipboardFrom;
        String st = "";
        while (buff != clipboardTo) {
            char c = '\0';
            for (int i = 0; i < buff.getLen(); i++) {
                try {
                    FileInputStream s = new FileInputStream(buff.getFile());
                    InputStreamReader r = new InputStreamReader(s, StandardCharsets.ISO_8859_1);
                    r.skip(buff.getFilePos() + i);
                    c = (char) r.read();
                    r.close();
                    s.close();
                    st += c;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (caret == null) {
                    text.insert(sel.beg.tpos, c, buff.getFont(), buff.getFontSize(), buff.getFontStyle());
                } else {
                    text.insert(caret.tpos, c, buff.getFont(), buff.getFontSize(), buff.getFontStyle());
                }
            }
            buff = buff.getNext();
        }
    }

    public void search() {
        if (sel == null) {
            return;
        }

        Piece buff = text.getFirstPiece();
        String st = "";
        String searchTerm = "";
        for (int i = sel.beg.tpos; i < sel.end.tpos; i++) {
            searchTerm += text.charAt(i);
        }

        do {
            char c = '\0';
            for (int i = 0; i < buff.getLen(); i++) {
                try {
                    FileInputStream s = new FileInputStream(buff.getFile());
                    InputStreamReader r = new InputStreamReader(s, StandardCharsets.ISO_8859_1);
                    r.skip(buff.getFilePos() + i);
                    c = (char) r.read();
                    r.close();
                    s.close();
                    st += c;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<Integer> positions = new ArrayList<Integer>();
            int startIndex = 0;

            while (startIndex < st.length()) {
                int foundIndex = st.indexOf(searchTerm, startIndex);

                if (foundIndex != -1) {
                    positions.add(foundIndex);
                    // Move startIndex to just after the found substring
                    startIndex = foundIndex + searchTerm.length();
                } else {
                    // No more occurrences found, exit the loop
                    break;
                }
            }
            if (positions.size() > 0) {
                int pos;
                if (positions.size() == 1) {
                    pos = positions.get(0);
                    int globPos = buff.getFilePos() + pos;
                    if (globPos != sel.beg.tpos) {
                        sel.beg.tpos = globPos;
                        sel.end.tpos = globPos + searchTerm.length();
                    } else {
                        sel.end.tpos = sel.beg.tpos + searchTerm.length();
                    }
                } else {
                    pos = positions.get(1);
                    int globPos = buff.getFilePos() + pos;
                    if (globPos != sel.beg.tpos) {
                        sel.beg.tpos = globPos;
                        sel.end.tpos = globPos + searchTerm.length();
                    } else {
                        sel.end.tpos = sel.beg.tpos + searchTerm.length();
                    }
                }
                break;
            }
            buff = buff.getNext();
        } while (buff != null);

    }

    public Viewer(PieceListText t, JScrollBar sb, String font, int fontSize, int fontStyle) {
        this._font = font;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        scrollBar = sb;
        scrollBar.addAdjustmentListener(this);
        scrollBar.setMaximum(t.length());
        scrollBar.setUnitIncrement(50);
        scrollBar.setBlockIncrement(500);
        text = t;
        text.addUpdateEventListener(this);
        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                doKeyTyped(e);
            }

            public void keyPressed(KeyEvent e) {
                doKeyPressed(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                doMousePressed(e);
            }

            public void mouseReleased(MouseEvent e) {
                doMouseReleased(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                doMouseDragged(e);
            }
        });
        // disable TAB as a focus traversal key
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    }

    /*------------------------------------------------------------
     *  reaction to scrolling events
     *-----------------------------------------------------------*/

    public void adjustmentValueChanged(AdjustmentEvent e) {
        int pos = e.getValue();
        if (pos > 0) { // find start of line
            char ch;
            do {
                ch = text.charAt(--pos);
            } while (pos > 0 && ch != '\n');
            if (pos > 0)
                pos++;
        }
        if (pos != firstTpos) { // scroll
            Position caret0 = caret;
            Selection sel0 = sel;
            removeSelection();
            removeCaret();
            firstTpos = pos;
            firstLine = fill(TOP, getHeight() - BOTTOM, firstTpos);
            repaint();
            if (caret0 != null) setCaret(caret0.tpos);
            if (sel0 != null) setSelection(sel0.beg.tpos, sel0.end.tpos);
        }
    }

    /*------------------------------------------------------------
     *  position handling
     *-----------------------------------------------------------*/

    private Position Pos(int textPos) {
        if (textPos < firstTpos) textPos = firstTpos;
        if (textPos > lastTpos) textPos = lastTpos;
        Position pos = new Position();
        Line line = firstLine, last = null;
        pos.org = firstTpos;
        while (line != null && textPos >= pos.org + line.len) {
            pos.org += line.len;
            last = line;
            line = line.next;
        }
        if (line == null) {
            pos.x = last.x + last.w;
            pos.y = last.base;
            pos.line = last;
            pos.org -= last.len;
            pos.off = last.len;
        } else {
            pos.x = line.x;
            pos.y = line.base;
            pos.line = line;
            pos.off = textPos - pos.org;
            int i = pos.org;
            while (i < textPos) {
                g.setFont(text.fontAt(i));
                FontMetrics m = g.getFontMetrics();
                char ch = text.charAt(i);
                i++;
                pos.x += charWidth(m, ch);
            }
        }
        pos.tpos = pos.org + pos.off;
        return pos;
    }

    private Position Pos(int x, int y) {
        Position pos = new Position();
        if (y >= getHeight() - BOTTOM) y = getHeight() - BOTTOM - 1;
        Line line = firstLine, last = null;
        pos.org = firstTpos;
        while (line != null && y >= line.y + line.h) {
            pos.org += line.len;
            last = line;
            line = line.next;
        }
        if (line == null) {
            line = last;
            pos.org -= last.len;
        }
        pos.y = line.base;
        pos.line = line;
        if (x >= line.x + line.w) {
            pos.x = line.x + line.w;
            pos.off = line.len;
            if (pos.org + line.len < text.length()) pos.off -= 1;//remove 1 because of LF
        } else {
            pos.x = line.x;
            int i = pos.org;
            char ch = text.charAt(i);
            g.setFont(text.fontAt(i));
            FontMetrics m = g.getFontMetrics();
            int w = charWidth(m, ch);
            while (x >= pos.x + w) {
                pos.x += w;
                i++;
                ch = text.charAt(i);
                g.setFont(text.fontAt(i));
                m = g.getFontMetrics();
                w = charWidth(m, ch);
            }
            pos.off = i - pos.org;
        }
        pos.tpos = pos.org + pos.off;
        return pos;
    }

    /*------------------------------------------------------------
     *  caret handling
     *-----------------------------------------------------------*/

    private void invertCaret() {
        g = getGraphics();
        g.setXORMode(Color.WHITE);
        int x = caret.x;
        int y = caret.y;
        g.drawLine(x, y, x, y + 3);
        x++;
        y++;
        g.drawLine(x, y, x, y + 2);
        x++;
        y++;
        g.drawLine(x, y, x, y + 1);
        x++;
        y++;
        g.drawLine(x, y, x, y);
        g.setPaintMode();
    }

    private void setCaret(Position pos) {
        removeCaret();
        removeSelection();
        caret = pos;
        invertCaret();
    }

    public void setCaret(int tpos) {
        if (tpos >= firstTpos && tpos <= lastTpos) {
            setCaret(Pos(tpos));
        }
    }

    public void setCaret(int x, int y) {
        setCaret(Pos(x, y));
    }

    public void removeCaret() {
        if (caret != null) invertCaret();
        caret = null;
    }

    /*------------------------------------------------------------
     *  selection handling
     *-----------------------------------------------------------*/

    private void invertSelection(Position beg, Position end) {
        g = getGraphics();
        g.setXORMode(Color.WHITE);
        Line line = beg.line;
        int x = beg.x;
        int y = line.y;
        int w;
        int h = line.h;
        while (line != end.line) {
            w = line.w + LEFT - x;
            g.fillRect(x, y, w, h);
            line = line.next;
            x = line.x;
            y = line.y;
        }
        w = end.x - x;
        g.fillRect(x, y, w, h);
        g.setPaintMode();
    }

    public void setSelection(int from, int to) {
        if (from < to) {
            removeCaret();
            Position beg = Pos(from);
            Position end = Pos(to);
            sel = new Selection(beg, end);
            invertSelection(beg, end);
        } else sel = null;
    }

    public void removeSelection() {
        if (sel != null) invertSelection(sel.beg, sel.end);
        sel = null;
    }

    /*------------------------------------------------------------
     *  keyboard handling
     *-----------------------------------------------------------*/

    private void doKeyTyped(KeyEvent e) {
        boolean selection = sel != null;
        if (selection) {
            text.delete(sel.beg.tpos, sel.end.tpos, _font, fontSize, fontStyle);
            // selection is removed; caret is set at sel.beg.tpos
        }
        if (caret != null) {
            char ch = e.getKeyChar();
            if (ch == KeyEvent.VK_BACK_SPACE) {
                if (caret.tpos > 0 && !selection) {
                    text.delete(caret.tpos - 1, caret.tpos, _font, fontSize, fontStyle);
                }
            } else if (ch == KeyEvent.VK_ESCAPE) {
            } else if (ch == KeyEvent.VK_ENTER) {
                text.insert(caret.tpos, (char) LF, _font, fontSize, fontStyle);
            } else {
                text.insert(caret.tpos, ch, _font, fontSize, fontStyle);
            }
            scrollBar.setValues(firstTpos, 0, 0, text.length());
        }
    }

    private void doKeyPressed(KeyEvent e) {
        if (caret != null) {
            int key = e.getKeyCode();
            int pos = caret.tpos;
            char ch;
            if (key == KeyEvent.VK_RIGHT) {
                pos++;
                ch = text.charAt(pos);
                if (ch == '\n') pos++;
                setCaret(pos);
            } else if (key == KeyEvent.VK_LEFT) {
                pos--;
                ch = text.charAt(pos);
                if (ch == '\n') pos--;
                setCaret(pos);
            } else if (key == KeyEvent.VK_UP) {
                setCaret(caret.x, caret.y - caret.line.h);
            } else if (key == KeyEvent.VK_DOWN) {
                setCaret(caret.x, caret.y + caret.line.h);
            } else if (key == KeyEvent.VK_F1) {
            }
        }
    }

    /*------------------------------------------------------------
     *  mouse handling
     *-----------------------------------------------------------*/

    private void doMousePressed(MouseEvent e) {
        removeCaret();
        removeSelection();
        Position pos = Pos(e.getX(), e.getY());

        if (e.getClickCount() == 2) {
            setSelection(text.getFirstLeftPos(pos.tpos), text.getFirstRightPos(pos.tpos));
        } else {
            sel = new Selection(pos, pos);
            lastPos = pos;
        }
    }

    private void doMouseDragged(MouseEvent e) {
        if (sel == null) return;
        Position pos = Pos(e.getX(), e.getY());
        if (pos.tpos < sel.beg.tpos) {
            if (lastPos.tpos >= sel.end.tpos) {
                invertSelection(sel.beg, lastPos);
                sel.end = sel.beg;
            }
            invertSelection(pos, sel.beg);
            sel.beg = pos;
        } else if (pos.tpos > sel.end.tpos) {
            if (lastPos.tpos <= sel.beg.tpos) {
                invertSelection(lastPos, sel.end);
                sel.beg = sel.end;
            }
            invertSelection(sel.end, pos);
            sel.end = pos;
        } else if (pos.tpos < lastPos.tpos) { // beg <= pos <= end; clear pos..end
            invertSelection(pos, sel.end);
            sel.end = pos;
        } else if (lastPos.tpos < pos.tpos) { // beg <= pos <= end; clear beg..pos
            invertSelection(sel.beg, pos);
            sel.beg = pos;
        }
        lastPos = pos;
    }

    private void doMouseReleased(MouseEvent e) {
        if (sel.beg.tpos == sel.end.tpos) {
            setCaret(sel.beg);

            notify(new FontEvent(text.fontAt(caret.tpos)));
        }
        lastPos = null;
    }

    ArrayList listeners = new ArrayList();

    public void addFontEventListener(FontEventListener listener) {
        listeners.add(listener);
    }

    public void removeFontEventListener(FontEventListener listener) {
        listeners.remove(listener);
    }

    private void notify(FontEvent e) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            FontEventListener listener = (FontEventListener) iter.next();
            listener.update(e);
        }
    }

    public interface FontEventListener {
        void update(FontEvent e);
    }

    public class FontEvent {
        Font font;

        FontEvent(Font f) {
            font = f;
        }

        ;
    }

    /*------------------------------------------------------------
     *  TAB handling
     *-----------------------------------------------------------*/

    private int charWidth(FontMetrics m, char ch) {
        if (ch == '\t') return 4 * m.charWidth(' ');
        else return m.charWidth(ch);
    }

    private int stringWidth(FontMetrics m, String s) {
        String s1 = s.replaceAll("\t", "    ");
        return m.stringWidth(s1);
    }

    private void drawLine(Graphics g, Line line) {
        int floatingX = line.x;
        for (CharFont cf : line.text) {
            g.setFont(cf.font);
            if (cf.c == '\t') {
                FontMetrics m = g.getFontMetrics();
                floatingX += 4 * m.charWidth(' ');
            } else {
                g.drawString(String.valueOf(cf.c), floatingX, line.base);
                floatingX += g.getFontMetrics().charWidth(cf.c);
            }
        }
    }

    private int charFontListWidth(Graphics g, LinkedList<CharFont> chars) {
        int i = 0;
        int lineWidth = 0;
        for (CharFont cf : chars) {
            g.setFont(cf.font);
            lineWidth += charWidth(g.getFontMetrics(), cf.c);
        }
        return lineWidth;
    }

    /*------------------------------------------------------------
     *  line handling
     *-----------------------------------------------------------*/

    private void updateLineMetrics(Graphics g, Line line) {
        int lineWidth = 0;
        int lineHeight = 0;
        int lineAscent = 0;
        int maxLineHeight = 0;
        for (CharFont cf : line.text) {
            g.setFont(cf.font);
            FontMetrics m = g.getFontMetrics();
            lineWidth += charWidth(m, cf.c);
            lineHeight = lineHeight > m.getHeight() ? lineHeight : m.getHeight();
            lineAscent = lineAscent > m.getAscent() ? lineAscent : m.getAscent();
        }
        line.w = lineWidth;
        line.h = lineHeight;
        line.base = lineAscent;
        if (line.h == 0) {
            g.setFont(new Font(_font, fontStyle, fontSize));
            FontMetrics m = g.getFontMetrics();
            line.h = m.getHeight();
            line.base = m.getAscent();
        }
    }

    private Line fill(int top, int bottom, int pos) {
        g = getGraphics();
        Line first = null, line = null;
        int y = top;
        lastTpos = pos;
        char ch = text.charAt(pos);
        Font ft = text.fontAt(pos);
        while (y < bottom) {
            if (first == null) {
                first = line = new Line();
            } else {
                Line prev = line;
                line.next = new Line();
                line = line.next;
                line.prev = prev;
            }
            StringBuffer buf = new StringBuffer();
            while (ch != '\n' && ch != EOF) {
                buf.append(ch);
                line.text.add(new CharFont(ch, ft));
                pos++;
                ch = text.charAt(pos);
                ft = text.fontAt(pos);
            }

            boolean eol = ch == '\n';
            if (eol) {
                buf.append(ch);
                line.text.add(new CharFont(ch, ft));
                pos++;
                ch = text.charAt(pos);
                ft = text.fontAt(pos);
            }

            line.len = buf.length();
            line.x = LEFT;
            line.y = y;
            updateLineMetrics(g, line);

            line.base += y;
            y += line.h;
            lastTpos += line.len;

            if (!eol) break;
        }
        return first;
    }

    private void rebuildFrom(Position pos) {
        Line line = pos.line;
        Line prev = line.prev;
        line = fill(line.y, getHeight() - BOTTOM, pos.org);
        if (prev == null) firstLine = line;
        else {
            prev.next = line;
            line.prev = prev;
        }
        repaint(LEFT, line.y, getWidth(), getHeight());
    }

    /*------------------------------------------------------------
     *  text drawing
     *-----------------------------------------------------------*/

    public void update(UpdateEvent e) {
        g = getGraphics();
        Position pos = caret;

        if (e.getFrom() == e.getTo()) { // insert
            if (e.getFrom() != caret.tpos) pos = Pos(e.getFrom());
            int newCarPos = pos.tpos + e.getText().length();
            if (e.getText().indexOf(LF) >= 0) {
                rebuildFrom(pos);
                if (pos.y + pos.line.h > getHeight() - BOTTOM)
                    scrollBar.setValue(firstTpos + firstLine.len);
            } else {

                int i = 0;
                for (char c : e.getText().toCharArray()) {
                    pos.line.text.add(pos.off + i, new CharFont(c, new Font(e.getFont(), e.getFontStyle(), e.getFontSize())));
                    i++;
                }

                //updateLineMetrics(g, pos.line);
                g.setFont(new Font(e.getFont(), e.getFontStyle(), e.getFontSize()));
                pos.line.w += stringWidth(g.getFontMetrics(), e.getText());
                pos.line.len += e.getText().length();
                lastTpos += e.getText().length();
                repaint(pos.line.x, pos.line.y, getWidth(), pos.line.h + 1);
            }
            setCaret(newCarPos);

        } else if (e.getText() == null && !e.getFontChanged()) { // delete
            if (caret == null || e.getTo() != caret.tpos) pos = Pos(e.getTo());
            int d = e.getTo() - e.getFrom();
            if (pos.off - d < 0) { // delete across lines
                rebuildFrom(Pos(e.getFrom()));
            } else {

                for (int i = pos.off - d; i < pos.off; i++) {
                    if (pos.line.text.size() <= i) {
                        break;
                    }
                    CharFont removed = pos.line.text.remove(i);
                    int abc = 10;
                    abc++;
                }

                updateLineMetrics(g, pos.line);
                pos.line.len -= d;
                lastTpos -= d;
                repaint(pos.line.x, pos.line.y, getWidth(), pos.line.h + 1);
                rebuildFrom(Pos(e.getFrom()));
            }
            setCaret(e.getFrom());
        } else if (e.getFontChanged()) { //font changed
            rebuildFrom(Pos(e.getFrom()));
        }
    }

    public void paint(Graphics g) {
        this.g = g;
        if (firstLine == null) {
            firstLine = fill(TOP, getHeight() - BOTTOM, 0);
            caret = Pos(0);
        }
        Line line = firstLine;
        while (line != null) {
            //drawString(g, line.text, line.x, line.base);
            drawLine(g, line);
            line = line.next;
        }
        if (caret != null) invertCaret();
        if (sel != null) invertSelection(sel.beg, sel.end);
    }

    public void save() {
        text.save();
        rebuildFrom(Pos(0));
        setCaret(0);
    }
}