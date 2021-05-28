/*
 * Copyright 2006 FXCM LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fxts.stations.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * The <code>SideLayout</code> class is a flexible layout
 * manager that aligns components vertically and horizontally,
 * without requiring that the components be of the same size.
 * Each <code>SideLayout</code> object maintains a dynamic,
 * rectangular grid of cells, with each component occupying
 * one or more cells, called its <em>display area</em>.
 */
public class SideLayout implements LayoutManager2,
                                   Serializable {
    /**
     * The maximum number of grid positions (both horizontally and
     * vertically) that can be laid out by the grid bag layout.
     */
    protected static final int MAXGRIDSIZE = 512;
    /**
     * The smallest grid that can be laid out by the grid bag layout.
     */
    protected static final int MINSIZE = 1;
    /**
     * The preferred grid size that can be laid out by the grid bag layout.
     */
    protected static final int PREFERREDSIZE = 2;
    /**
     * This field holds the overrides to the column weights.
     * If this field is non-<code>null</code> the values are
     * applied to the gridbag after all of the columns
     * weights have been calculated.
     * If <code>columnWeights[i]</code> &gt; weight for column i, then
     * column i is assigned the weight in <code>columnWeights[i]</code>.
     * If <code>columnWeights</code> has more elements than the number
     * of columns, the excess elements are ignored - they do
     * not cause more columns to be created.
     */
    public double[] mColumnWeights;
    /**
     * This field holds the overrides to the column minimum
     * width.  If this field is non-<code>null</code> the values are
     * applied to the gridbag after all of the minimum columns
     * widths have been calculated.
     * If columnWidths has more elements than the number of
     * columns, columns are added to the gridbag to match
     * the number of elements in columnWidth.
     */
    public int[] mColumnWidths;
    /**
     * This hashtable maintains the association between
     * a component and its gridbag constraints.
     * The Keys in <code>comptable</code> are the components and the
     * values are the instances of <code>SideConstraints</code>.
     */
    protected Hashtable mComptable;
    /**
     * This field holds a gridbag constraints instance
     * containing the default values, so if a component
     * does not have gridbag constraints associated with
     * it, then the component will be assigned a
     * copy of the <code>defaultConstraints</code>.
     */
    protected SideConstraints mDefaultConstraints;
    /**
     * This field holds the layout information
     * for the gridbag.  The information in this field
     * is based on the most recent validation of the
     * gridbag.
     * If <code>layoutInfo</code> is <code>null</code>
     * this indicates that there are no components in
     * the gridbag or if there are components, they have
     * not yet been validated.
     */
    protected SideLayoutInfo mLayoutInfo;
    private final Log mLogger = LogFactory.getLog(SideLayout.class);
    transient boolean mRightToLeft = false;
    /**
     * This field holds the overrides to the row minimum
     * heights.  If this field is non-</code>null</code> the values are
     * applied to the gridbag after all of the minimum row
     * heights have been calculated.
     * If <code>rowHeights</code> has more elements than the number of
     * rows, rowa are added to the gridbag to match
     * the number of elements in <code>rowHeights</code>.
     */
    public int[] mRowHeights;
    /**
     * This field holds the overrides to the row weights.
     * If this field is non-</code>null</code> the values are
     * applied to the gridbag after all of the rows
     * weights have been calculated.
     * If <code>rowWeights[i]</code> &gt; weight for row i, then
     * row i is assigned the weight in <code>rowWeights[i]</code>.
     * If <code>rowWeights</code> has more elements than the number
     * of rows, the excess elements are ignored - they do
     * not cause more rows to be created.
     */
    public double[] mRowWeights;

    /**
     * Creates a grid bag layout manager.
     */
    public SideLayout() {
        mComptable = new Hashtable();
        mDefaultConstraints = new SideConstraints();
    }

    /**
     * Adds the specified component with the specified name to the layout.
     *
     * @param aName the name of the component
     * @param aComp the component to be added
     */
    public void addLayoutComponent(String aName, Component aComp) {
    }

    /**
     * Adds the specified component to the layout, using the specified
     * <code>constraints</code> object.  Note that constraints
     * are mutable and are, therefore, cloned when cached.
     *
     * @param aComp        the component to be added
     * @param aConstraints an object that determines how
     *                     the component is added to the layout
     *
     * @throws IllegalArgumentException if <code>constraints</code>
     *                                  is not a <code>GridBagConstraint</code>
     */
    public void addLayoutComponent(Component aComp, Object aConstraints) {
        if (aConstraints instanceof SideConstraints) {
            setConstraints(aComp, (SideConstraints) aConstraints);
        } else if (aConstraints != null) {
            throw new IllegalArgumentException("cannot add to layout: constraints must be a GridBagConstraint");
        }
    }

    /**
     * Adjusts the x, y, width, and height fields to the correct
     * values depending on the constraint geometry and pads.
     *
     * @param aConstraints the constraints to be applied
     * @param aRectangle   the <code>Rectangle</code> to be adjusted
     */
    protected void adjustForGravity(SideConstraints aConstraints, Rectangle aRectangle) {
        int diffx, diffy;
        if (!mRightToLeft) {
            aRectangle.x += aConstraints.insets.left;
        } else {
            aRectangle.x -= aRectangle.width - aConstraints.insets.right;
        }
        aRectangle.width -= aConstraints.insets.left + aConstraints.insets.right;
        aRectangle.y += aConstraints.insets.top;
        aRectangle.height -= aConstraints.insets.top + aConstraints.insets.bottom;
        diffx = 0;
        if (aConstraints.fill != SideConstraints.HORIZONTAL && aConstraints.fill != SideConstraints.BOTH && aRectangle.width > aConstraints.minWidth + aConstraints.ipadx) {
            diffx = aRectangle.width - (aConstraints.minWidth + aConstraints.ipadx);
            aRectangle.width = aConstraints.minWidth + aConstraints.ipadx;
        }
        diffy = 0;
        if (aConstraints.fill != SideConstraints.VERTICAL && aConstraints.fill != SideConstraints.BOTH && aRectangle.height > aConstraints.minHeight + aConstraints.ipady) {
            diffy = aRectangle.height - (aConstraints.minHeight + aConstraints.ipady);
            aRectangle.height = aConstraints.minHeight + aConstraints.ipady;
        }
        switch (aConstraints.anchor) {
            case GridBagConstraints.CENTER:
                aRectangle.x += diffx / 2;
                aRectangle.y += diffy / 2;
                break;
            case SideConstraints.PAGE_START:
            case GridBagConstraints.NORTH:
                aRectangle.x += diffx / 2;
                break;
            case GridBagConstraints.NORTHEAST:
                aRectangle.x += diffx;
                break;
            case GridBagConstraints.EAST:
                aRectangle.x += diffx;
                aRectangle.y += diffy / 2;
                break;
            case GridBagConstraints.SOUTHEAST:
                aRectangle.x += diffx;
                aRectangle.y += diffy;
                break;
            case SideConstraints.PAGE_END:
            case GridBagConstraints.SOUTH:
                aRectangle.x += diffx / 2;
                aRectangle.y += diffy;
                break;
            case GridBagConstraints.SOUTHWEST:
                aRectangle.y += diffy;
                break;
            case GridBagConstraints.WEST:
                aRectangle.y += diffy / 2;
                break;
            case GridBagConstraints.NORTHWEST:
                break;
            case SideConstraints.LINE_START:
                if (mRightToLeft) {
                    aRectangle.x += diffx;
                }
                aRectangle.y += diffy / 2;
                break;
            case SideConstraints.LINE_END:
                if (!mRightToLeft) {
                    aRectangle.x += diffx;
                }
                aRectangle.y += diffy / 2;
                break;
            case SideConstraints.FIRST_LINE_START:
                if (mRightToLeft) {
                    aRectangle.x += diffx;
                }
                break;
            case SideConstraints.FIRST_LINE_END:
                if (!mRightToLeft) {
                    aRectangle.x += diffx;
                }
                break;
            case SideConstraints.LAST_LINE_START:
                if (mRightToLeft) {
                    aRectangle.x += diffx;
                }
                aRectangle.y += diffy;
                break;
            case SideConstraints.LAST_LINE_END:
                if (!mRightToLeft) {
                    aRectangle.x += diffx;
                }
                aRectangle.y += diffy;
                break;
            default:
                throw new IllegalArgumentException("illegal anchor value");
        }
    }

    /**
     * Lays out the grid.
     *
     * @param aParent the layout container
     */
    protected void arrangeGrid(Container aParent) {
        /////////////////////////////////////////////////////////
        //It`s only for debugging
        JComponent jc = (JComponent) aParent;
        String sType = (String) jc.getClientProperty("TYPE");
        if (sType != null) {
            boolean bInternal = "internal".equals(sType);
            mLogger.debug("\n" + sType);
        }
        //////////////////////////////////////////////////////////
        Component comp;
        int compindex;
        SideConstraints constraints;
        Insets insets = aParent.getInsets();
        Component[] components = aParent.getComponents();
        Dimension d;
        Rectangle r = new Rectangle();
        int i, diffw, diffh;
        double weight;
        SideLayoutInfo info;
        mRightToLeft = !aParent.getComponentOrientation().isLeftToRight();

        /*
        * If the parent has no slaves anymore, then don't do anything
        * at all:  just leave the parent's size as-is.
        */
        if (components.length == 0 &&
            (mColumnWidths == null || mColumnWidths.length == 0) &&
            (mRowHeights == null || mRowHeights.length == 0)) {
            return;
        }

        /*
        * Pass #1: scan all the slaves to figure out the total amount
        * of space needed.
        */

        info = getLayoutInfo(aParent, PREFERREDSIZE);
        d = getMinSize(aParent, info);

        //
        //    System.out.println("parent=w:" + parent.getWidth() + ",h:" + parent.getHeight() +
        //                       "min=w:" + d.getWidth() + ",h:" + d.getHeight());
        if (aParent.getWidth() < d.width || aParent.getHeight() < d.height) {
            info = getLayoutInfo(aParent, MINSIZE);
            d = getMinSize(aParent, info);
            //
            //      System.out.println("MINSIZE");
        } else {
            //
            //      System.out.println("Non MINSIZE");
        }
        mLayoutInfo = info;
        r.width = d.width;
        r.height = d.height;

        /*
        * If the current dimensions of the window don't match the desired
        * dimensions, then adjust the minWidth and minHeight arrays
        * according to the weights.
        */

        diffw = aParent.getWidth() - r.width;
        //
        //    System.out.println("diffw=" + diffw);
        if (diffw != 0) {
            weight = 0.0;
            for (i = 0; i < info.width; i++) {
                weight += info.weightX[i];
            }
            if (weight > 0.0) {
                for (i = 0; i < info.width; i++) {
                    int dx = (int) (((double) diffw * info.weightX[i]) / weight);
                    info.minWidth[i] += dx;
                    r.width += dx;
                    if (info.minWidth[i] < 0) {
                        r.width -= info.minWidth[i];
                        info.minWidth[i] = 0;
                    }
                }
            }
            diffw = aParent.getWidth() - r.width;
        } else {
            diffw = 0;
        }
        diffh = aParent.getHeight() - r.height;
        //
        //    System.out.println("diffh=" + diffh);
        if (diffh != 0) {
            weight = 0.0;
            for (i = 0; i < info.height; i++) {
                weight += info.weightY[i];
            }
            if (weight > 0.0) {
                for (i = 0; i < info.height; i++) {
                    int dy = (int) (((double) diffh * info.weightY[i]) / weight);
                    info.minHeight[i] += dy;
                    r.height += dy;
                    if (info.minHeight[i] < 0) {
                        r.height -= info.minHeight[i];
                        info.minHeight[i] = 0;
                    }
                }
            }
            diffh = aParent.getHeight() - r.height;
        } else {
            diffh = 0;
        }

        /*
        * Now do the actual layout of the slaves using the layout information
        * that has been collected.
        */

        info.startx = /*diffw/2 +*/ insets.left;
        info.starty = /*diffh/2 +*/ insets.top;
        //
        //    System.out.println("info.startx = " + info.startx);
        //    System.out.println("info.starty = " + info.startx);
        for (compindex = 0; compindex < components.length; compindex++) {
            comp = components[compindex];
            if (!comp.isVisible()) {
                continue;
            }
            constraints = lookupConstraints(comp);
            if (!mRightToLeft) {
                r.x = info.startx;
                for (i = 0; i < constraints.tempX; i++) {
                    r.x += info.minWidth[i];
                }
            } else {
                r.x = aParent.getWidth() - insets.right;
                for (i = 0; i < constraints.tempX; i++) {
                    r.x -= info.minWidth[i];
                }
            }
            r.y = info.starty;
            for (i = 0; i < constraints.tempY; i++) {
                r.y += info.minHeight[i];
            }
            r.width = 0;
            for (i = constraints.tempX;
                 i < constraints.tempX + constraints.tempWidth;
                 i++) {
                r.width += info.minWidth[i];
            }
            r.height = 0;
            for (i = constraints.tempY;
                 i < constraints.tempY + constraints.tempHeight;
                 i++) {
                r.height += info.minHeight[i];
            }
            adjustForGravity(constraints, r);
            if (r.x < 0) {
                r.width -= r.x;
                r.x = 0;
            }
            if (r.y < 0) {
                r.height -= r.y;
                r.y = 0;
            }

            /*
            * If the window is too small to be interesting then
            * unmap it.  Otherwise configure it and then make sure
            * it's mapped.
            */

            if (r.width <= 0 || r.height <= 0) {
                comp.setBounds(0, 0, 0, 0);
            } else {
                if (comp.getX() != r.x || comp.getY() != r.y ||
                    comp.getWidth() != r.width || comp.getHeight() != r.height) {
                    comp.setBounds(r.x, r.y, r.width, r.height);
                }
            }

            //        System.out.println("Initial component size (x = " + (int)comp.getX() +
            //                           ", y = " + (int)(comp.getY()) +
            //                           ", widht = " + (int)(comp.getWidth()) +
            //                           ", height = " + (int)(comp.getHeight()));
            if (diffw > 0) {
                //            System.out.println("It`s increasing by x!");
                //if (comp instanceof IResizableComponent) {
                //                System.out.println("It`s resizable component: " + comp);

                //IResizableComponent resizeComp = (IResizableComponent)comp;
                ResizeParameter param = constraints.resize;

                //                System.out.println("Params: Left=" + param.getLeft() + ",top=" + param.getTop() +
                //                                   ",Right=" + param.getRight() + ",bottom=" + param.getBottom());
                comp.setBounds((int) (comp.getX() + diffw * param.getLeft()),
                               comp.getY(),
                               (int) (comp.getWidth() +
                                      diffw * (param.getRight() - param.getLeft())),
                               comp.getHeight());

                //                System.out.println("Set Bounds (x = " + (int)(comp.getX() + diffw * param.getLeft()) +
                //                    ", y = " + (int)(comp.getY()) +
                //                    ", widht = " + (int)(comp.getWidth() +
                ///                                     diffw * (param.getRight() - param.getLeft())) +
                //                    ", height = " + (int)(comp.getHeight()));
                //            }
            }
            if (diffh > 0) {
                //            System.out.println("It`s increasing by y!");
                //            if (comp instanceof IResizableComponent) {
                //                System.out.println("It`s resizable component: " + comp);

                //                IResizableComponent resizeComp = (IResizableComponent)comp;
                ResizeParameter param = constraints.resize;

                //                System.out.println("Params: Left=" + param.getLeft() + ",top=" + param.getTop() +
                //                                   ",Right=" + param.getRight() + ",bottom=" + param.getBottom());
                comp.setBounds(comp.getX(),
                               (int) (comp.getY() + diffh * param.getTop()),
                               comp.getWidth(),
                               (int) (comp.getHeight() +
                                      diffh * (param.getBottom() - param.getTop())));

                //                System.out.println("Set Bounds (x = " + (int)(comp.getX()) +
                //                    ", y = " + (int)(comp.getY() + diffh * param.getTop()) +
                //                    ", widht = " + (int)(comp.getWidth()) +
                //                    ", height = " + (int)(comp.getHeight() +
                //                                     diffh * (param.getBottom() - param.getTop())));
                //            }
            }
        }
    }

    /**
     * Gets the constraints for the specified component.  A copy of
     * the actual <code>SideConstraints</code> object is returned.
     *
     * @param aComp the component to be queried
     *
     * @return the constraint for the specified component in this
     *         grid bag layout; a copy of the actual constraint
     *         object is returned
     */
    public SideConstraints getConstraints(Component aComp) {
        SideConstraints constraints = (SideConstraints) mComptable.get(aComp);
        if (constraints == null) {
            setConstraints(aComp, mDefaultConstraints);
            constraints = (SideConstraints) mComptable.get(aComp);
        }
        return (SideConstraints) constraints.clone();
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @return the value <code>0.5f</code> to indicate centered
     */
    public float getLayoutAlignmentX(Container aParent) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     *
     * @return the value <code>0.5f</code> to indicate centered
     */
    public float getLayoutAlignmentY(Container aParent) {
        return 0.5f;
    }

    /**
     * Determines column widths and row heights for the layout grid.
     * Most applications do not call this method directly.
     *
     * @return an array of two arrays, containing the widths
     *         of the layout columns and
     *         the heights of the layout rows
     */
    public int[][] getLayoutDimensions() {
        if (mLayoutInfo == null) {
            return new int[2][0];
        }
        int[][] dim = new int[2][];
        dim[0] = new int[mLayoutInfo.width];
        dim[1] = new int[mLayoutInfo.height];
        System.arraycopy(mLayoutInfo.minWidth, 0, dim[0], 0, mLayoutInfo.width);
        System.arraycopy(mLayoutInfo.minHeight, 0, dim[1], 0, mLayoutInfo.height);
        return dim;
    }

    /**
     * Fills in an instance of <code>SideLayoutInfo</code> for the
     * current set of managed children. This requires three passes through the
     * set of children:
     * <ol>
     * <li>Figure out the dimensions of the layout grid.
     * <li>Determine which cells the components occupy.
     * <li>Distribute the weights and min sizes amoung the rows/columns.
     * </ol>
     * This also caches the minsizes for all the children when they are
     * first encountered (so subsequent loops don't need to ask again).
     *
     * @param aParent   the layout container
     * @param aSizeflag either <code>PREFERREDSIZE</code> or <code>MINSIZE</code>
     *
     * @return the <code>SideLayoutInfo</code> for the set of children
     */
    protected SideLayoutInfo getLayoutInfo(Container aParent, int aSizeflag) {
        synchronized (aParent.getTreeLock()) {
            SideLayoutInfo r = new SideLayoutInfo();
            Component comp;
            SideConstraints constraints;
            Dimension d;
            Component[] components = aParent.getComponents();
            int compindex;
            int i;
            int j;
            int k;
            int px;
            int py;
            int pixels_diff;
            int nextSize;
            int curX, curY, curWidth, curHeight, curRow, curCol;
            double weight_diff;
            double weight;
            double start;
            double size;
            int[] xMax;
            int[] yMax;

            /*
            * Pass #1
            *
            * Figure out the dimensions of the layout grid (use a value of 1 for
            * zero or negative widths and heights).
            */

            r.width = r.height = 0;
            curRow = curCol = -1;
            xMax = new int[MAXGRIDSIZE];
            yMax = new int[MAXGRIDSIZE];
            for (compindex = 0; compindex < components.length; compindex++) {
                comp = components[compindex];
                if (!comp.isVisible()) {
                    continue;
                }
                constraints = lookupConstraints(comp);
                curX = constraints.gridx;
                curY = constraints.gridy;
                curWidth = constraints.gridwidth;
                if (curWidth <= 0) {
                    curWidth = 1;
                }
                curHeight = constraints.gridheight;
                if (curHeight <= 0) {
                    curHeight = 1;
                }

                /* If x or y is negative, then use relative positioning: */
                if (curX < 0 && curY < 0) {
                    if (curRow >= 0) {
                        curY = curRow;
                    } else if (curCol >= 0) {
                        curX = curCol;
                    } else {
                        curY = 0;
                    }
                }
                if (curX < 0) {
                    px = 0;
                    for (i = curY; i < curY + curHeight; i++) {
                        px = Math.max(px, xMax[i]);
                    }
                    curX = px - curX - 1;
                    if (curX < 0) {
                        curX = 0;
                    }
                } else if (curY < 0) {
                    py = 0;
                    for (i = curX; i < curX + curWidth; i++) {
                        py = Math.max(py, yMax[i]);
                    }
                    curY = py - curY - 1;
                    if (curY < 0) {
                        curY = 0;
                    }
                }

                /* Adjust the grid width and height */
                for (px = curX + curWidth; r.width < px; r.width++) {
                }
                for (py = curY + curHeight; r.height < py; r.height++) {
                }

                /* Adjust the xMax and yMax arrays */
                for (i = curX; i < curX + curWidth; i++) {
                    yMax[i] = py;
                }
                for (i = curY; i < curY + curHeight; i++) {
                    xMax[i] = px;
                }

                /* Cache the current slave's size. */
                if (aSizeflag == PREFERREDSIZE) {
                    d = comp.getPreferredSize();
                } else {
                    d = comp.getMinimumSize();
                }
                constraints.minWidth = d.width;
                //constraints.setMinWidth(d.width);
                constraints.minHeight = d.height;

                /* Zero width and height must mean that this is the last item (or
           * else something is wrong). */
                if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
                    curRow = curCol = -1;
                }

                /* Zero width starts a new row */
                if (constraints.gridheight == 0 && curRow < 0) {
                    curCol = curX + curWidth;
                }

                /* Zero height starts a new column */
                else if (constraints.gridwidth == 0 && curCol < 0) {
                    curRow = curY + curHeight;
                }
            }

            /*
            * Apply minimum row/column dimensions
            */
            if (mColumnWidths != null && r.width < mColumnWidths.length) {
                r.width = mColumnWidths.length;
            }
            if (mRowHeights != null && r.height < mRowHeights.length) {
                r.height = mRowHeights.length;
            }

            /*
            * Pass #2
            *
            * Negative values for gridX are filled in with the current x value.
            * Negative values for gridY are filled in with the current y value.
            * Negative or zero values for gridWidth and gridHeight end the current
            *  row or column, respectively.
            */

            curRow = curCol = -1;
            xMax = new int[MAXGRIDSIZE];
            yMax = new int[MAXGRIDSIZE];
            for (compindex = 0; compindex < components.length; compindex++) {
                comp = components[compindex];
                if (!comp.isVisible()) {
                    continue;
                }
                constraints = lookupConstraints(comp);
                curX = constraints.gridx;
                curY = constraints.gridy;
                curWidth = constraints.gridwidth;
                curHeight = constraints.gridheight;

                /* If x or y is negative, then use relative positioning: */
                if (curX < 0 && curY < 0) {
                    if (curRow >= 0) {
                        curY = curRow;
                    } else if (curCol >= 0) {
                        curX = curCol;
                    } else {
                        curY = 0;
                    }
                }
                if (curX < 0) {
                    if (curHeight <= 0) {
                        curHeight += r.height - curY;
                        if (curHeight < 1) {
                            curHeight = 1;
                        }
                    }
                    px = 0;
                    for (i = curY; i < curY + curHeight; i++) {
                        px = Math.max(px, xMax[i]);
                    }
                    curX = px - curX - 1;
                    if (curX < 0) {
                        curX = 0;
                    }
                } else if (curY < 0) {
                    if (curWidth <= 0) {
                        curWidth += r.width - curX;
                        if (curWidth < 1) {
                            curWidth = 1;
                        }
                    }
                    py = 0;
                    for (i = curX; i < curX + curWidth; i++) {
                        py = Math.max(py, yMax[i]);
                    }
                    curY = py - curY - 1;
                    if (curY < 0) {
                        curY = 0;
                    }
                }
                if (curWidth <= 0) {
                    curWidth += r.width - curX;
                    if (curWidth < 1) {
                        curWidth = 1;
                    }
                }
                if (curHeight <= 0) {
                    curHeight += r.height - curY;
                    if (curHeight < 1) {
                        curHeight = 1;
                    }
                }
                px = curX + curWidth;
                py = curY + curHeight;
                for (i = curX; i < curX + curWidth; i++) {
                    yMax[i] = py;
                }
                for (i = curY; i < curY + curHeight; i++) {
                    xMax[i] = px;
                }

                /* Make negative sizes start a new row/column */
                if (constraints.gridheight == 0 && constraints.gridwidth == 0) {
                    curRow = curCol = -1;
                }
                if (constraints.gridheight == 0 && curRow < 0) {
                    curCol = curX + curWidth;
                } else if (constraints.gridwidth == 0 && curCol < 0) {
                    curRow = curY + curHeight;
                }

                /* Assign the new values to the gridbag slave */
                constraints.tempX = curX;
                constraints.tempY = curY;
                constraints.tempWidth = curWidth;
                constraints.tempHeight = curHeight;
            }

            /*
            * Apply minimum row/column dimensions and weights
            */
            if (mColumnWidths != null) {
                System.arraycopy(mColumnWidths, 0, r.minWidth, 0, mColumnWidths.length);
            }
            if (mRowHeights != null) {
                System.arraycopy(mRowHeights, 0, r.minHeight, 0, mRowHeights.length);
            }
            if (mColumnWeights != null) {
                System.arraycopy(mColumnWeights, 0, r.weightX, 0, mColumnWeights.length);
            }
            if (mRowWeights != null) {
                System.arraycopy(mRowWeights, 0, r.weightY, 0, mRowWeights.length);
            }

            /*
            * Pass #3
            *
            * Distribute the minimun widths and weights:
            */

            nextSize = Integer.MAX_VALUE;
            for (i = 1;
                 i != Integer.MAX_VALUE;
                 i = nextSize, nextSize = Integer.MAX_VALUE) {
                for (compindex = 0; compindex < components.length; compindex++) {
                    comp = components[compindex];
                    if (!comp.isVisible()) {
                        continue;
                    }
                    constraints = lookupConstraints(comp);
                    if (constraints.tempWidth == i) {
                        px = constraints.tempX + constraints.tempWidth; /* right column */

                        /*
                        * Figure out if we should use this slave\'s weight.  If the weight
                        * is less than the total weight spanned by the width of the cell,
                        * then discard the weight.  Otherwise split the difference
                        * according to the existing weights.
                        */

                        weight_diff = constraints.weightx;
                        for (k = constraints.tempX; k < px; k++) {
                            weight_diff -= r.weightX[k];
                        }
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; k++) {
                                weight += r.weightX[k];
                            }
                            for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
                                double wt = r.weightX[k];
                                double dx = (wt * weight_diff) / weight;
                                r.weightX[k] += dx;
                                weight_diff -= dx;
                                weight -= wt;
                            }
                            /* Assign the remainder to the rightmost cell */
                            r.weightX[px - 1] += weight_diff;
                        }

                        /*
                        * Calculate the minWidth array values.
                        * First, figure out how wide the current slave needs to be.
                        * Then, see if it will fit within the current minWidth values.
                        * If it will not fit, add the difference according to the
                        * weightX array.
                        */

                        pixels_diff =
                                constraints.minWidth + constraints.ipadx +
                                constraints.insets.left + constraints.insets.right;
                        for (k = constraints.tempX; k < px; k++) {
                            pixels_diff -= r.minWidth[k];
                        }
                        if (pixels_diff > 0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; k++) {
                                weight += r.weightX[k];
                            }
                            for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
                                double wt = r.weightX[k];
                                int dx = (int) ((wt * (double) pixels_diff) / weight);
                                r.minWidth[k] += dx;
                                pixels_diff -= dx;
                                weight -= wt;
                            }
                            /* Any leftovers go into the rightmost cell */
                            r.minWidth[px - 1] += pixels_diff;
                        }
                    } else if (constraints.tempWidth > i && constraints.tempWidth < nextSize) {
                        nextSize = constraints.tempWidth;
                    }
                    if (constraints.tempHeight == i) {
                        py = constraints.tempY + constraints.tempHeight; /* bottom row */

                        /*
                        * Figure out if we should use this slave's weight.  If the weight
                        * is less than the total weight spanned by the height of the cell,
                        * then discard the weight.  Otherwise split it the difference
                        * according to the existing weights.
                        */

                        weight_diff = constraints.weighty;
                        for (k = constraints.tempY; k < py; k++) {
                            weight_diff -= r.weightY[k];
                        }
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempY; k < py; k++) {
                                weight += r.weightY[k];
                            }
                            for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
                                double wt = r.weightY[k];
                                double dy = (wt * weight_diff) / weight;
                                r.weightY[k] += dy;
                                weight_diff -= dy;
                                weight -= wt;
                            }
                            /* Assign the remainder to the bottom cell */
                            r.weightY[py - 1] += weight_diff;
                        }

                        /*
                        * Calculate the minHeight array values.
                        * First, figure out how tall the current slave needs to be.
                        * Then, see if it will fit within the current minHeight values.
                        * If it will not fit, add the difference according to the
                        * weightY array.
                        */

                        pixels_diff =
                                constraints.minHeight + constraints.ipady +
                                constraints.insets.top + constraints.insets.bottom;
                        for (k = constraints.tempY; k < py; k++) {
                            pixels_diff -= r.minHeight[k];
                        }
                        if (pixels_diff > 0) {
                            weight = 0.0;
                            for (k = constraints.tempY; k < py; k++) {
                                weight += r.weightY[k];
                            }
                            for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
                                double wt = r.weightY[k];
                                int dy = (int) ((wt * (double) pixels_diff) / weight);
                                r.minHeight[k] += dy;
                                pixels_diff -= dy;
                                weight -= wt;
                            }
                            /* Any leftovers go into the bottom cell */
                            r.minHeight[py - 1] += pixels_diff;
                        }
                    } else if (constraints.tempHeight > i &&
                               constraints.tempHeight < nextSize) {
                        nextSize = constraints.tempHeight;
                    }
                }
            }
            return r;
        }
    }

    /**
     * Determines the origin of the layout area, in the graphics coordinate
     * space of the target container.  This value represents the pixel
     * coordinates of the top-left corner of the layout area regardless of
     * the <code>ComponentOrientation</code> value of the container.  This
     * is distinct from the grid origin given by the cell coordinates (0,0).
     * Most applications do not call this method directly.
     *
     * @return the graphics origin of the cell in the top-left
     *         corner of the layout grid
     *
     * @see ComponentOrientation
     */
    public Point getLayoutOrigin() {
        Point origin = new Point(0, 0);
        if (mLayoutInfo != null) {
            origin.x = mLayoutInfo.startx;
            origin.y = mLayoutInfo.starty;
        }
        return origin;
    }

    /**
     * Determines the weights of the layout grid's columns and rows.
     * Weights are used to calculate how much a given column or row
     * stretches beyond its preferred size, if the layout has extra
     * room to fill.
     * Most applications do not call this method directly.
     *
     * @return an array of two arrays, representing the
     *         horizontal weights of the layout columns
     *         and the vertical weights of the layout rows
     */
    public double[][] getLayoutWeights() {
        if (mLayoutInfo == null) {
            return new double[2][0];
        }
        double[][] weights = new double[2][];
        weights[0] = new double[mLayoutInfo.width];
        weights[1] = new double[mLayoutInfo.height];
        System.arraycopy(mLayoutInfo.weightX, 0, weights[0], 0, mLayoutInfo.width);
        System.arraycopy(mLayoutInfo.weightY, 0, weights[1], 0, mLayoutInfo.height);
        return weights;
    }

    /**
     * Figures out the minimum size of the
     * master based on the information from getLayoutInfo().
     *
     * @param aParent the layout container
     * @param aInfo   the layout info for this parent
     *
     * @return a <code>Dimension</code> object containing the
     *         minimum size
     */
    protected Dimension getMinSize(Container aParent, SideLayoutInfo aInfo) {
        Dimension d = new Dimension();
        int i, t;
        Insets insets = aParent.getInsets();
        t = 0;
        for (i = 0; i < aInfo.width; i++) {
            t += aInfo.minWidth[i];
        }
        d.width = t + insets.left + insets.right;
        t = 0;
        for (i = 0; i < aInfo.height; i++) {
            t += aInfo.minHeight[i];
        }
        d.height = t + insets.top + insets.bottom;
        return d;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container aTarget) {
    }

    /**
     * Lays out the specified container using this grid bag layout.
     * This method reshapes components in the specified container in
     * order to satisfy the contraints of this <code>SideLayout</code>
     * object.
     * Most applications do not call this method directly.
     *
     * @param aParent the container in which to do the layout
     */
    public void layoutContainer(Container aParent) {
        arrangeGrid(aParent);
    }

    /**
     * Determines which cell in the layout grid contains the point
     * specified by <code>(x,&nbsp;y)</code>. Each cell is identified
     * by its column index (ranging from 0 to the number of columns
     * minus 1) and its row index (ranging from 0 to the number of
     * rows minus 1).
     * If the <code>(x,&nbsp;y)</code> point lies
     * outside the grid, the following rules are used.
     * The column index is returned as zero if <code>x</code> lies to the
     * left of the layout for a left-to-right container or to the right of
     * the layout for a right-to-left container.  The column index is returned
     * as the number of columns if <code>x</code> lies
     * to the right of the layout in a left-to-right container or to the left
     * in a right-to-left container.
     * The row index is returned as zero if <code>y</code> lies above the
     * layout, and as the number of rows if <code>y</code> lies
     * below the layout.  The orientation of a container is determined by its
     * <code>ComponentOrientation</code> property.
     *
     * @param x the <i>x</i> coordinate of a point
     * @param y the <i>y</i> coordinate of a point
     *
     * @return an ordered pair of indexes that indicate which cell
     *         in the layout grid contains the point
     *         (<i>x</i>,&nbsp;<i>y</i>).
     */
    public Point location(int x, int y) {
        Point loc = new Point(0, 0);
        int i, d;
        if (mLayoutInfo == null) {
            return loc;
        }
        d = mLayoutInfo.startx;
        if (!mRightToLeft) {
            for (i = 0; i < mLayoutInfo.width; i++) {
                d += mLayoutInfo.minWidth[i];
                if (d > x) {
                    break;
                }
            }
        } else {
            for (i = mLayoutInfo.width - 1; i >= 0; i--) {
                if (d > x) {
                    break;
                }
                d += mLayoutInfo.minWidth[i];
            }
            i++;
        }
        loc.x = i;
        d = mLayoutInfo.starty;
        for (i = 0; i < mLayoutInfo.height; i++) {
            d += mLayoutInfo.minHeight[i];
            if (d > y) {
                break;
            }
        }
        loc.y = i;
        return loc;
    }

    /**
     * Retrieves the constraints for the specified component.
     * The return value is not a copy, but is the actual
     * <code>SideConstraints</code> object used by the layout mechanism.
     *
     * @param aComp the component to be queried
     *
     * @return the contraints for the specified component
     */
    protected SideConstraints lookupConstraints(Component aComp) {
        SideConstraints constraints = (SideConstraints) mComptable.get(aComp);
        if (constraints == null) {
            setConstraints(aComp, mDefaultConstraints);
            constraints = (SideConstraints) mComptable.get(aComp);
        }
        return constraints;
    }

    /**
     * Returns the maximum dimensions for this layout given the components
     * in the specified target container.
     *
     * @param aTarget the container which needs to be laid out
     *
     * @return the maximum dimensions for this layout
     */
    public Dimension maximumLayoutSize(Container aTarget) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Determines the minimum size of the <code>parent</code> container
     * using this grid bag layout.
     * Most applications do not call this method directly.
     *
     * @param aParent the container in which to do the layout
     *
     * @return the minimum size of the <code>parent</code> container
     */
    public Dimension minimumLayoutSize(Container aParent) {
        SideLayoutInfo info = getLayoutInfo(aParent, MINSIZE);
        return getMinSize(aParent, info);
    }

    /**
     * Determines the preferred size of the <code>parent</code>
     * container using this grid bag layout.
     * Most applications do not call this method directly.
     *
     * @param aParent the container in which to do the layout
     *
     * @return the preferred size of the <code>parent</code>
     *         container
     */
    public Dimension preferredLayoutSize(Container aParent) {
        SideLayoutInfo info = getLayoutInfo(aParent, PREFERREDSIZE);
        return getMinSize(aParent, info);
    }

    /**
     * Removes the constraints for the specified component in this layout
     *
     * @param aComp the component to be modified
     */
    private void removeConstraints(Component aComp) {
        mComptable.remove(aComp);
    }

    /**
     * Removes the specified component from this layout.
     * Most applications do not call this method directly.
     *
     * @param aComp the component to be removed.
     */
    public void removeLayoutComponent(Component aComp) {
        removeConstraints(aComp);
    }

    /**
     * Sets the constraints for the specified component in this layout.
     *
     * @param aComp        the component to be modified
     * @param aConstraints the constraints to be applied
     */
    public void setConstraints(Component aComp, SideConstraints aConstraints) {
        mComptable.put(aComp, aConstraints.clone());
    }

    /**
     * Returns a string representation of this grid bag layout's values.
     *
     * @return a string representation of this grid bag layout.
     */
    public String toString() {
        return getClass().getName();
    }
}