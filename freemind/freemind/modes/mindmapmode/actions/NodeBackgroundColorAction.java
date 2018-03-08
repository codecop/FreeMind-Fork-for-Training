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
 * Created on 19.09.2004
 */


package freemind.modes.mindmapmode.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;

import freemind.controller.Controller;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;

@SuppressWarnings("serial")
public class NodeBackgroundColorAction extends MindmapAction {
	private final MindMapController controller;

	public NodeBackgroundColorAction(MindMapController controller) {
		super("node_background_color", (String) null, controller);
		this.controller = controller;
	}

	public void actionPerformed(ActionEvent e) {
		Color color = Controller.showCommonJColorChooserDialog(controller
				.getView().getSelected(), controller
				.getText("choose_node_background_color"), controller
				.getSelected().getBackgroundColor());
		if (color == null) {
			return;
		}
		for (MindMapNode selected : controller.getSelecteds()) {
			controller.setNodeBackgroundColor(selected, color);
		}
	}

	public static class RemoveNodeBackgroundColorAction extends
			NodeGeneralAction {

		public RemoveNodeBackgroundColorAction(
				final MindMapController controller) {
			super(controller, "remove_node_background_color", (String) null);
			setSingleNodeOperation(new SingleNodeOperation() {

				public void apply(MindMapMapModel map, MindMapNodeModel node) {
					controller.setNodeBackgroundColor(node, null);
				}
			});
		}

	}


}