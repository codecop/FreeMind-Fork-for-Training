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

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import freemind.extensions.PermanentNodeHook;
import freemind.extensions.PermanentNodeHookSubstituteUnknown;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.attributes.Attribute;

public class XMLElementAdapter extends XMLElement {


	// Logging:
	protected static java.util.logging.Logger logger;

	private Object userObject = null;
	private NodeAdapter mapChild = null;
	private HashMap<String, String> nodeAttributes = new HashMap<>();

	// Font attributes

	private String fontName;
	private int fontStyle = 0;
	private int fontSize = 0;

	// Icon attributes

	private String iconName;

	// arrow link attributes:
	protected Vector<ArrowLinkAdapter> mArrowLinkAdapters;
	protected HashMap<String, NodeAdapter> mIdToTarget;
	public static final String XML_NODE_TEXT = "TEXT";
	public static final String XML_NODE = "node";
	public static final String XML_NODE_ATTRIBUTE = "attribute";
	public static final String XML_NODE_ATTRIBUTE_LAYOUT = "attribute_layout";
	public static final String XML_NODE_ATTRIBUTE_REGISTRY = "attribute_registry";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_NAME = "attribute_name";
	public static final String XML_NODE_REGISTERED_ATTRIBUTE_VALUE = "attribute_value";
	// public static final String XML_NODE_CLASS_PREFIX = XML_NODE+"_";
	public static final String XML_NODE_CLASS = "AA_NODE_CLASS";
	public static final String XML_NODE_ADDITIONAL_INFO = "ADDITIONAL_INFO";
	public static final String XML_NODE_ENCRYPTED_CONTENT = "ENCRYPTED_CONTENT";
	public static final String XML_NODE_HISTORY_CREATED_AT = "CREATED";
	public static final String XML_NODE_HISTORY_LAST_MODIFIED_AT = "MODIFIED";

	public static final String XML_NODE_XHTML_TYPE_TAG = "TYPE";
	public static final String XML_NODE_XHTML_TYPE_NODE = "NODE";
	public static final String XML_NODE_XHTML_TYPE_NOTE = "NOTE";

	private String attributeName;

	private String attributeValue;

	protected final MapFeedback mMapFeedback;

	private boolean fontStyleStrikethrough;

	// Overhead methods

	public XMLElementAdapter(MapFeedback pMapFeedback) {
		this(pMapFeedback, new Vector<ArrowLinkAdapter>(), new HashMap<String, NodeAdapter>());
	}

	protected XMLElementAdapter(MapFeedback pMapFeedback,
			Vector<ArrowLinkAdapter> arrowLinkAdapters, HashMap<String, NodeAdapter> IDToTarget) {
		this.mMapFeedback = pMapFeedback;
		this.mArrowLinkAdapters = arrowLinkAdapters;
		this.mIdToTarget = IDToTarget;
		if (logger == null) {
			logger = freemind.main.Resources.getInstance().getLogger(
					this.getClass().getName());
		}
	}

	/** abstract method to create elements of my type (factory). */
	protected XMLElement createAnotherElement() {
		// We do not need to initialize the things of XMLElement.
		return new XMLElementAdapter(mMapFeedback, mArrowLinkAdapters,
				mIdToTarget);
	}



	public Object getUserObject() {
		return userObject;
	}

	protected void setUserObject(Object obj) {
		userObject = obj;
	}

	public NodeAdapter getMapChild() {
		return mapChild;
	}

	// Real parsing methods

	public void setName(String name) {
		super.setName(name);
		// Create user object based on name
		if (name.equals(XML_NODE)) {
			userObject = getMap().createNodeAdapter(getMap(), null);
			nodeAttributes.clear();
		} else if (name.equals("edge")) {
			userObject = getMap().createEdgeAdapter(null);
		} else if (name.equals("cloud")) {
			userObject = getMap().createCloudAdapter(null);
		} else if (name.equals("arrowlink")) {
			userObject = getMap().createArrowLinkAdapter(null, null);
		} else if (name.equals("linktarget")) {
			userObject = getMap().createArrowLinkTarget(null, null);
		} else if (name.equals("font")) {
			userObject = null;
		} else if (name.equals(XML_NODE_ATTRIBUTE)) {
			userObject = null;
		} else if (name.equals(XML_NODE_ATTRIBUTE_LAYOUT)) {
			userObject = null;
		} else if (name.equals("map")) {
			userObject = null;
		} else if (name.equals(XML_NODE_ATTRIBUTE_REGISTRY)) {
			userObject = null;
		} else if (name.equals(XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			userObject = null;
		} else if (name.equals(XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
			userObject = null;
		} else if (name.equals("icon")) {
			userObject = null;
		} else if (name.equals("hook")) {
			// we gather the xml element and send it to the hook after
			// completion.
			userObject = new XMLElement();
		} else {
			userObject = new XMLElement(); // for childs of hooks
		}
	}

	protected MindMap getMap() {
		return mMapFeedback.getMap();
	}

	public void addChild(XMLElement child) {
		if (getName().equals("map")) {
			mapChild = (NodeAdapter) child.getUserObject();
			return;
		}
		if (userObject instanceof XMLElement) {
			// ((XMLElement) userObject).addChild(child);
			super.addChild(child);
			return;
		}
		if (userObject instanceof NodeAdapter) {
			NodeAdapter node = (NodeAdapter) userObject;
			if (child.getUserObject() instanceof NodeAdapter) {
				node.insert((NodeAdapter) child.getUserObject(), -1);
			} // to the end without preferable... (PN)
				// node.getRealChildCount()); }
			else if (child.getUserObject() instanceof EdgeAdapter) {
				EdgeAdapter edge = (EdgeAdapter) child.getUserObject();
				edge.setTarget(node);
				node.setEdge(edge);
			} else if (child.getUserObject() instanceof CloudAdapter) {
				CloudAdapter cloud = (CloudAdapter) child.getUserObject();
				cloud.setTarget(node);
				node.setCloud(cloud);
			} else if (child.getUserObject() instanceof ArrowLinkTarget) {
				// ArrowLinkTarget is derived from ArrowLinkAdapter, so it must
				// be checked first.
				ArrowLinkTarget arrowLinkTarget = (ArrowLinkTarget) child
						.getUserObject();
				arrowLinkTarget.setTarget(node);
				mArrowLinkAdapters.add(arrowLinkTarget);
			} else if (child.getUserObject() instanceof ArrowLinkAdapter) {
				ArrowLinkAdapter arrowLink = (ArrowLinkAdapter) child
						.getUserObject();
				arrowLink.setSource(node);
				// annotate this link: (later processed by caller.).
				// System.out.println("arrowLink="+arrowLink);
				mArrowLinkAdapters.add(arrowLink);
			} else if (child.getName().equals("font")) {
				node.setFont((Font) child.getUserObject());
			} else if (child.getName().equals(XML_NODE_ATTRIBUTE)) {
				node.addAttribute(
						(Attribute) child.getUserObject());

			} else if (child.getName().equals(XML_NODE_ATTRIBUTE_LAYOUT)) {
//				node.createAttributeTableModel();
//				AttributeTableLayoutModel layout = node.getAttributes()
//						.getLayout();
//				layout.setColumnWidth(0,
//						((XMLElementAdapter) child).attributeNameWidth);
//				layout.setColumnWidth(1,
//						((XMLElementAdapter) child).attributeValueWidth);
			} else if (child.getName().equals("icon")) {
				node.addIcon((MindIcon) child.getUserObject(), MindIcon.LAST);
			} else if (child.getName().equals(XML_NODE_XHTML_CONTENT_TAG)) {
				String xmlText = ((XMLElement) child).getContent();
				Object typeAttribute = child
						.getAttribute(XML_NODE_XHTML_TYPE_TAG);
				if (typeAttribute == null
						|| XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)) {
					// output:
					logger.finest("Setting node html content to:" + xmlText);
					node.setXmlText(xmlText);
				} else {
					logger.finest("Setting note html content to:" + xmlText);
					node.setXmlNoteText(xmlText);
				}
			} else if (child.getName().equals("hook")) {
				XMLElement xml = (XMLElement) child/* .getUserObject() */;
				String loadName = (String) xml.getAttribute("NAME");
				PermanentNodeHook hook = null;
				try {
					// loadName=loadName.replace('/', File.separatorChar);
					/*
					 * The next code snippet is an exception. Normally, hooks
					 * have to be created via the ModeController. DO NOT COPY.
					 */
					hook = (PermanentNodeHook) mMapFeedback.createNodeHook(loadName, node);
					// this is a bad hack. Don't make use of this data unless
					// you know exactly what you are doing.
					hook.setNode(node);
				} catch (Exception e) {
					freemind.main.Resources.getInstance().logException(e);
					hook = new PermanentNodeHookSubstituteUnknown(loadName);
				}
				hook.loadFrom(xml);
				node.addHook(hook);
			}
			return;
		}
		if (child instanceof XMLElementAdapter
				&& getName().equals(XML_NODE_REGISTERED_ATTRIBUTE_NAME)
				&& child.getName().equals(XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
		}
	}

	public void setAttribute(String name, Object value) {
		// We take advantage of precondition that value != null.
		String sValue = value.toString();
		if (ignoreCase) {
			name = name.toUpperCase();
		}
		if (userObject instanceof XMLElement) {
			// ((XMLElement) userObject).setAttribute(name, value);
			super.setAttribute(name, value); // and to myself, as I am also an
												// xml element.
			return;
		}

		if (userObject instanceof NodeAdapter) {
			//
			NodeAdapter node = (NodeAdapter) userObject;
			userObject = setNodeAttribute(name, sValue, node);
			nodeAttributes.put(name, sValue);
			return;
		}

		if (userObject instanceof EdgeAdapter) {
			EdgeAdapter edge = (EdgeAdapter) userObject;
			if (name.equals("STYLE")) {
				edge.setStyle(sValue);
			} else if (name.equals("COLOR")) {
				edge.setColor(Tools.xmlToColor(sValue));
			} else if (name.equals("WIDTH")) {
				if (sValue.equals(EdgeAdapter.EDGE_WIDTH_THIN_STRING)) {
					edge.setWidth(EdgeAdapter.WIDTH_THIN);
				} else {
					edge.setWidth(Integer.parseInt(sValue));
				}
			}
			return;
		}

		if (userObject instanceof CloudAdapter) {
			CloudAdapter cloud = (CloudAdapter) userObject;
			if (name.equals("STYLE")) {
				cloud.setStyle(sValue);
			} else if (name.equals("COLOR")) {
				cloud.setColor(Tools.xmlToColor(sValue));
			} else if (name.equals("WIDTH")) {
				cloud.setWidth(Integer.parseInt(sValue));
			}
			return;
		}

		if (userObject instanceof ArrowLinkAdapter) {
			ArrowLinkAdapter arrowLink = (ArrowLinkAdapter) userObject;
			if (name.equals("STYLE")) {
				arrowLink.setStyle(sValue);
			} else if (name.equals("ID")) {
				arrowLink.setUniqueId(sValue);
			} else if (name.equals("COLOR")) {
				arrowLink.setColor(Tools.xmlToColor(sValue));
			} else if (name.equals("DESTINATION")) {
				arrowLink.setDestinationLabel(sValue);
			} else if (name.equals("REFERENCETEXT")) {
				arrowLink.setReferenceText((sValue));
			} else if (name.equals("STARTINCLINATION")) {
				arrowLink.setStartInclination(Tools.xmlToPoint(sValue));
			} else if (name.equals("ENDINCLINATION")) {
				arrowLink.setEndInclination(Tools.xmlToPoint(sValue));
			} else if (name.equals("STARTARROW")) {
				arrowLink.setStartArrow(sValue);
			} else if (name.equals("ENDARROW")) {
				arrowLink.setEndArrow(sValue);
			} else if (name.equals("WIDTH")) {
				arrowLink.setWidth(Integer.parseInt(sValue));
			}
			if (userObject instanceof ArrowLinkTarget) {
				ArrowLinkTarget arrowLinkTarget = (ArrowLinkTarget) userObject;
				if (name.equals("SOURCE")) {
					arrowLinkTarget.setSourceLabel(sValue);
				}
			}
			return;
		}

		if (getName().equals("font")) {
			if (name.equals("SIZE")) {
				fontSize = Integer.parseInt(sValue);
			} else if (name.equals("NAME")) {
				fontName = sValue;
			}

			// Styling
			else if (sValue.equals("true")) {
				if (name.equals("BOLD")) {
					fontStyle += Font.BOLD;
				} else if (name.equals("ITALIC")) {
					fontStyle += Font.ITALIC;
				} else if (name.equals("STRIKETHROUGH")){
					fontStyleStrikethrough = true;
				}
			}
		}
		/* icons */
		if (getName().equals("icon")) {
			if (name.equals("BUILTIN")) {
				iconName = sValue;
			}
		}
		/* attributes */
		else if (getName().equals(XML_NODE_ATTRIBUTE)) {
			if (name.equals("NAME")) {
				attributeName = sValue;
			} else if (name.equals("VALUE")) {
				attributeValue = sValue;
			}
		} else if (getName().equals(XML_NODE_ATTRIBUTE_LAYOUT)) {
//			if (name.equals("NAME_WIDTH")) {
//				attributeNameWidth = Integer.parseInt(sValue);
//			} else if (name.equals("VALUE_WIDTH")) {
//				attributeValueWidth = Integer.parseInt(sValue);
//			}
		} else if (getName().equals(XML_NODE_ATTRIBUTE_REGISTRY)) {
		} else if (getName().equals(XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			if (name.equals("NAME")) {
				attributeName = sValue;
			} else {
				super.setAttribute(name, sValue);
			}
		} else if (getName().equals(XML_NODE_REGISTERED_ATTRIBUTE_VALUE)) {
			if (name.equals("VALUE")) {
				attributeValue = sValue;
			}
		}
	}

	private NodeAdapter setNodeAttribute(String name, String sValue,
			NodeAdapter node) {
		if (name.equals(XML_NODE_TEXT)) {
			logger.finest("Setting node text content to:" + sValue);
			node.setUserObject(sValue);
		} else if (name.equals(XML_NODE_ENCRYPTED_CONTENT)) {
			// we change the node implementation to EncryptedMindMapNode.
			node = getMap().createEncryptedNode(sValue);
			setUserObject(node);
			copyAttributesToNode(node);
		} else if (name.equals(XML_NODE_HISTORY_CREATED_AT)) {
			if (node.getHistoryInformation() == null) {
				node.setHistoryInformation(new HistoryInformation());
			}
			node.getHistoryInformation().setCreatedAt(Tools.xmlToDate(sValue));
		} else if (name.equals(XML_NODE_HISTORY_LAST_MODIFIED_AT)) {
			if (node.getHistoryInformation() == null) {
				node.setHistoryInformation(new HistoryInformation());
			}
			node.getHistoryInformation().setLastModifiedAt(
					Tools.xmlToDate(sValue));
		} else if (name.equals("FOLDED")) {
			if (sValue.equals("true")) {
				node.setFolded(true);
			}
		} else if (name.equals("POSITION")) {
			// fc, 17.12.2003: Remove the left/right bug.
			node.setLeft(sValue.equals("left"));
		} else if (name.equals("COLOR")) {
			if (sValue.length() == 7) {
				node.setColor(Tools.xmlToColor(sValue));
			}
		} else if (name.equals("BACKGROUND_COLOR")) {
			if (sValue.length() == 7) {
				node.setBackgroundColor(Tools.xmlToColor(sValue));
			}
		} else if (name.equals("LINK")) {
			node.setLink(sValue);
		} else if (name.equals("STYLE")) {
			node.setStyle(sValue);
		} else if (name.equals("ID")) {
			// do not set label but annotate in list:
			// System.out.println("(sValue, node) = " + sValue + ", "+ node);
			mIdToTarget.put(sValue, node);
		} else if (name.equals("VSHIFT")) {
			node.setShiftY(Integer.parseInt(sValue));
		} else if (name.equals("VGAP")) {
			node.setVGap(Integer.parseInt(sValue));
		} else if (name.equals("HGAP")) {
			node.setHGap(Integer.parseInt(sValue));
		}
		return node;
	}

	/**
	 * Sets all attributes that were formely applied to the current userObject
	 * to a given (new) node. Thus, the instance of a node can be changed after
	 * the creation. (At the moment, relevant for encrypted nodes).
	 */
	protected void copyAttributesToNode(NodeAdapter node) {
		// reactivate all settings from nodeAttributes:
		for (String key : nodeAttributes.keySet()) {
			// to avoid self reference:
			setNodeAttribute(key, (String) nodeAttributes.get(key), node);
		}
	}

	protected void completeElement() {
		if (getName().equals(XML_NODE)) {
			// unify map child behaviour:
			if (mapChild == null) {
				mapChild = (NodeAdapter) userObject;
			}
			return;
		}
		if (getName().equals("font")) {
			Font font = new Font(fontName, fontStyle, fontSize);
			if(fontStyleStrikethrough){
				Map attr = font.getAttributes();
				attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
				font = new Font(attr);
			}
			userObject = mMapFeedback.getFontThroughMap(font);
			return;
		}
		/* icons */
		if (getName().equals("icon")) {
			userObject = MindIcon.factory(iconName);
			return;
		}
		/* attributes */
		if (getName().equals(XML_NODE_ATTRIBUTE)) {
			userObject = new Attribute(attributeName, attributeValue);
			return;
		}
		if (getName().equals(XML_NODE_REGISTERED_ATTRIBUTE_NAME)) {
			return;
		}
	}

	/**
	 * Completes the links within the getMap(). They are registered in the
	 * registry.
	 * 
	 * Case I: Source+Destination are pasted (Ia: cut, Ib: copy) Case II: Source
	 * is pasted, Destination remains unchanged in the map (IIa: cut, IIb: copy)
	 * Case III: Destination is pasted, Source remains unchanged in the map
	 * (IIIa: cut, IIIb: copy)
	 */
	public void processUnfinishedLinks(MindMapLinkRegistry registry) {
		// add labels to the nodes:
		for (String key : mIdToTarget.keySet()) {
			NodeAdapter target1 = (NodeAdapter) mIdToTarget.get(key);
			/*
			 * key is the proposed name for the target, is changed by the
			 * registry, if already present.
			 */
			registry.registerLinkTarget(target1, key);
		}
		// complete arrow links with right labels:
		for (int i = 0; i < mArrowLinkAdapters.size(); ++i) {
			Object arrowObject = mArrowLinkAdapters.get(i);
			if (arrowObject instanceof ArrowLinkTarget) {
				ArrowLinkTarget linkTarget = (ArrowLinkTarget) arrowObject;
				// do the same as for ArrowLinkAdapter and start to search for the source.
				String oldId = linkTarget.getSourceLabel();
				MindMapNode source = (MindMapNode) registry.getTargetForId(oldId);
				// find oldId in target list:
				if (mIdToTarget.containsKey(oldId)) {
					// link source present in the paste as well and has probably
					// been renamed (case I), do nothing, as the source does
					// all.
					continue;
				} else if (source == null) {
					// link source is in nowhere-land
					logger.severe("Cannot find the label " + oldId
							+ " in the map. The link target " + linkTarget
							+ " is not restored.");
					continue;
				}
				// link source remains in the map (case III)
				// set the source:
				linkTarget.setSource(source);
				// here, it is getting more complex: case IIIa: the arrowLink
				// has to be recreated.
				// case IIIb: the arrowLink has to be doubled! First,
				// distinguish between a and b.
				MindMapNode target = linkTarget.getTarget();
				String targetCurrentId = registry.getLabel(target);
				if (!mIdToTarget.containsKey(targetCurrentId)) {
					// the id of target has changed, we have case IIIb.
					MindMapLink link = registry.getLinkForId(linkTarget
							.getUniqueId());
					if (link == null) {
						logger.severe("Cannot find the label "
								+ linkTarget.getUniqueId()
								+ " for the link in the map. The link target "
								+ linkTarget + " is not restored.");
						continue;
					} else {
						// double the link.
						MindMapLink clone = (MindMapLink) link.clone();
						clone.setTarget(target);
						((ArrowLinkAdapter) clone)
								.setDestinationLabel(targetCurrentId);
						// add the new arrowLink and give it a new id
						registry.registerLink(clone);
						linkTarget.setUniqueId(clone.getUniqueId());
					}
				} else {
					// case IIIa: The link must only be re-added:
					// change from linkTarget to ArrowLinkAdapter and add it:
					ArrowLinkAdapter linkAdapter = linkTarget.createArrowLinkAdapter(registry);
					registry.registerLink(linkAdapter);
				}
			} else if (arrowObject instanceof ArrowLinkAdapter) {
				ArrowLinkAdapter arrowLink = (ArrowLinkAdapter) arrowObject;
				// here, the source is in the paste, and the destination is now
				// searched:
				String oldId = arrowLink.getDestinationLabel();
				NodeAdapter target = null;
				String newId = null;
				// find oldId in target list:
				if (mIdToTarget.containsKey(oldId)) {
					// link target present in the paste as well and has probably
					// been renamed (case I)
					target = (NodeAdapter) mIdToTarget.get(oldId);
					newId = registry.getLabel(target);
				} else if (registry.getTargetForId(oldId) != null) {
					// link target remains in the map (case II)
					target = (NodeAdapter) registry.getTargetForId(oldId);
					newId = oldId;
				} else {
					// link target is in nowhere-land
					logger.severe("Cannot find the label " + oldId
							+ " in the map. The link " + arrowLink
							+ " is not restored.");
					continue;
				}
				// set the new ID:
				arrowLink.setDestinationLabel(newId);
				// set the target:
				arrowLink.setTarget(target);
				// add the arrowLink:
				registry.registerLink(arrowLink);
			}
		}
	}

	public HashMap<String, NodeAdapter> getIDToTarget() {
		return mIdToTarget;
	}

	public void setIDToTarget(HashMap<String, NodeAdapter> pToTarget) {
		mIdToTarget = pToTarget;
	}


}
