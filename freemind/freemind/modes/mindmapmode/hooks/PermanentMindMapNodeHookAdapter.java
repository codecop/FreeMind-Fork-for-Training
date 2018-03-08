/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2005  Christian Foltin <christianfoltin@users.sourceforge.net>
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


package freemind.modes.mindmapmode.hooks;

import freemind.extensions.PermanentNodeHook;
import freemind.extensions.PermanentNodeHookAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;

/**
 * Normal Permanent... enhanced by the getMindMapController method.
 * 
 * This is a specialization adapted to the mindmap mode.
 * As currently, nearly all hooks belong to the mindmap mode, 
 * all derive from this class.
 * */
public class PermanentMindMapNodeHookAdapter extends PermanentNodeHookAdapter {

	/**
     *
     */
	public PermanentMindMapNodeHookAdapter() {
		super();

	}

	public MindMapController getMindMapController() {
		return (MindMapController) getController();
	}

	/**
	 * @param child
	 *            the child node the hook should be propagated to.
	 * @return returns the new hook or null if there is already such a hook.
	 */
	protected PermanentNodeHook propagate(MindMapNode child) {
		PermanentNodeHook hook = (PermanentNodeHook) getMindMapController()
				.createNodeHook(getName(), child);
		// invocation:
		child.invokeHook(hook);
		return hook;
	}

}
