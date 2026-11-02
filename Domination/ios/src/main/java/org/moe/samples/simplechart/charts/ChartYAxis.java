package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGSize;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSNumber;
import apple.foundation.NSSet;
import apple.uikit.UIColor;
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

/**
 * Class representing the y-axis labels settings and its entries.
 * Be aware that not all features the YLabels class provides are suitable for the RadarChart.
 * Customizations that affect the value range of the axis need to be applied before setting data for the chart.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartYAxis extends ChartAxisBase {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartYAxis(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartYAxis alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartYAxis allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("axisDependency")
    public native long axisDependency();

    @Generated
    @Selector("calculateWithMin:max:")
    public native void calculateWithMinMax(double dataMin, double dataMax);

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
     * indicates if the bottom y-label entry is drawn or not
     */
    @Generated
    @Selector("drawBottomYLabelEntryEnabled")
    public native boolean drawBottomYLabelEntryEnabled();

    /**
     * indicates if the top y-label entry is drawn or not
     */
    @Generated
    @Selector("drawTopYLabelEntryEnabled")
    public native boolean drawTopYLabelEntryEnabled();

    /**
     * flag that indicates if the zero-line should be drawn regardless of other grid lines
     */
    @Generated
    @Selector("drawZeroLineEnabled")
    public native boolean drawZeroLineEnabled();

    @Generated
    @Selector("getRequiredHeightSpace")
    public native double getRequiredHeightSpace();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartYAxis init();

    @Generated
    @Selector("initWithPosition:")
    public native ChartYAxis initWithPosition(long position);

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
     * flag that indicates if the axis is inverted or not
     */
    @Generated
    @Selector("inverted")
    public native boolean inverted();

    @Generated
    @Selector("isDrawBottomYLabelEntryEnabled")
    public native boolean isDrawBottomYLabelEntryEnabled();

    @Generated
    @Selector("isDrawTopYLabelEntryEnabled")
    public native boolean isDrawTopYLabelEntryEnabled();

    @Generated
    @Selector("isInverted")
    public native boolean isInverted();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    /**
     * the alignment of the text in the y-label
     */
    @Generated
    @Selector("labelAlignment")
    public native long labelAlignment();

    /**
     * the position of the y-labels relative to the chart
     */
    @Generated
    @Selector("labelPosition")
    public native long labelPosition();

    /**
     * the horizontal offset of the y-label
     */
    @Generated
    @Selector("labelXOffset")
    public native double labelXOffset();

    /**
     * the maximum width that the axis can take.
     * use Infinity for disabling the maximum.
     * <em>default</em>: CGFloat.infinity
     */
    @Generated
    @Selector("maxWidth")
    public native double maxWidth();

    /**
     * the minimum width that the axis should take
     * <em>default</em>: 0.0
     */
    @Generated
    @Selector("minWidth")
    public native double minWidth();

    /**
     * <code>true</code> if this axis needs horizontal offset, <code>false</code> ifno offset is needed.
     */
    @Generated
    @Selector("needsOffset")
    public native boolean needsOffset();

    @Generated
    @Owned
    @Selector("new")
    public static native ChartYAxis new_objc();

    @Generated
    @Selector("requiredSize")
    @ByValue
    public native CGSize requiredSize();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    /**
     * indicates if the bottom y-label entry is drawn or not
     */
    @Generated
    @Selector("setDrawBottomYLabelEntryEnabled:")
    public native void setDrawBottomYLabelEntryEnabled(boolean value);

    /**
     * indicates if the top y-label entry is drawn or not
     */
    @Generated
    @Selector("setDrawTopYLabelEntryEnabled:")
    public native void setDrawTopYLabelEntryEnabled(boolean value);

    /**
     * flag that indicates if the zero-line should be drawn regardless of other grid lines
     */
    @Generated
    @Selector("setDrawZeroLineEnabled:")
    public native void setDrawZeroLineEnabled(boolean value);

    /**
     * flag that indicates if the axis is inverted or not
     */
    @Generated
    @Selector("setInverted:")
    public native void setInverted(boolean value);

    /**
     * the alignment of the text in the y-label
     */
    @Generated
    @Selector("setLabelAlignment:")
    public native void setLabelAlignment(long value);

    /**
     * the position of the y-labels relative to the chart
     */
    @Generated
    @Selector("setLabelPosition:")
    public native void setLabelPosition(long value);

    /**
     * the horizontal offset of the y-label
     */
    @Generated
    @Selector("setLabelXOffset:")
    public native void setLabelXOffset(double value);

    /**
     * the maximum width that the axis can take.
     * use Infinity for disabling the maximum.
     * <em>default</em>: CGFloat.infinity
     */
    @Generated
    @Selector("setMaxWidth:")
    public native void setMaxWidth(double value);

    /**
     * the minimum width that the axis should take
     * <em>default</em>: 0.0
     */
    @Generated
    @Selector("setMinWidth:")
    public native void setMinWidth(double value);

    /**
     * axis space from the smallest value to the bottom in percent of the total axis range
     */
    @Generated
    @Selector("setSpaceBottom:")
    public native void setSpaceBottom(double value);

    /**
     * axis space from the largest value to the top in percent of the total axis range
     */
    @Generated
    @Selector("setSpaceTop:")
    public native void setSpaceTop(double value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * Color of the zero line
     */
    @Generated
    @Selector("setZeroLineColor:")
    public native void setZeroLineColor(@Nullable UIColor value);

    /**
     * This is the actual dash pattern.
     * I.e. [2, 3] will paint [–   –   ]
     * [1, 3, 4, 2] will paint [-   ––  -   ––  ]
     */
    @Generated
    @Selector("setZeroLineDashLengths:")
    public native void setZeroLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    /**
     * This is how much (in pixels) into the dash pattern are we starting from.
     */
    @Generated
    @Selector("setZeroLineDashPhase:")
    public native void setZeroLineDashPhase(double value);

    /**
     * Width of the zero line
     */
    @Generated
    @Selector("setZeroLineWidth:")
    public native void setZeroLineWidth(double value);

    /**
     * axis space from the smallest value to the bottom in percent of the total axis range
     */
    @Generated
    @Selector("spaceBottom")
    public native double spaceBottom();

    /**
     * axis space from the largest value to the top in percent of the total axis range
     */
    @Generated
    @Selector("spaceTop")
    public native double spaceTop();

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    /**
     * Color of the zero line
     */
    @Generated
    @Selector("zeroLineColor")
    @Nullable
    public native UIColor zeroLineColor();

    /**
     * This is the actual dash pattern.
     * I.e. [2, 3] will paint [–   –   ]
     * [1, 3, 4, 2] will paint [-   ––  -   ––  ]
     */
    @Generated
    @Selector("zeroLineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> zeroLineDashLengths();

    /**
     * This is how much (in pixels) into the dash pattern are we starting from.
     */
    @Generated
    @Selector("zeroLineDashPhase")
    public native double zeroLineDashPhase();

    /**
     * Width of the zero line
     */
    @Generated
    @Selector("zeroLineWidth")
    public native double zeroLineWidth();
}