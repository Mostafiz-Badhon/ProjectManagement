import javax.swing.JTree;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.awt.Color;

public class RedNodeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean isLeaf, int row,
                                                  boolean hasFocus) {

        JComponent component = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus);
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        if (treeNode.getUserObject() instanceof Node) {
            Node nodeData = (Node) treeNode.getUserObject();
            if (nodeData.isCritical()) {
                component.setForeground(Color.RED);
            }
        }
        return component;
    }
}
