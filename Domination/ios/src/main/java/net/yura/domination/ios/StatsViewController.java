package net.yura.domination.ios;

import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.StatType;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.mobile.gui.Application;
import apple.corefoundation.struct.CGRect;
import apple.corefoundation.struct.CGSize;
import apple.coregraphics.c.CoreGraphics;
import apple.foundation.NSArray;
import apple.foundation.NSDictionary;
import apple.foundation.NSMutableArray;
import apple.foundation.NSNumber;
import apple.foundation.NSProcessInfo;
import apple.foundation.struct.NSOperatingSystemVersion;
import apple.uikit.NSLayoutConstraint;
import apple.uikit.UIAction;
import apple.uikit.UIBarButtonItem;
import apple.uikit.UIColor;
import apple.uikit.UIFont;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.NFloat;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Selector;
import apple.uikit.UIGraphicsImageRenderer;
import apple.uikit.UIGraphicsImageRendererContext;
import apple.uikit.UIGraphicsImageRendererFormat;
import apple.uikit.UIImage;
import apple.uikit.UIMenu;
import apple.uikit.UINavigationBarAppearance;
import apple.uikit.UINavigationController;
import apple.uikit.UIPickerView;
import apple.uikit.UITextField;
import apple.uikit.UIToolbar;
import apple.uikit.UIViewController;
import apple.uikit.c.UIKit;
import apple.uikit.enums.UIBarButtonItemStyle;
import apple.uikit.enums.UIBarButtonSystemItem;
import apple.uikit.enums.UIBarStyle;
import apple.uikit.protocol.UIPickerViewDataSource;
import apple.uikit.protocol.UIPickerViewDelegate;
import apple.uikit.struct.UIEdgeInsets;
import org.moe.samples.simplechart.charts.ChartDataEntry;
import org.moe.samples.simplechart.charts.ChartViewBase;
import org.moe.samples.simplechart.charts.ChartXAxis;
import org.moe.samples.simplechart.charts.ChartYAxis;
import org.moe.samples.simplechart.charts.LineChartData;
import org.moe.samples.simplechart.charts.LineChartDataSet;
import org.moe.samples.simplechart.charts.LineChartView;
import org.moe.samples.simplechart.charts.enums.XAxisLabelPosition;
import org.moe.samples.simplechart.charts.protocol.ChartViewDelegate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * info about Charts versions: https://github.com/CocoaPods/Specs/tree/master/Specs/5/1/e/Charts
 */
@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("StatsViewController")
public class StatsViewController extends UIViewController implements ChartViewDelegate, UIAction.Block_actionWithTitleImageIdentifierHandler, UIPickerViewDataSource, UIPickerViewDelegate {

    /**
     * This is when apple introduced simple overflow menus
     */
    private static final NSOperatingSystemVersion IOS_14 = new NSOperatingSystemVersion(14, 0, 0);

    private static final double ICON_SIZE = 16;

    private StatType initialView = StatType.COUNTRIES;

    private long oldBarStyle;

    private final ResourceBundle resb = TranslationBundle.getBundle();

    private LineChartView lineChartView;

    @Owned
    @Selector("alloc")
    public static native StatsViewController alloc();

    @Selector("init")
    public native StatsViewController init();

    protected StatsViewController(Pointer peer) {
        super(peer);
    }

    public static List<Player> getPlayersStats() {
        DominationMain dmain = (DominationMain) Application.getInstance();
        RiskGame game = dmain.risk.getGame();
        // if we open the stats activity at the same time as closing the game, avoid throwing a error
        return game == null ? Collections.EMPTY_LIST : game.getPlayersStats();
    }

    private UIPickerView picker;
    private UITextField pickerViewTextField;

    public void setGraph(String graph) {
        initialView = StatType.valueOf(graph);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void viewDidLoad() {
        super.viewDidLoad();

        if (NSProcessInfo.processInfo().isOperatingSystemAtLeastVersion(IOS_14)) {
            NSMutableArray<UIAction> items = (NSMutableArray<UIAction>) NSMutableArray.arrayWithCapacity(StatType.values().length);
            for (StatType statType : StatType.values()) {
                // UIAction is iOS 13, though does not actually seem to work in iOS 13
                items.add(UIAction.actionWithTitleImageIdentifierHandler(
                        resb.getString("swing.toolbar." + statType.getName()),
                        null,
                        statType.name(),
                        this
                ));
            }
            // initWithImageMenu is iOS 14 ("ellipsis" was added in iOS 13)
            navigationItem().setRightBarButtonItem(UIBarButtonItem.alloc().initWithImageMenu(UIImage.systemImageNamed("ellipsis"), UIMenu.menuWithChildren(items)));
        }
        else {
            // from: https://jslim.net/blog/2013/10/16/bring-up-uipickerview-when-clicked-on-uibutton/

            picker = UIPickerView.alloc().init();
            picker.setDataSource(this);
            picker.setDelegate(this);

            pickerViewTextField = UITextField.alloc().initWithFrame(CoreGraphics.CGRectZero());
            view().addSubview(pickerViewTextField);
            pickerViewTextField.setInputView(picker);

            // add a toolbar with Cancel & Done button
            UIToolbar toolBar = UIToolbar.alloc().init();
            UIBarButtonItem doneButton = UIBarButtonItem.alloc().initWithBarButtonSystemItemTargetAction(UIBarButtonSystemItem.Done, this, new SEL("doneTouched:"));
            UIBarButtonItem cancelButton = UIBarButtonItem.alloc().initWithBarButtonSystemItemTargetAction(UIBarButtonSystemItem.Cancel, this, new SEL("cancelTouched:"));

            // the middle button is to make the Done button align to right
            toolBar.setItems((NSArray<UIBarButtonItem>)NSArray.arrayWithObjects(cancelButton, UIBarButtonItem.alloc().initWithBarButtonSystemItemTargetAction(UIBarButtonSystemItem.FlexibleSpace, null, null), doneButton, null));
            toolBar.sizeToFit();
            pickerViewTextField.setInputAccessoryView(toolBar);

            navigationItem().setRightBarButtonItem(UIBarButtonItem.alloc().initWithTitleStyleTargetAction("\u22EF", UIBarButtonItemStyle.Plain, this, new SEL("optionsMenuClicked:")));
        }

        // This seems to crash on some Simulators???? but works on real device
        // https://github.com/multi-os-engine/multi-os-engine/issues/190
        lineChartView = LineChartView.alloc().init();
        lineChartView.setDelegate(this);
        view().addSubview(lineChartView);

        lineChartView.setTranslatesAutoresizingMaskIntoConstraints(false);

        try {
            lineChartView.bottomAnchor().constraintEqualToAnchor(view().safeAreaLayoutGuide().bottomAnchor()).setActive(true);
            lineChartView.topAnchor().constraintEqualToAnchor(view().topAnchor()).setActive(true); // for top we setExtraTopOffset instead
            lineChartView.rightAnchor().constraintEqualToAnchor(view().safeAreaLayoutGuide().rightAnchor()).setActive(true);
            lineChartView.leftAnchor().constraintEqualToAnchor(view().safeAreaLayoutGuide().leftAnchor()).setActive(true);
        }
        catch (Throwable th) {
            // fallback for older iOS 9
            // from: https://github.com/multi-os-engine/moe-samples-java/blob/moe-master/Planets/ios/src/main/java/org/moe/samples/planets/ios/PlanetsController.java
            NSDictionary views = NSDictionary.dictionaryWithObjectForKey(lineChartView, "renderer");
            NSArray constrs = NSLayoutConstraint.constraintsWithVisualFormatOptionsMetricsViews(
                    "|-0-[renderer]-0-|", 0, (NSDictionary<String, Object>) NSDictionary.dictionary(), views);
            view().addConstraints(constrs);
            constrs = NSLayoutConstraint.constraintsWithVisualFormatOptionsMetricsViews(
                    "V:|-0-[renderer]-0-|", 0, (NSDictionary<String, Object>) NSDictionary.dictionary(), views);
            view().addConstraints(constrs);
        }

        lineChartView.setBackgroundColor(UIColor.blackColor());
        lineChartView.legend().setTextColor(UIColor.whiteColor());
        lineChartView.setDragEnabled(true);
        lineChartView.setScaleEnabled(true);
        lineChartView.setPinchZoomEnabled(true);
        lineChartView.setDrawGridBackgroundEnabled(false);

        ChartXAxis xAxis = lineChartView.xAxis();
        xAxis.setGridLineDashLengths(arrayOfFloats(10.0f, 10.0f));
        xAxis.setGridLineDashPhase(0f);
        xAxis.setLabelTextColor(UIColor.whiteColor());
        xAxis.setLabelPosition(XAxisLabelPosition.Bottom);
        xAxis.setAxisMinimum(0d);
        xAxis.setGranularity(1D);
        xAxis.setLabelCount(25);
        // TODO we want to add a label to the axis chartXAxisLabel: "Turn Number"

        ChartYAxis yAxis = lineChartView.leftAxis();
        yAxis.setAxisMinimum(0.0);
        yAxis.setGridLineDashLengths(arrayOfFloats(5.0f, 5.0f));
        yAxis.setDrawZeroLineEnabled(false);
        yAxis.setDrawLimitLinesBehindDataEnabled(true);
        yAxis.setLabelTextColor(UIColor.whiteColor());
        yAxis.setGranularity(1D);
        yAxis.setLabelCount(25);

        lineChartView.rightAxis().setEnabled(false);
        lineChartView.setAutoScaleMinMaxEnabled(true);

        // we HAVE to call this here, otherwise we may end up calling it too many times
        // as apple will call viewWillAppear multiple times as the view comes in and out of view
        // such as when you swipe to go back then change your mind and we do NOT want to keep resetting the graph
        setData(initialView);
    }

    /**
     * @see net.yura.ios.AppController#prefersStatusBarHidden()
     */
    @Override
    public boolean prefersStatusBarHidden() {
        return Boolean.getBoolean("isMacCatalystApp");
    }

    @Override
    public void viewSafeAreaInsetsDidChange() {
        super.viewSafeAreaInsetsDidChange();
        UIEdgeInsets safeAreaInsets = view().safeAreaInsets(); // {64, 0, 0, 0}
        lineChartView.setExtraTopOffset(safeAreaInsets.top());
    }

    @Override
    public void viewWillAppear(boolean animated) {
        super.viewWillAppear(animated);
        UINavigationController navigationController = navigationController();

        // even though this is a newer API, it does not seem to always work
        // on iPhone SE, it works the first time, but the 2nd time you launch this controller the title color is wrong?!
        // This issue ONLY happens on a real device and not on the simulator?!
        //navigationController.setOverrideUserInterfaceStyle(UIUserInterfaceStyle.Dark); // API ONLY iOS 13

        // new way of setting theme for navigation bar, needed as the old way breaks when back is cancelled (slow swipe from edge then)
        try {
            if (NSProcessInfo.processInfo().isOperatingSystemAtLeastVersion(IOS_14)) {
                UINavigationBarAppearance appearance = UINavigationBarAppearance.alloc().init();
                appearance.configureWithOpaqueBackground();
                appearance.setBackgroundColor(UIColor.blackColor());
                appearance.setTitleTextAttributes((NSDictionary<String, ?>) NSDictionary.dictionaryWithObjectForKey(UIColor.whiteColor(), UIKit.NSForegroundColorAttributeName()));
                navigationItem().setStandardAppearance(appearance); // iOS-13
                navigationItem().setScrollEdgeAppearance(appearance); // iOS-13
            }
        }
        catch (Throwable th) {
            th.printStackTrace();
        }

        // old way of setting theme for navigation bar
        oldBarStyle = navigationController.navigationBar().barStyle();
        navigationController.navigationBar().setBarStyle(UIBarStyle.Black);

        // here navigationController gets status bar color from child
        try {
            navigationController.setNavigationBarHiddenAnimated(false, animated);
        }
        catch (Throwable th) {
            // we seem to get crashes sometimes, so maybe this fallback will help
            navigationController.navigationBar().setHidden(false);
        }
        // here navigationController gets status bar color from itself
    }

    @Override
    public void viewWillDisappear(boolean animated) {
        navigationController().navigationBar().setBarStyle(oldBarStyle);
        super.viewWillDisappear(animated);
    }

    @Override
    public void call_actionWithTitleImageIdentifierHandler(UIAction action) {
        try {
            setData(StatType.valueOf(action.identifier()));
        }
        catch (Throwable ex) {
            Logger.getLogger(StatsViewController.class.getName()).log(Level.WARNING, "error setting data in graph " + action, ex);
        }
    }

    @Selector("optionsMenuClicked:")
    public void optionsMenuClicked() {
        pickerViewTextField.becomeFirstResponder();
    }

    @Selector("doneTouched:")
    public void doneTouched() {
        pickerViewTextField.resignFirstResponder();

        setData(StatType.values()[(int)picker.selectedRowInComponent(0)]);
    }

    @Selector("cancelTouched:")
    public void cancelTouched() {
        pickerViewTextField.resignFirstResponder();
    }

    @Override
    public long numberOfComponentsInPickerView(UIPickerView pickerView) {
        return 1;
    }

    @Override
    public long pickerViewNumberOfRowsInComponent(UIPickerView pickerView, long component) {
        return StatType.values().length;
    }

    @Override
    public String pickerViewTitleForRowForComponent(UIPickerView pickerView, long row, long component) {
        return resb.getString("swing.toolbar." + StatType.values()[(int)row].getName());
    }

    private void setData(StatType statType) {
        setTitle(resb.getString("swing.tab.statistics") + " - " + resb.getString("swing.toolbar." + statType.getName()));

        List<Player> players = getPlayersStats();
        NSMutableArray<LineChartDataSet> dataSets = (NSMutableArray<LineChartDataSet>)NSMutableArray.arrayWithCapacity(players.size());
        for (Player player : players) {
            dataSets.add(getLineChartDataSet(player, statType));
        }
        LineChartData data = LineChartData.alloc().initWithDataSets(dataSets);
        lineChartView.setData(data);
    }

    private LineChartDataSet getLineChartDataSet(Player player, StatType statType) {

        UIColor playerColor = Graphics.getColor(player.getColor());
        double[] stats = player.getStatistics(statType);

        final Image img = PicturePanel.getIconForColor(player.getColor());
        UIImage icon = null;

        if (img != null) {
            if (img.getWidth() > ICON_SIZE && img.getHeight() > ICON_SIZE) {
                double scale = Math.max(ICON_SIZE / img.getWidth(), ICON_SIZE / img.getHeight());
                icon = imageScaledToSize(img.getUIImage(), (int)(img.getWidth() * scale), (int)(img.getHeight() * scale));
            }
            else {
                icon = img.getUIImage();
            }
        }

        NSMutableArray<ChartDataEntry> values = (NSMutableArray<ChartDataEntry>)NSMutableArray.arrayWithCapacity(stats.length);
        double newPoint=0;
        for (int i = 0; i < stats.length; ++i) {

            if (statType.isSummable()) {
                newPoint += stats[i];
            }
            else {
                newPoint = stats[i];
            }

            if (!Double.isNaN(newPoint)) { // can not be set for dice, if no dice results are present for a turn
                ChartDataEntry entry = ChartDataEntry.alloc().initWithXY(i + 1, newPoint);
                if (icon != null) {
                    entry.setIcon(icon);
                }
                values.add(entry);
            }
        }

        LineChartDataSet set1 = LineChartDataSet.alloc().initWithEntriesLabel(values, MiniUtil.getStatsLabel(statType, player));

        if (player.getColor() == ColorUtil.BLACK) {
            set1.setColor(UIColor.whiteColor());
            set1.setCircleColor(UIColor.whiteColor());
            set1.setCircleRadius(5.0);
            set1.setDrawCircleHoleEnabled(true);
            set1.setCircleHoleRadius(3.0);
            set1.setCircleHoleColor(playerColor);
            set1.setLineDashLengths(arrayOfFloats(5.0f, 2.5f));
        }
        else {
            set1.setColor(playerColor);
            set1.setCircleColor(playerColor);
            set1.setCircleRadius(3.0);
            set1.setDrawCircleHoleEnabled(false);
        }

        set1.setHighlightLineDashLengths(arrayOfFloats(5.0f, 2.5f));
        set1.setLineWidth(1.0);
        set1.setValueFont(UIFont.systemFontOfSize(9.f));
        set1.setDrawIconsEnabled(img != null);
        set1.setDrawValuesEnabled(false);

        //set1.setFormLineDashLengths(arrayOfFloats(5.0f, 2.5f));
        set1.setFormLineWidth(1.0);
        set1.setFormSize(15.0);

        return set1;
    }

    private static UIImage imageScaledToSize(UIImage img, int width, int height) {
        UIGraphicsImageRendererFormat format = UIGraphicsImageRendererFormat.alloc().init();
        format.setScale(img.scale());
        format.setOpaque(false);
        CGSize newSize = new CGSize(width, height);
        UIGraphicsImageRenderer render = UIGraphicsImageRenderer.alloc().initWithSizeFormat(newSize, format);
        return render.imageWithActions(new UIGraphicsImageRenderer.Block_imageWithActions() {
            @Override
            public void call_imageWithActions(UIGraphicsImageRendererContext rendererContext) {
                //CoreGraphics.CGContextDrawImage(rendererContext.CGContext(), new CGRect(CoreGraphics.CGPointZero(), newSize), img.CGImage());
                img.drawInRect(new CGRect(CoreGraphics.CGPointZero(), newSize));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static NSArray<NSNumber> arrayOfFloats(float... values) {
        NSMutableArray array = NSMutableArray.alloc().initWithCapacity(values.length);
        for (float value : values) {
            array.add(NSNumber.alloc().initWithFloat(value));
        }
        return array;
    }

    @Override
    public void chartTranslatedDXDY(ChartViewBase chartView, @NFloat double dX, @NFloat double dY) {
        System.out.println("translated -> " + dX + ":" + dY);
    }
}
