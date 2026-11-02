package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
import apple.corefoundation.struct.CGSize;
import apple.coregraphics.opaque.CGContextRef;
import apple.foundation.NSArray;
import apple.foundation.NSDictionary;
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

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartXAxisRenderer extends ChartAxisRendererBase {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartXAxisRenderer(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartXAxisRenderer alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartXAxisRenderer allocWithZone(VoidPtr zone);

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
    @Selector("computeAxisValuesWithMin:max:")
    public native void computeAxisValuesWithMinMax(double min, double max);

    @Generated
    @Selector("computeAxisWithMin:max:inverted:")
    public native void computeAxisWithMinMaxInverted(double min, double max, boolean inverted);

    @Generated
    @Selector("computeSize")
    public native void computeSize();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    @Generated
    @Selector("drawGridLineWithContext:x:y:")
    public native void drawGridLineWithContextXY(@NotNull CGContextRef context, double x, double y);

    @Generated
    @Selector("drawLabelWithContext:formattedLabel:x:y:attributes:constrainedToSize:anchor:angleRadians:")
    public native void drawLabelWithContextFormattedLabelXYAttributesConstrainedToSizeAnchorAngleRadians(
            @NotNull CGContextRef context, @NotNull String formattedLabel, double x, double y,
            @NotNull NSDictionary<String, ?> attributes, @ByValue CGSize constrainedToSize, @ByValue CGPoint anchor,
            double angleRadians);

    /**
     * draws the x-labels on the specified y-position
     */
    @Generated
    @Selector("drawLabelsWithContext:pos:anchor:")
    public native void drawLabelsWithContextPosAnchor(@NotNull CGContextRef context, double pos, @ByValue CGPoint anchor);

    @Generated
    @Selector("gridClippingRect")
    @ByValue
    public native CGRect gridClippingRect();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartXAxisRenderer init();

    @Generated
    @Selector("initWithViewPortHandler:")
    public native ChartXAxisRenderer initWithViewPortHandler(@NotNull ChartViewPortHandler viewPortHandler);

    @Generated
    @Selector("initWithViewPortHandler:transformer:axis:")
    public native ChartXAxisRenderer initWithViewPortHandlerTransformerAxis(
            @NotNull ChartViewPortHandler viewPortHandler, @Nullable ChartTransformer transformer,
            @Nullable ChartAxisBase axis);

    @Generated
    @Selector("initWithViewPortHandler:xAxis:transformer:")
    public native ChartXAxisRenderer initWithViewPortHandlerXAxisTransformer(
            @NotNull ChartViewPortHandler viewPortHandler, @Nullable ChartXAxis xAxis,
            @Nullable ChartTransformer transformer);

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
    public static native ChartXAxisRenderer new_objc();

    @Generated
    @Selector("renderAxisLabelsWithContext:")
    public native void renderAxisLabelsWithContext(@NotNull CGContextRef context);

    @Generated
    @Selector("renderAxisLineWithContext:")
    public native void renderAxisLineWithContext(@NotNull CGContextRef context);

    @Generated
    @Selector("renderGridLinesWithContext:")
    public native void renderGridLinesWithContext(@NotNull CGContextRef context);

    @Generated
    @Selector("renderLimitLineLabelWithContext:limitLine:position:yOffset:")
    public native void renderLimitLineLabelWithContextLimitLinePositionYOffset(@NotNull CGContextRef context,
            @NotNull ChartLimitLine limitLine, @ByValue CGPoint position, double yOffset);

    @Generated
    @Selector("renderLimitLineLineWithContext:limitLine:position:")
    public native void renderLimitLineLineWithContextLimitLinePosition(@NotNull CGContextRef context,
            @NotNull ChartLimitLine limitLine, @ByValue CGPoint position);

    @Generated
    @Selector("renderLimitLinesWithContext:")
    public native void renderLimitLinesWithContext(@NotNull CGContextRef context);

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

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}