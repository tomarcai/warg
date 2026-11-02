package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSNumber;
import apple.foundation.NSSet;
import apple.uikit.UIColor;
import apple.uikit.UIFont;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moe.natj.c.ann.FunctionPtr;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.Class;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import org.moe.samples.simplechart.charts.protocol.IChartFillFormatter;
import org.moe.samples.simplechart.charts.protocol.IChartValueFormatter;
import org.moe.samples.simplechart.charts.protocol.ILineChartDataSet;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts16LineChartDataSet")
@ObjCClassBinding
public class LineChartDataSet extends LineRadarChartDataSet implements ILineChartDataSet {
    static {
        NatJ.register();
    }

    @Generated
    protected LineChartDataSet(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Selector("addColor:")
    public native void addColor(@NotNull UIColor color);

    @Generated
    @Selector("addEntry:")
    public native boolean addEntry(@NotNull ChartDataEntry e);

    @Generated
    @Selector("addEntryOrdered:")
    public native boolean addEntryOrdered(@NotNull ChartDataEntry e);

    @Generated
    @Owned
    @Selector("alloc")
    public static native LineChartDataSet alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native LineChartDataSet allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("axisDependency")
    public native long axisDependency();

    @Generated
    @Selector("calcMinMax")
    public native void calcMinMax();

    @Generated
    @Selector("calcMinMaxYFromX:toX:")
    public native void calcMinMaxYFromXToX(double fromX, double toX);

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:")
    public static native void cancelPreviousPerformRequestsWithTarget(
            @Mapped(ObjCObjectMapper.class) @NotNull Object aTarget);

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:selector:object:")
    public static native void cancelPreviousPerformRequestsWithTargetSelectorObject(
            @Mapped(ObjCObjectMapper.class) @NotNull Object aTarget, @NotNull SEL aSelector,
            @Mapped(ObjCObjectMapper.class) @Nullable Object anArgument);

    @Generated
    @Selector("circleColors")
    @NotNull
    public native NSArray<? extends UIColor> circleColors();

    @Generated
    @Selector("circleHoleColor")
    @Nullable
    public native UIColor circleHoleColor();

    @Generated
    @Selector("circleHoleRadius")
    public native double circleHoleRadius();

    @Generated
    @Selector("circleRadius")
    public native double circleRadius();

    @Generated
    @Selector("classFallbacksForKeyedArchiver")
    @NotNull
    public static native NSArray<String> classFallbacksForKeyedArchiver();

    @Generated
    @Selector("classForKeyedUnarchiver")
    @NotNull
    public static native Class classForKeyedUnarchiver();

    @Generated
    @Selector("clear")
    public native void clear();

    @Generated
    @Selector("colorAtIndex:")
    @NotNull
    public native UIColor colorAtIndex(long atIndex);

    @Generated
    @Selector("colors")
    @NotNull
    public native NSArray<? extends UIColor> colors();

    @Generated
    @Selector("contains:")
    public native boolean contains(@NotNull ChartDataEntry e);

    @Generated
    @Owned
    @Selector("copyWithZone:")
    @MappedReturn(ObjCObjectMapper.class)
    @NotNull
    public native Object copyWithZone(@Nullable VoidPtr zone);

    @Generated
    @Selector("cubicIntensity")
    public native double cubicIntensity();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    @Generated
    @Selector("drawCircleHoleEnabled")
    public native boolean drawCircleHoleEnabled();

    @Generated
    @Selector("drawCirclesEnabled")
    public native boolean drawCirclesEnabled();

    @Generated
    @Selector("drawFilledEnabled")
    public native boolean drawFilledEnabled();

    @Generated
    @Selector("drawHorizontalHighlightIndicatorEnabled")
    public native boolean drawHorizontalHighlightIndicatorEnabled();

    @Generated
    @Selector("drawIconsEnabled")
    public native boolean drawIconsEnabled();

    @Generated
    @Selector("drawValuesEnabled")
    public native boolean drawValuesEnabled();

    @Generated
    @Selector("drawVerticalHighlightIndicatorEnabled")
    public native boolean drawVerticalHighlightIndicatorEnabled();

    @Generated
    @Selector("entriesForXValue:")
    @NotNull
    public native NSArray<? extends ChartDataEntry> entriesForXValue(double xValue);

    @Generated
    @Selector("entryCount")
    public native long entryCount();

    @Generated
    @Selector("entryForIndex:")
    @Nullable
    public native ChartDataEntry entryForIndex(long i);

    @Generated
    @Selector("entryForXValue:closestToY:")
    @Nullable
    public native ChartDataEntry entryForXValueClosestToY(double xValue, double yValue);

    @Generated
    @Selector("entryForXValue:closestToY:rounding:")
    @Nullable
    public native ChartDataEntry entryForXValueClosestToYRounding(double xValue, double yValue, long rounding);

    @Generated
    @Selector("entryIndexWithEntry:")
    public native long entryIndexWithEntry(@NotNull ChartDataEntry e);

    @Generated
    @Selector("entryIndexWithX:closestToY:rounding:")
    public native long entryIndexWithXClosestToYRounding(double xValue, double yValue, long rounding);

    @Generated
    @Selector("fill")
    @Nullable
    public native ChartFill fill();

    @Generated
    @Selector("fillAlpha")
    public native double fillAlpha();

    @Generated
    @Selector("fillColor")
    @NotNull
    public native UIColor fillColor();

    @Generated
    @Selector("fillFormatter")
    @MappedReturn(ObjCObjectMapper.class)
    @Nullable
    public native IChartFillFormatter fillFormatter();

    @Generated
    @Selector("form")
    public native long form();

    @Generated
    @Selector("formLineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> formLineDashLengths();

    @Generated
    @Selector("formLineDashPhase")
    public native double formLineDashPhase();

    @Generated
    @Selector("formLineWidth")
    public native double formLineWidth();

    @Generated
    @Selector("formSize")
    public native double formSize();

    @Generated
    @Selector("getCircleColorAtIndex:")
    @Nullable
    public native UIColor getCircleColorAtIndex(long index);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("highlightColor")
    @NotNull
    public native UIColor highlightColor();

    @Generated
    @Selector("highlightEnabled")
    public native boolean highlightEnabled();

    @Generated
    @Selector("highlightLineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> highlightLineDashLengths();

    @Generated
    @Selector("highlightLineDashPhase")
    public native double highlightLineDashPhase();

    @Generated
    @Selector("highlightLineWidth")
    public native double highlightLineWidth();

    @Generated
    @Selector("iconsOffset")
    @ByValue
    public native CGPoint iconsOffset();

    @Generated
    @Selector("init")
    public native LineChartDataSet init();

    @Generated
    @Selector("initWithEntries:")
    public native LineChartDataSet initWithEntries(@Nullable NSArray<? extends ChartDataEntry> entries);

    @Generated
    @Selector("initWithEntries:label:")
    public native LineChartDataSet initWithEntriesLabel(@Nullable NSArray<? extends ChartDataEntry> entries,
            @Nullable String label);

    @Generated
    @Selector("initWithLabel:")
    public native LineChartDataSet initWithLabel(@Nullable String label);

    @Generated
    @Selector("instanceMethodForSelector:")
    @FunctionPtr(name = "call_instanceMethodForSelector_ret")
    public static native NSObject.Function_instanceMethodForSelector_ret instanceMethodForSelector(SEL aSelector);

    @Generated
    @Selector("instanceMethodSignatureForSelector:")
    public static native NSMethodSignature instanceMethodSignatureForSelector(SEL aSelector);

    @Generated
    @Selector("instancesRespondToSelector:")
    public static native boolean instancesRespondToSelector(SEL aSelector);

    @Generated
    @Selector("isDrawCircleHoleEnabled")
    public native boolean isDrawCircleHoleEnabled();

    @Generated
    @Selector("isDrawCirclesEnabled")
    public native boolean isDrawCirclesEnabled();

    @Generated
    @Selector("isDrawFilledEnabled")
    public native boolean isDrawFilledEnabled();

    @Generated
    @Selector("isDrawIconsEnabled")
    public native boolean isDrawIconsEnabled();

    @Generated
    @Selector("isDrawValuesEnabled")
    public native boolean isDrawValuesEnabled();

    @Generated
    @Selector("isHighlightEnabled")
    public native boolean isHighlightEnabled();

    @Generated
    @Selector("isHorizontalHighlightIndicatorEnabled")
    public native boolean isHorizontalHighlightIndicatorEnabled();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("isVerticalHighlightIndicatorEnabled")
    public native boolean isVerticalHighlightIndicatorEnabled();

    @Generated
    @Selector("isVisible")
    public native boolean isVisible();

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Selector("label")
    @Nullable
    public native String label();

    @Generated
    @Selector("lineCapType")
    public native int lineCapType();

    @Generated
    @Selector("lineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> lineDashLengths();

    @Generated
    @Selector("lineDashPhase")
    public native double lineDashPhase();

    @Generated
    @Selector("lineWidth")
    public native double lineWidth();

    @Generated
    @Selector("mode")
    public native long mode();

    @Generated
    @Selector("needsFormatter")
    public native boolean needsFormatter();

    @Generated
    @Owned
    @Selector("new")
    public static native LineChartDataSet new_objc();

    @Generated
    @Selector("notifyDataSetChanged")
    public native void notifyDataSetChanged();

    @Generated
    @Selector("removeEntry:")
    public native boolean removeEntry(@NotNull ChartDataEntry entry);

    @Generated
    @Selector("removeEntryWithIndex:")
    public native boolean removeEntryWithIndex(long index);

    @Generated
    @Selector("removeEntryWithX:")
    public native boolean removeEntryWithX(double x);

    @Generated
    @Selector("removeFirst")
    public native boolean removeFirst();

    @Generated
    @Selector("removeLast")
    public native boolean removeLast();

    @Generated
    @Selector("resetCircleColors:")
    public native void resetCircleColors(long index);

    @Generated
    @Selector("resetColors")
    public native void resetColors();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setCircleColor:")
    public native void setCircleColor(@NotNull UIColor color);

    @Generated
    @Selector("setCircleColors:")
    public native void setCircleColors(@NotNull NSArray<? extends UIColor> value);

    @Generated
    @Selector("setCircleHoleColor:")
    public native void setCircleHoleColor(@Nullable UIColor value);

    @Generated
    @Selector("setCircleHoleRadius:")
    public native void setCircleHoleRadius(double value);

    @Generated
    @Selector("setCircleRadius:")
    public native void setCircleRadius(double value);

    @Generated
    @Selector("setColor:")
    public native void setColor(@NotNull UIColor color);

    @Generated
    @Selector("setCubicIntensity:")
    public native void setCubicIntensity(double value);

    @Generated
    @Selector("setDrawCircleHoleEnabled:")
    public native void setDrawCircleHoleEnabled(boolean value);

    @Generated
    @Selector("setDrawCirclesEnabled:")
    public native void setDrawCirclesEnabled(boolean value);

    @Generated
    @Selector("setDrawFilledEnabled:")
    public native void setDrawFilledEnabled(boolean value);

    @Generated
    @Selector("setDrawHighlightIndicators:")
    public native void setDrawHighlightIndicators(boolean enabled);

    @Generated
    @Selector("setDrawHorizontalHighlightIndicatorEnabled:")
    public native void setDrawHorizontalHighlightIndicatorEnabled(boolean value);

    @Generated
    @Selector("setDrawIconsEnabled:")
    public native void setDrawIconsEnabled(boolean value);

    @Generated
    @Selector("setDrawValuesEnabled:")
    public native void setDrawValuesEnabled(boolean value);

    @Generated
    @Selector("setDrawVerticalHighlightIndicatorEnabled:")
    public native void setDrawVerticalHighlightIndicatorEnabled(boolean value);

    @Generated
    @Selector("setFill:")
    public native void setFill(@Nullable ChartFill value);

    @Generated
    @Selector("setFillAlpha:")
    public native void setFillAlpha(double value);

    @Generated
    @Selector("setFillColor:")
    public native void setFillColor(@NotNull UIColor value);

    @Generated
    @Selector("setFillFormatter:")
    public native void setFillFormatter(@Mapped(ObjCObjectMapper.class) @Nullable IChartFillFormatter value);

    @Generated
    @Selector("setHighlightColor:")
    public native void setHighlightColor(@NotNull UIColor value);

    @Generated
    @Selector("setHighlightEnabled:")
    public native void setHighlightEnabled(boolean value);

    @Generated
    @Selector("setHighlightLineDashLengths:")
    public native void setHighlightLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    @Generated
    @Selector("setHighlightLineDashPhase:")
    public native void setHighlightLineDashPhase(double value);

    @Generated
    @Selector("setHighlightLineWidth:")
    public native void setHighlightLineWidth(double value);

    @Generated
    @Selector("setIconsOffset:")
    public native void setIconsOffset(@ByValue CGPoint value);

    @Generated
    @Selector("setLineCapType:")
    public native void setLineCapType(int value);

    @Generated
    @Selector("setLineDashLengths:")
    public native void setLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    /**
     * This is how much (in pixels) into the dash pattern are we starting from.
     */
    @Generated
    @Selector("setLineDashPhase:")
    public native void setLineDashPhase(double value);

    @Generated
    @Selector("setLineWidth:")
    public native void setLineWidth(double value);

    @Generated
    @Selector("setMode:")
    public native void setMode(long value);

    @Generated
    @Selector("setValueFont:")
    public native void setValueFont(@NotNull UIFont value);

    @Generated
    @Selector("setValueFormatter:")
    public native void setValueFormatter(@Mapped(ObjCObjectMapper.class) @Nullable IChartValueFormatter value);

    @Generated
    @Selector("setValueTextColor:")
    public native void setValueTextColor(@NotNull UIColor value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("setVisible:")
    public native void setVisible(boolean value);

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("valueColors")
    @NotNull
    public native NSArray<? extends UIColor> valueColors();

    @Generated
    @Selector("valueFont")
    @NotNull
    public native UIFont valueFont();

    @Generated
    @Selector("valueFormatter")
    @MappedReturn(ObjCObjectMapper.class)
    @Nullable
    public native IChartValueFormatter valueFormatter();

    @Generated
    @Selector("valueTextColor")
    @NotNull
    public native UIColor valueTextColor();

    @Generated
    @Selector("valueTextColorAt:")
    @NotNull
    public native UIColor valueTextColorAt(long index);

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    @Generated
    @Selector("visible")
    public native boolean visible();

    @Generated
    @Selector("xMax")
    public native double xMax();

    @Generated
    @Selector("xMin")
    public native double xMin();

    @Generated
    @Selector("yMax")
    public native double yMax();

    @Generated
    @Selector("yMin")
    public native double yMin();
}