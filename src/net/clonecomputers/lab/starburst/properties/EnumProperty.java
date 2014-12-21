package net.clonecomputers.lab.starburst.properties;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class EnumProperty extends AbstractProperty<String> implements PropertyTree {
	private final Random r;
	private final String[] values;
	private final Map<String, Map<String, PropertyTreeNode>> subproperties;
	private JComboBox valueMenu;
	private JPanel centerPanel;

	public EnumProperty(String name, String category, Map<String, Map<String, PropertyTreeNode>> values, boolean shouldRandomize, Random r) {
		super(name, category, shouldRandomize);
		this.r = r;
		this.subproperties = values;
		this.values = new String[values.size()];
		int i = 0;
		for(String s: values.keySet()) this.values[i++] = s;
		finishConstruction();
	}

	@Override
	public boolean isValidValue(String newValue) {
		return subproperties.containsKey(newValue);
	}

	@Override
	public void randomize() {
		value = values[r.nextInt(values.length)];
	}

	@Override
	protected void toString1(StringBuilder sb, String indentString){
		for(Map.Entry<String, Map<String, PropertyTreeNode>> value: subproperties.entrySet()) {
			sb.append(indentString);
			if(value.getKey().equals(this.value)) {
				sb.append(" -");
			} else {
				sb.append("  ");
			}
			sb.append(value.getKey());
			if(value.getValue().isEmpty()) {
				sb.append(": {}\n");
			} else {
				sb.append(": {\n");
				for(PropertyTreeNode subproperty: value.getValue().values()) {
					sb.append(subproperty.toString(indentString.length()+4));
				}
				sb.append(indentString);
				sb.append("  }\n");
			}
		}
	}

	@Override
	public void applyChangePanel() {
		setValue((String)valueMenu.getSelectedItem());
		//TODO: apply all subproperties, or just the ones that are showing?
		for(Map<String, PropertyTreeNode> subpropertyGroup: subproperties.values()) {
			for(PropertyTreeNode subproperty: subpropertyGroup.values()) {
				subproperty.applyChangePanel();
			}
		}
	}

	@Override
	public void refreshChangePanel() {
		super.refreshChangePanel();
		for(Map<String, PropertyTreeNode> subpropertyGroup: subproperties.values()) {
			for(PropertyTreeNode subproperty: subpropertyGroup.values()) {
				subproperty.refreshChangePanel();
			}
		}
		valueMenu.setSelectedItem(value);
		valueMenu.validate();
	}
	
	private static class SmallCardLayout extends CardLayout {

		@Override
	    public Dimension minimumLayoutSize(Container parent) {

	        Component current = findCurrentComponent(parent);
	        if (current != null) {
	            Insets insets = parent.getInsets();
	            Dimension min = current.getMinimumSize();
	            min.width += insets.left + insets.right;
	            min.height += insets.top + insets.bottom;
	            return min;
	        }
	        return super.minimumLayoutSize(parent);
	    }
		
	    @Override
	    public Dimension preferredLayoutSize(Container parent) {

	        Component current = findCurrentComponent(parent);
	        if (current != null) {
	            Insets insets = parent.getInsets();
	            Dimension pref = current.getPreferredSize();
	            pref.width += insets.left + insets.right;
	            pref.height += insets.top + insets.bottom;
	            return pref;
	        }
	        return super.preferredLayoutSize(parent);
	    }

	    public Component findCurrentComponent(Container parent) {
	        for (Component comp : parent.getComponents()) {
	            if (comp.isVisible()) {
	                return comp;
	            }
	        }
	        return null;
	    }

	}

	@Override
	public Map<String, PropertyTreeNode> subproperties() {
		return subproperties.get(value);
	}

	@Override
	protected JComponent createPropertyPanel() {
		valueMenu = new JComboBox(values);
		valueMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((CardLayout)centerPanel.getLayout()).show(centerPanel, (String)valueMenu.getSelectedItem());
				changePanel.revalidate();
				changePanel.repaint();
				if(centerPanel.getSize().width  < centerPanel.getMinimumSize().width  ||
				   centerPanel.getSize().height < centerPanel.getMinimumSize().height) {
					Window w = SwingUtilities.getWindowAncestor(changePanel);
					if(w != null) w.pack();
				}
				//TODO: redo layout of other components
			}
		});
		return valueMenu;
	}

	@Override
	protected JComponent createCenterPanel() {
		centerPanel = new JPanel(new CardLayout());
		for(Map.Entry<String, Map<String, PropertyTreeNode>> subpropertyiesForValue: subproperties.entrySet()) {
			JComponent panel = new Box(BoxLayout.PAGE_AXIS);
			for(PropertyTreeNode p: subpropertyiesForValue.getValue().values()) {
				panel.add(p.getChangePanel());
			}
			if(panel.getComponentCount() > 0) {
				panel.setBorder(BorderFactory.createEtchedBorder());
			}
			JPanel panelWithExtraSpace = new JPanel(new BorderLayout());
			panelWithExtraSpace.add(panel, BorderLayout.BEFORE_FIRST_LINE);
			panelWithExtraSpace.setVisible(false);
			centerPanel.add(panelWithExtraSpace, subpropertyiesForValue.getKey());
		}
		return centerPanel;
	}
}
