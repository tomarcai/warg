package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGSize;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSNumber;
import apple.foundation.NSSet;
import apple.foundation.NSValue;
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
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.Class;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartLegend extends ChartComponentBase {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartLegend(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartLegend alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartLegend allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("calculateDimensionsWithLabelFont:viewPortHandler:")
    public native void calculateDimensionsWithLabelFontViewPortHandler(@NotNull UIFont labelFont,
            @NotNull ChartViewPortHandler viewPortHandler);

    @Generated
    @Selector("calculatedLabelBreakPoints")
    @NotNull
    public native NSArray<? extends NSNumber> calculatedLabelBreakPoints();

    @Generated
    @Selector("calculatedLabelSizes")
    @NotNull
    public native NSArray<? extends NSValue> calculatedLabelSizes();

    @Generated
    @Selector("calculatedLineSizes")
    @NotNull
    public native NSArray<? extends NSValue> calculatedLineSizes();

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
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * The text direction of the legend
     */
    @Generated
    @Selector("direction")
    public native long direction();

    /**
     * Flag indicating whether the legend will draw inside the chart or outside
     */
    @Generated
    @Selector("drawInside")
    public native boolean drawInside();

    /**
     * The legend entries array
     */
    @Generated
    @Selector("entries")
    @NotNull
    public native NSArray<? extends ChartLegendEntry> entries();

    /**
     * Entries that will be appended to the end of the auto calculated entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged() to let the changes take effect)
     */
    @Generated
    @Selector("extraEntries")
    @NotNull
    public native NSArray<? extends ChartLegendEntry> extraEntries();

    @Generated
    @Selector("font")
    @NotNull
    public native UIFont font();

    /**
     * The form/shape of the legend forms
     */
    @Generated
    @Selector("form")
    public native long form();

    /**
     * Line dash configuration for shapes that consist of lines.
     * This is the actual dash pattern.
     * I.e. [2, 3] will paint [–   –   ]
     * [1, 3, 4, 2] will paint [-   ––  -   ––  ]
     */
    @Generated
    @Selector("formLineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> formLineDashLengths();

    /**
     * Line dash configuration for shapes that consist of lines.
     * This is how much (in pixels) into the dash pattern are we starting from.
     */
    @Generated
    @Selector("formLineDashPhase")
    public native double formLineDashPhase();

    /**
     * The line width for forms that consist of lines
     */
    @Generated
    @Selector("formLineWidth")
    public native double formLineWidth();

    /**
     * The size of the legend forms
     */
    @Generated
    @Selector("formSize")
    public native double formSize();

    @Generated
    @Selector("formToTextSpace")
    public native double formToTextSpace();

    @Generated
    @Selector("getMaximumEntrySizeWithFont:")
    @ByValue
    public native CGSize getMaximumEntrySizeWithFont(@NotNull UIFont font);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    /**
     * The horizontal alignment of the legend
     */
    @Generated
    @Selector("horizontalAlignment")
    public native long horizontalAlignment();

    @Generated
    @Selector("init")
    public native ChartLegend init();

    @Generated
    @Selector("initWithEntries:")
    public native ChartLegend initWithEntries(@NotNull NSArray<? extends ChartLegendEntry> entries);

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

    /**
     * Flag indicating whether the legend will draw inside the chart or outside
     */
    @Generated
    @Selector("isDrawInsideEnabled")
    public native boolean isDrawInsideEnabled();

    /**
     * <em>default</em>: false (automatic legend)
     * <code>true</code> if a custom legend entries has been set
     */
    @Generated
    @Selector("isLegendCustom")
    public native boolean isLegendCustom();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    /**
     * if this is set, then word wrapping the legend is enabled.
     */
    @Generated
    @Selector("isWordWrapEnabled")
    public native boolean isWordWrapEnabled();

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    /**
     * The maximum relative size out of the whole chart view in percent.
     * If the legend is to the right/left of the chart, then this affects the width of the legend.
     * If the legend is to the top/bottom of the chart, then this affects the height of the legend.
     * <em>default</em>: 0.95 (95%)
     */
    @Generated
    @Selector("maxSizePercent")
    public native double maxSizePercent();

    @Generated
    @Selector("neededHeight")
    public native double neededHeight();

    @Generated
    @Selector("neededWidth")
    public native double neededWidth();

    @Generated
    @Owned
    @Selector("new")
    public static native ChartLegend new_objc();

    /**
     * The orientation of the legend
     */
    @Generated
    @Selector("orientation")
    public native long orientation();

    /**
     * Calling this will disable the custom legend entries (set by <code>setLegend(...)</code>). Instead, the entries will again be calculated automatically (after <code>notifyDataSetChanged()</code> is called).
     */
    @Generated
    @Selector("resetCustom")
    public native void resetCustom();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setCalculatedLabelBreakPoints:")
    public native void setCalculatedLabelBreakPoints(@NotNull NSArray<? extends NSNumber> value);

    @Generated
    @Selector("setCalculatedLabelSizes:")
    public native void setCalculatedLabelSizes(@NotNull NSArray<? extends NSValue> value);

    @Generated
    @Selector("setCalculatedLineSizes:")
    public native void setCalculatedLineSizes(@NotNull NSArray<? extends NSValue> value);

    /**
     * Sets a custom legend’s entries array.
     * <ul>
     *   <li>
     *     A nil label will start a group.
     *     This will disable the feature that automatically calculates the legend entries from the datasets.
     *     Call <code>resetCustom(...)</code> to re-enable automatic calculation (and then <code>notifyDataSetChanged()</code> is needed).
     *   </li>
     * </ul>
     */
    @Generated
    @Selector("setCustomWithEntries:")
    public native void setCustomWithEntries(@NotNull NSArray<? extends ChartLegendEntry> entries);

    /**
     * The text direction of the legend
     */
    @Generated
    @Selector("setDirection:")
    public native void setDirection(long value);

    /**
     * Flag indicating whether the legend will draw inside the chart or outside
     */
    @Generated
    @Selector("setDrawInside:")
    public native void setDrawInside(boolean value);

    /**
     * The legend entries array
     */
    @Generated
    @Selector("setEntries:")
    public native void setEntries(@NotNull NSArray<? extends ChartLegendEntry> value);

    /**
     * Entries that will be appended to the end of the auto calculated entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged() to let the changes take effect)
     */
    @Generated
    @Selector("setExtraEntries:")
    public native void setExtraEntries(@NotNull NSArray<? extends ChartLegendEntry> value);

    @Generated
    @Selector("setFont:")
    public native void setFont(@NotNull UIFont value);

    /**
     * The form/shape of the legend forms
     */
    @Generated
    @Selector("setForm:")
    public native void setForm(long value);

    /**
     * Line dash configuration for shapes that consist of lines.
     * This is the actual dash pattern.
     * I.e. [2, 3] will paint [–   –   ]
     * [1, 3, 4, 2] will paint [-   ––  -   ––  ]
     */
    @Generated
    @Selector("setFormLineDashLengths:")
    public native void setFormLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    /**
     * Line dash configuration for shapes that consist of lines.
     * This is how much (in pixels) into the dash pattern are we starting from.
     */
    @Generated
    @Selector("setFormLineDashPhase:")
    public native void setFormLineDashPhase(double value);

    /**
     * The line width for forms that consist of lines
     */
    @Generated
    @Selector("setFormLineWidth:")
    public native void setFormLineWidth(double value);

    /**
     * The size of the legend forms
     */
    @Generated
    @Selector("setFormSize:")
    public native void setFormSize(double value);

    @Generated
    @Selector("setFormToTextSpace:")
    public native void setFormToTextSpace(double value);

    /**
     * The horizontal alignment of the legend
     */
    @Generated
    @Selector("setHorizontalAlignment:")
    public native void setHorizontalAlignment(long value);

    /**
     * The maximum relative size out of the whole chart view in percent.
     * If the legend is to the right/left of the chart, then this affects the width of the legend.
     * If the legend is to the top/bottom of the chart, then this affects the height of the legend.
     * <em>default</em>: 0.95 (95%)
     */
    @Generated
    @Selector("setMaxSizePercent:")
    public native void setMaxSizePercent(double value);

    @Generated
    @Selector("setNeededHeight:")
    public native void setNeededHeight(double value);

    @Generated
    @Selector("setNeededWidth:")
    public native void setNeededWidth(double value);

    /**
     * The orientation of the legend
     */
    @Generated
    @Selector("setOrientation:")
    public native void setOrientation(long value);

    @Generated
    @Selector("setStackSpace:")
    public native void setStackSpace(double value);

    @Generated
    @Selector("setTextColor:")
    public native void setTextColor(@NotNull UIColor value);

    @Generated
    @Selector("setTextHeightMax:")
    public native void setTextHeightMax(double value);

    @Generated
    @Selector("setTextWidthMax:")
    public native void setTextWidthMax(double value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * The vertical alignment of the legend
     */
    @Generated
    @Selector("setVerticalAlignment:")
    public native void setVerticalAlignment(long value);

    /**
     * flag that indicates if word wrapping is enabled
     * this is currently supported only for <code>orientation == Horizontal</code>.
     * you may want to set maxSizePercent when word wrapping, to set the point where the text wraps.
     * <em>default</em>: true
     */
    @Generated
    @Selector("setWordWrapEnabled:")
    public native void setWordWrapEnabled(boolean value);

    @Generated
    @Selector("setXEntrySpace:")
    public native void setXEntrySpace(double value);

    @Generated
    @Selector("setYEntrySpace:")
    public native void setYEntrySpace(double value);

    @Generated
    @Selector("stackSpace")
    public native double stackSpace();

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Selector("textColor")
    @NotNull
    public native UIColor textColor();

    @Generated
    @Selector("textHeightMax")
    public native double textHeightMax();

    @Generated
    @Selector("textWidthMax")
    public native double textWidthMax();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    /**
     * The vertical alignment of the legend
     */
    @Generated
    @Selector("verticalAlignment")
    public native long verticalAlignment();

    /**
     * flag that indicates if word wrapping is enabled
     * this is currently supported only for <code>orientation == Horizontal</code>.
     * you may want to set maxSizePercent when word wrapping, to set the point where the text wraps.
     * <em>default</em>: true
     */
    @Generated
    @Selector("wordWrapEnabled")
    public native boolean wordWrapEnabled();

    @Generated
    @Selector("xEntrySpace")
    public native double xEntrySpace();

    @Generated
    @Selector("yEntrySpace")
    public native double yEntrySpace();
}