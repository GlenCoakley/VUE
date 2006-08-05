 /*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003, 2004 
 * Tufts University. All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */


package tufts.vue.gui;

import tufts.vue.VUE;
import tufts.vue.MapViewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.BorderFactory;

/**
 * Scroll pane for MapViewer / MapViewport with a focus indicator.
 *
 * @version $Revision: 1.5 $ / $Date: 2006-08-05 00:28:52 $ / $Author: sfraize $
 * @author Scott Fraize
 */

public class MapScrollPane extends javax.swing.JScrollPane
{
    public final static boolean UseMacFocusBorder = false;
    
    private FocusIndicator mFocusIndicator;

    private final MapViewer mViewer;
    
    public MapScrollPane(MapViewer viewer) {
        super(viewer);

        mViewer = viewer;

        setFocusable(false);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
        setWheelScrollingEnabled(true);
        getVerticalScrollBar().setUnitIncrement(16);
        getHorizontalScrollBar().setUnitIncrement(16);

        mFocusIndicator = new FocusIndicator(viewer);

        setCorner(LOWER_RIGHT_CORNER, mFocusIndicator);

        if (UseMacFocusBorder) {
            // Leave default installed special mac focus border.
            // The Mac Aqua focus border looks fantastic, but we have to
            // repaint the whole map every time the focus changes to
            // another map, which is slow.
        } else if (GUI.isMacAqua()) {
            if (GUI.isMacBrushedMetal())
                // use same color as mac brushed metal inactive border
                setBorder(BorderFactory.createLineBorder(new Color(155,155,155), 1));
            else
                setBorder(null); // no border at all for now for default mac look
        }
        
        //addFocusListener(viewer);
    }

    public void addNotify() {
        super.addNotify();
        MouseWheelListener[] currentListeners = getMouseWheelListeners();
        System.out.println("MapScrollPane: CurrentMouseWheelListeners: " + java.util.Arrays.asList(currentListeners));
        if (currentListeners != null && currentListeners[0] != null) {
            removeMouseWheelListener(currentListeners[0]);
            addMouseWheelListener(new MouseWheelRelay(mViewer.getMouseWheelListener(), currentListeners[0]));
        } else
            addMouseWheelListener(mViewer.getMouseWheelListener());
    }

    protected javax.swing.JViewport createViewport() {
        return new tufts.vue.MapViewport();
    }

    public java.awt.Component getFocusIndicator() {
        return mFocusIndicator;
    }


    /** a little box for the lower right of a JScrollPane indicating this viewer's focus state */
    private static class FocusIndicator extends javax.swing.JComponent {
        final Color fill;
        final Color line;
        final static int inset = 4;

        final MapViewer mViewer;
        
        FocusIndicator(MapViewer viewer) {
            mViewer = viewer;
            
            if (GUI.isMacAqua()) {
                fill = GUI.AquaFocusBorderLight;
                line = GUI.AquaFocusBorderLight.darker();
                //line = AquaFocusBorderDark;
            } else {
                fill = GUI.getToolbarColor();
                line = fill.darker();
            }
        }
        
        public void paintComponent(Graphics g) {
            //if (VUE.multipleMapsVisible() || DEBUG.Enabled || DEBUG.FOCUS)
            paintIcon(g);
        }
        
        void paintIcon(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            
            // no effect on muddling with mac aqua JScrollPane focus border
            //((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            
            // fill a block if we own the VUE application focus (Actions apply here)
            if (VUE.getActiveViewer() == mViewer) {
                g.setColor(fill);
                g.fillRect(inset, inset, w-inset*2, h-inset*2);
            }
            
            // Draw a box if we own the KEYBOARD focus, which will appear as a border to
            // the above block assuming we have VUE app focus.  Keyboard focus effects
            // special cases such as holding down the space-bar to trigger the hand/pan
            // tool, which is not an action, but a key detected right on the MapViewer.
            // Also, e.g., the viewer loses keyboard focus when there is an
            // activeTextEdit, while keeping VUE app focus.
            
            if (mViewer.isFocusOwner()) {
                g.setColor(line);
                w--; h--;
                g.drawRect(inset, inset, w-inset*2, h-inset*2);
            }
            //if (DEBUG.FOCUS) out("painted focusIndicator");
        }
    }
    
    
}


class MouseWheelRelay implements MouseWheelListener {
    private MouseWheelListener head, tail;
    public MouseWheelRelay(MouseWheelListener head, MouseWheelListener tail) {
        if (head == null || tail == null)
            throw new NullPointerException("MouseWheelRelay: neither head or tail can be null");
        this.head = head;
        this.tail = tail;
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        head.mouseWheelMoved(e);
        if (!e.isConsumed())
            tail.mouseWheelMoved(e);
    }
}
