package net.yura.domination.mobile.flashgui;

import java.util.Arrays;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.celleditor.TableCellEditor;
import net.yura.mobile.gui.cellrenderer.ListCellRenderer;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.FileChooser;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Table;
import net.yura.mobile.gui.layout.XULLoader;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.JavaCompatUtil;

public class ColorPicker extends FileChooser.GridList implements ActionListener {

    public enum PlayerColor {
        PINK(ColorUtil.PINK, "pink"),
        RED(ColorUtil.RED, "red"),
        ORANGE(ColorUtil.ORANGE, "orange"),
        YELLOW(ColorUtil.YELLOW, "yellow"),
        GREEN(ColorUtil.GREEN, "green"),
        CYAN(ColorUtil.CYAN, "cyan"),
        BLUE(ColorUtil.BLUE, "blue"),
        MAGENTA(ColorUtil.MAGENTA, "magenta"),

        WHITE(ColorUtil.WHITE, "white"),
        LTGRAY(ColorUtil.LIGHT_GRAY, "lightgray"),
        //GRAY(ColorUtil.GRAY, "gray"),
        DKGRAY(ColorUtil.DARK_GRAY, "darkgray"),
        BLACK(ColorUtil.BLACK, "black");

        public final int rgb;
        public final String name;
        PlayerColor(int rgb, String name) {
            this.rgb = rgb;
            this.name = name;
        }
    }

    class ColorButton extends Button implements ListCellRenderer, TableCellEditor {

        public ColorButton() {
            setName("ColorButton");
        }

        @Override
        public Component getListCellRendererComponent(Component listOrTable, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PlayerColor playerColor = (PlayerColor)value;
            setBackground(playerColor.rgb);
            setToolTipText(playerColor.name);

            Image image = PicturePanel.getIconForColor(playerColor.rgb);
            setIcon(image == null ? null : new Icon(image));

            return this;
        }

        private Object value;

        @Override
        public Component getTableCellEditorComponent(Table table, Object value, boolean isSelected, int row, int column) {
            this.value = value;
            return getListCellRendererComponent(table, value, 0, isSelected, false);
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }

    public static final String CMD_OK = "ok";

    private ActionListener listener;
    private Frame dialog;

    public ColorPicker() {
        super(XULLoader.adjustSizeToDensity(75));
        setPreferredSize(XULLoader.adjustSizeToDensity(75) * 4, -1);
        setListData(JavaCompatUtil.asVector(Arrays.asList(PlayerColor.values())));
        setDefaultRenderer(PlayerColor.class, new ColorButton());
        ColorButton button = new ColorButton();
        button.addActionListener(this);
        button.setActionCommand(CMD_OK);
        setDefaultEditor(PlayerColor.class, button);
    }

    public int getSelectedColor() {
        PlayerColor playerColor = (PlayerColor)getSelectedValue();
        return playerColor.rgb;
    }

    @Override
    public void actionPerformed(String actionCommand) {
        dialog.setVisible(false);
        dialog = null;
        listener.actionPerformed(actionCommand);
    }

    public void showDialog(ActionListener listener, String name) {
        this.listener = listener;

        dialog = new Frame();
        dialog.setTitle(TranslationBundle.getBundle().getString("newgame.label.color") + " - " + name);
        dialog.setCloseOnFocusLost(true);
        dialog.setName("Dialog");
        dialog.getTitlePane().setName("InternalFrameTitlePane");

        // this is needed so back button works on android
        Button cancel = new Button( (String)DesktopPane.get("cancelText") );
        cancel.setActionCommand(Frame.CMD_CLOSE);
        cancel.addActionListener(dialog.getTitlePane());
        cancel.setMnemonic(KeyEvent.KEY_END);
        dialog.addCommand(cancel);

        dialog.add(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
