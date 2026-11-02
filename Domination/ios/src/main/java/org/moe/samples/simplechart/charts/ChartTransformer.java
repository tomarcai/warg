package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGAffineTransform;
import apple.corefoundation.struct.CGPoint;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
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
 * Transformer class that contains all matrices and is responsible for transforming values into pixels on the screen and backwards.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartTransformer extends NSObject {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartTransformer(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartTransformer alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartTransformer allocWithZone(VoidPtr zone);

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
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartTransformer init();

    @Generated
    @Selector("initWithViewPortHandler:")
    public native ChartTransformer initWithViewPortHandler(@NotNull ChartViewPortHandler viewPortHandler);

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
    @Owned
    @Selector("new")
    public static native ChartTransformer new_objc();

    @Generated
    @Selector("pixelForValuesWithX:y:")
    @ByValue
    public native CGPoint pixelForValuesWithXY(double x, double y);

    @Generated
    @Selector("pixelToValueMatrix")
    @ByValue
    public native CGAffineTransform pixelToValueMatrix();

    /**
     * Prepares the matrix that contains all offsets.
     */
    @Generated
    @Selector("prepareMatrixOffsetWithInverted:")
    public native void prepareMatrixOffsetWithInverted(boolean inverted);

    /**
     * Prepares the matrix that transforms values to pixels. Calculates the scale factors from the charts size and offsets.
     */
    @Generated
    @Selector("prepareMatrixValuePxWithChartXMin:deltaX:deltaY:chartYMin:")
    public native void prepareMatrixValuePxWithChartXMinDeltaXDeltaYChartYMin(double chartXMin, double deltaX,
            double deltaY, double chartYMin);

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

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

    /**
     * returns:
     * The x and y values in the chart at the given touch point
     * (encapsulated in a CGPoint). This method transforms pixel coordinates to
     * coordinates / values in the chart.
     */
    @Generated
    @Selector("valueForTouchPoint:")
    @ByValue
    public native CGPoint valueForTouchPoint(@ByValue CGPoint point);

    /**
     * returns:
     * The x and y values in the chart at the given touch point
     * (x/y). This method transforms pixel coordinates to
     * coordinates / values in the chart.
     */
    @Generated
    @Selector("valueForTouchPointWithX:y:")
    @ByValue
    public native CGPoint valueForTouchPointWithXY(double x, double y);

    @Generated
    @Selector("valueToPixelMatrix")
    @ByValue
    public native CGAffineTransform valueToPixelMatrix();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}