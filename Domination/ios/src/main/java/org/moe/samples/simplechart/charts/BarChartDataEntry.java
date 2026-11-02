package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.foundation.NSArray;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSNumber;
import apple.foundation.NSSet;
import apple.uikit.UIImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moe.natj.c.ann.FunctionPtr;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
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

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts17BarChartDataEntry")
@ObjCClassBinding
public class BarChartDataEntry extends ChartDataEntry {
    static {
        NatJ.register();
    }

    @Generated
    protected BarChartDataEntry(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native BarChartDataEntry alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native BarChartDataEntry allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("calcPosNegSum")
    public native void calcPosNegSum();

    /**
     * Splits up the stack-values of the given bar-entry into Range objects.
     * \param entry
     * 
     * 
     * returns:
     */
    @Generated
    @Selector("calcRanges")
    public native void calcRanges();

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
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native BarChartDataEntry init();

    /**
     * Constructor for normal bars (not stacked).
     */
    @Generated
    @Selector("initWithX:y:")
    public native BarChartDataEntry initWithXY(double x, double y);

    /**
     * Constructor for normal bars (not stacked).
     */
    @Generated
    @Selector("initWithX:y:data:")
    public native BarChartDataEntry initWithXYData(double x, double y,
            @Mapped(ObjCObjectMapper.class) @Nullable Object data);

    /**
     * Constructor for normal bars (not stacked).
     */
    @Generated
    @Selector("initWithX:y:icon:")
    public native BarChartDataEntry initWithXYIcon(double x, double y, @Nullable UIImage icon);

    /**
     * Constructor for normal bars (not stacked).
     */
    @Generated
    @Selector("initWithX:y:icon:data:")
    public native BarChartDataEntry initWithXYIconData(double x, double y, @Nullable UIImage icon,
            @Mapped(ObjCObjectMapper.class) @Nullable Object data);

    /**
     * Constructor for stacked bar entries.
     */
    @Generated
    @Selector("initWithX:yValues:")
    public native BarChartDataEntry initWithXYValues(double x, @NotNull NSArray<? extends NSNumber> yValues);

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     */
    @Generated
    @Selector("initWithX:yValues:data:")
    public native BarChartDataEntry initWithXYValuesData(double x, @NotNull NSArray<? extends NSNumber> yValues,
            @Mapped(ObjCObjectMapper.class) @Nullable Object data);

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     */
    @Generated
    @Selector("initWithX:yValues:icon:")
    public native BarChartDataEntry initWithXYValuesIcon(double x, @NotNull NSArray<? extends NSNumber> yValues,
            @Nullable UIImage icon);

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     */
    @Generated
    @Selector("initWithX:yValues:icon:data:")
    public native BarChartDataEntry initWithXYValuesIconData(double x, @NotNull NSArray<? extends NSNumber> yValues,
            @Nullable UIImage icon, @Mapped(ObjCObjectMapper.class) @Nullable Object data);

    @Generated
    @Selector("initWithY:")
    public native BarChartDataEntry initWithY(double y);

    @Generated
    @Selector("initWithY:data:")
    public native BarChartDataEntry initWithYData(double y, @Mapped(ObjCObjectMapper.class) @Nullable Object data);

    @Generated
    @Selector("initWithY:icon:")
    public native BarChartDataEntry initWithYIcon(double y, @Nullable UIImage icon);

    @Generated
    @Selector("initWithY:icon:data:")
    public native BarChartDataEntry initWithYIconData(double y, @Nullable UIImage icon,
            @Mapped(ObjCObjectMapper.class) @Nullable Object data);

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
     * the values the stacked barchart holds
     */
    @Generated
    @Selector("isStacked")
    public native boolean isStacked();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    /**
     * The sum of all negative values this entry (if stacked) contains. (this is a positive number)
     */
    @Generated
    @Selector("negativeSum")
    public native double negativeSum();

    @Generated
    @Owned
    @Selector("new")
    public static native BarChartDataEntry new_objc();

    /**
     * The sum of all positive values this entry (if stacked) contains.
     */
    @Generated
    @Selector("positiveSum")
    public native double positiveSum();

    /**
     * The ranges of the individual stack-entries. Will return null if this entry is not stacked.
     */
    @Generated
    @Selector("ranges")
    @Nullable
    public native NSArray<? extends ChartRange> ranges();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * the values the stacked barchart holds
     */
    @Generated
    @Selector("setYValues:")
    public native void setYValues(@Nullable NSArray<? extends NSNumber> value);

    @Generated
    @Selector("sumBelowStackIndex:")
    public native double sumBelowStackIndex(long stackIndex);

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
     * the values the stacked barchart holds
     */
    @Generated
    @Selector("yValues")
    @Nullable
    public native NSArray<? extends NSNumber> yValues();
}