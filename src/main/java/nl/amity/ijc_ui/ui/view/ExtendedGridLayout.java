/**
 * Copyright (C) 2016 Leo van der Meulen
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3.0
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * See: http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Problemen in deze code:
 * - ...
 */
package nl.amity.ijc_ui.ui.view;

import java.awt.*;

//Grid Layout which allows components of different sizes
public class ExtendedGridLayout extends GridLayout {
	private static final long serialVersionUID = -8955988731671279921L;

	public ExtendedGridLayout() {
		this(1, 0, 0, 0);
	}

	public ExtendedGridLayout(int rows, int cols) {
		this(rows, cols, 0, 0);
	}

	public ExtendedGridLayout(int rows, int cols, int hgap, int vgap) {
		super(rows, cols, hgap, vgap);
	}

	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int ncomponents = parent.getComponentCount();
			int nrows = getRows();
			int ncols = getColumns();
			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int[] w = new int[ncols];
			int[] h = new int[nrows];
			for (int i = 0; i < ncomponents; i++) {
				int r = i / ncols;
				int c = i % ncols;
				Component comp = parent.getComponent(i);
				Dimension d = comp.getPreferredSize();
				if (w[c] < d.width) {
					w[c] = d.width;
				}
				if (h[r] < d.height) {
					h[r] = d.height;
				}
			}
			int nw = 0;
			for (int j = 0; j < ncols; j++) {
				nw += w[j];
			}
			int nh = 0;
			for (int i = 0; i < nrows; i++) {
				nh += h[i];
			}
			return new Dimension(insets.left + insets.right + nw + (ncols - 1) * getHgap(),
					insets.top + insets.bottom + nh + (nrows - 1) * getVgap());
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		System.err.println("minimumLayoutSize");
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int ncomponents = parent.getComponentCount();
			int nrows = getRows();
			int ncols = getColumns();
			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int[] w = new int[ncols];
			int[] h = new int[nrows];
			for (int i = 0; i < ncomponents; i++) {
				int r = i / ncols;
				int c = i % ncols;
				Component comp = parent.getComponent(i);
				Dimension d = comp.getMinimumSize();
				if (w[c] < d.width) {
					w[c] = d.width;
				}
				if (h[r] < d.height) {
					h[r] = d.height;
				}
			}
			int nw = 0;
			for (int j = 0; j < ncols; j++) {
				nw += w[j];
			}
			int nh = 0;
			for (int i = 0; i < nrows; i++) {
				nh += h[i];
			}
			return new Dimension(insets.left + insets.right + nw + (ncols - 1) * getHgap(),
					insets.top + insets.bottom + nh + (nrows - 1) * getVgap());
		}
	}

	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int ncomponents = parent.getComponentCount();
			int nrows = getRows();
			int ncols = getColumns();
			if (ncomponents == 0) {
				return;
			}
			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int hgap = getHgap();
			int vgap = getVgap();
			// scaling factors
			Dimension pd = preferredLayoutSize(parent);
			double sw = (1.0 * parent.getWidth()) / pd.width;
			double sh = (1.0 * parent.getHeight()) / pd.height;
			// scale
			int[] w = new int[ncols];
			int[] h = new int[nrows];
			for (int i = 0; i < ncomponents; i++) {
				int r = i / ncols;
				int c = i % ncols;
				Component comp = parent.getComponent(i);
				Dimension d = comp.getPreferredSize();
				d.width = (int) (sw * d.width);
				d.height = (int) (sh * d.height);
				if (w[c] < d.width) {
					w[c] = d.width;
				}
				if (h[r] < d.height) {
					h[r] = d.height;
				}
			}
			for (int c = 0, x = insets.left; c < ncols; c++) {
				for (int r = 0, y = insets.top; r < nrows; r++) {
					int i = r * ncols + c;
					if (i < ncomponents) {
						parent.getComponent(i).setBounds(x, y, w[c], h[r]);
					}
					y += h[r] + vgap;
				}
				x += w[c] + hgap;
			}
		}
	}
}
