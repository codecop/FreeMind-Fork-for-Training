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

package freemind.modes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import freemind.controller.Controller;
import freemind.controller.LastStateStorageManagement;
import freemind.controller.MapModuleManager;
import freemind.controller.MapMouseMotionListener;
import freemind.controller.MapMouseWheelListener;
import freemind.controller.MindMapNodesSelection;
import freemind.controller.NodeDragListener;
import freemind.controller.NodeDropListener;
import freemind.controller.NodeKeyListener;
import freemind.controller.NodeMotionListener;
import freemind.controller.NodeMouseMotionListener;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.actions.generated.instance.MindmapLastStateStorage;
import freemind.controller.actions.generated.instance.NodeListMember;
import freemind.extensions.PermanentNodeHook;
import freemind.main.FreeMindCommon;
import freemind.main.FreeMindMain;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.main.XMLParseException;
import freemind.modes.FreeMindFileDialog.DirectoryResultListener;
import freemind.modes.common.listeners.MindMapMouseWheelEventHandler;
import freemind.view.MapModule;
import freemind.view.mindmapview.IndependantMapViewCreator;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.ViewFeedback;

/**
 * Derive from this class to implement the Controller for your mode. Overload
 * the methods you need for your data model, or use the defaults. There are some
 * default Actions you may want to use for easy editing of your model. Take
 * MindMapController as a sample.
 */
@SuppressWarnings("serial")
public abstract class ControllerAdapter extends MapFeedbackAdapter implements ModeController,
		DirectoryResultListener {


	private Mode mode;

	private Color selectionColor = new Color(200, 220, 200);
	/**
	 * The model, this controller belongs to. It may be null, if it is the
	 * default controller that does not show a map.
	 */
	private MapAdapter mModel;
	private HashSet<NodeSelectionListener> mNodeSelectionListeners = new HashSet<>();
	private HashSet<NodeLifetimeListener> mNodeLifetimeListeners = new HashSet<>();
	private File lastCurrentDir = null;

	/**
	 * Instantiation order: first me and then the model.
	 */
	public ControllerAdapter(Mode mode) {
		this.setMode(mode);
		// for updates of nodes:
		// FIXME
		// do not associate each new ControllerAdapter
		// with the only one application viewport
		// DropTarget dropTarget = new DropTarget(getFrame().getViewport(),
		// new FileOpener());
	}

	public void setModel(MapAdapter model) {
		mModel = model;
	}

	//
	// Methods that should be overloaded
	//

	public abstract MindMapNode newNode(Object userObject, MindMap map);

	/**
	 * You _must_ implement this if you use one of the following actions:
	 * OpenAction, NewMapAction.
	 * 
	 * @param modeController
	 *            TODO
	 */
	public MapAdapter newModel(ModeController modeController) {
		throw new java.lang.UnsupportedOperationException();
	}

	/**
	 * You may want to implement this... It returns the FileFilter that is used
	 * by the open() and save() JFileChoosers.
	 */
	protected FileFilter getFileFilter() {
		return null;
	}

	/**
	 * Currently, this method is called by the mapAdapter. This is buggy, and is
	 * to be changed.
	 */
	public void nodeChanged(MindMapNode node) {
		setSaved(false);
		nodeRefresh(node, true);
	}

	public void setSaved(boolean pIsClean) {
		boolean stateChanged = getMap().setSaved(pIsClean);
		if (stateChanged) {
			getController().setTitle();
		}
	}


	public void nodeRefresh(MindMapNode node) {
		nodeRefresh(node, false);
	}

	private void nodeRefresh(MindMapNode node, boolean isUpdate) {
		logger.finest("nodeChanged called for node " + node + " parent="
				+ node.getParentNode());
		if (isUpdate) {
			// update modification times:
			if (node.getHistoryInformation() != null) {
				node.getHistoryInformation().setLastModifiedAt(new Date());
			}
			// Tell any node hooks that the node is changed:
			updateNode(node);
		}
		// fc, 10.10.06: Dirty hack in order to keep this method away from being
		// used by everybody.
		((MapAdapter) getMap()).nodeChangedInternal(node);
	}

	public void refreshMap() {
		final MindMapNode root = getMap().getRootNode();
		refreshMapFrom(root);
	}

	public void refreshMapFrom(MindMapNode node) {
		for(MindMapNode child : node.getChildren()) {
			refreshMapFrom(child);
		}
		((MapAdapter) getMap()).nodeChangedInternal(node);

	}

	/**
	 */
	public void nodeStructureChanged(MindMapNode node) {
		getMap().nodeStructureChanged(node);
	}

	/**
	 * Overwrite this method to perform additional operations to an node update.
	 */
	protected void updateNode(MindMapNode node) {
		for (NodeSelectionListener listener : mNodeSelectionListeners) {
			listener.onUpdateNodeHook(node);
		}
	}

	public void onLostFocusNode(NodeView node) {
		try {
			// deselect the old node:
			HashSet<NodeSelectionListener> copy = new HashSet<>(mNodeSelectionListeners);
			// we copied the set to be able to remove listeners during a
			// listener method.
			for (NodeSelectionListener listener : copy) {
				listener.onLostFocusNode(node);
			}
			for (PermanentNodeHook hook : node.getModel().getActivatedHooks()) {
				hook.onLostFocusNode(node);
			}
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Error in node selection listeners", e);
		}

	}

	public void onFocusNode(NodeView node) {
		try {
			// select the new node:
			HashSet<NodeSelectionListener> copy = new HashSet<>(mNodeSelectionListeners);
			// we copied the set to be able to remove listeners during a
			// listener method.
			for (NodeSelectionListener listener : copy) {
				listener.onFocusNode(node);
			}
			for (PermanentNodeHook hook : node.getModel().getActivatedHooks()) {
				hook.onFocusNode(node);
			}
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Error in node selection listeners", e);
		}

	}

	public void changeSelection(NodeView pNode, boolean pIsSelected) {
		try {
			HashSet<NodeSelectionListener> copy = new HashSet<>(mNodeSelectionListeners);
			for (NodeSelectionListener listener : copy) {
				listener.onSelectionChange(pNode, pIsSelected);
			}
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Error in node selection listeners", e);
		}

	}

	public void onViewCreatedHook(NodeView node) {
		for (PermanentNodeHook hook : node.getModel().getActivatedHooks()) {
			hook.onViewCreatedHook(node);
		}
	}

	public void onViewRemovedHook(NodeView node) {
		for (PermanentNodeHook hook : node.getModel().getActivatedHooks()) {
			hook.onViewRemovedHook(node);
		}
	}

	public void registerNodeSelectionListener(NodeSelectionListener listener,
			boolean pCallWithCurrentSelection) {
		mNodeSelectionListeners.add(listener);
		if (pCallWithCurrentSelection) {
			try {
				listener.onFocusNode(getSelectedView());
			} catch (Exception e) {
				freemind.main.Resources.getInstance().logException(e);
			}
			for (NodeView view : getView().getSelecteds()) {
				try {
					listener.onSelectionChange(view, true);
				} catch (Exception e) {
					freemind.main.Resources.getInstance().logException(e);
				}
			}
		}
	}

	public void deregisterNodeSelectionListener(NodeSelectionListener listener) {
		mNodeSelectionListeners.remove(listener);
	}

	public void registerNodeLifetimeListener(NodeLifetimeListener listener, boolean pFireCreateEvent) {
		mNodeLifetimeListeners.add(listener);
		if (pFireCreateEvent) {
			// call create node for all:
			// TODO: fc, 10.2.08: this event goes to all listeners. It should be for
			// the new listener only?
			fireRecursiveNodeCreateEvent(getRootNode());
		}
	}

	public void deregisterNodeLifetimeListener(NodeLifetimeListener listener) {
		mNodeLifetimeListeners.remove(listener);
	}

	public HashSet<NodeLifetimeListener> getNodeLifetimeListeners() {
		return mNodeLifetimeListeners;
	}

	public void fireNodePreDeleteEvent(MindMapNode node) {
		// call lifetime listeners:
		for (NodeLifetimeListener listener : mNodeLifetimeListeners) {
			listener.onPreDeleteNode(node);
		}
	}

	public void fireNodePostDeleteEvent(MindMapNode node, MindMapNode parent) {
		// call lifetime listeners:
		for (NodeLifetimeListener listener : mNodeLifetimeListeners) {
			listener.onPostDeleteNode(node, parent);
		}
	}

	public void fireRecursiveNodeCreateEvent(MindMapNode node) {
		for (Iterator<MindMapNode> i = node.childrenUnfolded(); i.hasNext();) {
			MindMapNode child = i.next();
			fireRecursiveNodeCreateEvent(child);
		}
		// call lifetime listeners:
		for (NodeLifetimeListener listener : mNodeLifetimeListeners) {
			listener.onCreateNodeHook(node);
		}
	}

	public void firePreSaveEvent(MindMapNode node) {
		// copy to prevent concurrent modification.
		HashSet<NodeSelectionListener> listenerCopy = new HashSet<>(mNodeSelectionListeners);
		for (NodeSelectionListener listener : listenerCopy) {
			listener.onSaveNode(node);
		}
	}

	//
	// Map Management
	//

	public String getText(String textId) {
		return getController().getResourceString(textId);
	}

	public ModeController newMap() {
		ModeController newModeController = getMode().createModeController();
		MapAdapter newModel = newModel(newModeController);
		newMap(newModel, newModeController);
		newModeController.getView().moveToRoot();
		return newModeController;
	}

	public void newMap(final MindMap mapModel, ModeController pModeController) {
		getController().getMapModuleManager().newMapModule(mapModel,
				pModeController);
		pModeController.setSaved(false);
	}

	/**
	 * You may decide to overload this or take the default and implement the
	 * functionality in your MapModel (implements MindMap)
	 */
	public MapFeedback load(URL file) throws FileNotFoundException,
			IOException, XMLParseException, URISyntaxException {
		String mapDisplayName = getController().getMapModuleManager()
				.checkIfFileIsAlreadyOpened(file);
		if (null != mapDisplayName) {
			getController().getMapModuleManager().changeToMapModule(
					mapDisplayName);
			return getController().getModeController();
		} else {
			final ModeController newModeController = getMode().createModeController();
			final MapAdapter model = newModel(newModeController);
			((ControllerAdapter) newModeController).loadInternally(file, model);
			newMap(model, newModeController);
			newModeController.setSaved(true);
			restoreMapsLastState(newModeController, model);
			return newModeController;
		}
	}

	/**
	 * @param model 
	 * @param pFile
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws XMLParseException 
	 */
	abstract protected void loadInternally(URL url, MapAdapter model) throws URISyntaxException, XMLParseException, IOException;

	/**
	 * You may decide to overload this or take the default and implement the
	 * functionality in your MapModel (implements MindMap)
	 */
	public MapFeedback load(File file) throws FileNotFoundException,
			IOException {
		try {
			return load(Tools.fileToUrl(file));
		} catch (XMLParseException e) {
			freemind.main.Resources.getInstance().logException(e);
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			freemind.main.Resources.getInstance().logException(e);
			throw new RuntimeException(e);
		}
	}

	protected void restoreMapsLastState(final ModeController newModeController,
			final MapAdapter model) {
		// restore zoom, etc.
		String lastStateMapXml = getFrame().getProperty(
				FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE);
		LastStateStorageManagement management = new LastStateStorageManagement(
				lastStateMapXml);
		MindmapLastStateStorage store = management.getStorage(model
				.getRestorable());
		if (store != null) {
			ModeController modeController = newModeController;
			// Zoom must be set on combo box, too.
			getController().setZoom(store.getLastZoom());
			MindMapNode sel = null;
			try {
				// Selected:
				sel = modeController.getNodeFromID(store.getLastSelected());
				modeController.centerNode(sel);
				List<MindMapNode> selected = new Vector<>();
				for (Iterator<NodeListMember> iter = store.getListNodeListMemberList().iterator(); iter.hasNext();) {
					NodeListMember member = iter.next();
					MindMapNode selNode = modeController.getNodeFromID(member.getNode());
					selected.add(selNode);
				}
				modeController.select(sel, selected);
			} catch (Exception e) {
				freemind.main.Resources.getInstance().logException(e);
				newModeController.getView().moveToRoot();
			}
		} else {
			newModeController.getView().moveToRoot();
		}
	}

	public boolean save() {
		if (getModel().isSaved())
			return true;
		if (getModel().getFile() == null || getModel().isReadOnly()) {
			return saveAs();
		} else {
			return save(getModel().getFile());
		}
	}

	public void loadURL(String relative) {
		try {
			logger.info("Trying to open " + relative);
			URL absolute = null;
			if (Tools.isAbsolutePath(relative)) {
				// Protocol can be identified by rexep pattern "[a-zA-Z]://.*".
				// This should distinguish a protocol path from a file path on
				// most platforms.
				// 1) UNIX / Linux - obviously
				// 2) Windows - relative path does not contain :, in absolute
				// path is : followed by \.
				// 3) Mac - cannot remember

				// If relative is an absolute path, then it cannot be a
				// protocol.
				// At least on Unix and Windows. But this is not true for Mac!!

				// Here is hidden an assumption that the existence of protocol
				// implies !Tools.isAbsolutePath(relative).
				// The code should probably be rewritten to convey more logical
				// meaning, on the other hand
				// it works on Windows and Linux.

				// absolute = new URL("file://"+relative); }
				absolute = Tools.fileToUrl(new File(relative));
			} else if (relative.startsWith("#")) {
				// inner map link, fc, 12.10.2004
				logger.finest("found relative link to " + relative);
				String target = relative.substring(1);
				try {
					centerNode(getNodeFromID(target));
				} catch (Exception e) {
					freemind.main.Resources.getInstance().logException(e);
					// give "not found" message
					getFrame().out(
							Tools.expandPlaceholders(getText("link_not_found"),
									target));
				}
				return;

			} else {
				/*
				 * Remark: getMap().getURL() returns URLs like file:/C:/... It
				 * seems, that it does not cause any problems.
				 */
				absolute = new URL(getMap().getURL(), relative);
			}
			// look for reference part in URL:
			URL originalURL = absolute;
			String ref = absolute.getRef();
			if (ref != null) {
				// remove ref from absolute:
				absolute = Tools.getURLWithoutReference(absolute);
			}
			String extension = Tools.getExtension(absolute.toString());
			if ((extension != null)
					&& extension
							.equals(freemind.main.FreeMindCommon.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) { // ----
																											// Open
																											// Mind
																											// Map
				logger.info("Trying to open mind map " + absolute);
				MapModuleManager mapModuleManager = getController()
						.getMapModuleManager();
				/*
				 * this can lead to confusion if the user handles multiple maps
				 * with the same name. Obviously, this is wrong. Get a better
				 * check whether or not the file is already opened.
				 */
				String mapExtensionKey = mapModuleManager
						.checkIfFileIsAlreadyOpened(absolute);
				if (mapExtensionKey == null) {
					setWaitingCursor(true);
					load(absolute);
				} else {
					mapModuleManager.tryToChangeToMapModule(mapExtensionKey);
				}
				if (ref != null) {
					try {
						ModeController newModeController = getController()
								.getModeController();
						// jump to link:
						newModeController.centerNode(newModeController
								.getNodeFromID(ref));
					} catch (Exception e) {
						freemind.main.Resources.getInstance().logException(e);
						getFrame().out(
								Tools.expandPlaceholders(
										getText("link_not_found"), ref));
						return;
					}
				}
			} else {
				// ---- Open URL in browser
				getFrame().openDocument(originalURL);
			}
		} catch (MalformedURLException ex) {
			freemind.main.Resources.getInstance().logException(ex);
			getController().errorMessage(getText("url_error") + "\n" + ex);
			return;
		} catch (Exception e) {
			freemind.main.Resources.getInstance().logException(e);
		} finally {
			setWaitingCursor(false);
		}
	}

	/* (non-Javadoc)
	 * @see freemind.modes.ExtendedMapFeedback#setWaitingCursor(boolean)
	 */
	public void setWaitingCursor(boolean pWaiting) {
		getFrame().setWaitingCursor(pWaiting);
	}

	
	/**
	 * fc, 24.1.2004: having two methods getSelecteds with different return
	 * values (linkedlists of models resp. views) is asking for trouble. @see
	 * MapView
	 * 
	 * @return returns a list of MindMapNode s.
	 */
	public List<MindMapNode> getSelecteds() {
		LinkedList<MindMapNode> selecteds = new LinkedList<>();
		ListIterator<NodeView> it = getView().getSelecteds().listIterator();
		if (it != null) {
			while (it.hasNext()) {
				NodeView selected = it.next();
				selecteds.add(selected.getModel());
			}
		}
		return selecteds;
	}

	@Override
	public void select(NodeView node) {
		getView().select(node);
	}

	public void select(MindMapNode primarySelected, List<MindMapNode> selecteds) {
		// are they visible?
		for (MindMapNode node : selecteds) {
			displayNode(node);
		}
		final NodeView focussedNodeView = getNodeView(primarySelected);
		if (focussedNodeView != null) {
			getView().selectAsTheOnlyOneSelected(focussedNodeView);
			getView().scrollNodeToVisible(focussedNodeView);
			for (Iterator<MindMapNode> i = selecteds.iterator(); i.hasNext();) {
				MindMapNode node = i.next();
				NodeView nodeView = getNodeView(node);
				if (nodeView != null) {
					getView().makeTheSelected(nodeView);
				}
			}
		}
		getController().obtainFocusForSelected();
	}

	public void selectBranch(NodeView selected, boolean extend) {
		displayNode(selected.getModel());
		getView().selectBranch(selected, extend);
	}

	public List<MindMapNode> getSelectedsByDepth() {
		// return an ArrayList of MindMapNodes.
		List<MindMapNode> result = getSelecteds();
		sortNodesByDepth(result);
		return result;
	}

	/**
	 * Return false is the action was cancelled, e.g. when it has to lead to
	 * saving as.
	 */
	public boolean save(File file) {
		boolean result = false;
		try {
			setWaitingCursor(true);
			result = getModel().save(file);
			// create thumbnail if desired.
			if (result && "true"
					.equals(getProperty(FreeMindCommon.CREATE_THUMBNAIL_ON_SAVE))) {
				File baseFileName = getModel().getFile();
				String fileName = Resources.getInstance()
						.createThumbnailFileName(baseFileName);
				// due to a windows bug, the file must not be hidden before writing it.
				Tools.makeFileHidden(new File(fileName), false);
				IndependantMapViewCreator.printToFile(getView(), fileName,
						true,
						getIntProperty(FreeMindCommon.THUMBNAIL_SIZE, 800));
				Tools.makeFileHidden(new File(fileName), true);
			}
		} catch (FileNotFoundException e) {
			freemind.main.Resources.getInstance().logException(e);
			String message = Tools.expandPlaceholders(getText("save_failed"),
					file.getName());
			getController().errorMessage(message);
		} catch (Exception e) {
			logger.severe("Error in MindMapMapModel.save(): ");
			freemind.main.Resources.getInstance().logException(e);
		} finally {
			setWaitingCursor(false);
		}
		if(result) {
			setSaved(true);
		}
		return result;
	}

	/** @return returns the new JMenuItem. */
	protected JMenuItem add(JMenu menu, Action action, String keystroke) {
		JMenuItem item = menu.add(action);
		item.setAccelerator(KeyStroke.getKeyStroke(getFrame()
				.getAdjustableProperty(keystroke)));
		return item;
	}

	/**
	 * @return returns the new JMenuItem.
	 * @param keystroke
	 *            can be null, if no keystroke should be assigned.
	 */
	protected JMenuItem add(StructuredMenuHolder holder, String category,
			Action action, String keystroke) {
		JMenuItem item = holder.addAction(action, category);
		if (keystroke != null) {
			String keyProperty = getFrame().getAdjustableProperty(keystroke);
			logger.finest("Found key stroke: " + keyProperty);
			item.setAccelerator(KeyStroke.getKeyStroke(keyProperty));
		}
		return item;
	}

	/**
	 * @return returns the new JCheckBoxMenuItem.
	 * @param keystroke
	 *            can be null, if no keystroke should be assigned.
	 */
	protected JMenuItem addCheckBox(StructuredMenuHolder holder,
			String category, Action action, String keystroke) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) holder.addMenuItem(
				new JCheckBoxMenuItem(action), category);
		if (keystroke != null) {
			item.setAccelerator(KeyStroke.getKeyStroke(getFrame()
					.getAdjustableProperty(keystroke)));
		}
		return item;
	}

	protected JMenuItem addRadioItem(StructuredMenuHolder holder,
			String category, Action action, String keystroke, boolean isSelected) {
		JRadioButtonMenuItem item = (JRadioButtonMenuItem) holder.addMenuItem(
				new JRadioButtonMenuItem(action), category);
		if (keystroke != null) {
			item.setAccelerator(KeyStroke.getKeyStroke(getFrame()
					.getAdjustableProperty(keystroke)));
		}
		item.setSelected(isSelected);
		return item;
	}

	protected void add(JMenu menu, Action action) {
		menu.add(action);
	}

	//
	// Dialogs with user
	//

	public void open() {
		FreeMindFileDialog chooser = getFileChooser();
		// fc, 24.4.2008: multi selection has problems as setTitle in Controller
		// doesn't works
		// chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(getView());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles;
			if (chooser.isMultiSelectionEnabled()) {
				selectedFiles = chooser.getSelectedFiles();
			} else {
				selectedFiles = new File[] { chooser.getSelectedFile() };
			}
			for (int i = 0; i < selectedFiles.length; i++) {
				File theFile = selectedFiles[i];
				try {
					lastCurrentDir = theFile.getParentFile();
					load(theFile);
				} catch (Exception ex) {
					handleLoadingException(ex);
					break;
				}
			}
		}
		getController().setTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.FreeMindFileDialog.DirectoryResultListener#setChosenDirectory
	 * (java.io.File)
	 */
	public void setChosenDirectory(File pDir) {
		lastCurrentDir = pDir;
	}

	/**
	 * Creates a file chooser with the last selected directory as default.
	 */
	public FreeMindFileDialog getFileChooser(FileFilter filter) {
		FreeMindFileDialog chooser = Resources.getInstance().getStandardFileChooser(filter);
		chooser.registerDirectoryResultListener(this);
		File parentFile = getMapsParentFile();
		// choose new lastCurrentDir only, if not previously set.
		if (parentFile != null && lastCurrentDir == null) {
			lastCurrentDir = parentFile;
		}
		if (lastCurrentDir != null) {
			chooser.setCurrentDirectory(lastCurrentDir);
		}
		return chooser;
	}

	public FreeMindFileDialog getFileChooser() {
		return getFileChooser(getFileFilter());
	}

	private File getMapsParentFile() {
		if ((getMap() != null) && (getMap().getFile() != null)
				&& (getMap().getFile().getParentFile() != null)) {
			return getMap().getFile().getParentFile();
		}
		return null;
	}

	public void handleLoadingException(Exception ex) {
		String exceptionType = ex.getClass().getName();
		if (exceptionType.equals("freemind.main.XMLParseException")) {
			int showDetail = JOptionPane.showConfirmDialog(getView(),
					getText("map_corrupted"), "FreeMind",
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				getController().errorMessage(ex);
			}
		} else if (exceptionType.equals("java.io.FileNotFoundException")) {
			getController().errorMessage(ex.getMessage());
		} else {
			freemind.main.Resources.getInstance().logException(ex);
			getController().errorMessage(ex);
		}
	}

	/**
	 * Save as; return false is the action was cancelled
	 */
	public boolean saveAs() {
		File f;
		FreeMindFileDialog chooser = getFileChooser();
		if (getMapsParentFile() == null) {
			chooser.setSelectedFile(new File(getFileNameProposal()
					+ freemind.main.FreeMindCommon.FREEMIND_FILE_EXTENSION));
		}
		chooser.setDialogTitle(getText("save_as"));
		boolean repeatSaveAsQuestion;
		do {
			repeatSaveAsQuestion = false;
			int returnVal = chooser.showSaveDialog(getView());
			if (returnVal != JFileChooser.APPROVE_OPTION) {// not ok pressed
				return false;
			}

			// |= Pressed O.K.
			f = chooser.getSelectedFile();
			lastCurrentDir = f.getParentFile();
			// Force the extension to be .mm
			String ext = Tools.getExtension(f.getName());
			if (!ext.equals(freemind.main.FreeMindCommon.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
				f = new File(f.getParent(), f.getName()
						+ freemind.main.FreeMindCommon.FREEMIND_FILE_EXTENSION);
			}

			if (f.exists()) { // If file exists, ask before overwriting.
				int overwriteMap = JOptionPane.showConfirmDialog(getView(),
						getText("map_already_exists"), "FreeMind",
						JOptionPane.YES_NO_OPTION);
				if (overwriteMap != JOptionPane.YES_OPTION) {
					// repeat the save as dialog.
					repeatSaveAsQuestion = true;
				}
			}
		} while (repeatSaveAsQuestion);
		try { // We have to lock the file of the map even when it does not exist
				// yet
			String lockingUser = getModel().tryToLock(f);
			if (lockingUser != null) {
				getFrame().getController().informationMessage(
						Tools.expandPlaceholders(
								getText("map_locked_by_save_as"), f.getName(),
								lockingUser));
				return false;
			}
		} catch (Exception e) { // Throwed by tryToLock
			getFrame().getController().informationMessage(
					Tools.expandPlaceholders(
							getText("locking_failed_by_save_as"), f.getName()));
			return false;
		}

		save(f);
		// Update the name of the map
		getController().getMapModuleManager().updateMapModuleName();
		return true;
	}

	/**
	 * Creates a proposal for a file name to save the map. Removes all illegal
	 * characters.
	 * 
	 * Fixed: When creating file names based on the text of the root node, now
	 * all the extra unicode characters are replaced with _. This is not very
	 * good. For chinese content, you would only get a list of ______ as a file
	 * name. Only characters special for building file paths shall be removed
	 * (rather than replaced with _), like : or /. The exact list of dangeous
	 * characters needs to be investigated. 0.8.0RC3.
	 * 
	 * 
	 * Keywords: suggest file name.
	 * 
	 */
	private String getFileNameProposal() {
		return Tools.getFileNameProposal(getMap().getRootNode());
	}

	/**
	 * Return false if user has canceled.
	 */
	public boolean close(boolean force, MapModuleManager mapModuleManager) {
		// remove old messages.
		getFrame().out("");
		if (!force && !getModel().isSaved()) {
			String text = getText("save_unsaved") + "\n"
					+ mapModuleManager.getMapModule().toString();
			String title = Tools.removeMnemonic(getText("save"));
			int returnVal = JOptionPane.showOptionDialog(getFrame()
					.getContentPane(), text, title,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				boolean savingNotCancelled = save();
				if (!savingNotCancelled) {
					return false;
				}
			} else if ((returnVal == JOptionPane.CANCEL_OPTION)
					|| (returnVal == JOptionPane.CLOSED_OPTION)) {
				return false;
			}
		}
		LastStateStorageManagement management = new LastStateStorageManagement(
				getFrame().getProperty(
						FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE));
		String restorable = getModel().getRestorable();
		if (restorable != null) {
			MindmapLastStateStorage store = management.getStorage(restorable);
			if (store == null) {
				store = new MindmapLastStateStorage();
			}
			store.setRestorableName(restorable);
			store.setLastZoom(getView().getZoom());
			Point viewLocation = getView().getViewPosition();
			if (viewLocation != null) {
				store.setX(viewLocation.x);
				store.setY(viewLocation.y);
			}
			String lastSelected = this.getNodeID(this.getSelected());
			store.setLastSelected(lastSelected);
			store.clearNodeListMemberList();
			List<MindMapNode> selecteds = this.getSelecteds();
			for (MindMapNode node : selecteds) {
				NodeListMember member = new NodeListMember();
				member.setNode(this.getNodeID(node));
				store.addNodeListMember(member);
			}
			management.changeOrAdd(store);
			getFrame().setProperty(
					FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE,
					management.getXml());
		}

		getModel().destroy();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemind.modes.ModeController#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		NodeView node = getSelectedView();
		if (visible) {
			onFocusNode(node);
		} else {
			// bug fix, fc 18.5.2004. This should not be here.
			if (node != null) {
				onLostFocusNode(node);
			}
		}
		changeSelection(node, !visible);
	}

	/**
	 * Overwrite this to set all of your actions which are dependent on whether
	 * there is a map or not.
	 */
	protected void setAllActions(boolean enabled) {
		// controller actions:
		getController().zoomIn.setEnabled(enabled);
		getController().zoomOut.setEnabled(enabled);
		getController().showFilterToolbarAction.setEnabled(enabled);
	}

	//
	// Node editing
	//

	/**
	 * listener, that blocks the controler if the menu is active (PN) Take care!
	 * This listener is also used for modelpopups (as for graphical links).
	 */
	private class ControllerPopupMenuListener implements PopupMenuListener {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			setBlocked(true); // block controller
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			setBlocked(false); // unblock controller
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
			setBlocked(false); // unblock controller
		}

	}

	/**
	 * Take care! This listener is also used for modelpopups (as for graphical
	 * links).
	 */
	protected final ControllerPopupMenuListener popupListenerSingleton = new ControllerPopupMenuListener();

	public void showPopupMenu(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu popupmenu = getPopupMenu();
			if (popupmenu != null) {
				// adding listener could be optimized but without much profit...
				popupmenu.addPopupMenuListener(this.popupListenerSingleton);
				popupmenu.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
			}
		}
	}

	/** Default implementation: no context menu. */
	public JPopupMenu getPopupForModel(java.lang.Object obj) {
		return null;
	}

	/**
	 * Overwrite this, if you have one.
	 */
	public Component getLeftToolBar() {
		return null;
	}

	/**
	 * Overwrite this, if you have one.
	 */
	public JToolBar getModeToolBar() {
		return null;
	}

	// status, currently: default, blocked (PN)
	// (blocked to protect against particular events e.g. in edit mode)
	private boolean isBlocked = false;

	private MapView mView;

	public boolean isBlocked() {
		return this.isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	//
	// Convenience methods
	//

	public Mode getMode() {
		return mode;
	}

	protected void setMode(Mode mode) {
		this.mode = mode;
	}

	public MindMap getMap() {
		return mModel;
	}

	public MindMapNode getRootNode() {
		return (MindMapNode) getMap().getRoot();
	}

	public URL getResource(String name) {
		return getFrame().getResource(name);
	}
	
	/* (non-Javadoc)
	 * @see freemind.modes.MindMap.MapFeedback#getResourceString(java.lang.String)
	 */
	@Override
	public String getResourceString(String pTextId) {
		return getFrame().getResourceString(pTextId);
	}

	public Controller getController() {
		return getMode().getController();
	}

	public FreeMindMain getFrame() {
		return getController().getFrame();
	}

	/**
	 * This was inserted by fc, 10.03.04 to enable all actions to refer to its
	 * controller easily.
	 */
	public ControllerAdapter getModeController() {
		return this;
	}

	// fc, 29.2.2004: there is no sense in having this private and the
	// controller public,
	// because the getController().getModel() method is available anyway.
	public MapAdapter getModel() {
		return mModel;
	}

	public MapView getView() {
		return mView;
	}
	
	/* (non-Javadoc)
	 * @see freemind.modes.MapFeedback#getViewAbstraction()
	 */
	@Override
	public ViewAbstraction getViewAbstraction() {
		return getView();
	}
	
	/* (non-Javadoc)
	 * @see freemind.modes.MapFeedback#getViewFeedback()
	 */
	@Override
	public ViewFeedback getViewFeedback() {
		return this;
	}

	public void setView(MapView pView) {
		mView = pView;
	}

	protected void updateMapModuleName() {
		getController().getMapModuleManager().updateMapModuleName();
	}

	public MindMapNode getSelected() {
		final NodeView selectedView = getSelectedView();
		if (selectedView != null)
			return selectedView.getModel();
		return null;
	}

	public NodeView getSelectedView() {
		if (getView() != null)
			return getView().getSelected();
		return null;
	}

	
	public class OpenAction extends AbstractAction {
		ControllerAdapter mc;

		public OpenAction(ControllerAdapter modeController) {
			super(getText("open"), freemind.view.ImageFactory.getInstance().createIcon(
					getResource("images/fileopen.png")));
			mc = modeController;
		}

		public void actionPerformed(ActionEvent e) {
			mc.open();
			getController().setTitle(); // Possible update of read-only
		}
	}

	public class SaveAction extends FreemindAction {

		public SaveAction() {
			super(Tools.removeMnemonic(getText("save")), freemind.view.ImageFactory.getInstance().createIcon(
					getResource("images/filesave.png")), ControllerAdapter.this);
		}

		public void actionPerformed(ActionEvent e) {
			boolean success = save();
			if (success) {
				getFrame().out(getText("saved")); // perhaps... (PN)
			} else {
				String message = "Saving failed.";
				getFrame().out(message);
				getController().errorMessage(message);
			}
			getController().setTitle(); // Possible update of read-only
		}

	}

	public class SaveAsAction extends FreemindAction {

		public SaveAsAction() {
			super(getText("save_as"), freemind.view.ImageFactory.getInstance().createIcon(
					getResource("images/filesaveas.png")), ControllerAdapter.this);
		}

		public void actionPerformed(ActionEvent e) {
			saveAs();
			getController().setTitle(); // Possible update of read-only
		}
	}

	protected class FileOpener implements DropTargetListener {
		private boolean isDragAcceptable(DropTargetDragEvent event) {
			// check if there is at least one File Type in the list
			DataFlavor[] flavors = event.getCurrentDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) {
					// event.acceptDrag(DnDConstants.ACTION_COPY);
					return true;
				}
			}
			// event.rejectDrag();
			return false;
		}

		private boolean isDropAcceptable(DropTargetDropEvent event) {
			// check if there is at least one File Type in the list
			DataFlavor[] flavors = event.getCurrentDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) {
					return true;
				}
			}
			return false;
		}

		public void drop(DropTargetDropEvent dtde) {
			if (!isDropAcceptable(dtde)) {
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			try {
				Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				if (data == null) {
					// Shouldn't happen because dragEnter() rejects drags w/out
					// at least
					// one javaFileListFlavor. But just in case it does ...
					dtde.dropComplete(false);
					return;
				}
				Iterator<File> iterator = ((List<File>) data).iterator();
				while (iterator.hasNext()) {
					File file = iterator.next();
					load(file);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(
						getView(),
						"Couldn't open dropped file(s). Reason: "
								+ e.getMessage()
				// getText("file_not_found")
						);
				dtde.dropComplete(false);
				return;
			}
			dtde.dropComplete(true);
		}

		public void dragEnter(DropTargetDragEvent dtde) {
			if (!isDragAcceptable(dtde)) {
				dtde.rejectDrag();
				return;
			}
		}

		public void dragOver(DropTargetDragEvent e) {
		}

		public void dragExit(DropTargetEvent e) {
		}

		public void dragScroll(DropTargetDragEvent e) {
		}

		public void dropActionChanged(DropTargetDragEvent e) {
		}
	}

	public Transferable copy(MindMapNode node, boolean saveInvisible) {
		throw new IllegalArgumentException("No copy so far.");
	}

	public Transferable copy() {
		return copy(getView().getSelectedNodesSortedByY(), false);
	}

	public Transferable copySingle() {

		final ArrayList<MindMapNode> selectedNodes = getView().getSingleSelectedNodes();
		return copy(selectedNodes, false);
	}

	public Transferable copy(List<MindMapNode> selectedNodes, boolean copyInvisible) {
		try {
			String forNodesFlavor = createForNodesFlavor(selectedNodes,
					copyInvisible);
			List<String> createForNodeIdsFlavor = createForNodeIdsFlavor(selectedNodes,
					copyInvisible);

			String plainText = getMap().getAsPlainText(selectedNodes);
			return new MindMapNodesSelection(forNodesFlavor, null, plainText,
					getMap().getAsRTF(selectedNodes), getMap().getAsHTML(
							selectedNodes), null, null, createForNodeIdsFlavor);
		}

		catch (UnsupportedFlavorException ex) {
			freemind.main.Resources.getInstance().logException(ex);
		} catch (IOException ex) {
			freemind.main.Resources.getInstance().logException(ex);
		}
		return null;
	}

	public String createForNodesFlavor(List<MindMapNode> selectedNodes, boolean copyInvisible)
			throws UnsupportedFlavorException, IOException {
		String forNodesFlavor = "";
		boolean firstLoop = true;
		for (MindMapNode tmpNode : selectedNodes) {
			if (firstLoop) {
				firstLoop = false;
			} else {
				forNodesFlavor += NODESEPARATOR;
			}

			forNodesFlavor += copy(tmpNode, copyInvisible).getTransferData(
					MindMapNodesSelection.mindMapNodesFlavor);
		}
		return forNodesFlavor;
	}

	public List<String> createForNodeIdsFlavor(List<MindMapNode> selectedNodes, boolean copyInvisible)
			throws UnsupportedFlavorException, IOException {
		Vector<String> forNodesFlavor = new Vector<>();

		for (MindMapNode tmpNode : selectedNodes) {
			forNodesFlavor.add(getNodeID(tmpNode));
		}
		return forNodesFlavor;
	}

	/**
     */
	public Color getSelectionColor() {
		return selectionColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see freemind.modes.ModeController#updatePopupMenu(freemind.controller.
	 * StructuredMenuHolder)
	 */
	public void updatePopupMenu(StructuredMenuHolder holder) {

	}

	/**
     *
     */

	public void shutdownController() {
		setAllActions(false);
		getMapMouseWheelListener().deregister();
	}

	/**
	 * This method is called after and before a change of the map module. Use it
	 * to perform the actions that cannot be performed at creation time.
	 * 
	 */
	public void startupController() {
		setAllActions(true);
		if (getFrame().getView() != null) {
			FileOpener fileOpener = new FileOpener();
			new DropTarget(getFrame().getView(),fileOpener);
		}
		getMapMouseWheelListener().register(
				new MindMapMouseWheelEventHandler(this));
	}

	public String getLinkShortText(MindMapNode node) {
		String adaptedText = node.getLink();
		if (adaptedText == null)
			return null;
		if (adaptedText.startsWith("#")) {
			try {
				MindMapNode dest = getNodeFromID(adaptedText.substring(1));
				return dest.getShortText(this);
			} catch (Exception e) {
				return getText("link_not_available_any_more");
			}
		}
		return adaptedText;
	}

	public void displayNode(MindMapNode node) {
		displayNode(node, null);
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	public void displayNode(MindMapNode node, ArrayList<MindMapNode> nodesUnfoldedByDisplay) {
		// Unfold the path to the node
		Object[] path = getMap().getPathToRoot(node);
		// Iterate the path with the exception of the last node
		for (int i = 0; i < path.length - 1; i++) {
			MindMapNode nodeOnPath = (MindMapNode) path[i];
			// System.out.println(nodeOnPath);
			if (nodeOnPath.isFolded()) {
				if (nodesUnfoldedByDisplay != null)
					nodesUnfoldedByDisplay.add(nodeOnPath);
				setFolded(nodeOnPath, false);
			}
		}

	}

	/** Select the node and scroll to it. **/
	private void centerNode(NodeView node) {
		getView().centerNode(node);
		getView().selectAsTheOnlyOneSelected(node);
	}

	public void centerNode(MindMapNode node) {
		NodeView view = null;
		if (node != null) {
			view = getController().getView().getNodeView(node);
		} else {
			return;
		}
		if (view == null) {
			displayNode(node);
			view = getController().getView().getNodeView(node);
		}
		centerNode(view);
	}

	@Override
	public NodeView getNodeView(MindMapNode node) {
		return getView().getNodeView(node);
	}

	public void loadURL() {
		String link = getSelected().getLink();
		if (link != null) {
			loadURL(link);
		}
	}

	public Set<MouseWheelEventHandler> getRegisteredMouseWheelEventHandler() {
		return Collections.emptySet();
	}

	public MapModule getMapModule() {
		return getController().getMapModuleManager()
				.getModuleGivenModeController(this);
	}

	public void setToolTip(MindMapNode node, String key, String value) {
		node.setToolTip(key, value);
		nodeRefresh(node);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap.MapFeedback#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String pResourceId) {
		return getController().getProperty(pResourceId);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap.MapFeedback#getDefaultFont()
	 */
	@Override
	public Font getDefaultFont() {
		return getController().getDefaultFont();
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap.MapFeedback#getFontThroughMap(java.awt.Font)
	 */
	@Override
	public Font getFontThroughMap(Font pFont) {
		return getController().getFontThroughMap(pFont);
	}

	@Override
	public NodeMouseMotionListener getNodeMouseMotionListener() {
		return getController().getNodeMouseMotionListener();
	}

	@Override
	public NodeMotionListener getNodeMotionListener() {
		return getController().getNodeMotionListener();
	}

	@Override
	public NodeKeyListener getNodeKeyListener() {
		return getController().getNodeKeyListener();
	}

	@Override
	public NodeDragListener getNodeDragListener() {
		return getController().getNodeDragListener();
	}

	@Override
	public NodeDropListener getNodeDropListener() {
		return getController().getNodeDropListener();
	}

	@Override
	public MapMouseMotionListener getMapMouseMotionListener() {
		return getController().getMapMouseMotionListener();
	}

	@Override
	public MapMouseWheelListener getMapMouseWheelListener() {
		return getController().getMapMouseWheelListener();
	}


	/**
	 * @throws {@link IllegalArgumentException} when node isn't found.
	 */
	@Override
	public NodeAdapter getNodeFromID(String nodeID) {
		NodeAdapter node = (NodeAdapter) getMap().getLinkRegistry().getTargetForId(nodeID);
		if (node == null) {
			throw new IllegalArgumentException("Node belonging to the node id "
					+ nodeID + " not found in map " + getMap().getFile());
		}
		return node;
	}

	@Override
	public String getNodeID(MindMapNode selected) {
		return getMap().getLinkRegistry().registerLinkTarget(selected);
	}

	@Override
	public void setProperty(String pProperty, String pValue) {
		// this method fires a property change event to inform others.
		getController().setProperty(pProperty, pValue);
	}
	
	
}
