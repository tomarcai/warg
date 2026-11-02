package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
import apple.coregraphics.opaque.CGColorRef;
import apple.coregraphics.opaque.CGContextRef;
import apple.coregraphics.opaque.CGGradientRef;
import apple.coregraphics.opaque.CGImageRef;
import apple.coregraphics.opaque.CGLayerRef;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
import apple.uikit.UIColor;
import apple.uikit.UIImage;
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
public class ChartFill extends NSObject {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartFill(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartFill alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartFill allocWithZone(VoidPtr zone);

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
    @Selector("color")
    @Nullable
    public native CGColorRef color();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * Draws the provided path in filled mode with the provided area
     */
    @Generated
    @Selector("fillPathWithContext:rect:")
    public native void fillPathWithContextRect(@NotNull CGContextRef context, @ByValue CGRect rect);

    @Generated
    @Selector("fillWithCGColor:")
    @NotNull
    public static native ChartFill fillWithCGColor(@NotNull CGColorRef CGColor);

    @Generated
    @Selector("fillWithCGImage:")
    @NotNull
    public static native ChartFill fillWithCGImage(@NotNull CGImageRef CGImage);

    @Generated
    @Selector("fillWithCGImage:tiled:")
    @NotNull
    public static native ChartFill fillWithCGImageTiled(@NotNull CGImageRef CGImage, boolean tiled);

    @Generated
    @Selector("fillWithCGLayer:")
    @NotNull
    public static native ChartFill fillWithCGLayer(@NotNull CGLayerRef CGLayer);

    @Generated
    @Selector("fillWithColor:")
    @NotNull
    public static native ChartFill fillWithColor(@NotNull UIColor color);

    @Generated
    @Selector("fillWithImage:")
    @NotNull
    public static native ChartFill fillWithImage(@NotNull UIImage image);

    @Generated
    @Selector("fillWithImage:tiled:")
    @NotNull
    public static native ChartFill fillWithImageTiled(@NotNull UIImage image, boolean tiled);

    @Generated
    @Selector("fillWithLinearGradient:angle:")
    @NotNull
    public static native ChartFill fillWithLinearGradientAngle(@NotNull CGGradientRef linearGradient, double angle);

    @Generated
    @Selector("fillWithRadialGradient:")
    @NotNull
    public static native ChartFill fillWithRadialGradient(@NotNull CGGradientRef radialGradient);

    @Generated
    @Selector("fillWithRadialGradient:startOffsetPercent:startRadiusPercent:endOffsetPercent:endRadiusPercent:")
    @NotNull
    public static native ChartFill fillWithRadialGradientStartOffsetPercentStartRadiusPercentEndOffsetPercentEndRadiusPercent(
            @NotNull CGGradientRef radialGradient, @ByValue CGPoint startOffsetPercent, double startRadiusPercent,
            @ByValue CGPoint endOffsetPercent, double endRadiusPercent);

    @Generated
    @Selector("gradient")
    @Nullable
    public native CGGradientRef gradient();

    @Generated
    @Selector("gradientAngle")
    public native double gradientAngle();

    @Generated
    @Selector("gradientEndOffsetPercent")
    @ByValue
    public native CGPoint gradientEndOffsetPercent();

    @Generated
    @Selector("gradientEndRadiusPercent")
    public native double gradientEndRadiusPercent();

    @Generated
    @Selector("gradientStartOffsetPercent")
    @ByValue
    public native CGPoint gradientStartOffsetPercent();

    @Generated
    @Selector("gradientStartRadiusPercent")
    public native double gradientStartRadiusPercent();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("image")
    @Nullable
    public native CGImageRef image();

    @Generated
    @Selector("init")
    public native ChartFill init();

    @Generated
    @Selector("initWithCGColor:")
    public native ChartFill initWithCGColor(@NotNull CGColorRef CGColor);

    @Generated
    @Selector("initWithCGImage:")
    public native ChartFill initWithCGImage(@NotNull CGImageRef CGImage);

    @Generated
    @Selector("initWithCGImage:tiled:")
    public native ChartFill initWithCGImageTiled(@NotNull CGImageRef CGImage, boolean tiled);

    @Generated
    @Selector("initWithCGLayer:")
    public native ChartFill initWithCGLayer(@NotNull CGLayerRef CGLayer);

    @Generated
    @Selector("initWithColor:")
    public native ChartFill initWithColor(@NotNull UIColor color);

    @Generated
    @Selector("initWithImage:")
    public native ChartFill initWithImage(@NotNull UIImage image);

    @Generated
    @Selector("initWithImage:tiled:")
    public native ChartFill initWithImageTiled(@NotNull UIImage image, boolean tiled);

    @Generated
    @Selector("initWithLinearGradient:angle:")
    public native ChartFill initWithLinearGradientAngle(@NotNull CGGradientRef linearGradient, double angle);

    @Generated
    @Selector("initWithRadialGradient:")
    public native ChartFill initWithRadialGradient(@NotNull CGGradientRef radialGradient);

    @Generated
    @Selector("initWithRadialGradient:startOffsetPercent:startRadiusPercent:endOffsetPercent:endRadiusPercent:")
    public native ChartFill initWithRadialGradientStartOffsetPercentStartRadiusPercentEndOffsetPercentEndRadiusPercent(
            @NotNull CGGradientRef radialGradient, @ByValue CGPoint startOffsetPercent, double startRadiusPercent,
            @ByValue CGPoint endOffsetPercent, double endRadiusPercent);

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
    @Selector("layer")
    @Nullable
    public native CGLayerRef layer();

    @Generated
    @Owned
    @Selector("new")
    public static native ChartFill new_objc();

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
    @Selector("type")
    public native long type();

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();
}