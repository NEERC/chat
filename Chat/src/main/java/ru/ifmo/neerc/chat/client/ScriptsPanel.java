package ru.ifmo.neerc.chat.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

public class ScriptsPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger("ScriptsPanel");

    private JTable list;
    private ScriptsModel model;
    private TableRowSorter sorter;
    private ScriptFilter filter;
    private JTextField searchField;

    private ChatInputArea inputArea;

    public ScriptsPanel(ChatInputArea inputArea) {
        super(new BorderLayout());

        this.inputArea = inputArea;

        add(createToolBar(), BorderLayout.PAGE_START);
        add(new JScrollPane(createList()), BorderLayout.CENTER);
        add(createSearchField(), BorderLayout.PAGE_END);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(new AddAction());
        toolBar.add(new DeleteAction());
        toolBar.add(new ModifyAction());
        toolBar.add(new ReloadAction());

        return toolBar;
    }

    private JTextField createSearchField() {
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }
        });

        return searchField;
    }

    private JTable createList() {
        model = new ScriptsModel();
        model.load();

        list = new JTable(model);
        list.setTableHeader(null);
        list.setDefaultRenderer(Object.class, new ScriptRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2)
                    return;

                int index = list.getSelectedRow();
                if (index == -1)
                    return;

                inputArea.setText(model.get(index));
            }
        });

        sorter = new TableRowSorter(list.getModel());
        filter = new ScriptFilter();
        sorter.setRowFilter(filter);
        list.setRowSorter(sorter);

        return list;
    }

    private void updateFilter() {
        filter.setFilter(searchField.getText());
        sorter.sort();
    }

    protected void updateRowHeights(int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            int rowHeight = 0;

            for (int column = 0; column < list.getColumnCount(); column++) {
                Component comp = list.prepareRenderer(list.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }

            if (rowHeight != list.getRowHeight(row)) {
                list.setRowHeight(row, rowHeight);
            }
        }
    }

    private class ScriptsModel extends AbstractTableModel {
        private final String SCRIPTS_FILENAME = "scripts.yaml";

        private List<String> scripts = new ArrayList<>();

        public ScriptsModel() {
            addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(final TableModelEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateRowHeights(e.getFirstRow(), e.getLastRow());
                        }
                    });
                }
            });
        }

        public void add(String script) {
            scripts.add(script);
            save();
            fireTableRowsInserted(scripts.size() - 1, scripts.size() - 1);
        }

        public void delete(int index) {
            scripts.remove(index);
            save();
            fireTableRowsDeleted(index, index);
        }

        public void modify(int index, String script) {
            scripts.set(index, script);
            save();
            fireTableRowsUpdated(index, index);
        }

        public String get(int index) {
            return scripts.get(index);
        }

        public void load() {
            try {
                Yaml yaml = new Yaml();
                FileInputStream stream = new FileInputStream(SCRIPTS_FILENAME);
                scripts = yaml.loadAs(stream, scripts.getClass());
                fireTableDataChanged();
            } catch (FileNotFoundException e) {
                LOG.debug("Failed to load scripts");
            }
        }

        public void save() {
            try {
                Yaml yaml = new Yaml();
                FileWriter writer = new FileWriter(SCRIPTS_FILENAME);
                yaml.dump(scripts, writer);
            } catch (IOException e) {
                LOG.error("Failed to save scripts", e);
            }
        }

        @Override
        public int getRowCount() {
            return scripts.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return scripts.get(row);
        }
    }

    private class ScriptFilter extends RowFilter {
        private String filter = "";

        public void setFilter(String filter) {
            this.filter = filter.toLowerCase();
        }

        @Override
        public boolean include(Entry entry) {
            for (int i = 0; i < entry.getValueCount(); i++) {
                String value = entry.getStringValue(i).toLowerCase();
                if (value.contains(filter))
                    return true;
            }
            return false;
        }
    }

    private class ScriptRenderer extends JTextArea implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((String) value);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            TableColumnModel columnModel = table.getColumnModel();
            setSize(columnModel.getColumn(column).getWidth(), Integer.MAX_VALUE);

            return this;
        }
    }

    private class AddAction extends AbstractAction {
        public AddAction() {
            super("Add", new ImageIcon(ScriptsPanel.class.getResource("res/task_add.gif")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String script = inputArea.getText().trim();
            if (!script.isEmpty())
                model.add(script);
        }
    }

    private class DeleteAction extends AbstractAction {
        public DeleteAction() {
            super("Delete", new ImageIcon(ScriptsPanel.class.getResource("res/task_remove.gif")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedRow();
            if (index != -1)
                model.delete(list.convertRowIndexToModel(index));
        }
    }

    private class ModifyAction extends AbstractAction {
        public ModifyAction() {
            super("Modify", new ImageIcon(ScriptsPanel.class.getResource("res/btn_modify.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedRow();
            if (index != -1)
                model.modify(list.convertRowIndexToModel(index), inputArea.getText());
        }
    }

    private class ReloadAction extends AbstractAction {
        public ReloadAction() {
            super("Reload", new ImageIcon(ScriptsPanel.class.getResource("res/btn_reload.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.load();
        }
    }
}
