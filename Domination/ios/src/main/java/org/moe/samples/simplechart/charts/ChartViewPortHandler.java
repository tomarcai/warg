package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGAffineTransform;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
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
 * Class that contains information about the charts current viewport settings, including offsets, scale & translation levels, …
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassBinding
public class ChartViewPortHandler extends NSObject {
    static {
        NatJ.register();
    }

    @Generated
    protected ChartViewPortHandler(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Owned
    @Selector("alloc")
    public static native ChartViewPortHandler alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native ChartViewPortHandler allocWithZone(VoidPtr zone);

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    /**
     * <code>true</code> if the chart is not yet fully zoomed in on the x-axis
     */
    @Generated
    @Selector("canZoomInMoreX")
    public native boolean canZoomInMoreX();

    /**
     * <code>true</code> if the chart is not yet fully zoomed in on the y-axis
     */
    @Generated
    @Selector("canZoomInMoreY")
    public native boolean canZoomInMoreY();

    /**
     * <code>true</code> if the chart is not yet fully zoomed out on the x-axis
     */
    @Generated
    @Selector("canZoomOutMoreX")
    public native boolean canZoomOutMoreX();

    /**
     * <code>true</code> if the chart is not yet fully zoomed out on the y-axis
     */
    @Generated
    @Selector("canZoomOutMoreY")
    public native boolean canZoomOutMoreY();

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:")
    public static native void cancelPreviousPerformRequestsWithTarget(
            @Mapped(ObjCObjectMapper.class) @NotNull Object aTarget);

    @Generated
    @Selector("cancelPreviousPerformRequestsWithTarget:selector:object:")
    public static native void cancelPreviousPerformRequestsWithTargetSelectorObject(
            @Mapped(ObjCObjectMapper.class) @NotNull Object aTarget, @NotNull SEL aSelector,
            @Mapped(ObjCObjectMapper.class) @Nullable Object anArgument);

    /**
     * Centers the viewport around the specified position (x-index and y-value) in the chart.
     * Centering the viewport outside the bounds of the chart is not possible.
     * Makes most sense in combination with the setScaleMinima(…) method.
     */
    @Generated
    @Selector("centerViewPortWithPt:chart:")
    public native void centerViewPortWithPtChart(@ByValue CGPoint pt, @NotNull ChartViewBase chart);

    @Generated
    @Selector("chartHeight")
    public native double chartHeight();

    @Generated
    @Selector("chartWidth")
    public native double chartWidth();

    @Generated
    @Selector("classFallbacksForKeyedArchiver")
    @NotNull
    public static native NSArray<String> classFallbacksForKeyedArchiver();

    @Generated
    @Selector("classForKeyedUnarchiver")
    @NotNull
    public static native Class classForKeyedUnarchiver();

    @Generated
    @Selector("contentBottom")
    public native double contentBottom();

    @Generated
    @Selector("contentCenter")
    @ByValue
    public native CGPoint contentCenter();

    @Generated
    @Selector("contentHeight")
    public native double contentHeight();

    @Generated
    @Selector("contentLeft")
    public native double contentLeft();

    @Generated
    @Selector("contentRect")
    @ByValue
    public native CGRect contentRect();

    @Generated
    @Selector("contentRight")
    public native double contentRight();

    @Generated
    @Selector("contentTop")
    public native double contentTop();

    @Generated
    @Selector("contentWidth")
    public native double contentWidth();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it’s bounds.
     */
    @Generated
    @Selector("fitScreen")
    @ByValue
    public native CGAffineTransform fitScreen();

    @Generated
    @Selector("hasChartDimens")
    public native boolean hasChartDimens();

    /**
     * <code>true</code> if both drag offsets (x and y) are zero or smaller.
     */
    @Generated
    @Selector("hasNoDragOffset")
    public native boolean hasNoDragOffset();

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    @Generated
    @Selector("init")
    public native ChartViewPortHandler init();

    /**
     * Constructor - don’t forget calling setChartDimens(…)
     */
    @Generated
    @Selector("initWithWidth:height:")
    public native ChartViewPortHandler initWithWidthHeight(double width, double height);

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
     * if the chart is fully zoomed out, return true
     */
    @Generated
    @Selector("isFullyZoomedOut")
    public native boolean isFullyZoomedOut();

    /**
     * <code>true</code> if the chart is fully zoomed out on it’s x-axis (horizontal).
     */
    @Generated
    @Selector("isFullyZoomedOutX")
    public native boolean isFullyZoomedOutX();

    /**
     * <code>true</code> if the chart is fully zoomed out on it’s y-axis (vertical).
     */
    @Generated
    @Selector("isFullyZoomedOutY")
    public native boolean isFullyZoomedOutY();

    @Generated
    @Selector("isInBoundsBottom:")
    public native boolean isInBoundsBottom(double y);

    @Generated
    @Selector("isInBoundsLeft:")
    public native boolean isInBoundsLeft(double x);

    @Generated
    @Selector("isInBoundsRight:")
    public native boolean isInBoundsRight(double x);

    @Generated
    @Selector("isInBoundsTop:")
    public native boolean isInBoundsTop(double y);

    /**
     * A method to check whether coordinate lies within the viewport.
     * \param point a coordinate.
     */
    @Generated
    @Selector("isInBoundsWithPoint:")
    public native boolean isInBoundsWithPoint(@ByValue CGPoint point);

    @Generated
    @Selector("isInBoundsWithX:y:")
    public native boolean isInBoundsWithXY(double x, double y);

    @Generated
    @Selector("isInBoundsX:")
    public native boolean isInBoundsX(double x);

    @Generated
    @Selector("isInBoundsY:")
    public native boolean isInBoundsY(double y);

    /**
     * A method to check whether a line between two coordinates intersects with the view port  by using a linear function.
     * Linear function (calculus): <code>y = ax + b</code>
     * Note: this method will not check for collision with the right edge of the view port, as we assume lines run from left
     * to right (e.g. <code>startPoint < endPoint</code>).
     * \param startPoint the start coordinate of the line.
     * 
     * \param endPoint the end coordinate of the line.
     */
    @Generated
    @Selector("isIntersectingLineFrom:to:")
    public native boolean isIntersectingLineFromTo(@ByValue CGPoint startPoint, @ByValue CGPoint endPoint);

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    /**
     * The minimum x-scale factor
     */
    @Generated
    @Selector("maxScaleX")
    public native double maxScaleX();

    /**
     * The minimum y-scale factor
     */
    @Generated
    @Selector("maxScaleY")
    public native double maxScaleY();

    /**
     * The minimum x-scale factor
     */
    @Generated
    @Selector("minScaleX")
    public native double minScaleX();

    /**
     * The minimum y-scale factor
     */
    @Generated
    @Selector("minScaleY")
    public native double minScaleY();

    @Generated
    @Owned
    @Selector("new")
    public static native ChartViewPortHandler new_objc();

    @Generated
    @Selector("offsetBottom")
    public native double offsetBottom();

    @Generated
    @Selector("offsetLeft")
    public native double offsetLeft();

    @Generated
    @Selector("offsetRight")
    public native double offsetRight();

    @Generated
    @Selector("offsetTop")
    public native double offsetTop();

    /**
     * call this method to refresh the graph with a given matrix
     */
    @Generated
    @Selector("refreshWithNewMatrix:chart:invalidate:")
    @ByValue
    public native CGAffineTransform refreshWithNewMatrixChartInvalidate(@ByValue CGAffineTransform newMatrix,
            @NotNull ChartViewBase chart, boolean invalidate);

    /**
     * Zooms out to original size.
     */
    @Generated
    @Selector("resetZoom")
    @ByValue
    public native CGAffineTransform resetZoom();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    @Generated
    @Selector("restrainViewPortWithOffsetLeft:offsetTop:offsetRight:offsetBottom:")
    public native void restrainViewPortWithOffsetLeftOffsetTopOffsetRightOffsetBottom(double offsetLeft,
            double offsetTop, double offsetRight, double offsetBottom);

    /**
     * The current x-scale factor
     */
    @Generated
    @Selector("scaleX")
    public native double scaleX();

    /**
     * The current y-scale factor
     */
    @Generated
    @Selector("scaleY")
    public native double scaleY();

    @Generated
    @Selector("setChartDimensWithWidth:height:")
    public native void setChartDimensWithWidthHeight(double width, double height);

    /**
     * Set an offset in pixels that allows the user to drag the chart over it’s bounds on the x-axis.
     */
    @Generated
    @Selector("setDragOffsetX:")
    public native void setDragOffsetX(double offset);

    /**
     * Set an offset in pixels that allows the user to drag the chart over it’s bounds on the y-axis.
     */
    @Generated
    @Selector("setDragOffsetY:")
    public native void setDragOffsetY(double offset);

    /**
     * Sets the maximum scale factor for the x-axis
     */
    @Generated
    @Selector("setMaximumScaleX:")
    public native void setMaximumScaleX(double xScale);

    /**
     * Sets the maximum scale factor for the y-axis
     */
    @Generated
    @Selector("setMaximumScaleY:")
    public native void setMaximumScaleY(double yScale);

    /**
     * Sets the minimum and maximum scale factors for the x-axis
     */
    @Generated
    @Selector("setMinMaxScaleXWithMinScaleX:maxScaleX:")
    public native void setMinMaxScaleXWithMinScaleXMaxScaleX(double minScaleX, double maxScaleX);

    @Generated
    @Selector("setMinMaxScaleYWithMinScaleY:maxScaleY:")
    public native void setMinMaxScaleYWithMinScaleYMaxScaleY(double minScaleY, double maxScaleY);

    /**
     * Sets the minimum scale factor for the x-axis
     */
    @Generated
    @Selector("setMinimumScaleX:")
    public native void setMinimumScaleX(double xScale);

    /**
     * Sets the minimum scale factor for the y-axis
     */
    @Generated
    @Selector("setMinimumScaleY:")
    public native void setMinimumScaleY(double yScale);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * Sets the scale factor to the specified values.
     */
    @Generated
    @Selector("setZoomWithScaleX:scaleY:")
    @ByValue
    public native CGAffineTransform setZoomWithScaleXScaleY(double scaleX, double scaleY);

    /**
     * Sets the scale factor to the specified values. x and y is pivot.
     */
    @Generated
    @Selector("setZoomWithScaleX:scaleY:x:y:")
    @ByValue
    public native CGAffineTransform setZoomWithScaleXScaleYXY(double scaleX, double scaleY, double x, double y);

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Selector("touchMatrix")
    @ByValue
    public native CGAffineTransform touchMatrix();

    /**
     * The translation (drag / pan) distance on the x-axis
     */
    @Generated
    @Selector("transX")
    public native double transX();

    /**
     * The translation (drag / pan) distance on the y-axis
     */
    @Generated
    @Selector("transY")
    public native double transY();

    /**
     * Translates to the specified point.
     */
    @Generated
    @Selector("translateWithPt:")
    @ByValue
    public native CGAffineTransform translateWithPt(@ByValue CGPoint pt);

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    /**
     * Zooms in by 1.4, x and y are the coordinates (in pixels) of the zoom center.
     */
    @Generated
    @Selector("zoomInX:y:")
    @ByValue
    public native CGAffineTransform zoomInXY(double x, double y);

    /**
     * Zooms out by 0.7, x and y are the coordinates (in pixels) of the zoom center.
     */
    @Generated
    @Selector("zoomOutWithX:y:")
    @ByValue
    public native CGAffineTransform zoomOutWithXY(double x, double y);

    /**
     * Zooms by the specified zoom factors.
     */
    @Generated
    @Selector("zoomWithScaleX:scaleY:")
    @ByValue
    public native CGAffineTransform zoomWithScaleXScaleY(double scaleX, double scaleY);

    /**
     * Zooms around the specified center
     */
    @Generated
    @Selector("zoomWithScaleX:scaleY:x:y:")
    @ByValue
    public native CGAffineTransform zoomWithScaleXScaleYXY(double scaleX, double scaleY, double x, double y);
}