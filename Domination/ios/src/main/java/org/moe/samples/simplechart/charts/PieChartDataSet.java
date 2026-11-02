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
import org.moe.samples.simplechart.charts.protocol.IChartValueFormatter;
import org.moe.samples.simplechart.charts.protocol.IPieChartDataSet;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts15PieChartDataSet")
@ObjCClassBinding
public class PieChartDataSet extends ChartDataSet implements IPieChartDataSet {
    static {
        NatJ.register();
    }

    @Generated
    protected PieChartDataSet(Pointer peer) {
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
    public static native PieChartDataSet alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native PieChartDataSet allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyDisableSliceSpacing")
    public native boolean automaticallyDisableSliceSpacing();

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
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    @Generated
    @Selector("drawIconsEnabled")
    public native boolean drawIconsEnabled();

    @Generated
    @Selector("drawValuesEnabled")
    public native boolean drawValuesEnabled();

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
    @Selector("entryLabelColor")
    @Nullable
    public native UIColor entryLabelColor();

    @Generated
    @Selector("entryLabelFont")
    @Nullable
    public native UIFont entryLabelFont();

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
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("highlightColor")
    @Nullable
    public native UIColor highlightColor();

    @Generated
    @Selector("highlightEnabled")
    public native boolean highlightEnabled();

    @Generated
    @Selector("iconsOffset")
    @ByValue
    public native CGPoint iconsOffset();

    @Generated
    @Selector("init")
    public native PieChartDataSet init();

    @Generated
    @Selector("initWithEntries:")
    public native PieChartDataSet initWithEntries(@Nullable NSArray<? extends ChartDataEntry> entries);

    @Generated
    @Selector("initWithEntries:label:")
    public native PieChartDataSet initWithEntriesLabel(@Nullable NSArray<? extends ChartDataEntry> entries,
            @Nullable String label);

    @Generated
    @Selector("initWithLabel:")
    public native PieChartDataSet initWithLabel(@Nullable String label);

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
    @Selector("isDrawIconsEnabled")
    public native boolean isDrawIconsEnabled();

    @Generated
    @Selector("isDrawValuesEnabled")
    public native boolean isDrawValuesEnabled();

    @Generated
    @Selector("isHighlightEnabled")
    public native boolean isHighlightEnabled();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

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
    @Selector("needsFormatter")
    public native boolean needsFormatter();

    @Generated
    @Owned
    @Selector("new")
    public static native PieChartDataSet new_objc();

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
    @Selector("resetColors")
    public native void resetColors();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("selectionShift")
    public native double selectionShift();

    @Generated
    @Selector("setAutomaticallyDisableSliceSpacing:")
    public native void setAutomaticallyDisableSliceSpacing(boolean value);

    @Generated
    @Selector("setColor:")
    public native void setColor(@NotNull UIColor color);

    @Generated
    @Selector("setDrawIconsEnabled:")
    public native void setDrawIconsEnabled(boolean value);

    @Generated
    @Selector("setDrawValuesEnabled:")
    public native void setDrawValuesEnabled(boolean value);

    @Generated
    @Selector("setEntryLabelColor:")
    public native void setEntryLabelColor(@Nullable UIColor value);

    @Generated
    @Selector("setEntryLabelFont:")
    public native void setEntryLabelFont(@Nullable UIFont value);

    @Generated
    @Selector("setHighlightColor:")
    public native void setHighlightColor(@Nullable UIColor value);

    @Generated
    @Selector("setHighlightEnabled:")
    public native void setHighlightEnabled(boolean value);

    @Generated
    @Selector("setIconsOffset:")
    public native void setIconsOffset(@ByValue CGPoint value);

    @Generated
    @Selector("setSelectionShift:")
    public native void setSelectionShift(double value);

    @Generated
    @Selector("setSliceSpace:")
    public native void setSliceSpace(double value);

    @Generated
    @Selector("setUseValueColorForLine:")
    public native void setUseValueColorForLine(boolean value);

    @Generated
    @Selector("setValueFont:")
    public native void setValueFont(@NotNull UIFont value);

    @Generated
    @Selector("setValueFormatter:")
    public native void setValueFormatter(@Mapped(ObjCObjectMapper.class) @Nullable IChartValueFormatter value);

    @Generated
    @Selector("setValueLineColor:")
    public native void setValueLineColor(@Nullable UIColor value);

    @Generated
    @Selector("setValueLinePart1Length:")
    public native void setValueLinePart1Length(double value);

    @Generated
    @Selector("setValueLinePart1OffsetPercentage:")
    public native void setValueLinePart1OffsetPercentage(double value);

    @Generated
    @Selector("setValueLinePart2Length:")
    public native void setValueLinePart2Length(double value);

    @Generated
    @Selector("setValueLineVariableLength:")
    public native void setValueLineVariableLength(boolean value);

    @Generated
    @Selector("setValueLineWidth:")
    public native void setValueLineWidth(double value);

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
    @Selector("setXValuePosition:")
    public native void setXValuePosition(long value);

    @Generated
    @Selector("setYValuePosition:")
    public native void setYValuePosition(long value);

    @Generated
    @Selector("sliceSpace")
    public native double sliceSpace();

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("useValueColorForLine")
    public native boolean useValueColorForLine();

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
    @Selector("valueLineColor")
    @Nullable
    public native UIColor valueLineColor();

    @Generated
    @Selector("valueLinePart1Length")
    public native double valueLinePart1Length();

    @Generated
    @Selector("valueLinePart1OffsetPercentage")
    public native double valueLinePart1OffsetPercentage();

    @Generated
    @Selector("valueLinePart2Length")
    public native double valueLinePart2Length();

    @Generated
    @Selector("valueLineVariableLength")
    public native boolean valueLineVariableLength();

    @Generated
    @Selector("valueLineWidth")
    public native double valueLineWidth();

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
    @Selector("xValuePosition")
    public native long xValuePosition();

    @Generated
    @Selector("yMax")
    public native double yMax();

    @Generated
    @Selector("yMin")
    public native double yMin();

    @Generated
    @Selector("yValuePosition")
    public native long yValuePosition();
}