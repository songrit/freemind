/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2011 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitri Polivaev and others.
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package plugins.map;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.openstreetmap.gui.jmapviewer.JMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * Default map controller which implements map moving by pressing the right
 * mouse button and zooming by double click or by mouse wheel.
 * 
 * @author Jan Peter Stotz
 *
 * FreeMind Extensions:
 * - Move with button 1 (consistency with FreeMind UI) OK
 * - Single click for Set Cursor OK
 * - Mouse Wheel: Zoom OK
 * - Control-Mouse Wheel: ?
 * - (Right click +) Menu: popup menu mit
 *   * If right click, then the cursor is set to that position (consistency with FM-UI)
 *   * Place node(s) ==> the node gets a {@link MapMarkerLocation} here. The position, the position of the map
 *     and the zoom is stored in the node.
 *   * 
 *   
 *   Node Extra Menu Items:
 *   * Show node(s) in Map ==> Chooses the best view for the nodes and selects them. 
 * 
 */
public class FreeMindMapController extends JMapController implements
		MouseListener, MouseMotionListener, MouseWheelListener {

	JCursorMapViewer getMap() {
		return (JCursorMapViewer) map;
	}
	
	private static final int MOUSE_BUTTONS_MASK = MouseEvent.BUTTON3_DOWN_MASK
			| MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;

	private static final int MAC_MOUSE_BUTTON3_MASK = MouseEvent.CTRL_DOWN_MASK
			| MouseEvent.BUTTON1_DOWN_MASK;
	private static final int MAC_MOUSE_BUTTON1_MASK = MouseEvent.BUTTON1_DOWN_MASK;

	public FreeMindMapController(JMapViewer map) {
		super(map);
	}

	private Point lastDragPoint;

	private boolean isMoving = false;

	private boolean movementEnabled = true;

	private int movementMouseButton = MouseEvent.BUTTON1;
	private int movementMouseButtonMask = MouseEvent.BUTTON1_DOWN_MASK;

	private boolean wheelZoomEnabled = true;
	private boolean doubleClickZoomEnabled = true;

	public void mouseDragged(MouseEvent e) {
		if (!movementEnabled || !isMoving)
			return;
		// Is only the selected mouse button pressed?
		if ((e.getModifiersEx() & MOUSE_BUTTONS_MASK) == movementMouseButtonMask) {
			Point p = e.getPoint();
			if (lastDragPoint != null) {
				int diffx = lastDragPoint.x - p.x;
				int diffy = lastDragPoint.y - p.y;
				map.moveMap(diffx, diffy);
//				System.out.println("Move to " + map.getPosition() + " with zoom " + map.getZoom() );
			}
			lastDragPoint = p;
		}
	}

	public void mouseClicked(MouseEvent e) {
    	// is button 1?
    	if(e.getButton() == MouseEvent.BUTTON1|| isPlatformOsx()
			&& e.getModifiersEx() == MAC_MOUSE_BUTTON1_MASK)
        getMap().setCursorPosition(map.getPosition(e.getPoint()));
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == movementMouseButton || isPlatformOsx()
				&& e.getModifiersEx() == MAC_MOUSE_BUTTON1_MASK) {
			lastDragPoint = null;
			isMoving = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == movementMouseButton || isPlatformOsx()
				&& e.getButton() == MouseEvent.BUTTON1) {
			lastDragPoint = null;
			isMoving = false;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (wheelZoomEnabled) {
			map.setZoom(map.getZoom() - e.getWheelRotation(), e.getPoint());
		}
	}

	public boolean isMovementEnabled() {
		return movementEnabled;
	}

	/**
	 * Enables or disables that the map pane can be moved using the mouse.
	 * 
	 * @param movementEnabled
	 */
	public void setMovementEnabled(boolean movementEnabled) {
		this.movementEnabled = movementEnabled;
	}

	public int getMovementMouseButton() {
		return movementMouseButton;
	}

	/**
	 * Sets the mouse button that is used for moving the map. Possible values
	 * are:
	 * <ul>
	 * <li>{@link MouseEvent#BUTTON1} (left mouse button)</li>
	 * <li>{@link MouseEvent#BUTTON2} (middle mouse button)</li>
	 * <li>{@link MouseEvent#BUTTON3} (right mouse button)</li>
	 * </ul>
	 * 
	 * @param movementMouseButton
	 */
	public void setMovementMouseButton(int movementMouseButton) {
		this.movementMouseButton = movementMouseButton;
		switch (movementMouseButton) {
		case MouseEvent.BUTTON1:
			movementMouseButtonMask = MouseEvent.BUTTON1_DOWN_MASK;
			break;
		case MouseEvent.BUTTON2:
			movementMouseButtonMask = MouseEvent.BUTTON2_DOWN_MASK;
			break;
		case MouseEvent.BUTTON3:
			movementMouseButtonMask = MouseEvent.BUTTON3_DOWN_MASK;
			break;
		default:
			throw new RuntimeException("Unsupported button");
		}
	}

	public boolean isWheelZoomEnabled() {
		return wheelZoomEnabled;
	}

	public void setWheelZoomEnabled(boolean wheelZoomEnabled) {
		this.wheelZoomEnabled = wheelZoomEnabled;
	}

	public boolean isDoubleClickZoomEnabled() {
		return doubleClickZoomEnabled;
	}

	public void setDoubleClickZoomEnabled(boolean doubleClickZoomEnabled) {
		this.doubleClickZoomEnabled = doubleClickZoomEnabled;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		// Mac OSX simulates with ctrl + mouse 1 the second mouse button hence
		// no dragging events get fired.
		//
		if (isPlatformOsx()) {
			if (!movementEnabled || !isMoving)
				return;
			// Is only the selected mouse button pressed?
			if (e.getModifiersEx() == 0 /* MouseEvent.CTRL_DOWN_MASK */ ) {
				Point p = e.getPoint();
				if (lastDragPoint != null) {
					int diffx = lastDragPoint.x - p.x;
					int diffy = lastDragPoint.y - p.y;
					map.moveMap(diffx, diffy);
				}
				lastDragPoint = p;
			}

		}

	}

	/**
	 * Replies true if we are currently running on OSX
	 * 
	 * @return true if we are currently running on OSX
	 */
	public static boolean isPlatformOsx() {
		String os = System.getProperty("os.name");
		return os != null && os.toLowerCase().startsWith("mac os x");
	}
}