package piecelist;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;

public class PieceListText {
    int len; // total text length
    Piece firstPiece; // first piece of the text
    File scratch; // scratch file

    public PieceListText(String path, String font, int fontSize, int fontStyle) {
        init(path, font, fontSize, fontStyle);
    }

    public Piece getFirstPiece() {
        return firstPiece;
    }

    private void init(String path, String font, int fontSize, int fontStyle) {

        File f = new File(path);
        if (f.exists()) {
            if (path.endsWith(".txt")) {
                int iOffset = -1;
                String header = "";
                try {
                    FileInputStream s = new FileInputStream(f);
                    InputStreamReader r = new InputStreamReader(s, StandardCharsets.ISO_8859_1);
                    int c;
                    String offset = "";
                    boolean fontsreached = false;
                    header = "";
                    int buffLen = 0;
                    while (true) {

                        c = r.read();
                        if (c == -1) {
                            break;
                        }
                        if (c == ';' && !fontsreached) {
                            iOffset = Integer.valueOf(offset) + 1 + offset.length();
                            fontsreached = true;
                            continue;
                        }

                        if (fontsreached) {
                            header += (char) c;
                            if (buffLen == iOffset - 2 - offset.length()) {
                                break;
                            }
                            buffLen++;
                        } else {
                            offset += (char) c;
                        }
                    }

                    r.close();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (iOffset == -1) {
                    firstPiece = new Piece(0, f, 0, font, fontSize, fontStyle);
                    len = 0;
                } else {
                    int fulllen = 0;
                    firstPiece = new Piece(0, f, iOffset, font, fontSize, fontStyle);
                    String[] splitHeader = header.split(";");
                    Piece buff = firstPiece;
                    for (int i = 0; i < splitHeader.length; i += 4) {
                        Piece p = new Piece(Integer.valueOf(splitHeader[i]), f, buff.filePos + buff.len, splitHeader[i + 1], Integer.valueOf(splitHeader[i + 3]), Integer.valueOf(splitHeader[i + 2]));
                        fulllen += p.len;
                        buff.next = p;
                        buff = buff.next;
                    }
                    len = fulllen;

                }
            } else {
                firstPiece = new Piece(0, f, 0, font, fontSize, fontStyle);
                firstPiece.next = new Piece((int) f.length(), f, 0, font, fontSize, fontStyle);
                len = firstPiece.next.len;
            }

        } else {
            firstPiece = new Piece(0, f, 0, font, fontSize, fontStyle);
            len = 0;
        }
        scratch = new File(path + "~");
        scratch.delete();
    }

    public void setFont(int from, int to, String font) {
        if (from == to) {
            return;
        }
        Piece a = split(from);
        Piece b = split(to).next;
        Piece buff = a.next;
        buff.font = font;
        while (buff != b) {
            buff.font = font;
            buff = buff.next;
        }
        notify(new UpdateEvent(from, to, null, font, -1, -1, true));
    }

    public void setStyle(int from, int to, int style) {
        if (from == to) {
            return;
        }
        Piece a = split(from);
        Piece b = split(to).next;
        Piece buff = a.next;
        buff.fontStyle = style;
        while (buff != b) {
            buff.fontStyle = style;
            buff = buff.next;
        }
        notify(new UpdateEvent(from, to, null, null, -1, style, true));
    }

    public void setSize(int from, int to, int size) {
        if (from == to) {
            return;
        }
        Piece a = split(from);
        Piece b = split(to).next;
        Piece buff = a.next;
        buff.fontSize = size;
        while (buff != b) {
            buff.fontSize = size;
            buff = buff.next;
        }
        notify(new UpdateEvent(from, to, null, null, size, -1, true));
    }

    public void insert(int pos, int ch, String font, int fontSize, int fontStyle) {
        if (pos > len || pos < 0) {
            return;
        }

        Piece p = split(pos); // split piece at pos
        int scratchlen = (int) scratch.length();

        if (p.file != scratch || p.font != font || p.fontSize != fontSize || p.fontStyle != fontStyle || (p.file == scratch && p.filePos + p.len != scratchlen)) {
            Piece q = new Piece(0, scratch, (int) scratch.length(), font, fontSize, fontStyle);
            q.next = p.next;
            p.next = q;
            p = q;
        }

        // p is last piece on scratch file
        try {
            FileOutputStream s = new FileOutputStream(scratch, true);
            OutputStreamWriter r = new OutputStreamWriter(s, StandardCharsets.ISO_8859_1);
            r.write((char) ch);
            r.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.len++;
        len++;
        notify(new UpdateEvent(pos, pos, Character.toString((char) ch), font, fontSize, fontStyle, false));
    }

    public Piece split(int pos) {
        if (pos == 0) return firstPiece;
        //--- set p to piece containing pos
        Piece p = firstPiece;
        int len = p.len;
        while (pos > len) {
            p = p.next;
            len = len + p.len;
        }
        //--- split piece p
        if (pos != len) {
            int len2 = len - pos;
            int len1 = p.len - len2;
            p.len = len1;
            Piece q = new Piece(len2, p.file, p.filePos + len1, p.font, p.fontSize, p.fontStyle);
            q.next = p.next;
            p.next = q;
        }
        return p;
    }

    public int length() {
        return len;
    }

    public Font fontAt(int pos) {
        if (pos < 0 || pos >= len || firstPiece.next == null) return null;
        Piece buff = firstPiece.next;
        int lenBuff = buff.len;

        while (pos >= lenBuff && buff.next != null) {
            buff = buff.next;
            lenBuff += buff.len;
        }
        return new Font(buff.font, buff.fontStyle, buff.fontSize);
    }

    public int getFirstLeftPos(int pos) {
        while (pos != 0) {
            char c = charAt(pos);
            if (!((c <= 90 && c >= 65) || (c <= 122 && c >= 97))) {
                return pos + 1;
            }
            pos--;
        }
        return pos;
    }

    public int getFirstRightPos(int pos) {
        char c = (char) -1;
        while (c != '\0') {
            c = charAt(pos);
            if (!((c <= 90 && c >= 65) || (c <= 122 && c >= 97))) {
                return pos;
            }
            pos++;
        }
        return pos;
    }

    public char charAt(int pos) {
        if (pos < 0 || pos >= len || firstPiece.next == null) return '\0';
        Piece buff = firstPiece.next;
        int lenBuff = buff.len;

        while (pos >= lenBuff && buff.next != null) {
            buff = buff.next;
            lenBuff += buff.len;
        }
        pos -= (lenBuff - buff.len);
        int c = '\0';
        try {
            FileInputStream s = new FileInputStream(buff.file);
            InputStreamReader r = new InputStreamReader(s, StandardCharsets.ISO_8859_1);
            r.skip(buff.filePos + pos);
            c = r.read();
            r.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (c == -1) {
            return '\0';
        }
        return (char) c;
    }

    public Piece delete(int from, int to, String font, int fontSize, int fontStyle) {
        if (to > len || from == to) {
            return null;
        }
        Piece a = split(from);
        Piece b = split(to);
        Piece deleted = a.next;
        Piece buff = deleted;
        a.next = b.next;
        while (buff != a.next) {
            len -= buff.len;
            buff = buff.next;
        }
        notify(new UpdateEvent(from, to, null, font, fontSize, fontStyle, false));
        b.next = null;
        return deleted;
    }

    public void save() {
        Piece p = firstPiece.next;

        String fontHeader = "";
        File tempFile = new File("1"), tempFileHeader = new File("2");
        int buffLen = 0;

        while (p != null) {
            String pHeader = "";
            buffLen += p.len;

            if (p.next == null || !(p.font == p.next.font && p.fontStyle == p.next.fontStyle && p.fontSize == p.next.fontSize)) {
                pHeader = buffLen + ";";
                pHeader += p.font + ";";
                pHeader += p.fontStyle + ";";
                pHeader += p.fontSize + ";";
                fontHeader += pHeader;
                buffLen = 0;
            }

            try {
                FileInputStream fr = new FileInputStream(p.file);
                InputStreamReader r = new InputStreamReader(fr, StandardCharsets.ISO_8859_1);
                char[] cbuf = new char[p.len + p.filePos];
                r.read(cbuf, 0, p.filePos + p.len);
                r.close();
                fr.close();

                FileOutputStream fw = new FileOutputStream(tempFile, true);
                OutputStreamWriter w = new OutputStreamWriter(fw, StandardCharsets.ISO_8859_1);
                String st = new String(cbuf);
                st = st.substring(p.filePos, p.filePos + p.len);
                w.write(st);
                w.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            p = p.next;
        }


        try {

            FileOutputStream fw = new FileOutputStream(tempFileHeader, true);
            OutputStreamWriter w = new OutputStreamWriter(fw, StandardCharsets.ISO_8859_1);
            w.write(fontHeader.length() + ";");
            w.write(fontHeader);

            FileInputStream fr = new FileInputStream(tempFile);
            InputStreamReader r = new InputStreamReader(fr, StandardCharsets.ISO_8859_1);
            int c = r.read();
            StringBuilder t = new StringBuilder(Character.toString(((char) c)));
            while (c != -1 && c != '\0') {
                w.write(c);
                c = r.read();
                t.append((char) c);
            }
            w.close();
            fw.close();
            r.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.move(tempFileHeader.toPath(), firstPiece.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempFile.delete();
        scratch.delete();
        firstPiece.next = null;
        init(firstPiece.file.getAbsolutePath(), firstPiece.font, firstPiece.fontSize, firstPiece.fontStyle);
    }
    /*-------------------------------------------------------------------
     **  notification of listeners
     **-----------------------------------------------------------------*/

    ArrayList listeners = new ArrayList();

    public void addUpdateEventListener(UpdateEventListener listener) {
        listeners.add(listener);
    }

    public void removeUpdateEventListener(UpdateEventListener listener) {
        listeners.remove(listener);
    }

    private void notify(UpdateEvent e) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            UpdateEventListener listener = (UpdateEventListener) iter.next();
            listener.update(e);
        }
    }

}