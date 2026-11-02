package org.moe.samples.simplechart.charts.protocol;


import apple.foundation.NSArray;
import apple.uikit.UIColor;
import org.jetbrains.annotations.NotNull;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.ObjCProtocolSourceName;
import org.moe.natj.objc.ann.Selector;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCProtocolSourceName("IBarChartDataSet")
@ObjCProtocolName("_TtP6Charts16IBarChartDataSet_")
public interface IBarChartDataSet extends IBarLineScatterCandleBubbleChartDataSet {
    /**
     * the color drawing borders around the bars.
     */
    @Generated
    @Selector("barBorderColor")
    @NotNull
    UIColor barBorderColor();

    /**
     * the width used for drawing borders around the bars. If borderWidth == 0, no border will be drawn.
     */
    @Generated
    @Selector("barBorderWidth")
    double barBorderWidth();

    /**
     * the color used for drawing the bar-shadows. The bar shadows is a surface behind the bar that indicates the maximum value
     */
    @Generated
    @Selector("barShadowColor")
    @NotNull
    UIColor barShadowColor();

    /**
     * the alpha value (transparency) that is used for drawing the highlight indicator bar. min = 0.0 (fully transparent), max = 1.0 (fully opaque)
     */
    @Generated
    @Selector("highlightAlpha")
    double highlightAlpha();

    /**
     * <code>true</code> if this DataSet is stacked (stacksize > 1) or not.
     */
    @Generated
    @Selector("isStacked")
    boolean isStacked();

    /**
     * the color drawing borders around the bars.
     */
    @Generated
    @Selector("setBarBorderColor:")
    void setBarBorderColor(@NotNull UIColor value);

    /**
     * the width used for drawing borders around the bars. If borderWidth == 0, no border will be drawn.
     */
    @Generated
    @Selector("setBarBorderWidth:")
    void setBarBorderWidth(double value);

    /**
     * the color used for drawing the bar-shadows. The bar shadows is a surface behind the bar that indicates the maximum value
     */
    @Generated
    @Selector("setBarShadowColor:")
    void setBarShadowColor(@NotNull UIColor value);

    /**
     * the alpha value (transparency) that is used for drawing the highlight indicator bar. min = 0.0 (fully transparent), max = 1.0 (fully opaque)
     */
    @Generated
    @Selector("setHighlightAlpha:")
    void setHighlightAlpha(double value);

    /**
     * array of labels used to describe the different values of the stacked bars
     */
    @Generated
    @Selector("setStackLabels:")
    void setStackLabels(@NotNull NSArray<String> value);

    /**
     * array of labels used to describe the different values of the stacked bars
     */
    @Generated
    @Selector("stackLabels")
    @NotNull
    NSArray<String> stackLabels();

    /**
     * The maximum number of bars that can be stacked upon another in this DataSet.
     */
    @Generated
    @Selector("stackSize")
    long stackSize();
}