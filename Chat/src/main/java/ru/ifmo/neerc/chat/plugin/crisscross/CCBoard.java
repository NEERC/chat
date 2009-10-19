/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/*
 * Date: Nov 25, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin.crisscross;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * <code>CCBoard</code> class
 *
 * @author Matvey Kazakov
 */
public class CCBoard extends JComponent {

    public static final int DIMENSION = 1000;
    private static final int DIMENSION_2 = DIMENSION / 2;
    
    private int[] board = new int[(DIMENSION * DIMENSION + 15) / 16];

    public static final int EMPTY = 0;
    public static final int CROSS = 1;

    public static final int ZERO = 2;
    private JScrollBar hScroll = new JScrollBar();

    private JScrollBar vScroll = new JScrollBar();
    private int x, y;
    private static final int SCROLL_GAP = 20;
    private static final int LABEL_HEIGHT = 30;
    private static final int CELL_HEIGHT = 30;
    private static final int LABEL_WIDTH = 30;

    private static final int CELL_WIDTH = 30;
    private int[] columnStart = new int[DIMENSION];
    private int[] rowStart = new int[DIMENSION];
    private static final Color BORDER_COLOR = Color.gray;
    private static final Color LABEL_BORDER_COLOR = Color.black;
    private static final Color BACKGROUND_COLOR = Color.white;
    private static final Color LABEL_BACKGROUND_COLOR = Color.gray;
    private static final Color CROSS_COLOR = Color.red;
    private static final Color ZERO_COLOR = Color.blue;
    private static final Color SELECTED_BACKGROUND_COLOR = Color.lightGray;
    private static final Color LABEL_TEXT_COLOR = Color.white;
    private static final int BORDER_WIDTH = 1;
    private static final BasicStroke THICK_STROKE = new BasicStroke(2);
    private static final BasicStroke THIN_STROKE = new BasicStroke(1);
    
    boolean working = false;
    
    private int mark = EMPTY;

    public CCBoard() {
        Arrays.fill(board, 0);
        setLayout(null);
        for (int i = 0; i < columnStart.length; i++) {
            columnStart[i] = CELL_WIDTH * i;
            rowStart[i] = CELL_HEIGHT * i;
        }
        // create vertical bar
        vScroll = new JScrollBar(JScrollBar.VERTICAL);
        // set default bounds and extent. 
        // It does not matter here because it will be updated in updateScrollPositions()
        vScroll.setBounds(0, SCROLL_GAP, SCROLL_GAP, 100);
        vScroll.getModel().setExtent(1);
        vScroll.setUnitIncrement(CELL_HEIGHT);
        vScroll.setBlockIncrement(CELL_HEIGHT);
        // add to chart
        add(vScroll);
        hScroll = new JScrollBar(JScrollBar.HORIZONTAL);
        // set default bounds and extent. 
        // It does not matter here because it will be updated in updateScrollPositions()
        hScroll.setBounds(SCROLL_GAP, 0, 100, SCROLL_GAP);
        hScroll.getModel().setExtent(1);
        hScroll.setUnitIncrement(CELL_HEIGHT);
        hScroll.setBlockIncrement(CELL_HEIGHT);
        // add to chart
        add(hScroll);
        
        moveTo();
        
        // scroll listener will move table position (cells area)
        vScroll.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (y != vScroll.getValue()) {
                    // change table start position
                    y = vScroll.getValue();
                    // make table chart dirty
                    revalidate();
                    repaint();
                }
            }
        });
        // scroll listener will move table position (cells area)
        hScroll.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (x != hScroll.getValue()) {
                    // change table start position
                    x = hScroll.getValue();
                    // make table chart dirty
                    invalidate();
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (working) {
                    int col = calculateColumn(e);
                    int row = calculateRow(e);
                    if (col >= 0 && col < DIMENSION && row >= 0 && row < DIMENSION) {
                        innerset(col, row, CROSS);
                        // repaint new selection segment
                        repaintBoardSegment(col, row);
                    }
                }
            }
        });
    }
    
    public void start(int value) {
        working = true;
        mark = value;
    }
    
    public void makeOppositeTurn(int x, int y) {
        working = true;
        set(x, y, ZERO +  CROSS - mark);
    }
    
    /**
     * Repaints given segment based on its position in absolute coordinates.<br/>
     * This method assumes that segment, though it is Rectagle, stores virtual table coordinates,
     * i.e. segment's x means index of the left border of the selection segment. <br/>
     * So this method first converts this virtual segment into real coordinates and intersects
     * with the visible coordinates. Then it repains it.
     *
     * @param segment segment with virual coordinates
     */
    private void repaintBoardSegment(int x,  int y) {

        // this rectangle will be updated
        Rectangle updateRect = addCellsInSegmentToRect(x, y, null);

        // if updating rectangle is not empty
        // we repaint it finally
        if (updateRect != null) {
            repaint(updateRect);
        }
    }
    
    private void moveTo() {
        vScroll.setMaximum(DIMENSION * CELL_HEIGHT);
        vScroll.setValue(DIMENSION_2 * CELL_HEIGHT);
        hScroll.setMaximum(DIMENSION * CELL_WIDTH);
        hScroll.setValue(DIMENSION_2 * CELL_WIDTH);
        updateScrollPositions();
    }
    
    /**
     * Adds cells in the given segment with virtual coordinates to the given rectangle with real (screen coordinates). <br/>
     * This method is used to calculate update area for the repainting method for the selection.<br/>
     * This method assumes that segment, though it is Rectagle, stores virtual table coordinates,
     * i.e. segment's x means index of the left border of the selection segment. <br/>
     *
     * @param s          segment with virtual coordinates
     * @param updateRect real screen coordinates rectangle to add coordinates to (if null assumed as empty)
     * @return real coordinates updated with cells inside given virtual segment,
     *         null if segment does not contain cells
     * @see #repaintBoardSegment(int, int)
     */
    private Rectangle addCellsInSegmentToRect(int x, int y, Rectangle updateRect) {

        Rectangle rect = new Rectangle(columnStart[x], rowStart[y], CELL_WIDTH, CELL_HEIGHT);
        // shift according to scrolls
        rect.setLocation(rect.x - this.x + LABEL_WIDTH, rect.y - this.y + LABEL_HEIGHT);
        // crop it to the cells visible area
        rect.intersection(calculateCellsRect());
        // add to update region
        // add to update region
        updateRect = addRectangles(updateRect, rect);
        return updateRect;
    }
    
    /**
     * Combines two rectangles
     *
     * @param r1 first rectangle, if null then assumed as empty
     * @param r2 second rectangle, if null then assumed as empty
     * @return sum of two rectangles or null if sum is empty
     */
    private Rectangle addRectangles(Rectangle r1, Rectangle r2) {
        // by default r2 is in result
        Rectangle r = r2;
        // if r1 != null then we try to add it
        if (r1 != null) {
            if (r != null) {
                // add first one
                r.add(r1);
            } else {
                // r1 is only non-null, so use it for sum
                r = r1;
            }
        }
        return r;
    }
    
    

    void set(int x, int y, int value) {
        innerset(x + DIMENSION_2, y + DIMENSION_2, value);
    }

    private void innerset(int x, int y, int value) {
        assert x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION && value >= EMPTY && value <= ZERO;
        int n = y * DIMENSION + x; // from the beginning
        board[n / 16] |= value << (n % 16) * 2;
    }

    int get(int x, int y) {
        return innerget(x + DIMENSION_2, y + DIMENSION_2);
    }

    int innerget(int x, int y) {
        assert x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION;
        int n = y * DIMENSION + x; // from the beginning
        int mask = 0x3 << (n % 16) * 2;
        return (board[n / 16] & mask) >> (n % 16) * 2;
    }

    /**
     * Override setBounds to refresh current inner layout on external changes.
     *
     * @see JComponent#setBounds(int,int,int,int)
     */
    public void setBounds(int x, int y, int width, int height) {
        // call super action
        super.setBounds(x, y, width, height);

        // and scrolls
        updateScrollPositions();
    }
    
    /**
     * Updates scroller and selection buttons positions
     */
    private void updateScrollPositions() {
        // get component width
        int w = getWidth();
        // get component width
        int h = getHeight();
        // update horizontal scroll
        updateHorizontalScrollPosition();
        // update vertical scroll
        updateVerticalScrollPosition();

    }

    /**
     * Performs update of the vertical scroller position
     */
    private void updateVerticalScrollPosition() {
        // get component width
        int w = getWidth();
        // get component width
        int h = getHeight();
        // get bottommost border of the whole table
        int bottomBorder = rowStart[DIMENSION - 1] + CELL_HEIGHT + BORDER_WIDTH;
        // calculate visible part height of cells area of the table
        int visibleHeight = h - LABEL_HEIGHT - SCROLL_GAP;
        // check conditions for showing vertical scroll bar
        // - show in edit mode only
        // - do not show if visibleHeight <= 0 - there is nothing to scroll (cells are invisible)
        // - do not show if visibleHeight > bottomBorder - there is nothing to scroll (all cells are visible)
        if (visibleHeight <= 0 || bottomBorder <= visibleHeight) {
            // do not show
            vScroll.setVisible(false);
            // scroll real coordinates to the top  
            if (visibleHeight <= 0 || bottomBorder <= visibleHeight) {
                y = 0;
            }
        } else {
            // scroll bar size corresponds to the visible rows
            vScroll.setBounds(w - SCROLL_GAP, LABEL_HEIGHT, SCROLL_GAP, visibleHeight);
            // bottommost - means showing bottommost cells
            vScroll.setMaximum(bottomBorder);
            // current value is based on current position
            vScroll.setValue(Math.min(y, bottomBorder - visibleHeight));
            // scroller size... size of the area that we scroll
            vScroll.setVisibleAmount(visibleHeight);
            // show
            vScroll.setVisible(true);
            // repaint
            vScroll.revalidate();
            // get value back to the coordinate
            // it can be changed if bottomBorder - visibleHeight became less then y, i.e.
            // when table size is increased
            y = vScroll.getValue();
        }
    }

    /**
     * Performs update of the horizontal scroller position
     */
    private void updateHorizontalScrollPosition() {
        // get component width
        int w = getWidth();
        // get component width
        int h = getHeight();
        // calculate visible part width of cells area of the table
        int visibleWidth = w - LABEL_WIDTH - SCROLL_GAP;
        // get rightmost border of the whole table
        int rightBorder = columnStart[DIMENSION - 1] + CELL_WIDTH + BORDER_WIDTH;
        // check conditions for showing horizontal scroll bar
        // - show in edit mode only
        // - do not show if visibleWidth <= 0 - there is nothing to scroll (cells are invisible)
        // - do not show if visibleWidth > rightBorder - there is nothing to scroll (all cells are visible)
        if (visibleWidth <= 0 || rightBorder <= visibleWidth) {
            // do not show
            hScroll.setVisible(false);
            // scroll real coordinates to the left
            if (visibleWidth <= 0 || rightBorder <= visibleWidth) {
                x = 0;
            }
        } else {
            // scroll bar size corresponds to the visible rows
            hScroll.setBounds(LABEL_WIDTH, h - SCROLL_GAP, visibleWidth, SCROLL_GAP);
            // rightmost - means showing rightmost cells
            hScroll.setMaximum(rightBorder);
            // current value is based on current position
            hScroll.setValue(Math.min(x, rightBorder - visibleWidth));
            // scroller size... size of the area that we scroll
            hScroll.setVisibleAmount(visibleWidth);
            // show
            hScroll.setVisible(true);
            // repaint
            hScroll.revalidate();
            // get value back to the coordinate
            // it can be changed if rightBorder - visibleWidth became less then x, i.e.
            // when table size is increased
            x = hScroll.getValue();
        }
    }

    protected void paintComponent(Graphics g) {
        // set anti-aliased output
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // get main clipping area - should be restored at the end
        Shape mainClip = g.getClip();
        // calculate total table clipping area
        Rectangle totalClip = new Rectangle(0, 0, getWidth() - SCROLL_GAP, getHeight() - SCROLL_GAP);
        Rectangle clipRect = g.getClipBounds();
        // intersect clilling area with table are
        clipRect = clipRect.intersection(totalClip);
        // find region to update inside cells area
        Rectangle cellURect = clipRect.intersection(calculateCellsRect());
        // this will be the coordinates to draw
        int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
        // search for leftmost column to draw
        x1 = findIndex(x + cellURect.x - LABEL_WIDTH, columnStart);
        // search for rightmost column to draw
        x2 = findIndex(x + cellURect.x + cellURect.width - LABEL_WIDTH, columnStart);
        // search for topmost row to draw
        y1 = findIndex(y + cellURect.y - LABEL_HEIGHT, rowStart);
        // search for bottommost row to draw
        y2 = findIndex(y + cellURect.y + cellURect.height - LABEL_HEIGHT, rowStart);

        // this is special case for the non-table area updates
        if (x2 <= 0) { x1 = 0; }
        if (y2 <= 0) { y1 = 0; }

        // paint cells from data area
        paintCells(g, cellURect, x1, x2, y1, y2);
        // paint column labels
        paintYLabels(g, clipRect.intersection(calculateYLabelsRect()), y1, y2);
        // paint row labels
        paintXLabels(g, clipRect.intersection(calculateXLabelsRect()), x1, x2);
        // return back total clipping area
        g.setClip(totalClip);
        // intersect with clipping bounds 
        g.clipRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
        // draw corner label
        renderText(g, 0, 0, LABEL_WIDTH, LABEL_HEIGHT, "");
        g.setClip(totalClip);
        // return back clipping area
        g.setClip(mainClip);
    }
    
      /**
     * Paints cells part of the table
     *
     * @param g         Graphics to draw on
     * @param cellURect rectangle to draw inside (grapphics will be clipped to this rectangle)
     * @param x1        starting column to draw
     * @param x2        ending column to draw
     * @param y1        starting row to draw
     * @param y2        ending row to draw
     */
    private void paintCells(Graphics g, Rectangle cellURect, int x1, int x2, int y1, int y2) {
        // clip graphics to prevend dirt drawing outside this rect
        g.setClip(cellURect);
        // leftmost border of all cells
        int leftBound = LABEL_WIDTH - x;
        // topmost border of all cells
        int topBound = LABEL_HEIGHT - y;
        // iterate cells
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                // ad draw them
                renderValue(g, leftBound + columnStart[i], topBound + rowStart[j], innerget(i, j), false);
            }
        }
    }

    /**
     * Calculates rectangle that is occupied by column labels (header area - corder label)
     *
     * @return bounding rectangle
     */
    private Rectangle calculateXLabelsRect() {
        // assume that labels are started just after title 
        // left border starts after row labels
        return new Rectangle(LABEL_WIDTH, 0, getWidth() - LABEL_WIDTH - SCROLL_GAP, LABEL_HEIGHT);
    }

    /**
     * Calculates rectangle that is occupied by row labels (labels area)
     *
     * @return bounding rectangle
     */
    private Rectangle calculateYLabelsRect() {
        // assume that labels are started just after title and column labels
        // left border coinsides with chart border
        return new Rectangle(0, LABEL_HEIGHT, LABEL_WIDTH, getHeight() - LABEL_HEIGHT - SCROLL_GAP);
    }
    
    /**
     * Paints column labels on the chart
     *
     * @param g            Graphics to draw on
     * @param xLabelsURect clipping rectangle of the column labels
     * @param x1           starting column to draw (calculated before)
     * @param x2           ending column (calculated before)
     */
    private void paintXLabels(Graphics g, Rectangle xLabelsURect, int x1, int x2) {
        // draw only inside clipping area
        g.setClip(xLabelsURect);
        // do not show the labels if update rectangle is very small
        if (xLabelsURect.height > 0) {
            // left border of all columns
            int leftBound = LABEL_WIDTH - x;
            // iterate columns that intersect update rectangle
            for (int i = x1; i <= x2; i++) {
                // draw one column label
                renderText(g, columnStart[i] + leftBound, 0, CELL_WIDTH, LABEL_HEIGHT, String.valueOf(i - DIMENSION_2));
            }
        }
    }
    
    /**
     * Paints row labels on the chart
     *
     * @param g            Graphics to draw on
     * @param yLabelsURect clipping rectangle of the row labels
     * @param y1           starting row to draw (calculated before)
     * @param y2           ending row (calculated before)
     */
    private void paintYLabels(Graphics g, Rectangle yLabelsURect, int y1, int y2) {
        // draw only inside clipping area
        g.setClip(yLabelsURect);
        // do not show the labels if update rectangle is very small
        if (yLabelsURect.width > 0) {
            // top border of all rows
            int topBound = LABEL_HEIGHT - y;
            // iterate rows that intersect update rectangle
            for (int j = y1; j <= y2; j++) {
                // draw one row label
                renderText(g, 0, rowStart[j] + topBound, LABEL_WIDTH, CELL_HEIGHT, String.valueOf(j - DIMENSION_2));
            }
        }
    }
    

    private void renderValue(Graphics g, int x, int y, int value, boolean selected) {
        ((Graphics2D)g).setStroke(THIN_STROKE);
        g.setColor(BORDER_COLOR);
        g.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);
        g.setColor(selected ? SELECTED_BACKGROUND_COLOR : BACKGROUND_COLOR);
        g.fillRect(x + 1, y + 1, CELL_WIDTH - 2, CELL_HEIGHT - 2);
        ((Graphics2D)g).setStroke(THICK_STROKE);
        int gap = 3;
        switch (value) {
            case ZERO:
                g.setColor(ZERO_COLOR);
                g.drawOval(x+ gap, y+ gap, CELL_WIDTH - 2*gap, CELL_HEIGHT - 2*gap);
                break;
            case CROSS:
                g.setColor(CROSS_COLOR);
                g.drawLine(x + gap, y + gap, x + CELL_WIDTH - gap, y + CELL_HEIGHT - gap);
                g.drawLine(x + CELL_WIDTH - gap, y + gap, x + gap, y + CELL_HEIGHT - gap);
                break;
        }
    }

    private void renderText(Graphics g, int x, int y, int w, int h, String value) {
        ((Graphics2D)g).setStroke(THIN_STROKE);
        g.setColor(LABEL_BORDER_COLOR);
        g.drawRect(x, y, w, h);
        g.setColor(LABEL_BACKGROUND_COLOR);
        g.fillRect(x + 1, y + 1, w - 1, h - 1);
        g.setColor(LABEL_TEXT_COLOR);
        FontMetrics fontMetrics = g.getFontMetrics();
        Rectangle2D bounds = fontMetrics.getStringBounds(value, g);
        g.drawString(value, (int)(x + (w - bounds.getWidth()) / 2), y + h / 2 + fontMetrics.getHeight() / 2 - fontMetrics.getDescent());
    }


    /**
     * Calculates rectangle that is occupied by table cells (data area)
     *
     * @return bounding rectangle
     */
    private Rectangle calculateCellsRect() {
        // assume that data is started just after title and column labels
        // left border starts after row labels
        return new Rectangle(LABEL_WIDTH, LABEL_HEIGHT,
                getWidth() - LABEL_WIDTH - SCROLL_GAP, getHeight() - LABEL_WIDTH - SCROLL_GAP);
    }


    /**
     * Answers index of the cell that given point corresponds to.
     * It is assumes that coordinates are boundaries of the intervals starting from zero:
     * (0, a[0]), (a[0], a[1]) ...
     *
     * @param coord     coordinate to search
     * @param allCoords list of coordinates to search among
     * @return index of coordinate containing given one.
     */
    private int findIndex(int coord, int[] allCoords) {
        int index = Arrays.binarySearch(allCoords, coord);
        index = index >= 0 ? index : -index - 2;
        return index < allCoords.length ? index : index - 1;
    }
    /**
     * Calculates column that mouse event is occured on
     *
     * @param e mouse event occured
     * @return -1-based column index or {@link #DIMENSION} + 1 if outside of any column
     */
    private int calculateColumn(MouseEvent e) {
        // original x
        int ox = e.getX();
        // by default -1 - means label
        int col = -1;
        // if original x is less then label width, then -1 is fine 
        if (ox >= LABEL_WIDTH) {
            // find index where this point corresponds
            col = findIndex(ox + this.x - LABEL_WIDTH, columnStart);
        }
        // anacceptable case
        if (// too far to the right
                ox > LABEL_WIDTH + columnStart[DIMENSION - 1] + CELL_WIDTH) {
            col = DIMENSION + 1;
        }
        // return calculated value
        return col;
    }

    /**
     * Calculates row that mouse event is occured on
     *
     * @param e mouse event occured
     * @return -1-based row index or {@link #DIMENSION} + 1 if outside of any row
     */
    private int calculateRow(MouseEvent e) {
        // original y
        int oy = e.getY();
        // top gap before first row
        int lh = LABEL_HEIGHT;
        // by default - assume it is label
        int row = -1;
        // if original x is less then top gap, then -1 is fine 
        if (oy >= lh) {
            // find index where this point corresponds
            row = findIndex(oy + this.y - lh, rowStart);
        }

        // anacceptable case
        if (// too low - below last row
                oy > lh + rowStart[DIMENSION - 1] + CELL_HEIGHT) {
            row = DIMENSION + 1;
        }
        // return calculated value
        return row;
    }
    
    /**
     * Answers component real visible width (respecting scrolls and table border)
     *
     * @return component visible rectangle
     */
    private int getRealWidth() {
        return getWidth() - SCROLL_GAP - BORDER_WIDTH;
    }

    
   

}
