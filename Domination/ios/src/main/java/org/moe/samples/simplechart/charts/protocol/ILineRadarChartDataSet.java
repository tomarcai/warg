package org.moe.samples.simplechart.charts.protocol;


import apple.uikit.UIColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.ObjCProtocolSourceName;
import org.moe.natj.objc.ann.Selector;
import org.moe.samples.simplechart.charts.ChartFill;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCProtocolSourceName("ILineRadarChartDataSet")
@ObjCProtocolName("_TtP6Charts22ILineRadarChartDataSet_")
public interface ILineRadarChartDataSet extends ILineScatterCandleRadarChartDataSet {
    /**
     * Set to <code>true</code> if the DataSet should be drawn filled (surface), and not just as a line.
     * Disabling this will give great performance boost.
     * Please note that this method uses the path clipping for drawing the filled area (with images, gradients and layers).
     */
    @Generated
    @Selector("drawFilledEnabled")
    boolean drawFilledEnabled();

    /**
     * returns:
     * The object that is used for filling the area below the line.
     * <em>default</em>: nil
     */
    @Generated
    @Selector("fill")
    @Nullable
    ChartFill fill();

    /**
     * The alpha value that is used for filling the line surface.
     * <em>default</em>: 0.33
     */
    @Generated
    @Selector("fillAlpha")
    double fillAlpha();

    /**
     * The color that is used for filling the line surface area.
     */
    @Generated
    @Selector("fillColor")
    @NotNull
    UIColor fillColor();

    /**
     * <code>true</code> if filled drawing is enabled, <code>false</code> if not
     */
    @Generated
    @Selector("isDrawFilledEnabled")
    boolean isDrawFilledEnabled();

    /**
     * line width of the chart (min = 0.0, max = 10)
     * <em>default</em>: 1
     */
    @Generated
    @Selector("lineWidth")
    double lineWidth();

    /**
     * Set to <code>true</code> if the DataSet should be drawn filled (surface), and not just as a line.
     * Disabling this will give great performance boost.
     * Please note that this method uses the path clipping for drawing the filled area (with images, gradients and layers).
     */
    @Generated
    @Selector("setDrawFilledEnabled:")
    void setDrawFilledEnabled(boolean value);

    /**
     * returns:
     * The object that is used for filling the area below the line.
     * <em>default</em>: nil
     */
    @Generated
    @Selector("setFill:")
    void setFill(@Nullable ChartFill value);

    /**
     * The alpha value that is used for filling the line surface.
     * <em>default</em>: 0.33
     */
    @Generated
    @Selector("setFillAlpha:")
    void setFillAlpha(double value);

    /**
     * The color that is used for filling the line surface area.
     */
    @Generated
    @Selector("setFillColor:")
    void setFillColor(@NotNull UIColor value);

    /**
     * line width of the chart (min = 0.0, max = 10)
     * <em>default</em>: 1
     */
    @Generated
    @Selector("setLineWidth:")
    void setLineWidth(double value);
}