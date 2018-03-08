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
/*$Id: StylePatternFactory.java,v 1.1.2.3.2.5 2007/09/13 20:33:07 christianfoltin Exp $*/

package freemind.modes;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import freemind.common.TextTranslator;
import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternChild;
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
import freemind.controller.actions.generated.instance.PatternNodeFontStrikethrough;
import freemind.controller.actions.generated.instance.PatternNodeStyle;
import freemind.controller.actions.generated.instance.PatternPropertyBase;
import freemind.controller.actions.generated.instance.Patterns;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.mindmapmode.MindMapController.MindMapControllerPlugin;
import freemind.modes.mindmapmode.actions.ApplyPatternAction.ExternalPatternAction;

/**
 * This class constructs patterns from files or from nodes and saves them back.
 */
public class StylePatternFactory {
	public static final String FALSE_VALUE = "false";

	public static final String TRUE_VALUE = "true";

	public static List<Pattern> loadPatterns(File file) throws Exception {
		return loadPatterns(new BufferedReader(new FileReader(file)));
	}

	/**
	 * @return a List of Pattern elements.
	 * @throws Exception
	 */
	public static List<Pattern> loadPatterns(Reader reader) throws Exception {
		Patterns patterns = (Patterns) XmlBindingTools.getInstance()
				.unMarshall(reader);
		// translate standard strings:
		for (Iterator<Pattern> iterator = patterns.getListChoiceList().iterator(); iterator.hasNext();) {
			Pattern pattern = iterator.next();
			String originalName = pattern.getName();
			String name = originalName;
			if (name == null) {
				continue;
			}
			// make private:
			name = "__pattern_string_" + name.replace(" ", "_");
			String translatedName = Resources.getInstance().getResourceString(
					name);
			if (!Tools.safeEquals(translatedName, name)) {
				// there is a translation:
				pattern.setName(translatedName);
				// store original name to be able to translate back
				pattern.setOriginalName(originalName);
				// look, whether or not the string occurs in other situations:
				for (Iterator<Pattern> it = patterns.getListChoiceList().iterator(); it.hasNext();) {
					Pattern otherPattern = it.next();
					PatternChild child = otherPattern.getPatternChild();
					if (child != null) {
						if (Tools.safeEquals(originalName, child.getValue())) {
							child.setValue(translatedName);
						}
					}
				}
			}
		}
		return patterns.getListChoiceList();
	}

	/**
	 * the result is written to, and it is closed afterwards List of Pattern
	 * elements.
	 * 
	 * @throws Exception
	 */
	public static void savePatterns(Writer writer, List<Pattern> listOfPatterns)
			throws Exception {
		Patterns patterns = new Patterns();
		HashMap<String, Pattern> nameToPattern = new HashMap<>();
		for (Pattern pattern : listOfPatterns) {
			patterns.addChoice(pattern);
			if (pattern.getOriginalName() != null) {
				nameToPattern.put(pattern.getName(), pattern);
				pattern.setName(pattern.getOriginalName());
				pattern.setOriginalName(null);
			}
		}
		for (Iterator<Pattern> it = patterns.getListChoiceList().iterator(); it.hasNext();) {
			Pattern pattern = it.next();
			PatternChild patternChild = pattern.getPatternChild();
			if (patternChild != null
					&& nameToPattern.containsKey(patternChild.getValue())) {
				Pattern childPattern = (Pattern) nameToPattern.get(patternChild
						.getValue());
				patternChild.setValue(childPattern.getName());
			}
		}
		String marshalledResult = XmlBindingTools.getInstance().marshall(
				patterns);
		writer.write(marshalledResult);
		writer.close();
	}

	public static Pattern createPatternFromNode(MindMapNode node) {
		Pattern pattern = new Pattern();

		if (node.getColor() != null) {
			PatternNodeColor subPattern = new PatternNodeColor();
			subPattern.setValue(Tools.colorToXml(node.getColor()));
			pattern.setPatternNodeColor(subPattern);
		}
		if (node.getBackgroundColor() != null) {
			PatternNodeBackgroundColor subPattern = new PatternNodeBackgroundColor();
			subPattern.setValue(Tools.colorToXml(node.getBackgroundColor()));
			pattern.setPatternNodeBackgroundColor(subPattern);
		}
		if (node.getStyle() != null) {
			PatternNodeStyle subPattern = new PatternNodeStyle();
			subPattern.setValue(node.getStyle());
			pattern.setPatternNodeStyle(subPattern);
		}

		PatternNodeFontBold nodeFontBold = new PatternNodeFontBold();
		nodeFontBold.setValue(node.isBold() ? TRUE_VALUE : FALSE_VALUE);
		pattern.setPatternNodeFontBold(nodeFontBold);
		PatternNodeFontStrikethrough nodeFontStrikethrough = new PatternNodeFontStrikethrough();
		nodeFontStrikethrough.setValue(node.isStrikethrough() ? TRUE_VALUE : FALSE_VALUE);
		pattern.setPatternNodeFontStrikethrough(nodeFontStrikethrough);
		PatternNodeFontItalic nodeFontItalic = new PatternNodeFontItalic();
		nodeFontItalic.setValue(node.isItalic() ? TRUE_VALUE : FALSE_VALUE);
		pattern.setPatternNodeFontItalic(nodeFontItalic);
		if (node.getFontSize() != null) {
			PatternNodeFontSize nodeFontSize = new PatternNodeFontSize();
			nodeFontSize.setValue(node.getFontSize());
			pattern.setPatternNodeFontSize(nodeFontSize);
		}
		if (node.getFontFamilyName() != null) {
			PatternNodeFontName subPattern = new PatternNodeFontName();
			subPattern.setValue(node.getFontFamilyName());
			pattern.setPatternNodeFontName(subPattern);
		}

		if (node.getIcons().size() == 1) {
			PatternIcon iconPattern = new PatternIcon();
			iconPattern.setValue(((MindIcon) node.getIcons().get(0)).getName());
			pattern.setPatternIcon(iconPattern);
		}
		if (node.getEdge().getColor() != null) {
			PatternEdgeColor subPattern = new PatternEdgeColor();
			subPattern.setValue(Tools.colorToXml(node.getEdge().getColor()));
			pattern.setPatternEdgeColor(subPattern);
		}
		if (node.getEdge().getStyle() != null) {
			PatternEdgeStyle subPattern = new PatternEdgeStyle();
			subPattern.setValue(node.getEdge().getStyle());
			pattern.setPatternEdgeStyle(subPattern);
		}
		if (node.getEdge().getWidth() != EdgeAdapter.DEFAULT_WIDTH) {
			PatternEdgeWidth subPattern = new PatternEdgeWidth();
			subPattern.setValue("" + node.getEdge().getWidth());
			pattern.setPatternEdgeWidth(subPattern);
		}

		return pattern;
	}

	public static String toString(Pattern pPattern, TextTranslator translator) {
		String result = "";
		if (pPattern.getPatternNodeColor() != null) {
			result = addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeColor().getValue() == null) {
				result += "-" + translator.getText("PatternToString.color");
			} else {
				result += "+" + translator.getText("PatternToString.color");
			}
		}
		if (pPattern.getPatternNodeBackgroundColor() != null) {
			result = addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeBackgroundColor().getValue() == null) {
				result += "-"
						+ translator.getText("PatternToString.backgroundColor");
			} else {
				result += "+"
						+ translator.getText("PatternToString.backgroundColor");
			}
		}
		result = addSubPatternToString(translator, result,
				pPattern.getPatternNodeFontSize(),
				"PatternToString.NodeFontSize");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternNodeFontName(), "PatternToString.FontName");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternNodeFontBold(), "PatternToString.FontBold");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternNodeFontStrikethrough(), "PatternToString.FontStrikethrough");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternNodeFontItalic(),
				"PatternToString.FontItalic");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternEdgeStyle(), "PatternToString.EdgeStyle");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternEdgeColor(), "PatternToString.EdgeColor");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternEdgeWidth(), "PatternToString.EdgeWidth");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternIcon(), "PatternToString.Icon");
		result = addSubPatternToString(translator, result,
				pPattern.getPatternChild(), "PatternToString.Child");
		return result;
	}

	private static String addSubPatternToString(TextTranslator translator,
			String result, PatternPropertyBase patternType, String patternString) {
		if (patternType != null) {
			result = addSeparatorIfNecessary(result);
			if (patternType.getValue() == null) {
				result += "-" + translator.getText(patternString);
			} else {
				result += "+" + translator.getText(patternString) + " "
						+ patternType.getValue();
			}
		}
		return result;
	}

	private static String addSeparatorIfNecessary(String result) {
		if (result.length() > 0) {
			result += ", ";
		}
		return result;
	}

	private static final String PATTERN_DUMMY = "<pattern name='dummy'/>";

	public static Pattern getPatternFromString(String pattern) {
		String patternString = pattern;
		if (patternString == null) {
			patternString = PATTERN_DUMMY;
		}
		Pattern pat = (Pattern) XmlBindingTools.getInstance().unMarshall(
				patternString);
		return pat;
	}

	private static final String PATTERNS_DUMMY = "<patterns/>";

	public static Patterns getPatternsFromString(String patterns) {
		String patternsString = patterns;
		if (patternsString == null) {
			patternsString = PATTERNS_DUMMY;
		}
		Patterns pat = (Patterns) XmlBindingTools.getInstance().unMarshall(
				patternsString);
		return pat;
	}

	/**
	 * Build the intersection of two patterns. Only, if the property is the
	 * same, or both properties are to be removed, it is kept, otherwise it is
	 * set to 'don't touch'.
	 */
	public static Pattern intersectPattern(Pattern p1, Pattern p2) {
		Pattern result = new Pattern();
		result.setPatternEdgeColor((PatternEdgeColor) processPatternProperties(
				p1.getPatternEdgeColor(), p2.getPatternEdgeColor(),
				new PatternEdgeColor()));
		result.setPatternEdgeStyle((PatternEdgeStyle) processPatternProperties(
				p1.getPatternEdgeStyle(), p2.getPatternEdgeStyle(),
				new PatternEdgeStyle()));
		result.setPatternEdgeWidth((PatternEdgeWidth) processPatternProperties(
				p1.getPatternEdgeWidth(), p2.getPatternEdgeWidth(),
				new PatternEdgeWidth()));
		result.setPatternIcon((PatternIcon) processPatternProperties(
				p1.getPatternIcon(), p2.getPatternIcon(), new PatternIcon()));
		result.setPatternNodeBackgroundColor((PatternNodeBackgroundColor) processPatternProperties(
				p1.getPatternNodeBackgroundColor(),
				p2.getPatternNodeBackgroundColor(),
				new PatternNodeBackgroundColor()));
		result.setPatternNodeColor((PatternNodeColor) processPatternProperties(
				p1.getPatternNodeColor(), p2.getPatternNodeColor(),
				new PatternNodeColor()));
		result.setPatternNodeFontBold((PatternNodeFontBold) processPatternProperties(
				p1.getPatternNodeFontBold(), p2.getPatternNodeFontBold(),
				new PatternNodeFontBold()));
		result.setPatternNodeFontStrikethrough((PatternNodeFontStrikethrough) processPatternProperties(
				p1.getPatternNodeFontStrikethrough(), p2.getPatternNodeFontStrikethrough(),
				new PatternNodeFontStrikethrough()));
		result.setPatternNodeFontItalic((PatternNodeFontItalic) processPatternProperties(
				p1.getPatternNodeFontItalic(), p2.getPatternNodeFontItalic(),
				new PatternNodeFontItalic()));
		result.setPatternNodeFontName((PatternNodeFontName) processPatternProperties(
				p1.getPatternNodeFontName(), p2.getPatternNodeFontName(),
				new PatternNodeFontName()));
		result.setPatternNodeFontSize((PatternNodeFontSize) processPatternProperties(
				p1.getPatternNodeFontSize(), p2.getPatternNodeFontSize(),
				new PatternNodeFontSize()));
		result.setPatternNodeStyle((PatternNodeStyle) processPatternProperties(
				p1.getPatternNodeStyle(), p2.getPatternNodeStyle(),
				new PatternNodeStyle()));
		return result;
	}

	private static PatternPropertyBase processPatternProperties(
			PatternPropertyBase prop1, PatternPropertyBase prop2,
			PatternPropertyBase destination) {
		if (prop1 == null || prop2 == null) {
			return null;
		}
		// both delete the value or both have the same value:
		if (Tools.safeEquals(prop1.getValue(), prop2.getValue())) {
			destination.setValue(prop1.getValue());
			return destination;
		}
		return null;
	}

	public static Pattern createPatternFromSelected(MindMapNode focussed,
			List<MindMapNode> selected) {
		Pattern nodePattern = createPatternFromNode(focussed);
		for (MindMapNode node : selected) {
			Pattern tempNodePattern = createPatternFromNode(node);
			nodePattern = intersectPattern(nodePattern, tempNodePattern);
		}
		return nodePattern;
	}

	/**
	 * @return a pattern, that removes all properties of a node to its defaults.
	 */
	public static Pattern getRemoveAllPattern() {
		Pattern result = new Pattern();
		result.setPatternEdgeColor(new PatternEdgeColor());
		result.setPatternEdgeStyle(new PatternEdgeStyle());
		result.setPatternEdgeWidth(new PatternEdgeWidth());
		result.setPatternIcon(new PatternIcon());
		result.setPatternNodeBackgroundColor(new PatternNodeBackgroundColor());
		result.setPatternNodeColor(new PatternNodeColor());
		result.setPatternNodeFontBold(new PatternNodeFontBold());
		result.setPatternNodeFontStrikethrough(new PatternNodeFontStrikethrough());
		result.setPatternNodeFontItalic(new PatternNodeFontItalic());
		result.setPatternNodeFontName(new PatternNodeFontName());
		result.setPatternNodeFontSize(new PatternNodeFontSize());
		result.setPatternNodeStyle(new PatternNodeStyle());
		return result;
	}
	
	@Deprecated 
	public static void applyPattern(Pattern pattern, MindMapNode pNode, MapFeedback pFeedback) {
		if (pattern.getPatternNodeColor() != null) {
			pNode.setColor(Tools.xmlToColor(pattern
					.getPatternNodeColor().getValue()));
		}
		if (pattern.getPatternNodeBackgroundColor() != null) {
			pNode.setBackgroundColor(Tools
					.xmlToColor(pattern
							.getPatternNodeBackgroundColor()
							.getValue()));
		}
		if (pattern.getPatternNodeStyle() != null) {
			pNode.setStyle(pattern.getPatternNodeStyle()
					.getValue());
		}
		if (pattern.getPatternEdgeColor() != null) {
			((EdgeAdapter) pNode.getEdge()).setColor(
					Tools.xmlToColor(pattern.getPatternEdgeColor()
							.getValue()));
		}
		if (pattern.getPatternNodeText() != null) {
			if (pattern.getPatternNodeText().getValue() != null) {
				pNode.setText(pattern.getPatternNodeText().getValue());
			} else {
				// clear text:
				pNode.setText("");
			}
		}
		if (pattern.getPatternIcon() != null) {
			String iconName = pattern.getPatternIcon().getValue();
			if (iconName == null) {
				while (pNode.getIcons().size() > 0 && pNode.removeIcon(0) > 0) {
				}
			} else {
				// check if icon is already present:
				List<MindIcon> icons = pNode.getIcons();
				boolean found = false;
				for (MindIcon icon : icons) {

					if (icon.getName() != null
							&& icon.getName().equals(iconName)) {
						found = true;
						break;
					}
				}
				if (!found) {
					pNode.addIcon(MindIcon.factory(iconName), pNode.getIcons().size());
				}
			}
		} 
		if (pattern.getPatternNodeFontName() != null) {
			String nodeFontFamily = pattern.getPatternNodeFontName().getValue();
			if (nodeFontFamily == null) {
				pNode.setFont(pFeedback.getDefaultFont());
			} else {
				((NodeAdapter) pNode).establishOwnFont();
				pNode.setFont(pFeedback.getFontThroughMap(
						new Font(nodeFontFamily, pNode.getFont().getStyle(), pNode
								.getFont().getSize())));
			}
		}
		if (pattern.getPatternNodeFontSize() != null) {
			String nodeFontSize = pattern.getPatternNodeFontSize().getValue();
			if (nodeFontSize == null) {
				pNode.setFontSize(pFeedback.getDefaultFont().getSize());
			} else {
				try {
					pNode.setFontSize(Integer
							.parseInt(nodeFontSize));
				} catch (Exception e) {
					freemind.main.Resources.getInstance()
							.logException(e);
				}
			}
		}
		if (pattern.getPatternNodeFontItalic() != null) {
			((NodeAdapter) pNode)
					.setItalic(
							TRUE_VALUE.equals(pattern.getPatternNodeFontItalic()
									.getValue()));
		}
		if (pattern.getPatternNodeFontBold() != null) {
			((NodeAdapter) pNode).setBold(TRUE_VALUE.equals(pattern.getPatternNodeFontBold().getValue()));
		}
		if (pattern.getPatternNodeFontStrikethrough() != null) {
			((NodeAdapter) pNode).setStrikethrough(TRUE_VALUE.equals(pattern.getPatternNodeFontStrikethrough().getValue()));
		}

		if (pattern.getPatternEdgeStyle() != null) {
			((EdgeAdapter) pNode.getEdge()).setStyle(pattern.getPatternEdgeStyle().getValue());
		}
		PatternEdgeWidth patternEdgeWidth = pattern.getPatternEdgeWidth();
		if (patternEdgeWidth != null) {
			if (patternEdgeWidth.getValue() != null) {
				((EdgeAdapter) pNode.getEdge()).setWidth(Tools.edgeWidthStringToInt(patternEdgeWidth.getValue()));
			} else {
				((EdgeAdapter) pNode.getEdge()).setWidth(EdgeAdapter.DEFAULT_WIDTH);
			}
		}
	}
	
	public static void applyPattern(MindMapNode node, Pattern pattern, 
			List<Pattern> pPatternList, Set<MindMapControllerPlugin> pPlugins, ExtendedMapFeedback pMapFeedback) {
		if (pattern.getPatternNodeText() != null) {
			if (pattern.getPatternNodeText().getValue() != null) {
				pMapFeedback.setNodeText(node, pattern.getPatternNodeText()
						.getValue());
			} else {
				// clear text:
				pMapFeedback.setNodeText(node, "");
			}
		}
		if (pattern.getPatternNodeColor() != null) {
			pMapFeedback.setNodeColor(node,
					Tools.xmlToColor(pattern.getPatternNodeColor().getValue()));
		}
		if (pattern.getPatternNodeBackgroundColor() != null) {
			pMapFeedback.setNodeBackgroundColor(node, Tools.xmlToColor(pattern
					.getPatternNodeBackgroundColor().getValue()));
		}
		// Perhaps already fixed?:
		// FIXME: fc, 3.1.2004: setting the style to "null" causes strange
		// behaviour.
		// see
		// https://sourceforge.net/tracker/?func=detail&atid=107118&aid=1094623&group_id=7118
		if (pattern.getPatternNodeStyle() != null) {
			pMapFeedback.setNodeStyle(node, pattern.getPatternNodeStyle()
					.getValue());
		}
		if (pattern.getPatternIcon() != null) {
			String iconName = pattern.getPatternIcon().getValue();
			if (iconName == null) {
				while (pMapFeedback.removeLastIcon(node) > 0) {
				}
			} else {
				// check if icon is already present:
				List<MindIcon> icons = node.getIcons();
				boolean found = false;
				for (MindIcon icon : icons) {
					if (icon.getName() != null
							&& icon.getName().equals(iconName)) {
						found = true;
						break;
					}
				}
				if (!found) {
					pMapFeedback.addIcon(node, MindIcon.factory(iconName));
				}
			}
		} // fc, 28.9.2003
		if (pattern.getPatternNodeFontName() != null) {
			String nodeFontFamily = pattern.getPatternNodeFontName().getValue();
			if (nodeFontFamily == null) {
				nodeFontFamily = pMapFeedback.getDefaultFont().getFamily();
			}
			pMapFeedback.setFontFamily(node, nodeFontFamily);
		}
		if (pattern.getPatternNodeFontSize() != null) {
			String nodeFontSize = pattern.getPatternNodeFontSize().getValue();
			if (nodeFontSize == null) {
				nodeFontSize = "" + pMapFeedback.getDefaultFont().getSize();
			}
			pMapFeedback.setFontSize(node, String.valueOf(nodeFontSize));
		}
		if (pattern.getPatternNodeFontItalic() != null) {
			pMapFeedback.setItalic(node, TRUE_VALUE.equals(pattern
					.getPatternNodeFontItalic().getValue()));
		}
		if (pattern.getPatternNodeFontBold() != null) {
			pMapFeedback.setBold(node,
					TRUE_VALUE.equals(pattern.getPatternNodeFontBold().getValue()));
		}
		if (pattern.getPatternNodeFontStrikethrough() != null) {
			pMapFeedback.setStrikethrough(node,
					TRUE_VALUE.equals(pattern.getPatternNodeFontStrikethrough().getValue()));
		}

		if (pattern.getPatternEdgeColor() != null) {
			pMapFeedback.setEdgeColor(node,
					Tools.xmlToColor(pattern.getPatternEdgeColor().getValue()));
		}
		if (pattern.getPatternEdgeStyle() != null) {
			pMapFeedback.setEdgeStyle(node, pattern.getPatternEdgeStyle()
					.getValue());
		}
		PatternEdgeWidth patternEdgeWidth = pattern.getPatternEdgeWidth();
		if (patternEdgeWidth != null) {
			if (patternEdgeWidth.getValue() != null) {
				pMapFeedback
						.setEdgeWidth(node, Tools
								.edgeWidthStringToInt(patternEdgeWidth
										.getValue()));
			} else {
				pMapFeedback.setEdgeWidth(node, EdgeAdapter.DEFAULT_WIDTH);
			}
		}

		if (pattern.getPatternChild() != null
				&& pattern.getPatternChild().getValue() != null) {
			// find children among all patterns:
			String searchedPatternName = pattern.getPatternChild().getValue();
			for (Pattern otherPattern : pPatternList) {
				if (otherPattern.getName().equals(searchedPatternName)) {
					for (ListIterator j = node.childrenUnfolded(); j.hasNext();) {
						NodeAdapter child = (NodeAdapter) j.next();
						applyPattern(child, otherPattern, pPatternList, pPlugins, pMapFeedback);
					}
					break;
				}
			}
		}
		for (MindMapControllerPlugin plugin : pPlugins) {
			if (plugin instanceof ExternalPatternAction) {
				ExternalPatternAction externalAction = (ExternalPatternAction) plugin;
				externalAction.act(node, pattern);
			}
		}
	}
}
