package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.coregraphics.opaque.CGContextRef;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
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

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts21CombinedChartRenderer")
@ObjCClassBinding
public class CombinedChartRenderer extends ChartDataRendererBase {
    static {
        NatJ.register();
    }

    @Generated
    protected CombinedChartRenderer(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native CombinedChartRenderer alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native CombinedChartRenderer allocWithZone(VoidPtr zone);

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
    @Selector("chart")
    @Nullable
    public native CombinedChartView chart();

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
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    @Generated
    @Selector("drawBarShadowEnabled")
    public native boolean drawBarShadowEnabled();

    @Generated
    @Selector("drawDataWithContext:")
    public native void drawDataWithContext(@NotNull CGContextRef context);

    @Generated
    @Selector("drawExtrasWithContext:")
    public native void drawExtrasWithContext(@NotNull CGContextRef context);

    @Generated
    @Selector("drawHighlightedWithContext:indices:")
    public native void drawHighlightedWithContextIndices(@NotNull CGContextRef context,
            @NotNull NSArray<? extends ChartHighlight> indices);

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    @Generated
    @Selector("drawValueAboveBarEnabled")
    public native boolean drawValueAboveBarEnabled();

    @Generated
    @Selector("drawValuesWithContext:")
    public native void drawValuesWithContext(@NotNull CGContextRef context);

    /**
     * returns:
     * The sub-renderer object at the specified index.
     */
    @Generated
    @Selector("getSubRendererWithIndex:")
    @Nullable
    public native ChartDataRendererBase getSubRendererWithIndex(long index);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native CombinedChartRenderer init();

    @Generated
    @Selector("initBuffers")
    public native void initBuffers();

    @Generated
    @Selector("initWithAnimator:viewPortHandler:")
    public native CombinedChartRenderer initWithAnimatorViewPortHandler(@NotNull ChartAnimator animator,
            @NotNull ChartViewPortHandler viewPortHandler);

    @Generated
    @Selector("initWithChart:animator:viewPortHandler:")
    public native CombinedChartRenderer initWithChartAnimatorViewPortHandler(@NotNull CombinedChartView chart,
            @NotNull ChartAnimator animator, @NotNull ChartViewPortHandler viewPortHandler);

    @Generated
    @Selector("initWithViewPortHandler:")
    public native CombinedChartRenderer initWithViewPortHandler(@NotNull ChartViewPortHandler viewPortHandler);

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
     * <code>true</code> if drawing shadows (maxvalue) for each bar is enabled, <code>false</code> ifnot
     */
    @Generated
    @Selector("isDrawBarShadowEnabled")
    public native boolean isDrawBarShadowEnabled();

    /**
     * <code>true</code> if drawing values above bars is enabled, <code>false</code> ifnot
     */
    @Generated
    @Selector("isDrawValueAboveBarEnabled")
    public native boolean isDrawValueAboveBarEnabled();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Owned
    @Selector("new")
    public static native CombinedChartRenderer new_objc();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setChart:")
    public native void setChart_unsafe(@Nullable CombinedChartView value);

    @Generated
    public void setChart(@Nullable CombinedChartView value) {
        Object __old = chart();
        if (value != null) {
            org.moe.natj.objc.ObjCRuntime.associateObjCObject(this, value);
        }
        setChart_unsafe(value);
        if (__old != null) {
            org.moe.natj.objc.ObjCRuntime.dissociateObjCObject(this, __old);
        }
    }

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    @Generated
    @Selector("setDrawBarShadowEnabled:")
    public native void setDrawBarShadowEnabled(boolean value);

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    @Generated
    @Selector("setDrawValueAboveBarEnabled:")
    public native void setDrawValueAboveBarEnabled(boolean value);

    /**
     * All sub-renderers.
     */
    @Generated
    @Selector("setSubRenderers:")
    public native void setSubRenderers(@NotNull NSArray<? extends ChartDataRendererBase> value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * All sub-renderers.
     */
    @Generated
    @Selector("subRenderers")
    @NotNull
    public native NSArray<? extends ChartDataRendererBase> subRenderers();

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