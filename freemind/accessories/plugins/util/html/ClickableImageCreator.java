/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
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

package accessories.plugins.util.html;

import java.awt.Point;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;

import freemind.main.HtmlTools;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;

/** */
public class ClickableImageCreator {

	public static class AreaHolder {
		// <area shape="rect" href="#id47808" alt="Import/Export of parts of a
		// map" title="Import/Export of parts of a map" coords="" />
		// <area shape="rect" href="#id47708" alt="Screenshots cross-red coming
		// soon." title="Screenshots cross-red coming soon."
		// coords="699,143,835,168" />
		String shape = "rect";

		String href;

		String alt;

		String title;

		Rectangle coordinates = new Rectangle();

		/**
		 * Optionally holds the link (URL) of a node.
		 */
		public String link;

	}

	Vector<AreaHolder> area = new Vector<>();

	private final MindMapNode root;

	private final ModeController modeController;

	private Rectangle innerBounds;

	private final String linkFormatter;

	private MapView mapView;

	/**
	 * @param linkFormatter
	 *            if for example the link abc must be replaced with FMabcFM,
	 *            then this string has to be FM$1FM.
	 */
	public ClickableImageCreator(MindMapNode root,
			ModeController modeController, String linkFormatter) {
		super();
		this.root = root;
		this.linkFormatter = linkFormatter;
		mapView = modeController.getView();
		if (mapView != null) {
			innerBounds = mapView.getInnerBounds();
		} else {
			// test case: give any bounds:
			innerBounds = new Rectangle(0, 0, 100, 100);
		}
		this.modeController = modeController;
		createArea();
	}

	public String generateHtml() {
		StringBuffer htmlArea = new StringBuffer();
		for (Iterator<AreaHolder> i = area.iterator(); i.hasNext();) {
			AreaHolder holder = i.next();
			MessageFormat formatter = new MessageFormat(linkFormatter);
			String replacement = formatter.format(new Object[] { holder.href, holder.link });
			if(replacement.isEmpty()) {
				continue;
			}
			htmlArea.append("<area shape=\"" + holder.shape + "\" href=\""
					+ replacement + "\" alt=\""
					+ HtmlTools.toXMLEscapedText(holder.alt) + "\" title=\""
					+ HtmlTools.toXMLEscapedText(holder.title) + "\" coords=\""
					+ holder.coordinates.x + "," + holder.coordinates.y + ","
					+ (holder.coordinates.width + holder.coordinates.x) + ","
					+ +(holder.coordinates.height + holder.coordinates.y)
					+ "\" />");
		}
		return htmlArea.toString();
	}

	private void createArea() {
		createArea(root);
	}

	/**
     */
	private void createArea(MindMapNode node) {
		if (mapView == null) {
			return;
		}
		final NodeView nodeView = mapView.getNodeView(node);
		if (nodeView != null) {
			AreaHolder holder = new AreaHolder();
			holder.title = node.getShortText(modeController);
			holder.alt = node.getShortText(modeController);
			holder.href = node.getObjectId(modeController);
			holder.link = node.getLink()!=null?node.getLink():"";
			Point contentXY = mapView.getNodeContentLocation(nodeView);
			final JComponent content = nodeView.getContent();
			holder.coordinates.x = (int) (contentXY.x - innerBounds.getMinX());
			holder.coordinates.y = (int) (contentXY.y - innerBounds.getMinY());
			holder.coordinates.width = content.getWidth();
			holder.coordinates.height = content.getHeight();
			area.add(holder);
			for (Iterator<MindMapNode> i = node.childrenUnfolded(); i.hasNext();) {
				MindMapNode child = i.next();
				createArea(child);
			}
		}
	}

}
