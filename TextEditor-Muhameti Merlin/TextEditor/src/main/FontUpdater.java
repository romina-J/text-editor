package main;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import viewer.Viewer.FontEvent;
import viewer.Viewer.FontEventListener;

public class FontUpdater implements FontEventListener{
	JComboBox<String> fontcb = null;
	 JComboBox<String> size  = null;
	 JCheckBox cBold = null;
	 JCheckBox cItalic = null;
	public FontUpdater(JComboBox<String> fontcb, JComboBox<String> size , JCheckBox cBold, JCheckBox cItalic) {
		this.fontcb = fontcb;
		this.size = size;
		this.cBold = cBold;
		this.cItalic = cItalic;
	}
	@Override
	public void update(FontEvent e) {
		System.out.println("FontUpdater.update()");
		/*
		Editor.bUpdating = true;
		if(e.font == null){
			return;
		}
		fontcb.setSelectedItem(e.font.getFontName());
		size.setSelectedItem(String.valueOf(e.font.getSize()));
		cBold.setSelected(e.font.isBold());
		cItalic.setSelected(e.font.isItalic());
		Editor.bUpdating = false;

		 */
	}
}
