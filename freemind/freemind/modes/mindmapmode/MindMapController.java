/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006 Joerg Mueller, Daniel Polansky, Christian Foltin and others.
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

package freemind.modes.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import freemind.common.OptionalDontShowMeAgainDialog;
import freemind.common.XmlBindingTools;
import freemind.controller.MenuBar;
import freemind.controller.MindMapNodesSelection;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.actions.generated.instance.MenuActionBase;
import freemind.controller.actions.generated.instance.MenuCategoryBase;
import freemind.controller.actions.generated.instance.MenuCheckedAction;
import freemind.controller.actions.generated.instance.MenuRadioAction;
import freemind.controller.actions.generated.instance.MenuSeparator;
import freemind.controller.actions.generated.instance.MenuStructure;
import freemind.controller.actions.generated.instance.MenuSubmenu;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternEdgeColor;
import freemind.controller.actions.generated.instance.PatternEdgeStyle;
import freemind.controller.actions.generated.instance.PatternEdgeWidth;
import freemind.controller.actions.generated.instance.PatternIcon;
import freemind.controller.actions.generated.instance.PatternNodeBackgroundColor;
import freemind.controller.actions.generated.instance.PatternNodeColor;
import freemind.controller.actions.generated.instance.PatternNodeFontBold;
import freemind.controller.actions.generated.instance.PatternNodeFontItalic;
import freemind.controller.actions.generated.instance.PatternNodeFontName;
import freemind.controller.actions.generated.instance.PatternNodeFontSize;
import freemind.controller.actions.generated.instance.PatternNodeStyle;
import freemind.controller.actions.generated.instance.PatternNodeText;
import freemind.controller.actions.generated.instance.WindowConfigurationStorage;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.extensions.HookFactory;
import freemind.extensions.HookFactory.RegistrationContainer;
import freemind.extensions.HookRegistration;
import freemind.extensions.ModeControllerHook;
import freemind.extensions.NodeHook;
import freemind.extensions.PermanentNodeHook;
import freemind.extensions.UndoEventReceiver;
import freemind.main.ExampleFileFilter;
import freemind.main.FixedHTMLWriter;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.main.XMLParseException;
import freemind.modes.ControllerAdapter;
import freemind.modes.EdgeAdapter;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.FreeMindFileDialog;
import freemind.modes.MapAdapter;
import freemind.modes.MindIcon;
import freemind.modes.MindMap;
import freemind.modes.MindMap.AskUserBeforeUpdateCallback;
import freemind.modes.MindMap.MapSourceChangedObserver;
import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapNode;
import freemind.modes.Mode;
import freemind.modes.ModeController;
import freemind.modes.NodeAdapter;
import freemind.modes.NodeDownAction;
import freemind.modes.StylePatternFactory;
import freemind.modes.attributes.Attribute;
import freemind.modes.common.CommonNodeKeyListener;
import freemind.modes.common.CommonNodeKeyListener.EditHandler;
import freemind.modes.common.GotoLinkNodeAction;
import freemind.modes.common.actions.FindAction;
import freemind.modes.common.actions.FindAction.FindNextAction;
import freemind.modes.common.actions.NewMapAction;
import freemind.modes.common.listeners.CommonNodeMouseMotionListener;
import freemind.modes.mindmapmode.actions.AddArrowLinkAction;
import freemind.modes.mindmapmode.actions.AddLocalLinkAction;
import freemind.modes.mindmapmode.actions.ApplyPatternAction;
import freemind.modes.mindmapmode.actions.BoldAction;
import freemind.modes.mindmapmode.actions.ChangeArrowsInArrowLinkAction;
import freemind.modes.mindmapmode.actions.CloudAction;
import freemind.modes.mindmapmode.actions.ColorArrowLinkAction;
import freemind.modes.mindmapmode.actions.CopyAction;
import freemind.modes.mindmapmode.actions.CopySingleAction;
import freemind.modes.mindmapmode.actions.CutAction;
import freemind.modes.mindmapmode.actions.DeleteChildAction;
import freemind.modes.mindmapmode.actions.EdgeColorAction;
import freemind.modes.mindmapmode.actions.EdgeStyleAction;
import freemind.modes.mindmapmode.actions.EdgeWidthAction;
import freemind.modes.mindmapmode.actions.EditAction;
import freemind.modes.mindmapmode.actions.ExportBranchAction;
import freemind.modes.mindmapmode.actions.FontFamilyAction;
import freemind.modes.mindmapmode.actions.FontSizeAction;
import freemind.modes.mindmapmode.actions.HookAction;
import freemind.modes.mindmapmode.actions.IconAction;
import freemind.modes.mindmapmode.actions.ImportExplorerFavoritesAction;
import freemind.modes.mindmapmode.actions.ImportFolderStructureAction;
import freemind.modes.mindmapmode.actions.ItalicAction;
import freemind.modes.mindmapmode.actions.JoinNodesAction;
import freemind.modes.mindmapmode.actions.MindMapControllerHookAction;
import freemind.modes.mindmapmode.actions.MindmapAction;
import freemind.modes.mindmapmode.actions.MoveNodeAction;
import freemind.modes.mindmapmode.actions.NewChildAction;
import freemind.modes.mindmapmode.actions.NewPreviousSiblingAction;
import freemind.modes.mindmapmode.actions.NewSiblingAction;
import freemind.modes.mindmapmode.actions.NodeBackgroundColorAction;
import freemind.modes.mindmapmode.actions.NodeBackgroundColorAction.RemoveNodeBackgroundColorAction;
import freemind.modes.mindmapmode.actions.NodeColorAction;
import freemind.modes.mindmapmode.actions.NodeColorBlendAction;
import freemind.modes.mindmapmode.actions.NodeGeneralAction;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import freemind.modes.mindmapmode.actions.NodeStyleAction;
import freemind.modes.mindmapmode.actions.NodeUpAction;
import freemind.modes.mindmapmode.actions.PasteAction;
import freemind.modes.mindmapmode.actions.PasteAsPlainTextAction;
import freemind.modes.mindmapmode.actions.RedoAction;
import freemind.modes.mindmapmode.actions.RemoveAllIconsAction;
import freemind.modes.mindmapmode.actions.RemoveArrowLinkAction;
import freemind.modes.mindmapmode.actions.RemoveIconAction;
import freemind.modes.mindmapmode.actions.RevertAction;
import freemind.modes.mindmapmode.actions.SelectAllAction;
import freemind.modes.mindmapmode.actions.SelectBranchAction;
import freemind.modes.mindmapmode.actions.SetLinkByTextFieldAction;
import freemind.modes.mindmapmode.actions.SingleNodeOperation;
import freemind.modes.mindmapmode.actions.StrikethroughAction;
import freemind.modes.mindmapmode.actions.ToggleChildrenFoldedAction;
import freemind.modes.mindmapmode.actions.ToggleFoldedAction;
import freemind.modes.mindmapmode.actions.UnderlinedAction;
import freemind.modes.mindmapmode.actions.UndoAction;
import freemind.modes.mindmapmode.actions.UsePlainTextAction;
import freemind.modes.mindmapmode.actions.UseRichFormattingAction;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.actions.xml.ActionRegistry;
import freemind.modes.mindmapmode.actions.xml.DefaultActionHandler;
import freemind.modes.mindmapmode.actions.xml.UndoActionHandler;
import freemind.modes.mindmapmode.actions.xml.actors.XmlActorFactory;
import freemind.modes.mindmapmode.hooks.MindMapHookFactory;
import freemind.modes.mindmapmode.listeners.MindMapMouseMotionManager;
import freemind.modes.mindmapmode.listeners.MindMapNodeDropListener;
import freemind.modes.mindmapmode.listeners.MindMapNodeMotionListener;
import freemind.view.MapModule;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
@SuppressWarnings("serial")
public class MindMapController extends ControllerAdapter implements
		ExtendedMapFeedback, MapSourceChangedObserver {

	public static final String REGEXP_FOR_NUMBERS_IN_STRINGS = "([+\\-]?[0-9]*[.,]?[0-9]+)\\b";
	/**
	 * @author foltin
	 * @date 19.11.2013
	 */
	private final class NodeInformationTimerAction implements ActionListener {
		private boolean mIsInterrupted = false;
		private boolean mIsDone = true;

		public boolean isRunning() {
			return !mIsDone;
		}

		/**
		 * @return true, if successfully interrupted.
		 */
		public boolean interrupt() {
			mIsInterrupted = true;
			int i = 1000;
			try {
				while (i > 0 && !mIsDone) {
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				freemind.main.Resources.getInstance().logException(e);
			}
			mIsInterrupted = false;
			return i > 0;
		}

		@Override
		public void actionPerformed(ActionEvent pE) {
			mIsDone = false;
			actionPerformedInternally(pE);
			mIsDone = true;
		}

		public void actionPerformedInternally(ActionEvent pE) {
			String nodeStatusLine;
			List<MindMapNode> selecteds = getSelecteds();
			int amountOfSelecteds = selecteds.size();
			if (amountOfSelecteds == 0)
				return;
			double sum = 0d;
			java.util.regex.Pattern p = java.util.regex.Pattern
					.compile(REGEXP_FOR_NUMBERS_IN_STRINGS);
			for (MindMapNode selectedNode : selecteds) {
				if (mIsInterrupted) {
					return;
				}
				Matcher m = p.matcher(selectedNode.getText());
				while (m.find()) {
					String number = m.group();
					try {
						sum += Double.parseDouble(number);
					} catch (NumberFormatException e) {
						// freemind.main.Resources.getInstance().logException(e);
					}
				}
			}
			if (amountOfSelecteds > 1) {
				nodeStatusLine = Resources.getInstance().format(
						"node_status_line_several_selected_nodes",
						new Object[] { new Integer(amountOfSelecteds),
								new Double(sum) });
			} else {
				MindMapNode sel = (MindMapNode) selecteds.get(0);
				long amountOfChildren = 0;
				Vector<MindMapNode> allDescendants = Tools.getVectorWithSingleElement(sel);
				while (!allDescendants.isEmpty()) {
					if (mIsInterrupted) {
						return;
					}
					MindMapNode child = (MindMapNode) allDescendants
							.firstElement();
					amountOfChildren += child.getChildCount();
					allDescendants.addAll(child.getChildren());
					allDescendants.remove(0);
				}
				nodeStatusLine = Resources.getInstance().format(
						"node_status_line",
						new Object[] {
								sel.getShortText(MindMapController.this),
								new Integer(sel.getChildCount()),
								amountOfChildren });
			}
			getFrame().out(nodeStatusLine);
		}

	}

	/**
	 * @author foltin
	 * @date 24.01.2012
	 */
	private final class MapSourceChangeDialog implements Runnable {
		/**
		 * 
		 */
		private boolean mReturnValue = true;

		/**
		 * @param pReturnValue
		 */
		private MapSourceChangeDialog() {
		}

		public void run() {
			int showResult = new OptionalDontShowMeAgainDialog(
					getFrame().getJFrame(),
					getSelectedView(),
					"file_changed_on_disk_reload",
					"confirmation",
					MindMapController.this,
					new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
							getController(),
							FreeMind.RESOURCES_RELOAD_FILES_WITHOUT_QUESTION),
					OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED)
					.show().getResult();
			if (showResult != JOptionPane.OK_OPTION) {
				getFrame().out(
						Tools.expandPlaceholders(getText("file_not_reloaded"),
								getMap().getFile().toString()));
				mReturnValue = false;
				return;
			}
			revertAction.actionPerformed(null);
		}

		public boolean getReturnValue() {
			return mReturnValue;
		}
	}


	public interface MindMapControllerPlugin {

	}

	private static final String RESOURCE_UNFOLD_ON_PASTE = "unfold_on_paste";
	// for MouseEventHandlers
	private HashSet<MouseWheelEventHandler> mRegisteredMouseWheelEventHandler = new HashSet<>();

	private ActionRegistry actionFactory;
	private Vector<HookAction> hookActions;
	// Mode mode;
	private MindMapPopupMenu popupmenu;
	// private JToolBar toolbar;
	private MindMapToolBar toolbar;
	private Clipboard clipboard = null;
	private Clipboard selection = null;

	private HookFactory nodeHookFactory;

	public ApplyPatternAction patterns[] = new ApplyPatternAction[0]; // Make
																		// sure
																		// it is
																		// initialized
	public Action newMap = new NewMapAction(this);
	public Action open = new OpenAction(this);
	public Action save = new SaveAction();
	public Action saveAs = new SaveAsAction();
	public Action exportToHTML = new ExportToHTMLAction(this);
	public Action exportBranchToHTML = new ExportBranchToHTMLAction(this);

	public Action editLong = new EditLongAction();
	public Action newSibling = new NewSiblingAction(this);
	public Action newPreviousSibling = new NewPreviousSiblingAction(this);
	public Action setLinkByFileChooser = new SetLinkByFileChooserAction();
	public Action setImageByFileChooser = new SetImageByFileChooserAction();
	public Action followLink = new FollowLinkAction();
	public Action openLinkDirectory = new OpenLinkDirectoryAction();
	public Action exportBranch = new ExportBranchAction(this);
	public Action importBranch = new ImportBranchAction();
	public Action importLinkedBranch = new ImportLinkedBranchAction();
	public Action importLinkedBranchWithoutRoot = new ImportLinkedBranchWithoutRootAction();

	public Action propertyAction = null;

	public Action increaseNodeFont = new NodeGeneralAction(this,
			"increase_node_font_size", null, new SingleNodeOperation() {
				public void apply(MindMapMapModel map, MindMapNodeModel node) {
					increaseFontSize(node, 1);
				}
			});
	public Action decreaseNodeFont = new NodeGeneralAction(this,
			"decrease_node_font_size", null, new SingleNodeOperation() {
				public void apply(MindMapMapModel map, MindMapNodeModel node) {
					increaseFontSize(node, -1);
				}
			});

	public UndoAction undo = null;
	public RedoAction redo = null;
	public CopyAction copy = null;
	public Action copySingle = null;
	public CutAction cut = null;
	public PasteAction paste = null;
	public PasteAsPlainTextAction pasteAsPlainText = null;
	public BoldAction bold = null;
	public StrikethroughAction strikethrough = null;
	public ItalicAction italic = null;
	public UnderlinedAction underlined = null;
	public FontSizeAction fontSize = null;
	public FontFamilyAction fontFamily = null;
	public NodeColorAction nodeColor = null;
	public EditAction edit = null;
	public NewChildAction newChild = null;
	public DeleteChildAction deleteChild = null;
	public ToggleFoldedAction toggleFolded = null;
	public ToggleChildrenFoldedAction toggleChildrenFolded = null;
	public UseRichFormattingAction useRichFormatting = null;
	public UsePlainTextAction usePlainText = null;
	public NodeUpAction nodeUp = null;
	public NodeDownAction nodeDown = null;
	public EdgeColorAction edgeColor = null;
	public EdgeWidthAction EdgeWidth_WIDTH_PARENT = null;
	public EdgeWidthAction EdgeWidth_WIDTH_THIN = null;
	public EdgeWidthAction EdgeWidth_1 = null;
	public EdgeWidthAction EdgeWidth_2 = null;
	public EdgeWidthAction EdgeWidth_4 = null;
	public EdgeWidthAction EdgeWidth_8 = null;
	public EdgeWidthAction edgeWidths[] = null;
	public EdgeStyleAction EdgeStyle_linear = null;
	public EdgeStyleAction EdgeStyle_bezier = null;
	public EdgeStyleAction EdgeStyle_sharp_linear = null;
	public EdgeStyleAction EdgeStyle_sharp_bezier = null;
	public EdgeStyleAction edgeStyles[] = null;
	public NodeColorBlendAction nodeColorBlend = null;
	public NodeStyleAction fork = null;
	public NodeStyleAction bubble = null;
	public CloudAction cloud = null;
	public freemind.modes.mindmapmode.actions.CloudColorAction cloudColor = null;
	public AddArrowLinkAction addArrowLinkAction = null;
	public RemoveArrowLinkAction removeArrowLinkAction = null;
	public ColorArrowLinkAction colorArrowLinkAction = null;
	public ChangeArrowsInArrowLinkAction changeArrowsInArrowLinkAction = null;
	public NodeBackgroundColorAction nodeBackgroundColor = null;
	public RemoveNodeBackgroundColorAction removeNodeBackgroundColor = null;

	public IconAction unknownIconAction = null;
	public RemoveIconAction removeLastIconAction = null;
	public RemoveAllIconsAction removeAllIconsAction = null;
	public SetLinkByTextFieldAction setLinkByTextField = null;
	public AddLocalLinkAction addLocalLinkAction = null;
	public GotoLinkNodeAction gotoLinkNodeAction = null;
	public JoinNodesAction joinNodes = null;
	public MoveNodeAction moveNodeAction = null;
	public ImportExplorerFavoritesAction importExplorerFavorites = null;
	public ImportFolderStructureAction importFolderStructure = null;

	public FindAction find = null;
	public FindNextAction findNext = null;
	public NodeHookAction nodeHookAction = null;
	public RevertAction revertAction = null;
	public SelectBranchAction selectBranchAction = null;
	public SelectAllAction selectAllAction = null;

	// Extension Actions
	public Vector<IconAction> iconActions = new Vector<>(); // fc

	FileFilter filefilter = new MindMapFilter();

	private MenuStructure mMenuStructure;
	private List<HookRegistration> mRegistrations;
	private List<Pattern> mPatternsList = new Vector<Pattern>();
	private long mGetEventIfChangedAfterThisTimeInMillies = 0;

	public MindMapController(Mode mode) {
		super(mode);
		// create action factory:
		actionFactory = new ActionRegistry();
		// create node information timer and actions. They don't fire, until
		// called to do so.
		mNodeInformationTimerAction = new NodeInformationTimerAction();
		mNodeInformationTimer = new Timer(100, mNodeInformationTimerAction);
		mNodeInformationTimer.setRepeats(false);

		init();
	}

	protected void init() {
		logger.info("createXmlActions");
		mActorFactory = new XmlActorFactory(this);
		logger.info("createIconActions");
		// create standard actions:
		createStandardActions();
		// icon actions:
		createIconActions();
		logger.info("createNodeHookActions");
		// node hook actions:
		createNodeHookActions();

		logger.info("mindmap_menus");
		// load menus:
		try {
			InputStream in;
			in = this.getFrame().getResource("mindmap_menus.xml").openStream();
			mMenuStructure = updateMenusFromXml(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			freemind.main.Resources.getInstance().logException(e);
		}

		logger.info("MindMapPopupMenu");
		popupmenu = new MindMapPopupMenu(this);
		logger.info("MindMapToolBar");
		toolbar = new MindMapToolBar(this);

		mRegistrations = new Vector<>();

		propertyAction = getController().propertyAction;
	}

	private void createStandardActions() {
		// prepare undo:
		undo = new UndoAction(this);
		redo = new RedoAction(this);
		// register default action handler:
		// the executor must be the first here, because it is executed last
		// then.
		getActionRegistry().registerHandler(
				new DefaultActionHandler(getActionRegistry()));
		getActionRegistry().registerUndoHandler(
				new UndoActionHandler(this, undo, redo));
		// debug:
		// getActionFactory().registerHandler(
		// new freemind.modes.mindmapmode.actions.xml.PrintActionHandler(
		// this));

		cut = new CutAction(this);
		paste = new PasteAction(this);
		pasteAsPlainText = new PasteAsPlainTextAction(this);
		copy = new CopyAction(this);
		copySingle = new CopySingleAction(this);
		bold = new BoldAction(this);
		strikethrough = new StrikethroughAction(this);
		italic = new ItalicAction(this);
		underlined = new UnderlinedAction(this);
		fontSize = new FontSizeAction(this);
		fontFamily = new FontFamilyAction(this);
		edit = new EditAction(this);
		useRichFormatting = new UseRichFormattingAction(this);
		usePlainText = new UsePlainTextAction(this);
		newChild = new NewChildAction(this);
		deleteChild = new DeleteChildAction(this);
		toggleFolded = new ToggleFoldedAction(this);
		toggleChildrenFolded = new ToggleChildrenFoldedAction(this);
		nodeUp = new NodeUpAction(this);
		nodeDown = new NodeDownAction(this);
		edgeColor = new EdgeColorAction(this);
		nodeColor = new NodeColorAction(this);
		nodeColorBlend = new NodeColorBlendAction(this);
		fork = new NodeStyleAction(this, MindMapNode.STYLE_FORK);
		bubble = new NodeStyleAction(this, MindMapNode.STYLE_BUBBLE);
		// this is an unknown icon and thus corrected by mindicon:
		removeLastIconAction = new RemoveIconAction(this);
		// this action handles the xml stuff: (undo etc.)
		unknownIconAction = new IconAction(this,
				MindIcon.factory((String) MindIcon.getAllIconNames().get(0)),
				removeLastIconAction);
		removeLastIconAction.setIconAction(unknownIconAction);
		removeAllIconsAction = new RemoveAllIconsAction(this, unknownIconAction);
		// load pattern actions:
		loadPatternActions();
		EdgeWidth_WIDTH_PARENT = new EdgeWidthAction(this,
				EdgeAdapter.WIDTH_PARENT);
		EdgeWidth_WIDTH_THIN = new EdgeWidthAction(this, EdgeAdapter.WIDTH_THIN);
		EdgeWidth_1 = new EdgeWidthAction(this, 1);
		EdgeWidth_2 = new EdgeWidthAction(this, 2);
		EdgeWidth_4 = new EdgeWidthAction(this, 4);
		EdgeWidth_8 = new EdgeWidthAction(this, 8);
		edgeWidths = new EdgeWidthAction[] { EdgeWidth_WIDTH_PARENT,
				EdgeWidth_WIDTH_THIN, EdgeWidth_1, EdgeWidth_2, EdgeWidth_4,
				EdgeWidth_8 };
		EdgeStyle_linear = new EdgeStyleAction(this,
				EdgeAdapter.EDGESTYLE_LINEAR);
		EdgeStyle_bezier = new EdgeStyleAction(this,
				EdgeAdapter.EDGESTYLE_BEZIER);
		EdgeStyle_sharp_linear = new EdgeStyleAction(this,
				EdgeAdapter.EDGESTYLE_SHARP_LINEAR);
		EdgeStyle_sharp_bezier = new EdgeStyleAction(this,
				EdgeAdapter.EDGESTYLE_SHARP_BEZIER);
		edgeStyles = new EdgeStyleAction[] { EdgeStyle_linear,
				EdgeStyle_bezier, EdgeStyle_sharp_linear,
				EdgeStyle_sharp_bezier };
		cloud = new CloudAction(this);
		cloudColor = new freemind.modes.mindmapmode.actions.CloudColorAction(
				this);
		addArrowLinkAction = new AddArrowLinkAction(this);
		removeArrowLinkAction = new RemoveArrowLinkAction(this, null);
		colorArrowLinkAction = new ColorArrowLinkAction(this, null);
		changeArrowsInArrowLinkAction = new ChangeArrowsInArrowLinkAction(this,
				"none", null, null, true, true);
		nodeBackgroundColor = new NodeBackgroundColorAction(this);
		removeNodeBackgroundColor = new RemoveNodeBackgroundColorAction(this);
		setLinkByTextField = new SetLinkByTextFieldAction(this);
		addLocalLinkAction = new AddLocalLinkAction(this);
		gotoLinkNodeAction = new GotoLinkNodeAction(this, null);
		moveNodeAction = new MoveNodeAction(this);
		joinNodes = new JoinNodesAction(this);
		importExplorerFavorites = new ImportExplorerFavoritesAction(this);
		importFolderStructure = new ImportFolderStructureAction(this);
		find = new FindAction(this);
		findNext = new FindNextAction(this, find);
		nodeHookAction = new NodeHookAction("no_title", this);
		revertAction = new RevertAction(this);
		selectBranchAction = new SelectBranchAction(this);
		selectAllAction = new SelectAllAction(this);
	}

	/**
	 * Tries to load the user patterns and proposes an update to the new format,
	 * if they are old fashioned (this is determined by having an exception
	 * while reading the pattern file).
	 */
	private void loadPatternActions() {
		try {
			loadPatterns(getPatternReader());
		} catch (Exception ex) {
			System.err.println("Patterns not loaded:" + ex);
			// repair old patterns:
			String repairTitle = "Repair patterns";
			File patternsFile = getFrame().getPatternsFile();
			int result = JOptionPane
					.showConfirmDialog(
							null,
							"<html>The pattern file format has changed, <br>"
									+ "and it seems, that your pattern file<br>"
									+ "'"
									+ patternsFile.getAbsolutePath()
									+ "'<br> is formatted in the old way. <br>"
									+ "Should I try to repair the pattern file <br>"
									+ "(otherwise, you should update it by hand or delete it)?",
							repairTitle, JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				// try xslt script:
				boolean success = false;
				try {
					loadPatterns(Tools.getUpdateReader(
							Tools.getReaderFromFile(patternsFile),
							"patterns_updater.xslt"));
					// save patterns directly:
					StylePatternFactory.savePatterns(new FileWriter(
							patternsFile), mPatternsList);
					success = true;
				} catch (Exception e) {
					freemind.main.Resources.getInstance().logException(e);
				}
				if (success) {
					JOptionPane.showMessageDialog(null,
							"Successfully repaired the pattern file.",
							repairTitle, JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null,
							"An error occured repairing the pattern file.",
							repairTitle, JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Reader getPatternReader() throws FileNotFoundException, IOException {
		Reader reader = null;
		File patternsFile = getFrame().getPatternsFile();
		if (patternsFile != null && patternsFile.exists()) {
			reader = new FileReader(patternsFile);
		} else {
			System.out.println("User patterns file " + patternsFile
					+ " not found.");
			reader = new InputStreamReader(getResource("patterns.xml")
					.openStream());
		}
		return reader;
	}

	/**
	 * @return the patternsList
	 */
	public List<Pattern> getPatternsList() {
		return mPatternsList;
	}
	
	public boolean isUndoAction() {
		return undo.isUndoAction() || redo.isUndoAction();
	}

	protected void loadInternally(URL url, MapAdapter model) throws URISyntaxException, XMLParseException, IOException {
		logger.info("Loading file: " + url.toString());
		File file = Tools.urlToFile(url);
		if (!file.exists()) {
			throw new FileNotFoundException(Tools.expandPlaceholders(
					getText("file_not_found"), file.getPath()));
		}
		if (!file.canWrite()) {
			model.setReadOnly(true);
		} else {
			// try to lock the map
			try {
				String lockingUser = model.tryToLock(file);
				if (lockingUser != null) {
					getFrame().getController().informationMessage(
							Tools.expandPlaceholders(
									getText("map_locked_by_open"),
									file.getName(), lockingUser));
					model.setReadOnly(true);
				} else {
					model.setReadOnly(false);
				}
			} catch (Exception e) { 
				// Thrown by tryToLock
				freemind.main.Resources.getInstance().logException(e);
				getFrame().getController().informationMessage(
						Tools.expandPlaceholders(
								getText("locking_failed_by_open"),
								file.getName()));
				model.setReadOnly(true);
			}
		}

		synchronized (model) {
			MindMapNode root = loadTree(file);
			if (root != null) {
				model.setRoot(root);
			}
			model.setFile(file);
			model.setFileTime();
		}
	}
	
	MindMapNode loadTree(final File pFile) throws XMLParseException,
			IOException {
		return loadTree(new Tools.FileReaderCreator(pFile));
	}

	public MindMapNode loadTree(Tools.ReaderCreator pReaderCreator)
			throws XMLParseException, IOException {
		return getMap().loadTree(pReaderCreator, new AskUserBeforeUpdateCallback() {
			
			@Override
			public boolean askUserForUpdate() {
				int showResult = new OptionalDontShowMeAgainDialog(
						getFrame().getJFrame(),
						getSelectedView(),
						"really_convert_to_current_version2",
						"confirmation",
						MindMapController.this,
						new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
								getController(),
								FreeMind.RESOURCES_CONVERT_TO_CURRENT_VERSION),
						OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED)
						.show().getResult();
				return (showResult == JOptionPane.OK_OPTION);
			}
		});
	}

	/**
	 * Creates the patterns actions (saved in array patterns), and the pure
	 * patterns list (saved in mPatternsList).
	 * 
	 * @throws Exception
	 */
	public void loadPatterns(Reader reader) throws Exception {
		createPatterns(StylePatternFactory.loadPatterns(reader));
	}

	private void createPatterns(List<Pattern> patternsList) throws Exception {
		mPatternsList = patternsList;
		patterns = new ApplyPatternAction[patternsList.size()];
		for (int i = 0; i < patterns.length; i++) {
			Pattern actualPattern = (Pattern) patternsList.get(i);
			patterns[i] = new ApplyPatternAction(this, actualPattern);

			// search icons for patterns:
			PatternIcon patternIcon = actualPattern.getPatternIcon();
			if (patternIcon != null && patternIcon.getValue() != null) {
				patterns[i].putValue(Action.SMALL_ICON,
						MindIcon.factory(patternIcon.getValue()).getIcon());
			}
		}
	}

	/**
	 * This method is called after and before a change of the map module. Use it
	 * to perform the actions that cannot be performed at creation time.
	 * 
	 */
	public void startupController() {
		super.startupController();
		getToolBar().startup();
		HookFactory hookFactory = getHookFactory();
		List<RegistrationContainer> pluginRegistrations = hookFactory.getRegistrations();
		logger.fine("mScheduledActions are executed: " + pluginRegistrations.size());
		for (RegistrationContainer container : pluginRegistrations) {
			// call constructor:
			try {
				Class registrationClass = container.hookRegistrationClass;
				Constructor hookConstructor = registrationClass.getConstructor(new Class[] { ModeController.class, MindMap.class });
				HookRegistration registrationInstance = (HookRegistration) hookConstructor
						.newInstance(new Object[] { this, getMap() });
				// register the instance to enable basePlugins.
				hookFactory.registerRegistrationContainer(container,
						registrationInstance);
				registrationInstance.register();
				mRegistrations.add(registrationInstance);
			} catch (Exception e) {
				freemind.main.Resources.getInstance().logException(e);
			}
		}
		invokeHooksRecursively((NodeAdapter) getRootNode(), getMap());

		// register mouse motion handler:
		getMapMouseMotionListener().register(
				new MindMapMouseMotionManager(this));
		getNodeDropListener().register(
				new MindMapNodeDropListener(this));
		getNodeKeyListener().register(
				new CommonNodeKeyListener(this, new EditHandler() {

					public void edit(KeyEvent e, boolean addNew,
							boolean editLong) {
						MindMapController.this.edit(e, addNew, editLong);

					}
				}));
		getNodeMotionListener().register(
				new MindMapNodeMotionListener(this));
		getNodeMouseMotionListener().register(
				new CommonNodeMouseMotionListener(this));
		getMap().registerMapSourceChangedObserver(this,
				mGetEventIfChangedAfterThisTimeInMillies);
	}

	public void shutdownController() {
		super.shutdownController();
		for (HookRegistration registrationInstance : mRegistrations) {
			registrationInstance.deRegister();
		}
		getHookFactory().deregisterAllRegistrationContainer();
		mRegistrations.clear();
		// deregister motion handler
		getMapMouseMotionListener().deregister();
		getNodeDropListener().deregister();
		getNodeKeyListener().deregister();
		getNodeMotionListener().deregister();
		getNodeMouseMotionListener().deregister();
		mGetEventIfChangedAfterThisTimeInMillies = getMap()
				.deregisterMapSourceChangedObserver(this);
		getToolBar().shutdown();

	}

	public MapAdapter newModel(ModeController modeController) {
		MindMapMapModel model = new MindMapMapModel(modeController);
		modeController.setModel(model);
		return model;
	}

	private void createIconActions() {
		Vector<String> iconNames = MindIcon.getAllIconNames();
		File iconDir = new File(Resources.getInstance().getFreemindDirectory(), "icons");
		if (iconDir.exists()) {
			String[] userIconArray = iconDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.matches(".*\\.png");
				}
			});
			if (userIconArray != null)
				for (int i = 0; i < userIconArray.length; ++i) {
					String iconName = userIconArray[i];
					iconName = iconName.substring(0, iconName.length() - 4);
					if (iconName.equals("")) {
						continue;
					}
					iconNames.add(iconName);
				}
		}
		for (int i = 0; i < iconNames.size(); ++i) {
			String iconName = ((String) iconNames.get(i));
			MindIcon myIcon = MindIcon.factory(iconName);
			IconAction myAction = new IconAction(this, myIcon,
					removeLastIconAction);
			iconActions.add(myAction);
		}
	}

	/**
	 *
	 */
	protected void createNodeHookActions() {
		if (hookActions == null) {
			hookActions = new Vector<>();
			// HOOK TEST
			MindMapHookFactory factory = (MindMapHookFactory) getHookFactory();
			List<String> nodeHookNames = factory.getPossibleNodeHooks();
			for (String hookName : nodeHookNames) {
				// create hook action.
				NodeHookAction action = new NodeHookAction(hookName, this);
				hookActions.add(action);
			}
			List<String> modeControllerHookNames = factory.getPossibleModeControllerHooks();
			for (String hookName : modeControllerHookNames) {
				MindMapControllerHookAction action = new MindMapControllerHookAction(hookName, this);
				hookActions.add(action);
			}
			// HOOK TEST END
		}
	}

	public FileFilter getFileFilter() {
		return filefilter;
	}

	public void nodeChanged(MindMapNode n) {
		super.nodeChanged(n);
		final MapModule mapModule = getController().getMapModule();
		// only for the selected node (fc, 2.5.2004)
		if (mapModule != null
				&& n == mapModule.getModeController().getSelected()) {
			updateToolbar(n);
			updateNodeInformation();
		}
	}

	@Override
	public void nodeStyleChanged(MindMapNode node) {
		nodeChanged(node);
		final ListIterator childrenFolded = node.childrenFolded();
		while (childrenFolded.hasNext()) {
			MindMapNode child = (MindMapNode) childrenFolded.next();
			if (!(child.hasStyle() && child.getEdge().hasStyle())) {
				nodeStyleChanged(child);
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.ControllerAdapter#onFocusNode(freemind.view.mindmapview
	 * .NodeView)
	 */
	public void onFocusNode(NodeView pNode) {
		super.onFocusNode(pNode);
		MindMapNode model = pNode.getModel();
		updateToolbar(model);
		updateNodeInformation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.ControllerAdapter#onLostFocusNode(freemind.view.mindmapview
	 * .NodeView)
	 */
	public void onLostFocusNode(NodeView pNode) {
		super.onLostFocusNode(pNode);
		updateNodeInformation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.ControllerAdapter#changeSelection(freemind.view.mindmapview
	 * .NodeView, boolean)
	 */
	public void changeSelection(NodeView pNode, boolean pIsSelected) {
		super.changeSelection(pNode, pIsSelected);
		updateNodeInformation();
	}

	/**
	 * Updates the status line.
	 */
	private void updateNodeInformation() {
		mNodeInformationTimer.stop();
		if (mNodeInformationTimerAction.isRunning()) {
			mNodeInformationTimerAction.interrupt();
		}
		mNodeInformationTimer.start();
	}

	protected void updateToolbar(MindMapNode n) {
		toolbar.selectFontSize(n.getFontSize());
		toolbar.selectFontName(n.getFontFamilyName());
		toolbar.selectColor(n.getColor());
	}

	// fc, 14.12.2004: changes, such that different models can be used:
	private NewNodeCreator myNewNodeCreator = null;
	/**
	 * A general list of MindMapControllerPlugin s. Members need to be tested
	 * for the right class and casted to be applied.
	 */
	private HashSet<MindMapControllerPlugin> mPlugins = new HashSet<>();
	private Timer mNodeInformationTimer;
	private NodeInformationTimerAction mNodeInformationTimerAction;
	private XmlActorFactory mActorFactory;

	public interface NewNodeCreator {
		MindMapNode createNode(Object userObject, MindMap map);
	}

	public class DefaultMindMapNodeCreator implements NewNodeCreator {

		public MindMapNode createNode(Object userObject, MindMap map) {
			return new MindMapNodeModel(userObject, map);
		}

	}

	public void setNewNodeCreator(NewNodeCreator creator) {
		myNewNodeCreator = creator;
	}

	public MindMapNode newNode(Object userObject, MindMap map) {
		// singleton default:
		if (myNewNodeCreator == null) {
			myNewNodeCreator = new DefaultMindMapNodeCreator();
		}
		return myNewNodeCreator.createNode(userObject, map);
	}

	// fc, 14.12.2004: end "different models" change

	// get/set methods

	/**
	 */
	public void updateMenus(StructuredMenuHolder holder) {

		processMenuCategory(holder, mMenuStructure.getListChoiceList(), ""); /*
																			 * MenuBar
																			 * .
																			 * MENU_BAR_PREFIX
																			 */
		// add hook actions to this holder.
		// hooks, fc, 1.3.2004:
		MindMapHookFactory hookFactory = (MindMapHookFactory) getHookFactory();
		for (int i = 0; i < hookActions.size(); ++i) {
			AbstractAction hookAction = (AbstractAction) hookActions.get(i);
			String hookName = ((HookAction) hookAction).getHookName();
			hookFactory.decorateAction(hookName, hookAction);
			List<String> hookMenuPositions = hookFactory.getHookMenuPositions(hookName);
			for (String pos : hookMenuPositions) {
				holder.addMenuItem(hookFactory.getMenuItem(hookName, hookAction), pos);
			}
		}
		// update popup and toolbar:
		popupmenu.update(holder);
		toolbar.update(holder);

		// editMenu.add(getExtensionMenu());
		String formatMenuString = MenuBar.FORMAT_MENU;
		createPatternSubMenu(holder, formatMenuString);

		// editMenu.add(getIconMenu());
		addIconsToMenu(holder, MenuBar.INSERT_MENU + "icons");

	}

	public void addIconsToMenu(StructuredMenuHolder holder, String iconMenuString) {
		JMenu iconMenu = holder.addMenu(new JMenu(getText("icon_menu")), iconMenuString + "/.");
		holder.addAction(removeLastIconAction, iconMenuString
				+ "/removeLastIcon");
		holder.addAction(removeAllIconsAction, iconMenuString
				+ "/removeAllIcons");
		holder.addSeparator(iconMenuString);
		for (int i = 0; i < iconActions.size(); ++i) {
			holder.addAction((Action) iconActions.get(i), iconMenuString + "/" + i);
		}
	}

	/**
     */
	public void createPatternSubMenu(StructuredMenuHolder holder,
			String formatMenuString) {
		for (int i = 0; i < patterns.length; ++i) {
			JMenuItem item = holder.addAction(patterns[i], formatMenuString
					+ "patterns/patterns/" + i);
			item.setAccelerator(KeyStroke
					.getKeyStroke(getFrame().getAdjustableProperty(
							"keystroke_apply_pattern_" + (i + 1))));
		}
	}

	public MenuStructure updateMenusFromXml(InputStream in) {
		// get from resources:
		try {
			IUnmarshallingContext unmarshaller = XmlBindingTools.getInstance()
					.createUnmarshaller();
			MenuStructure menus = (MenuStructure) unmarshaller
					.unmarshalDocument(in, null);
			return menus;
		} catch (JiBXException e) {
			freemind.main.Resources.getInstance().logException(e);
			throw new IllegalArgumentException(
					"Menu structure could not be read.");
		}
	}

	/**
     */
	public void processMenuCategory(StructuredMenuHolder holder, List<Object> list, String category) {
		String categoryCopy = category;
		ButtonGroup buttonGroup = null;
		for (Object obj : list) {
			if (obj instanceof MenuCategoryBase) {
				MenuCategoryBase cat = (MenuCategoryBase) obj;
				String newCategory = categoryCopy + "/" + cat.getName();
				holder.addCategory(newCategory);
				if (cat instanceof MenuSubmenu) {
					MenuSubmenu submenu = (MenuSubmenu) cat;
					holder.addMenu(new JMenu(getText(submenu.getNameRef())),
							newCategory + "/.");
				}
				processMenuCategory(holder, cat.getListChoiceList(), newCategory);
			} else if (obj instanceof MenuActionBase) {
				MenuActionBase action = (MenuActionBase) obj;
				String field = action.getField();
				String name = action.getName();
				if (name == null) {
					name = field;
				}
				String keystroke = action.getKeyRef();
				try {
					Action theAction = (Action) Tools.getField(new Object[] {
							this, getController() }, field);
					String theCategory = categoryCopy + "/" + name;
					if (obj instanceof MenuCheckedAction) {
						addCheckBox(holder, theCategory, theAction, keystroke);
					} else if (obj instanceof MenuRadioAction) {
						final JRadioButtonMenuItem item = (JRadioButtonMenuItem) addRadioItem(
								holder, theCategory, theAction, keystroke,
								((MenuRadioAction) obj).getSelected());
						if (buttonGroup == null)
							buttonGroup = new ButtonGroup();
						buttonGroup.add(item);

					} else {
						add(holder, theCategory, theAction, keystroke);
					}
				} catch (Exception e1) {
					freemind.main.Resources.getInstance().logException(e1);
				}
			} else if (obj instanceof MenuSeparator) {
				holder.addSeparator(categoryCopy);
			} /* else exception */
		}
	}

	public JPopupMenu getPopupMenu() {
		return popupmenu;
	}

	/**
	 * Link implementation: If this is a link, we want to make a popup with at
	 * least removelink available.
	 */
	public JPopupMenu getPopupForModel(java.lang.Object obj) {
		if (obj instanceof MindMapArrowLinkModel) {
			// yes, this is a link.
			MindMapArrowLinkModel link = (MindMapArrowLinkModel) obj;
			JPopupMenu arrowLinkPopup = new JPopupMenu();
			// block the screen while showing popup.
			arrowLinkPopup.addPopupMenuListener(this.popupListenerSingleton);
			removeArrowLinkAction.setArrowLink(link);
			arrowLinkPopup.add(new RemoveArrowLinkAction(this, link));
			arrowLinkPopup.add(new ColorArrowLinkAction(this, link));
			arrowLinkPopup.addSeparator();
			/* The arrow state as radio buttons: */
			JRadioButtonMenuItem itemnn = new JRadioButtonMenuItem(
					new ChangeArrowsInArrowLinkAction(this, "none",
							"images/arrow-mode-none.png", link, false, false));
			JRadioButtonMenuItem itemnt = new JRadioButtonMenuItem(
					new ChangeArrowsInArrowLinkAction(this, "forward",
							"images/arrow-mode-forward.png", link, false, true));
			JRadioButtonMenuItem itemtn = new JRadioButtonMenuItem(
					new ChangeArrowsInArrowLinkAction(this, "backward",
							"images/arrow-mode-backward.png", link, true, false));
			JRadioButtonMenuItem itemtt = new JRadioButtonMenuItem(
					new ChangeArrowsInArrowLinkAction(this, "both",
							"images/arrow-mode-both.png", link, true, true));
			itemnn.setText(null);
			itemnt.setText(null);
			itemtn.setText(null);
			itemtt.setText(null);
			arrowLinkPopup.add(itemnn);
			arrowLinkPopup.add(itemnt);
			arrowLinkPopup.add(itemtn);
			arrowLinkPopup.add(itemtt);
			// select the right one:
			boolean a = !link.getStartArrow().equals("None");
			boolean b = !link.getEndArrow().equals("None");
			itemtt.setSelected(a && b);
			itemnt.setSelected(!a && b);
			itemtn.setSelected(a && !b);
			itemnn.setSelected(!a && !b);

			arrowLinkPopup.addSeparator();

			arrowLinkPopup.add(new GotoLinkNodeAction(this, link.getSource()));
			arrowLinkPopup.add(new GotoLinkNodeAction(this, link.getTarget()));

			arrowLinkPopup.addSeparator();
			// add all links from target and from source:
			HashSet<MindMapNode> NodeAlreadyVisited = new HashSet<>();
			NodeAlreadyVisited.add(link.getSource());
			NodeAlreadyVisited.add(link.getTarget());
			Vector<MindMapLink> links = getMindMapMapModel().getLinkRegistry().getAllLinks(link.getSource());
			links.addAll(getMindMapMapModel().getLinkRegistry().getAllLinks(
					link.getTarget()));
			for (int i = 0; i < links.size(); ++i) {
				MindMapArrowLinkModel foreign_link = (MindMapArrowLinkModel) links
						.get(i);
				if (NodeAlreadyVisited.add(foreign_link.getTarget())) {
					arrowLinkPopup.add(new GotoLinkNodeAction(this,
							foreign_link.getTarget()));
				}
				if (NodeAlreadyVisited.add(foreign_link.getSource())) {
					arrowLinkPopup.add(new GotoLinkNodeAction(this,
							foreign_link.getSource()));
				}
			}
			return arrowLinkPopup;
		}
		return null;
	}

	// convenience methods
	public MindMapMapModel getMindMapMapModel() {
		return (MindMapMapModel) getMap();
	}

	public JToolBar getModeToolBar() {
		return getToolBar();
	}

	MindMapToolBar getToolBar() {
		return toolbar;
	}

	public Component getLeftToolBar() {
		return toolbar.getLeftToolBar();
	}

	/**
	 * Enabled/Disabled all actions that are dependent on whether there is a map
	 * open or not.
	 */
	protected void setAllActions(boolean enabled) {
		logger.fine("setAllActions:" + enabled);
		super.setAllActions(enabled);
		// own actions
		increaseNodeFont.setEnabled(enabled);
		decreaseNodeFont.setEnabled(enabled);
		exportBranch.setEnabled(enabled);
		exportBranchToHTML.setEnabled(enabled);
		editLong.setEnabled(enabled);
		newSibling.setEnabled(enabled);
		newPreviousSibling.setEnabled(enabled);
		setLinkByFileChooser.setEnabled(enabled);
		setImageByFileChooser.setEnabled(enabled);
		followLink.setEnabled(enabled);
		for (int i = 0; i < iconActions.size(); ++i) {
			((Action) iconActions.get(i)).setEnabled(enabled);
		}
		save.setEnabled(enabled);
		saveAs.setEnabled(enabled);
		getToolBar().setAllActions(enabled);
		exportBranch.setEnabled(enabled);
		exportToHTML.setEnabled(enabled);
		importBranch.setEnabled(enabled);
		importLinkedBranch.setEnabled(enabled);
		importLinkedBranchWithoutRoot.setEnabled(enabled);
		// hooks:
		for (int i = 0; i < hookActions.size(); ++i) {
			((Action) hookActions.get(i)).setEnabled(enabled);
		}
		cut.setEnabled(enabled);
		copy.setEnabled(enabled);
		copySingle.setEnabled(enabled);
		paste.setEnabled(enabled);
		pasteAsPlainText.setEnabled(enabled);
		undo.setEnabled(enabled);
		redo.setEnabled(enabled);
		edit.setEnabled(enabled);
		newChild.setEnabled(enabled);
		toggleFolded.setEnabled(enabled);
		toggleChildrenFolded.setEnabled(enabled);
		setLinkByTextField.setEnabled(enabled);
		italic.setEnabled(enabled);
		bold.setEnabled(enabled);
		strikethrough.setEnabled(enabled);
		find.setEnabled(enabled);
		findNext.setEnabled(enabled);
		addArrowLinkAction.setEnabled(enabled);
		addLocalLinkAction.setEnabled(enabled);
		nodeColorBlend.setEnabled(enabled);
		nodeUp.setEnabled(enabled);
		nodeBackgroundColor.setEnabled(enabled);
		nodeDown.setEnabled(enabled);
		importExplorerFavorites.setEnabled(enabled);
		importFolderStructure.setEnabled(enabled);
		joinNodes.setEnabled(enabled);
		deleteChild.setEnabled(enabled);
		cloud.setEnabled(enabled);
		cloudColor.setEnabled(enabled);
		// normalFont.setEnabled(enabled);
		nodeColor.setEnabled(enabled);
		edgeColor.setEnabled(enabled);
		removeLastIconAction.setEnabled(enabled);
		removeAllIconsAction.setEnabled(enabled);
		selectAllAction.setEnabled(enabled);
		selectBranchAction.setEnabled(enabled);
		removeNodeBackgroundColor.setEnabled(enabled);
		moveNodeAction.setEnabled(enabled);
		revertAction.setEnabled(enabled);
		for (int i = 0; i < edgeWidths.length; ++i) {
			edgeWidths[i].setEnabled(enabled);
		}
		fork.setEnabled(enabled);
		bubble.setEnabled(enabled);
		for (int i = 0; i < edgeStyles.length; ++i) {
			edgeStyles[i].setEnabled(enabled);
		}
		for (int i = 0; i < patterns.length; ++i) {
			patterns[i].setEnabled(enabled);
		}
		useRichFormatting.setEnabled(enabled);
		usePlainText.setEnabled(enabled);
	}

	//
	// Actions
	// _______________________________________________________________________________

	// This may later be moved to ControllerAdapter. So far there is no reason
	// for it.
	protected class ExportToHTMLAction extends MindmapAction {

		public ExportToHTMLAction(MindMapController controller) {
			super("export_to_html", controller);
		}

		public void actionPerformed(ActionEvent e) {
			// from
			// https://sourceforge.net/tracker2/?func=detail&atid=307118&aid=1789765&group_id=7118
			if (getMap().getFile() == null) {
				JOptionPane.showMessageDialog(getFrame().getContentPane(),
						getText("map_not_saved"), "FreeMind",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				File file = new File(getMindMapMapModel().getFile() + ".html");
				saveHTML((MindMapNodeModel) getMindMapMapModel().getRoot(),
						file);
				loadURL(file.toString());
			} catch (IOException ex) {
				freemind.main.Resources.getInstance().logException(ex);
			}
		}
	}

	protected class ExportBranchToHTMLAction extends MindmapAction {

		public ExportBranchToHTMLAction(MindMapController controller) {
			super("export_branch_to_html", controller);
		}

		public void actionPerformed(ActionEvent e) {
			File mindmapFile = getMap().getFile();
			if (mindmapFile == null) {
				JOptionPane.showMessageDialog(getFrame().getContentPane(),
						getText("map_not_saved"), "FreeMind",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				File file = File.createTempFile(
						mindmapFile.getName().replace(
								FreeMindCommon.FREEMIND_FILE_EXTENSION, "_"),
						".html", mindmapFile.getParentFile());
				saveHTML((MindMapNodeModel) getSelected(), file);
				loadURL(file.toString());
			} catch (IOException ex) {
			}
		}
	}

	private class ImportBranchAction extends MindmapAction {
		ImportBranchAction() {
			super("import_branch", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			MindMapNodeModel parent = (MindMapNodeModel) getSelected();
			if (parent == null) {
				return;
			}
			FreeMindFileDialog chooser = getFileChooser();
			int returnVal = chooser.showOpenDialog(getFrame().getContentPane());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					MindMapNode node = loadTree(
							chooser.getSelectedFile());
					paste(node, parent);
					invokeHooksRecursively(node, getMindMapMapModel());
				} catch (Exception ex) {
					handleLoadingException(ex);
				}
			}
		}
	}

	private class ImportLinkedBranchAction extends MindmapAction {
		ImportLinkedBranchAction() {
			super("import_linked_branch", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			MindMapNodeModel selected = (MindMapNodeModel) getSelected();
			if (selected == null || selected.getLink() == null) {
				JOptionPane.showMessageDialog(getView(),
						getText("import_linked_branch_no_link"));
				return;
			}
			URL absolute = null;
			try {
				String relative = selected.getLink();
				absolute = new URL(Tools.fileToUrl(getMap().getFile()),
						relative);
			} catch (MalformedURLException ex) {
				JOptionPane.showMessageDialog(getView(),
						"Couldn't create valid URL for:" + getMap().getFile());
				freemind.main.Resources.getInstance().logException(ex);
				return;
			}
			try {
				MindMapNode node = loadTree(
						Tools.urlToFile(absolute));
				paste(node, selected);
				invokeHooksRecursively(node, getMindMapMapModel());
			} catch (Exception ex) {
				handleLoadingException(ex);
			}
		}
	}

	/**
	 * This is exactly the opposite of exportBranch.
	 */
	private class ImportLinkedBranchWithoutRootAction extends MindmapAction {
		ImportLinkedBranchWithoutRootAction() {
			super("import_linked_branch_without_root", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			MindMapNodeModel selected = (MindMapNodeModel) getSelected();
			if (selected == null || selected.getLink() == null) {
				JOptionPane.showMessageDialog(getView(),
						getText("import_linked_branch_no_link"));
				return;
			}
			URL absolute = null;
			try {
				String relative = selected.getLink();
				absolute = new URL(Tools.fileToUrl(getMap().getFile()),
						relative);
			} catch (MalformedURLException ex) {
				JOptionPane.showMessageDialog(getView(),
						"Couldn't create valid URL.");
				return;
			}
			try {
				MindMapNode node = loadTree(
						Tools.urlToFile(absolute));
				for (ListIterator i = node.childrenUnfolded(); i.hasNext();) {
					MindMapNodeModel importNode = (MindMapNodeModel) i.next();
					paste(importNode, selected);
					invokeHooksRecursively(importNode, getMindMapMapModel());
				}
			}
			// getModel().setLink(parent, null); }
			catch (Exception ex) {
				handleLoadingException(ex);
			}
		}
	}

	/*
	 * MindMapController.java private class NodeViewStyleAction extends
	 * AbstractAction { NodeViewStyleAction(final String style) {
	 * super(getText(style)); m_style = style; } public void
	 * actionPerformed(ActionEvent e) { for(ListIterator it =
	 * getSelecteds().listIterator();it.hasNext();) { MindMapNodeModel selected
	 * = (MindMapNodeModel)it.next(); getModel().setNodeStyle(selected,
	 * m_style); }} private String m_style;}
	 * 
	 * private class EdgeStyleAction extends AbstractAction { String style;
	 * EdgeStyleAction(String style) { super(getText(style)); this.style =
	 * style; } public void actionPerformed(ActionEvent e) { for(ListIterator it
	 * = getSelecteds().listIterator();it.hasNext();) { MindMapNodeModel
	 * selected = (MindMapNodeModel)it.next(); getModel().setEdgeStyle(selected,
	 * style); }}}
	 * 
	 * private class ApplyPatternAction extends AbstractAction { StylePattern
	 * pattern; ApplyPatternAction(StylePattern pattern) {
	 * super(pattern.getName()); this.pattern=pattern; } public void
	 * actionPerformed(ActionEvent e) { for(ListIterator it =
	 * getSelecteds().listIterator();it.hasNext();) { MindMapNodeModel selected
	 * = (MindMapNodeModel)it.next();
	 * ((MindMapMapModel)getModel()).applyPattern(selected, pattern); }}}
	 * 
	 * 
	 * 
	 * 
	 * // Nonaction classes //
	 * ________________________________________________________________________
	 */
	private class MindMapFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String extension = Tools.getExtension(f.getName());
			if (extension != null) {
				if (extension
						.equals(freemind.main.FreeMindCommon.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}

		public String getDescription() {
			return getText("mindmaps_desc");
		}
	}

	public void setBold(MindMapNode node, boolean bolded) {
		mActorFactory.getBoldActor().setBold(node, bolded);
	}

	public void setStrikethrough(MindMapNode node, boolean strikethrough) {
		mActorFactory.getStrikethroughActor().setStrikethrough(node, strikethrough);
	}
	
	public void setItalic(MindMapNode node, boolean isItalic) {
		mActorFactory.getItalicActor().setItalic(node, isItalic);
	}

	public void setCloud(MindMapNode node, boolean enable) {
		mActorFactory.getCloudActor().setCloud(node, enable);
	}

	public void setCloudColor(MindMapNode node, Color color) {
		mActorFactory.getCloudColorActor().setCloudColor(node, color);
	}

	// Node editing
	public void setFontSize(MindMapNode node, String fontSizeValue) {
		getActorFactory().getFontSizeActor().setFontSize(node, fontSizeValue);
	}

	/**
     *
     */

	public void increaseFontSize(MindMapNode node, int increment) {
		int newSize = Integer.valueOf(node.getFontSize()).intValue()
				+ increment;

		if (newSize > 0) {
			setFontSize(node, Integer.toString(newSize));
		}
	}

	public void setFontFamily(MindMapNode node, String fontFamilyValue) {
		getActorFactory().getFontFamilyActor().setFontFamily(node, fontFamilyValue);
	}

	public void setNodeColor(MindMapNode node, Color color) {
		getActorFactory().getNodeColorActor().setNodeColor(node, color);
	}

	public void setNodeBackgroundColor(MindMapNode node, Color color) {
		getActorFactory().getNodeBackgroundColorActor().setNodeBackgroundColor(node, color);
	}

	public void blendNodeColor(MindMapNode node) {
		Color mapColor = getView().getBackground();
		Color nodeColor = node.getColor();
		if (nodeColor == null) {
			nodeColor = MapView.standardNodeTextColor;
		}
		setNodeColor(node,
				new Color((3 * mapColor.getRed() + nodeColor.getRed()) / 4,
						(3 * mapColor.getGreen() + nodeColor.getGreen()) / 4,
						(3 * mapColor.getBlue() + nodeColor.getBlue()) / 4));
	}

	public void setEdgeColor(MindMapNode node, Color color) {
		getActorFactory().getEdgeColorActor().setEdgeColor(node, color);
	}

	public void applyPattern(MindMapNode node, String patternName) {
		for (int i = 0; i < patterns.length; i++) {
			ApplyPatternAction patternAction = patterns[i];
			if (patternAction.getPattern().getName().equals(patternName)) {
				StylePatternFactory.applyPattern(node, patternAction.getPattern(), getPatternsList(), getPlugins(), this);
				break;
			}
		}
	}

	public void applyPattern(MindMapNode node, Pattern pattern) {
		StylePatternFactory.applyPattern(node, pattern, getPatternsList(), getPlugins(), this);
	}

	public void addIcon(MindMapNode node, MindIcon icon) {
		mActorFactory.getAddIconActor().addIcon(node, icon);
	}

	public void removeAllIcons(MindMapNode node) {
		mActorFactory.getRemoveAllIconsActor().removeAllIcons(node);
	}

	public int removeLastIcon(MindMapNode node) {
		return mActorFactory.getRemoveIconActor().removeLastIcon(node);
	}

	/**
     *
     */

	public void addLink(MindMapNode source, MindMapNode target) {
		getActorFactory().getAddArrowLinkActor().addLink(source, target);
	}

	public void removeReference(MindMapLink arrowLink) {
		getActorFactory().getRemoveArrowLinkActor().removeReference(arrowLink);
	}

	public void setArrowLinkColor(MindMapLink arrowLink, Color color) {
		getActorFactory().getColorArrowLinkActor().setArrowLinkColor(arrowLink, color);
	}

	/**
     *
     */

	public void changeArrowsOfArrowLink(MindMapArrowLink arrowLink,
			boolean hasStartArrow, boolean hasEndArrow) {
		getActorFactory().getChangeArrowsInArrowLinkActor().changeArrowsOfArrowLink(arrowLink,
				hasStartArrow, hasEndArrow);
	}

	public void setArrowLinkEndPoints(MindMapArrowLink link, Point startPoint,
			Point endPoint) {
		getActorFactory().getChangeArrowLinkEndPointsActor().setArrowLinkEndPoints(link, startPoint,
				endPoint);
	}

	public void setLink(MindMapNode node, String link) {
		getActorFactory().getSetLinkActor().setLink(node, link);
	}

	// edit begins with home/end or typing (PN 6.2)
	public void edit(KeyEvent e, boolean addNew, boolean editLong) {
		edit.edit(e, addNew, editLong);
	}

	public void setNodeText(MindMapNode selected, String newText) {
		getActorFactory().getEditActor().setNodeText(selected, newText);
	}

	/**
     *
     */

	public void setEdgeWidth(MindMapNode node, int width) {
		getActorFactory().getEdgeWidthActor().setEdgeWidth(node, width);
	}

	/**
     *
     */

	public void setEdgeStyle(MindMapNode node, String style) {
		getActorFactory().getEdgeStyleActor().setEdgeStyle(node, style);
	}

	/**
     *
     */

	public void setNodeStyle(MindMapNode node, String style) {
		getActorFactory().getNodeStyleActor().setStyle(node, style);
	}

	@Override
	public Transferable copy(MindMapNode node, boolean saveInvisible) {
		StringWriter stringWriter = new StringWriter();
		try {
			((MindMapNodeModel) node).save(stringWriter, getMap()
					.getLinkRegistry(), saveInvisible, true);
		} catch (IOException e) {
		}
		Vector<String> nodeList = Tools.getVectorWithSingleElement(getNodeID(node));
		return new MindMapNodesSelection(stringWriter.toString(), null, null,
				null, null, null, null, nodeList);
	}

	public Transferable cut() {
		return cut(getView().getSelectedNodesSortedByY());
	}

	public Transferable cut(List<MindMapNode> nodeList) {
		return getActorFactory().getCutActor().cut(nodeList);
	}

	public void paste(Transferable t, MindMapNode parent) {
		paste(t, /* target= */parent, /* asSibling= */false,
				parent.isNewChildLeft());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.mindmapmode.actions.MindMapActions#paste(java.awt.datatransfer
	 * .Transferable, freemind.modes.MindMapNode, boolean, boolean)
	 */
	public boolean paste(Transferable t, MindMapNode target, boolean asSibling,
			boolean isLeft) {
		if (!asSibling
				&& target.isFolded()
				&& Resources.getInstance().getBoolProperty(
						RESOURCE_UNFOLD_ON_PASTE)) {
			setFolded(target, false);
		}
		return mActorFactory.getPasteActor().paste(t, target, asSibling, isLeft);
	}

	public void paste(MindMapNode node, MindMapNode parent) {
		mActorFactory.getPasteActor().paste(node, parent);
	}

	public MindMapNode addNew(final MindMapNode target, final int newNodeMode,
			final KeyEvent e) {
		edit.stopEditing();
		return newChild.addNew(target, newNodeMode, e);
	}

	public MindMapNode addNewNode(MindMapNode parent, int index,
			boolean newNodeIsLeft) {
		return mActorFactory.getNewChildActor().addNewNode(parent, index, newNodeIsLeft);
	}

	public void deleteNode(MindMapNode selectedNode) {
		mActorFactory.getDeleteChildActor().deleteNode(selectedNode);
	}

	public void toggleFolded() {
		getActorFactory().getToggleFoldedActor().toggleFolded(getSelecteds().listIterator());
	}

	public void setFolded(MindMapNode node, boolean folded) {
		getActorFactory().getToggleFoldedActor().setFolded(node, folded);
	}

	public void moveNodes(MindMapNode selected, List<MindMapNode> selecteds, int direction) {
		getActorFactory().getNodeUpActor().moveNodes(selected, selecteds, direction);
	}

	public void joinNodes(MindMapNode selectedNode, List<MindMapNode> selectedNodes) {
		joinNodes.joinNodes(selectedNode, selectedNodes);
	}

	protected void setLinkByFileChooser() {
		String relative = getLinkByFileChooser(null);
		if (relative != null)
			setLink((NodeAdapter) getSelected(), relative);
	}

	protected void setImageByFileChooser() {
		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("png");
		filter.addExtension("gif");
		filter.setDescription("JPG, PNG and GIF Images");

		String relative = getLinkByFileChooser(filter);
		if (relative != null) {
			String strText = "<html><body><img src=\"" + relative
					+ "\"/></body></html>";
			setNodeText((MindMapNode) getSelected(), strText);
		}
	}

	protected String getLinkByFileChooser(FileFilter fileFilter) {
		String relative = null;
		File input;
		FreeMindFileDialog chooser = getFileChooser(fileFilter);
		if (getMap().getFile() == null) {
			JOptionPane.showMessageDialog(getFrame().getContentPane(),
					getText("not_saved_for_link_error"), "FreeMind",
					JOptionPane.WARNING_MESSAGE);
			return null;
			// In the previous version Freemind automatically displayed save
			// dialog. It happened very often, that user took this save
			// dialog to be an open link dialog; as a result, the new map
			// overwrote the linked map.

		}

		int returnVal = chooser.showOpenDialog(getFrame().getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			input = chooser.getSelectedFile();
			relative = Tools.fileToRelativeUrlString(input, getMap().getFile());
		}
		return relative;
	}

	public void loadURL(String relative) {
		if (getMap().getFile() == null) {
			getFrame().out("You must save the current map first!");
			boolean result = save();
			// canceled??
			if (!result) {
				return;
			}
		}
		super.loadURL(relative);
	}
	
	

	public void addHook(MindMapNode focussed, List<MindMapNode> selecteds, String hookName, Properties pHookProperties) {
		getActorFactory().getAddHookActor().addHook(focussed, selecteds, hookName, pHookProperties);
	}

	public void removeHook(MindMapNode focussed, List<MindMapNode> selecteds, String hookName) {
		getActorFactory().getAddHookActor().removeHook(focussed, selecteds, hookName);
	}

	protected class SetLinkByFileChooserAction extends MindmapAction {
		public SetLinkByFileChooserAction() {
			super("set_link_by_filechooser", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			setLinkByFileChooser();
		}
	}

	protected class SetImageByFileChooserAction extends MindmapAction {
		public SetImageByFileChooserAction() {
			super("set_image_by_filechooser", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			setImageByFileChooser();
			getController().obtainFocusForSelected();
		}
	}

	protected abstract class LinkActionBase extends MindmapAction {
		public LinkActionBase(String pText) {
			super(pText, MindMapController.this);
		}

		public boolean isEnabled(JMenuItem pItem, Action pAction) {
			if (!super.isEnabled(pItem, pAction)) {
				return false;
			}
			for (MindMapNode selNode : getSelecteds()) {
				if (selNode.getLink() != null)
					return true;
			}
			return false;
		}

	}

	protected class FollowLinkAction extends LinkActionBase {
		public FollowLinkAction() {
			super("follow_link");
		}

		public void actionPerformed(ActionEvent e) {
			for (MindMapNode selNode : getSelecteds()) {
				if (selNode.getLink() != null) {
					loadURL(selNode.getLink());
				}
			}
		}
	}

	protected class OpenLinkDirectoryAction extends LinkActionBase {
		public OpenLinkDirectoryAction() {
			super("open_link_directory");
		}

		public void actionPerformed(ActionEvent event) {
			String link = "";
			for (MindMapNode selNode : getSelecteds()) {
				link = selNode.getLink();
				if (link != null) {
					// as link is an URL, '/' is the only correct one.
					final int i = link.lastIndexOf('/');
					if (i >= 0) {
						link = link.substring(0, i + 1);
					}
					logger.info("Opening link for directory " + link);
					loadURL(link);
				}
			}
		}
	}

	public void moveNodePosition(MindMapNode node, int parentVGap, int hGap,
			int shiftY) {
		getActorFactory().getMoveNodeActor().moveNodeTo(node, parentVGap, hGap, shiftY);
	}

	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////

	public void plainClick(MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelecteds().size() != 1)
			return;
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			loadURL();
		} else {
			MindMapNode node = (component).getNodeView().getModel();
			if (!node.hasChildren()) {
				// then emulate the plain click.
				doubleClick(e);
				return;
			}
			toggleFolded();
		}
	}

	public HookFactory getHookFactory() {
		// lazy creation.
		if (nodeHookFactory == null) {
			nodeHookFactory = new MindMapHookFactory();
		}
		return nodeHookFactory;
	}

	public NodeHook createNodeHook(String hookName, MindMapNode node) {
		HookFactory hookFactory = getHookFactory();
		NodeHook hook = (NodeHook) hookFactory.createNodeHook(hookName);
		hook.setController(this);
		hook.setMap(getMap());
		if (hook instanceof PermanentNodeHook) {
			PermanentNodeHook permHook = (PermanentNodeHook) hook;
			if (hookFactory.getInstanciationMethod(hookName).isSingleton()) {
				// search for already instanciated hooks of this type:
				PermanentNodeHook otherHook = hookFactory.getHookInNode(node,
						hookName);
				if (otherHook != null) {
					return otherHook;
				}
			}
			node.addHook(permHook);
		}
		return hook;
	}

	public void invokeHook(ModeControllerHook hook) {
		try {
			hook.setController(this);
			// initialize:
			// the main invocation:
			hook.startupMapHook();
			// and good bye.
			hook.shutdownMapHook();
		} catch (Exception e) {
			freemind.main.Resources.getInstance().logException(e);
		}
	}

	public ActionRegistry getActionRegistry() {
		return actionFactory;
	}
	
	public XmlActorFactory getActorFactory() {
		return mActorFactory;
	}

	protected class EditLongAction extends MindmapAction {
		public EditLongAction() {
			super("edit_long_node", MindMapController.this);
		}

		public void actionPerformed(ActionEvent e) {
			edit(null, false, true);
		}
	}

	static public void saveHTML(MindMapNodeModel rootNodeOfBranch, File file)
			throws IOException {
		BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file)));
		MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(fileout);
		htmlWriter.saveHTML(rootNodeOfBranch);
	}

	static public void saveHTML(List<MindMapNodeModel> mindMapNodes, Writer fileout)
			throws IOException {
		MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(fileout);
		htmlWriter.saveHTML(mindMapNodes);
	}

	/**
     */
	public void splitNode(MindMapNode node, int caretPosition, String newText) {
		if (node.isRoot()) {
			return;
		}
		// If there are children, they go to the node below
		String futureText = newText != null ? newText : node.toString();

		String[] strings = getContent(futureText, caretPosition);
		if (strings == null) { // do nothing
			return;
		}
		String newUpperContent = strings[0];
		String newLowerContent = strings[1];
		setNodeText(node, newUpperContent);

		MindMapNode parent = node.getParentNode();
		MindMapNode lowerNode = addNewNode(parent,
				parent.getChildPosition(node) + 1, node.isLeft());
		lowerNode.setColor(node.getColor());
		lowerNode.setFont(node.getFont());
		setNodeText(lowerNode, newLowerContent);

	}

	private String[] getContent(String text, int pos) {
		if (pos <= 0) {
			return null;
		}
		String[] strings = new String[2];
		if (text.startsWith("<html>")) {
			HTMLEditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = new HTMLDocument();
			StringReader buf = new StringReader(text);
			try {
				kit.read(buf, doc, 0);
				final char[] firstText = doc.getText(0, pos).toCharArray();
				int firstStart = 0;
				int firstLen = pos;
				while ((firstStart < firstLen)
						&& (firstText[firstStart] <= ' ')) {
					firstStart++;
				}
				while ((firstStart < firstLen)
						&& (firstText[firstLen - 1] <= ' ')) {
					firstLen--;
				}
				int secondStart = 0;
				int secondLen = doc.getLength() - pos;
				final char[] secondText = doc.getText(pos, secondLen)
						.toCharArray();
				while ((secondStart < secondLen)
						&& (secondText[secondStart] <= ' ')) {
					secondStart++;
				}
				while ((secondStart < secondLen)
						&& (secondText[secondLen - 1] <= ' ')) {
					secondLen--;
				}
				if (firstStart == firstLen || secondStart == secondLen) {
					return null;
				}
				StringWriter out = new StringWriter();
				new FixedHTMLWriter(out, doc, firstStart, firstLen - firstStart)
						.write();
				strings[0] = out.toString();
				out = new StringWriter();
				new FixedHTMLWriter(out, doc, pos + secondStart, secondLen
						- secondStart).write();
				strings[1] = out.toString();
				return strings;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				freemind.main.Resources.getInstance().logException(e);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				freemind.main.Resources.getInstance().logException(e);
			}
		} else {
			if (pos >= text.length()) {
				return null;
			}
			strings[0] = text.substring(0, pos);
			strings[1] = text.substring(pos);
		}
		return strings;
	}

	protected void updateNode(MindMapNode node) {
		super.updateNode(node);
		recursiveCallUpdateHooks((MindMapNode) node, (MindMapNode) node /*
																		 * self
																		 * update
																		 */);
	}

	/**
     */
	private void recursiveCallUpdateHooks(MindMapNode node,
			MindMapNode changedNode) {
		// Tell any node hooks that the node is changed:
		if (node instanceof MindMapNode) {
			for (PermanentNodeHook hook : node.getActivatedHooks()) {
				if ((!isUndoAction()) || hook instanceof UndoEventReceiver) {
					if (node == changedNode)
						hook.onUpdateNodeHook();
					else
						hook.onUpdateChildrenHook(changedNode);
				}
			}
		}
		if (!node.isRoot() && node.getParentNode() != null)
			recursiveCallUpdateHooks(node.getParentNode(), changedNode);
	}

	public void doubleClick(MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelecteds().size() != 1)
			return;
		MindMapNode node = ((MainView) e.getComponent()).getNodeView()
				.getModel();
		// edit the node only if the node is a leaf (fc 0.7.1), or the root node
		// (fc 0.9.0)
		if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown()
				&& !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1
				&& (node.getLink() == null)) {
			edit(null, false, false);
		}
	}

	public boolean extendSelection(MouseEvent e) {
		NodeView newlySelectedNodeView = ((MainView) e.getComponent())
				.getNodeView();
		// MindMapNode newlySelectedNode = newlySelectedNodeView.getModel();
		boolean extend = e.isControlDown();
		// Fixes Cannot select multiple single nodes *
		// https://sourceforge.net/tracker/?func=detail&atid=107118&aid=1675829&group_id=7118
		if (Tools.isMacOsX()) {
			extend |= e.isMetaDown();
		}
		boolean range = e.isShiftDown();
		boolean branch = e.isAltGraphDown() || e.isAltDown(); /*
															 * windows alt,
															 * linux altgraph
															 * ....
															 */
		boolean retValue = false;

		if (extend || range || branch
				|| !getView().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend)
					getView().toggleSelected(newlySelectedNodeView);
				else
					select(newlySelectedNodeView);
				retValue = true;
			} else {
				retValue = getView().selectContinuous(newlySelectedNodeView);
				// /* fc, 25.1.2004: replace getView by controller methods.*/
				// if (newlySelectedNodeView != getView().getSelected() &&
				// newlySelectedNodeView.isSiblingOf(getView().getSelected())) {
				// getView().selectContinuous(newlySelectedNodeView);
				// retValue = true;
				// } else {
				// /* if shift was down, but no range can be selected, then the
				// new node is simply selected: */
				// if(!getView().isSelected(newlySelectedNodeView)) {
				// getView().toggleSelected(newlySelectedNodeView);
				// retValue = true;
				// }
			}
			if (branch) {
				getView().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}

		if (retValue) {
			e.consume();

			// Display link in status line
			String link = newlySelectedNodeView.getModel().getLink();
			if (link != null) {
				getController().getFrame().out(link);
			}
		}
		logger.fine("MouseEvent: extend:" + extend + ", range:" + range
				+ ", branch:" + branch + ", event:" + e + ", retValue:"
				+ retValue);
		obtainFocusForSelected();
		return retValue;
	}

	public void registerMouseWheelEventHandler(MouseWheelEventHandler handler) {
		logger.fine("Registered   MouseWheelEventHandler " + handler);
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public void deRegisterMouseWheelEventHandler(MouseWheelEventHandler handler) {
		logger.fine("Deregistered MouseWheelEventHandler " + handler);
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public Set<MouseWheelEventHandler> getRegisteredMouseWheelEventHandler() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);

	}

	public String marshall(XmlAction action) {
		return Tools.marshall(action);
	}

	public XmlAction unMarshall(String inputString) {
		return Tools.unMarshall(inputString);
	}

	public void storeDialogPositions(JDialog dialog,
			WindowConfigurationStorage pStorage,
			String window_preference_storage_property) {
		XmlBindingTools.getInstance().storeDialogPositions(getController(),
				dialog, pStorage, window_preference_storage_property);
	}

	public WindowConfigurationStorage decorateDialog(JDialog dialog,
			String window_preference_storage_property) {
		return XmlBindingTools.getInstance().decorateDialog(getController(),
				dialog, window_preference_storage_property);
	}

	public void insertNodeInto(MindMapNode newNode, MindMapNode parent,
			int index) {
		setSaved(false);
		getMap().insertNodeInto(newNode, parent, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * freemind.modes.MindMap#insertNodeInto(javax.swing.tree.MutableTreeNode,
	 * javax.swing.tree.MutableTreeNode)
	 */
	public void insertNodeInto(MindMapNode newChild, MindMapNode parent) {
		insertNodeInto(newChild, parent, parent.getChildCount());
	}



	public void removeNodeFromParent(MindMapNode selectedNode) {
		setSaved(false);
		// first deselect, and then remove.
		NodeView nodeView = getView().getNodeView(selectedNode);
		getView().deselect(nodeView);
		getModel().removeNodeFromParent(selectedNode);
	}

	public void repaintMap() {
		getView().repaint();
	}

	/**
	 * Erases all content of the node as text, colors, fonts, etc.
	 */
	public void clearNodeContents(MindMapNode pNode) {
		Pattern erasePattern = new Pattern();
		erasePattern.setPatternEdgeColor(new PatternEdgeColor());
		erasePattern.setPatternEdgeStyle(new PatternEdgeStyle());
		erasePattern.setPatternEdgeWidth(new PatternEdgeWidth());
		erasePattern.setPatternIcon(new PatternIcon());
		erasePattern
				.setPatternNodeBackgroundColor(new PatternNodeBackgroundColor());
		erasePattern.setPatternNodeColor(new PatternNodeColor());
		erasePattern.setPatternNodeFontBold(new PatternNodeFontBold());
		erasePattern.setPatternNodeFontItalic(new PatternNodeFontItalic());
		erasePattern.setPatternNodeFontName(new PatternNodeFontName());
		erasePattern.setPatternNodeFontSize(new PatternNodeFontSize());
		erasePattern.setPatternNodeStyle(new PatternNodeStyle());
		erasePattern.setPatternNodeText(new PatternNodeText());
		applyPattern(pNode, erasePattern);
		setNoteText(pNode, null);
	}

	public void registerPlugin(MindMapControllerPlugin pPlugin) {
		mPlugins.add(pPlugin);
	}

	public void deregisterPlugin(MindMapControllerPlugin pPlugin) {
		mPlugins.remove(pPlugin);
	}

	public Set<MindMapControllerPlugin> getPlugins() {
		return Collections.unmodifiableSet(mPlugins);
	}

	/**
	 */
	public Transferable getClipboardContents() {
		getClipboard();
		return clipboard.getContents(this);
	}

	protected void getClipboard() {
		if (clipboard == null) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			selection = toolkit.getSystemSelection();
			clipboard = toolkit.getSystemClipboard();

		}
	}

	/**
	 */
	public void setClipboardContents(Transferable t) {
		getClipboard();
		clipboard.setContents(t, null);
		if (selection != null) {
			selection.setContents(t, null);
		}
	}

	public boolean mapSourceChanged(MindMap pMap) throws Exception {
		// ask the user, if he wants to reload the map.
		MapSourceChangeDialog runnable = new MapSourceChangeDialog();
		Tools.invokeAndWait(runnable);
		return runnable.getReturnValue();
	}

	public void setNodeHookFactory(HookFactory pNodeHookFactory) {
		nodeHookFactory = pNodeHookFactory;
	}

	/** Creates and invokes a ModeControllerHook.*/
	public void createModeControllerHook(String pHookName) {
		HookFactory hookFactory = getHookFactory();
		// two different invocation methods:single or selecteds
		ModeControllerHook hook = hookFactory
				.createModeControllerHook(pHookName);
		hook.setController(this);
		invokeHook(hook);
	}

	/**
	 * Delegate method to Controller. Must be called after cut.s
	 */
	public void obtainFocusForSelected() {
		getController().obtainFocusForSelected();

	}

	public boolean doTransaction(String pName, ActionPair pPair) {
		return actionFactory.doTransaction(pName, pPair);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.MindMapActions#setAttribute(freemind.modes.MindMapNode, int, freemind.modes.attributes.Attribute)
	 */
	@Override
	public void setAttribute(MindMapNode pNode, int pPosition,
			Attribute pAttribute) {
		getActorFactory().getSetAttributeActor().setAttribute(pNode, pPosition, pAttribute);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.MindMapActions#insertAttribute(freemind.modes.MindMapNode, int, freemind.modes.attributes.Attribute)
	 */
	@Override
	public void insertAttribute(MindMapNode pNode, int pPosition,
			Attribute pAttribute) {
		getActorFactory().getInsertAttributeActor().insertAttribute(pNode, pPosition, pAttribute);
	}
	
	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.MindMapActions#addAttribute(freemind.modes.MindMapNode, freemind.modes.attributes.Attribute)
	 */
	@Override
	public int addAttribute(MindMapNode pNode, Attribute pAttribute) {
		return getActorFactory().getAddAttributeActor().addAttribute(pNode, pAttribute);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.MindMapActions#removeAttribute(freemind.modes.MindMapNode, int)
	 */
	@Override
	public void removeAttribute(MindMapNode pNode, int pPosition) {
		getActorFactory().getRemoveAttributeActor().removeAttribute(pNode, pPosition);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.MindMap.MapFeedback#out(java.lang.String)
	 */
	@Override
	public void out(String pFormat) {
		getFrame().out(pFormat);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.ExtendedMapFeedback#close(boolean)
	 */
	@Override
	public void close(boolean pForce) {
		getController().close(pForce);
	}

	/* (non-Javadoc)
	 * @see freemind.modes.mindmapmode.actions.MindMapActions#setNoteText(freemind.modes.MindMapNode, java.lang.String)
	 */
	@Override
	public void setNoteText(MindMapNode pSelected, String pNewText) {
		getActorFactory().getChangeNoteTextActor().setNoteText(pSelected, pNewText);
	}

}
