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


package freemind.modes.filemode;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import freemind.main.FreeMindMain;
import freemind.modes.ArrowLinkAdapter;
import freemind.modes.ArrowLinkTarget;
import freemind.modes.CloudAdapter;
import freemind.modes.EdgeAdapter;
import freemind.modes.MapAdapter;
import freemind.modes.MindMap;
import freemind.modes.MindMapLinkRegistry;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.NodeAdapter;

@SuppressWarnings("serial")
public class FileMapModel extends MapAdapter {

	private MindMapLinkRegistry linkRegistry;

	//
	// Constructors
	//

	public FileMapModel(FreeMindMain frame, ModeController modeController) {
		this(new File(File.separator), frame, modeController);
	}

	public FileMapModel(File root, FreeMindMain frame,
			ModeController modeController) {
		super(modeController);
		setRoot(new FileNodeModel(root, this));
		getRootNode().setFolded(false);
		linkRegistry = new MindMapLinkRegistry();
	}

	//
	// Other methods
	//
	public MindMapLinkRegistry getLinkRegistry() {
		return linkRegistry;
	}

	//
	// Other methods
	//
	public boolean save(File file) {
		return true;
	}

	/**
     *
     */

	public void destroy() {
		/*
		 * fc, 8.8.2004: don't call super.destroy as this method tries to remove
		 * the hooks recursively. This must fail.
		 */
		// super.destroy();
		cancelFileChangeObservationTimer();
	}

	public boolean isSaved() {
		return true;
	}

	public String toString() {
		return "File: " + getRoot().toString();
	}

	public void changeNode(MindMapNode node, String newText) {
		// File file = ((FileNodeModel)node).getFile();
		// File newFile = new File(file.getParentFile(), newText);
		// file.renameTo(newFile);
		// System.out.println(file);
		// FileNodeModel parent = (FileNodeModel)node.getParent();
		// // removeNodeFromParent(node);

		// insertNodeInto(new FileNodeModel(newFile),parent,0);

		// nodeChanged(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemind.modes.MindMap#setLinkInclinationChanged()
	 */
	public void setLinkInclinationChanged() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemind.modes.MindMap#getXml(java.io.Writer)
	 */
	public void getXml(Writer fileout) throws IOException {
		// nothing.
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemind.modes.MindMap#getFilteredXml(java.io.Writer)
	 */
	public void getFilteredXml(Writer fileout) throws IOException {
		// nothing.
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createNodeAdapter(freemind.modes.MindMap, java.lang.String)
	 */
	@Override
	public NodeAdapter createNodeAdapter(MindMap pMap, String pNodeClass) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createEdgeAdapter(freemind.modes.NodeAdapter)
	 */
	@Override
	public EdgeAdapter createEdgeAdapter(NodeAdapter pNode) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createCloudAdapter(freemind.modes.NodeAdapter)
	 */
	@Override
	public CloudAdapter createCloudAdapter(NodeAdapter pNode) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createArrowLinkAdapter(freemind.modes.NodeAdapter, freemind.modes.NodeAdapter)
	 */
	@Override
	public ArrowLinkAdapter createArrowLinkAdapter(NodeAdapter pSource,
			NodeAdapter pTarget) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createArrowLinkTarget(freemind.modes.NodeAdapter, freemind.modes.NodeAdapter)
	 */
	@Override
	public ArrowLinkTarget createArrowLinkTarget(NodeAdapter pSource,
			NodeAdapter pTarget) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap#createEncryptedNode(java.lang.String)
	 */
	@Override
	public NodeAdapter createEncryptedNode(String pAdditionalInfo) {
		// FIXME: Implement me if you need me.
		throw new RuntimeException("Unimplemented method called.");
	}

}

// public class FileSystemModel extends AbstractTreeTableModel
// implements TreeTableModel {

// // The the returned file length for directories.
// public static final Integer ZERO = new Integer(0);

// //
// // Some convenience methods.
// //

// protected File getFile(Object node) {
// FileNode fileNode = ((FileNode)node);
// return fileNode.getFile();
// }

// protected Object[] getChildren(Object node) {
// FileNode fileNode = ((FileNode)node);
// return fileNode.getChildren();
// }

// //
// // The TreeModel interface
// //

// public int getChildCount(Object node) {
// Object[] children = getChildren(node);
// return (children == null) ? 0 : children.length;
// }

// public Object getChild(Object node, int i) {
// return getChildren(node)[i];
// }
// }
