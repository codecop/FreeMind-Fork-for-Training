/*
 * FreeMind - A Program for creating and viewing Mindmaps Copyright (C)
 * 2000-2004 Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 * 
 * See COPYING for Details
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Created on 05.10.2004
 */


package freemind.modes.mindmapmode.actions;

import javax.swing.Action;
import javax.swing.JMenuItem;

import freemind.controller.MenuItemSelectedListener;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.xml.ActionPair;

@SuppressWarnings("serial")
public class NodeStyleAction extends NodeGeneralAction implements MenuItemSelectedListener {
	private final String mStyle;

	public NodeStyleAction(MindMapController controller, String style) {
		super(controller, style, null);
		this.mStyle = style;
	}

	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.NodeGeneralAction#getActionPair(freemind.modes.mindmapmode.MindMapNodeModel)
	 */
	@Override
	protected ActionPair getActionPair(MindMapNodeModel pSelected) {
		return getMindMapController().getActorFactory().getNodeStyleActor().getActionPair(pSelected, mStyle);
	}
	

	public boolean isSelected(JMenuItem pCheckItem, Action pAction) {
		MindMapNode selected = modeController.getSelected();
		if (!selected.hasStyle())
			return false;
		return Tools.safeEquals(mStyle, selected.getStyle());
	}
}