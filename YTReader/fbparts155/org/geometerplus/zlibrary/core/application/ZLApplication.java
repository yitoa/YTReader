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

package org.geometerplus.zlibrary.core.application;

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import com.yt.reader.base.BookView;

public class ZLApplication {
	public static ZLApplication Instance() {
		return ourInstance;
	}

	private static ZLApplication ourInstance;

	private volatile ZLView myView;


	public ZLApplication() {
		ourInstance = this;
	}

	public final void setView(ZLView view) {
		if (view != null) {
			myView = view;
			final BookView widget = getViewWidget();
			if (widget != null) {
				widget.postInvalidate();
			}
		}
	}

	public final ZLView getCurrentView() {
		return myView;
	}


	public final BookView getViewWidget() {
		return getLibrary().getWidget();
	}
	
	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}
}
