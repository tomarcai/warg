package org.moe.samples.simplechart.charts.protocol;


import apple.uikit.UIColor;
import apple.uikit.UIFont;
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
@ObjCProtocolSourceName("IPieChartDataSet")
@ObjCProtocolName("_TtP6Charts16IPieChartDataSet_")
public interface IPieChartDataSet extends IChartDataSet {
    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be smaller than the slice spacing itself.
     */
    @Generated
    @Selector("automaticallyDisableSliceSpacing")
    boolean automaticallyDisableSliceSpacing();

    /**
     * the color for the slice-text labels
     */
    @Generated
    @Selector("entryLabelColor")
    @Nullable
    UIColor entryLabelColor();

    /**
     * the font for the slice-text labels
     */
    @Generated
    @Selector("entryLabelFont")
    @Nullable
    UIFont entryLabelFont();

    /**
     * get/sets the color for the highlighted sector
     */
    @Generated
    @Selector("highlightColor")
    @Nullable
    UIColor highlightColor();

    /**
     * indicates the selection distance of a pie slice
     */
    @Generated
    @Selector("selectionShift")
    double selectionShift();

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be smaller than the slice spacing itself.
     */
    @Generated
    @Selector("setAutomaticallyDisableSliceSpacing:")
    void setAutomaticallyDisableSliceSpacing(boolean value);

    /**
     * the color for the slice-text labels
     */
    @Generated
    @Selector("setEntryLabelColor:")
    void setEntryLabelColor(@Nullable UIColor value);

    /**
     * the font for the slice-text labels
     */
    @Generated
    @Selector("setEntryLabelFont:")
    void setEntryLabelFont(@Nullable UIFont value);

    /**
     * get/sets the color for the highlighted sector
     */
    @Generated
    @Selector("setHighlightColor:")
    void setHighlightColor(@Nullable UIColor value);

    /**
     * indicates the selection distance of a pie slice
     */
    @Generated
    @Selector("setSelectionShift:")
    void setSelectionShift(double value);

    /**
     * the space in pixels between the pie-slices
     * <em>default</em>: 0
     * <em>maximum</em>: 20
     */
    @Generated
    @Selector("setSliceSpace:")
    void setSliceSpace(double value);

    /**
     * When valuePosition is OutsideSlice and enabled, line will have the same color as the slice
     */
    @Generated
    @Selector("setUseValueColorForLine:")
    void setUseValueColorForLine(boolean value);

    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    @Generated
    @Selector("setValueLineColor:")
    void setValueLineColor(@Nullable UIColor value);

    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    @Generated
    @Selector("setValueLinePart1Length:")
    void setValueLinePart1Length(double value);

    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    @Generated
    @Selector("setValueLinePart1OffsetPercentage:")
    void setValueLinePart1OffsetPercentage(double value);

    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    @Generated
    @Selector("setValueLinePart2Length:")
    void setValueLinePart2Length(double value);

    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    @Generated
    @Selector("setValueLineVariableLength:")
    void setValueLineVariableLength(boolean value);

    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    @Generated
    @Selector("setValueLineWidth:")
    void setValueLineWidth(double value);

    @Generated
    @Selector("setXValuePosition:")
    void setXValuePosition(long value);

    @Generated
    @Selector("setYValuePosition:")
    void setYValuePosition(long value);

    /**
     * the space in pixels between the pie-slices
     * <em>default</em>: 0
     * <em>maximum</em>: 20
     */
    @Generated
    @Selector("sliceSpace")
    double sliceSpace();

    /**
     * When valuePosition is OutsideSlice and enabled, line will have the same color as the slice
     */
    @Generated
    @Selector("useValueColorForLine")
    boolean useValueColorForLine();

    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    @Generated
    @Selector("valueLineColor")
    @Nullable
    UIColor valueLineColor();

    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    @Generated
    @Selector("valueLinePart1Length")
    double valueLinePart1Length();

    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    @Generated
    @Selector("valueLinePart1OffsetPercentage")
    double valueLinePart1OffsetPercentage();

    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    @Generated
    @Selector("valueLinePart2Length")
    double valueLinePart2Length();

    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    @Generated
    @Selector("valueLineVariableLength")
    boolean valueLineVariableLength();

    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    @Generated
    @Selector("valueLineWidth")
    double valueLineWidth();

    @Generated
    @Selector("xValuePosition")
    long xValuePosition();

    @Generated
    @Selector("yValuePosition")
    long yValuePosition();
}