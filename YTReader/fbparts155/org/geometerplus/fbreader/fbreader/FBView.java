/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext.WallpaperMode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

import com.yt.reader.base.BookView;

public final class FBView extends ZLTextView {
	public FBView() {
		super(ZLApplication.Instance());
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
	}

	BookView widget = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();

	public int leftMargin = 30;
	public int rightMargin = 30;
	public int topMargin = 40;
	public int bottomMargin = 50;
	private String str_day_night = "defaultLight";

	@Override
	public int getLeftMargin() {
		return leftMargin;
	}

	@Override
	public int getRightMargin() {
		return rightMargin;
	}

	@Override
	public int getTopMargin() {
		return topMargin;
	}

	@Override
	public int getBottomMargin() {
		return bottomMargin;
	}

	@Override
	public ZLColor getBackgroundColor() {
		return ColorProfile.get(str_day_night).BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedBackgroundColor() {
		return ColorProfile.get(str_day_night).SelectionBackgroundOption.getValue();
	}

	

	@Override
	public ZLColor getHighlightingColor() {
		return ColorProfile.get(str_day_night).HighlightingOption.getValue();
	}

	@Override
	public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = ColorProfile.get(str_day_night);
		switch (hyperlink.Type) {
			default:
			case FBHyperlinkType.NONE:
				return profile.RegularTextOption.getValue();
			case FBHyperlinkType.INTERNAL:
				/*return myReader.Model.Book.isHyperlinkVisited(hyperlink.Id)
					? profile.VisitedHyperlinkTextOption.getValue()
					: profile.HyperlinkTextOption.getValue();*/
			case FBHyperlinkType.EXTERNAL:
				return profile.HyperlinkTextOption.getValue();
		}
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;
	//滚动条设置
	@Override
	public int scrollbarType() {
		return FBView.SCROLLBAR_SHOW_AS_FOOTER;
	}

	@Override
	public ZLColor getSelectedForegroundColor() {
		return null;
	}

	@Override
	public ZLFile getWallpaperFile() {
		return null;
	}

	@Override
	public WallpaperMode getWallpaperMode() {
		return null;
	}
}
