package org.moe.samples.simplechart.charts;


import apple.NSObject;
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
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

/**
 * The limit line is an additional feature for all Line, Bar and ScatterCharts.
 * It allows the displaying of an additional line in the chart that marks a certain maximum / limit on the specified axis (x- or y-axis).
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts14ChartLimitLine")
@ObjCClassBinding
public class ChartLimitLine extends ChartComponentBase {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartLimitLine(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartLimitLine alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartLimitLine allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

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

    @Generated
    @Selector("drawLabelEnabled")
    public native boolean drawLabelEnabled();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartLimitLine init();

    @Generated
    @Selector("initWithLimit:")
    public native ChartLimitLine initWithLimit(double limit);

    @Generated
    @Selector("initWithLimit:label:")
    public native ChartLimitLine initWithLimitLabel(double limit, @NotNull String label);

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
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Selector("label")
    @NotNull
    public native String label();

    @Generated
    @Selector("labelPosition")
    public native long labelPosition();

    /**
     * limit / maximum (the y-value or xIndex)
     */
    @Generated
    @Selector("limit")
    public native double limit();

    @Generated
    @Selector("lineColor")
    @NotNull
    public native UIColor lineColor();

    @Generated
    @Selector("lineDashLengths")
    @Nullable
    public native NSArray<? extends NSNumber> lineDashLengths();

    @Generated
    @Selector("lineDashPhase")
    public native double lineDashPhase();

    /**
     * set the line width of the chart (min = 0.2, max = 12); default 2
     */
    @Generated
    @Selector("lineWidth")
    public native double lineWidth();

    @Generated
    @Owned
    @Selector("new")
    public static native ChartLimitLine new_objc();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setDrawLabelEnabled:")
    public native void setDrawLabelEnabled(boolean value);

    @Generated
    @Selector("setLabel:")
    public native void setLabel(@NotNull String value);

    @Generated
    @Selector("setLabelPosition:")
    public native void setLabelPosition(long value);

    /**
     * limit / maximum (the y-value or xIndex)
     */
    @Generated
    @Selector("setLimit:")
    public native void setLimit(double value);

    @Generated
    @Selector("setLineColor:")
    public native void setLineColor(@NotNull UIColor value);

    @Generated
    @Selector("setLineDashLengths:")
    public native void setLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    @Generated
    @Selector("setLineDashPhase:")
    public native void setLineDashPhase(double value);

    /**
     * set the line width of the chart (min = 0.2, max = 12); default 2
     */
    @Generated
    @Selector("setLineWidth:")
    public native void setLineWidth(double value);

    @Generated
    @Selector("setValueFont:")
    public native void setValueFont(@NotNull UIFont value);

    @Generated
    @Selector("setValueTextColor:")
    public native void setValueTextColor(@NotNull UIColor value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("valueFont")
    @NotNull
    public native UIFont valueFont();

    @Generated
    @Selector("valueTextColor")
    @NotNull
    public native UIColor valueTextColor();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}