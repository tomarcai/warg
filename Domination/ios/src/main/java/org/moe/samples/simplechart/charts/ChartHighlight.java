package org.moe.samples.simplechart.charts;


import apple.NSObject;
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

@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartHighlight extends NSObject {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartHighlight(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartHighlight alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartHighlight allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Selector("axis")
    public native long axis();

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

    /**
     * the index of the data object - in case it refers to more than one
     */
    @Generated
    @Selector("dataIndex")
    public native long dataIndex();

    @Generated
    @Selector("dataSetIndex")
    public native long dataSetIndex();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    @NotNull
    public native String description();

    /**
     * the x-position (pixels) on which this highlight object was last drawn
     */
    @Generated
    @Selector("drawX")
    public native double drawX();

    /**
     * the y-position (pixels) on which this highlight object was last drawn
     */
    @Generated
    @Selector("drawY")
    public native double drawY();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartHighlight init();

    /**
     * \param x the x-value of the highlighted value
     * 
     * \param dataSetIndex the index of the DataSet the highlighted value belongs to
     * 
     * \param stackIndex references which value of a stacked-bar entry has been selected
     */
    @Generated
    @Selector("initWithX:dataSetIndex:stackIndex:")
    public native ChartHighlight initWithXDataSetIndexStackIndex(double x, long dataSetIndex, long stackIndex);

    /**
     * \param x the x-value of the highlighted value
     * 
     * \param y the y-value of the highlighted value
     * 
     * \param dataSetIndex the index of the DataSet the highlighted value belongs to
     * 
     * \param dataIndex The data index to search in (only used in CombinedChartView currently)
     */
    @Generated
    @Selector("initWithX:y:dataSetIndex:dataIndex:")
    public native ChartHighlight initWithXYDataSetIndexDataIndex(double x, double y, long dataSetIndex, long dataIndex);

    /**
     * \param x the x-value of the highlighted value
     * 
     * \param y the y-value of the highlighted value
     * 
     * \param xPx the x-pixel of the highlighted value
     * 
     * \param yPx the y-pixel of the highlighted value
     * 
     * \param dataIndex the index of the Data the highlighted value belongs to
     * 
     * \param dataSetIndex the index of the DataSet the highlighted value belongs to
     * 
     * \param stackIndex references which value of a stacked-bar entry has been selected
     * 
     * \param axis the axis the highlighted value belongs to
     */
    @Generated
    @Selector("initWithX:y:xPx:yPx:dataIndex:dataSetIndex:stackIndex:axis:")
    public native ChartHighlight initWithXYXPxYPxDataIndexDataSetIndexStackIndexAxis(double x, double y, double xPx,
            double yPx, long dataIndex, long dataSetIndex, long stackIndex, long axis);

    /**
     * \param x the x-value of the highlighted value
     * 
     * \param y the y-value of the highlighted value
     * 
     * \param xPx the x-pixel of the highlighted value
     * 
     * \param yPx the y-pixel of the highlighted value
     * 
     * \param dataIndex the index of the Data the highlighted value belongs to
     * 
     * \param dataSetIndex the index of the DataSet the highlighted value belongs to
     * 
     * \param stackIndex references which value of a stacked-bar entry has been selected
     * 
     * \param axis the axis the highlighted value belongs to
     */
    @Generated
    @Selector("initWithX:y:xPx:yPx:dataSetIndex:axis:")
    public native ChartHighlight initWithXYXPxYPxDataSetIndexAxis(double x, double y, double xPx, double yPx,
            long dataSetIndex, long axis);

    /**
     * \param x the x-value of the highlighted value
     * 
     * \param y the y-value of the highlighted value
     * 
     * \param xPx the x-pixel of the highlighted value
     * 
     * \param yPx the y-pixel of the highlighted value
     * 
     * \param dataSetIndex the index of the DataSet the highlighted value belongs to
     * 
     * \param stackIndex references which value of a stacked-bar entry has been selected
     * 
     * \param axis the axis the highlighted value belongs to
     */
    @Generated
    @Selector("initWithX:y:xPx:yPx:dataSetIndex:stackIndex:axis:")
    public native ChartHighlight initWithXYXPxYPxDataSetIndexStackIndexAxis(double x, double y, double xPx, double yPx,
            long dataSetIndex, long stackIndex, long axis);

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
    @Selector("isEqual:")
    public native boolean isEqual(@Mapped(ObjCObjectMapper.class) @Nullable Object object);

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

    @Generated
    @Owned
    @Selector("new")
    public static native ChartHighlight new_objc();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    /**
     * the index of the data object - in case it refers to more than one
     */
    @Generated
    @Selector("setDataIndex:")
    public native void setDataIndex(long value);

    /**
     * Sets the x- and y-position (pixels) where this highlight was last drawn.
     */
    @Generated
    @Selector("setDrawWithPt:")
    public native void setDrawWithPt(@ByValue CGPoint pt);

    /**
     * Sets the x- and y-position (pixels) where this highlight was last drawn.
     */
    @Generated
    @Selector("setDrawWithX:y:")
    public native void setDrawWithXY(double x, double y);

    /**
     * the x-position (pixels) on which this highlight object was last drawn
     */
    @Generated
    @Selector("setDrawX:")
    public native void setDrawX(double value);

    /**
     * the y-position (pixels) on which this highlight object was last drawn
     */
    @Generated
    @Selector("setDrawY:")
    public native void setDrawY(double value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("stackIndex")
    public native long stackIndex();

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
    @Selector("x")
    public native double x();

    @Generated
    @Selector("xPx")
    public native double xPx();

    @Generated
    @Selector("y")
    public native double y();

    @Generated
    @Selector("yPx")
    public native double yPx();
}