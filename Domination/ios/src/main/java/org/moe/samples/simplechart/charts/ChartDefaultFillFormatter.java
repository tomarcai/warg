package org.moe.samples.simplechart.charts;


import apple.NSObject;
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
import org.moe.natj.objc.ann.ObjCBlock;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import org.moe.samples.simplechart.charts.protocol.IChartFillFormatter;
import org.moe.samples.simplechart.charts.protocol.ILineChartDataSet;
import org.moe.samples.simplechart.charts.protocol.LineChartDataProvider;

/**
 * Default formatter that calculates the position of the filled line.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartDefaultFillFormatter extends NSObject implements IChartFillFormatter {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartDefaultFillFormatter(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartDefaultFillFormatter alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartDefaultFillFormatter allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("block")
    @ObjCBlock(name = "call_block_ret")
    @Nullable
    public native Block_block_ret block();

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_block_ret {
        @Generated
        double call_block_ret(@Mapped(ObjCObjectMapper.class) @NotNull ILineChartDataSet arg0,
                @Mapped(ObjCObjectMapper.class) @NotNull LineChartDataProvider arg1);
    }

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
    @Selector("getFillLinePositionWithDataSet:dataProvider:")
    public native double getFillLinePositionWithDataSetDataProvider(
            @Mapped(ObjCObjectMapper.class) @NotNull ILineChartDataSet dataSet,
            @Mapped(ObjCObjectMapper.class) @NotNull LineChartDataProvider dataProvider);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartDefaultFillFormatter init();

    @Generated
    @Selector("initWithBlock:")
    public native ChartDefaultFillFormatter initWithBlock(
            @ObjCBlock(name = "call_initWithBlock") @NotNull Block_initWithBlock block);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_initWithBlock {
        @Generated
        double call_initWithBlock(@Mapped(ObjCObjectMapper.class) @NotNull ILineChartDataSet arg0,
                @Mapped(ObjCObjectMapper.class) @NotNull LineChartDataProvider arg1);
    }

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
    public static native ChartDefaultFillFormatter new_objc();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("setBlock:")
    public native void setBlock(@ObjCBlock(name = "call_setBlock") @Nullable Block_setBlock value);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_setBlock {
        @Generated
        double call_setBlock(@Mapped(ObjCObjectMapper.class) @NotNull ILineChartDataSet arg0,
                @Mapped(ObjCObjectMapper.class) @NotNull LineChartDataProvider arg1);
    }

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

    @Generated
    @Selector("withBlock:")
    @Nullable
    public static native ChartDefaultFillFormatter withBlock(
            @ObjCBlock(name = "call_withBlock") @NotNull Block_withBlock block);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_withBlock {
        @Generated
        double call_withBlock(@Mapped(ObjCObjectMapper.class) @NotNull Object arg0,
                @Mapped(ObjCObjectMapper.class) @NotNull Object arg1);
    }
}