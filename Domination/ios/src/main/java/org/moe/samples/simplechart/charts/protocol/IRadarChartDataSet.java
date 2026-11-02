package org.moe.samples.simplechart.charts.protocol;


import apple.uikit.UIColor;
import org.jetbrains.annotations.Nullable;
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
@ObjCProtocolSourceName("IRadarChartDataSet")
@ObjCProtocolName("_TtP6Charts18IRadarChartDataSet_")
public interface IRadarChartDataSet extends ILineRadarChartDataSet {
    /**
     * flag indicating whether highlight circle should be drawn or not
     */
    @Generated
    @Selector("drawHighlightCircleEnabled")
    boolean drawHighlightCircleEnabled();

    @Generated
    @Selector("highlightCircleFillColor")
    @Nullable
    UIColor highlightCircleFillColor();

    @Generated
    @Selector("highlightCircleInnerRadius")
    double highlightCircleInnerRadius();

    @Generated
    @Selector("highlightCircleOuterRadius")
    double highlightCircleOuterRadius();

    @Generated
    @Selector("highlightCircleStrokeAlpha")
    double highlightCircleStrokeAlpha();

    /**
     * The stroke color for highlight circle.
     * If <code>nil</code>, the color of the dataset is taken.
     */
    @Generated
    @Selector("highlightCircleStrokeColor")
    @Nullable
    UIColor highlightCircleStrokeColor();

    @Generated
    @Selector("highlightCircleStrokeWidth")
    double highlightCircleStrokeWidth();

    @Generated
    @Selector("isDrawHighlightCircleEnabled")
    boolean isDrawHighlightCircleEnabled();

    /**
     * flag indicating whether highlight circle should be drawn or not
     */
    @Generated
    @Selector("setDrawHighlightCircleEnabled:")
    void setDrawHighlightCircleEnabled(boolean value);

    @Generated
    @Selector("setHighlightCircleFillColor:")
    void setHighlightCircleFillColor(@Nullable UIColor value);

    @Generated
    @Selector("setHighlightCircleInnerRadius:")
    void setHighlightCircleInnerRadius(double value);

    @Generated
    @Selector("setHighlightCircleOuterRadius:")
    void setHighlightCircleOuterRadius(double value);

    @Generated
    @Selector("setHighlightCircleStrokeAlpha:")
    void setHighlightCircleStrokeAlpha(double value);

    /**
     * The stroke color for highlight circle.
     * If <code>nil</code>, the color of the dataset is taken.
     */
    @Generated
    @Selector("setHighlightCircleStrokeColor:")
    void setHighlightCircleStrokeColor(@Nullable UIColor value);

    @Generated
    @Selector("setHighlightCircleStrokeWidth:")
    void setHighlightCircleStrokeWidth(double value);
}