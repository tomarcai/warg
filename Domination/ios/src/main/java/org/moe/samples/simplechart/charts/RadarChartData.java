package org.moe.samples.simplechart.charts;


import apple.NSObject;
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
import org.moe.samples.simplechart.charts.protocol.IChartDataSet;

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts14RadarChartData")
@ObjCClassBinding
public class RadarChartData extends ChartData {
    static {
        NatJ.register();
    }

    @Generated
    protected RadarChartData(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native RadarChartData alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native RadarChartData allocWithZone(VoidPtr zone);

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
    @Selector("entryForHighlight:")
    @Nullable
    public native ChartDataEntry entryForHighlight(@NotNull ChartHighlight highlight);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("highlightColor")
    @NotNull
    public native UIColor highlightColor();

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
    @Selector("init")
    public native RadarChartData init();

    @Generated
    @Selector("initWithDataSet:")
    public native RadarChartData initWithDataSet(@Mapped(ObjCObjectMapper.class) @Nullable IChartDataSet dataSet);

    @Generated
    @Selector("initWithDataSets:")
    public native RadarChartData initWithDataSets(@Nullable NSArray<?> dataSets);

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

    /**
     * Sets labels that should be drawn around the RadarChart at the end of each web line.
     */
    @Generated
    @Selector("labels")
    @NotNull
    public native NSArray<String> labels();

    @Generated
    @Owned
    @Selector("new")
    public static native RadarChartData new_objc();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setHighlightColor:")
    public native void setHighlightColor(@NotNull UIColor value);

    @Generated
    @Selector("setHighlightLineDashLengths:")
    public native void setHighlightLineDashLengths(@Nullable NSArray<? extends NSNumber> value);

    @Generated
    @Selector("setHighlightLineDashPhase:")
    public native void setHighlightLineDashPhase(double value);

    @Generated
    @Selector("setHighlightLineWidth:")
    public native void setHighlightLineWidth(double value);

    /**
     * Sets labels that should be drawn around the RadarChart at the end of each web line.
     */
    @Generated
    @Selector("setLabels:")
    public native void setLabels(@NotNull NSArray<String> value);

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
    @Selector("version")
    @NInt
    public static native long version_static();
}