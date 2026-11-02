package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
import apple.foundation.NSArray;
import apple.foundation.NSCoder;
import apple.foundation.NSDate;
import apple.foundation.NSDictionary;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
import apple.uikit.UIColor;
import apple.uikit.UIEvent;
import apple.uikit.UIGestureRecognizer;
import apple.uikit.UIPress;
import apple.uikit.UITouch;
import apple.uikit.UITraitCollection;
import apple.uikit.UIView;
import apple.uikit.protocol.UIAppearanceContainer;
import apple.uikit.protocol.UIGestureRecognizerDelegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moe.natj.c.ann.FunctionPtr;
import org.moe.natj.c.ann.Variadic;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.MappedReturn;
import org.moe.natj.general.ann.NFloat;
import org.moe.natj.general.ann.NInt;
import org.moe.natj.general.ann.NUInt;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.general.ptr.VoidPtr;
import org.moe.natj.objc.Class;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.IsOptional;
import org.moe.natj.objc.ann.ObjCBlock;
import org.moe.natj.objc.ann.ObjCClassBinding;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.ProtocolClassMethod;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;
import org.moe.samples.simplechart.charts.protocol.BarLineScatterCandleBubbleChartDataProvider;
import org.moe.samples.simplechart.charts.protocol.IBarLineScatterCandleBubbleChartDataSet;

/**
 * Base-class of LineChart, BarChart, ScatterChart and CandleStickChart.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts20BarLineChartViewBase")
@ObjCClassBinding
public class BarLineChartViewBase extends ChartViewBase implements BarLineScatterCandleBubbleChartDataProvider,
        UIGestureRecognizerDelegate {
    static {
        NatJ.register();
    }

    @Generated
    protected BarLineChartViewBase(Pointer peer) {
        super(peer);
    }

    @Generated
    @Selector("accessInstanceVariablesDirectly")
    public static native boolean accessInstanceVariablesDirectly();

    @Generated
    @Selector("addKeyframeWithRelativeStartTime:relativeDuration:animations:")
    public static native void addKeyframeWithRelativeStartTimeRelativeDurationAnimations(
            double frameStartTime,
            double frameDuration,
            @ObjCBlock(name = "call_addKeyframeWithRelativeStartTimeRelativeDurationAnimations") @NotNull UIView.Block_addKeyframeWithRelativeStartTimeRelativeDurationAnimations animations);

    @Generated
    @Owned
    @Selector("alloc")
    public static native BarLineChartViewBase alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native BarLineChartViewBase allocWithZone(VoidPtr zone);

    @Generated
    @Selector("animateKeyframesWithDuration:delay:options:animations:completion:")
    public static native void animateKeyframesWithDurationDelayOptionsAnimationsCompletion(
            double duration,
            double delay,
            @NUInt long options,
            @ObjCBlock(name = "call_animateKeyframesWithDurationDelayOptionsAnimationsCompletion_3") @NotNull UIView.Block_animateKeyframesWithDurationDelayOptionsAnimationsCompletion_3 animations,
            @ObjCBlock(name = "call_animateKeyframesWithDurationDelayOptionsAnimationsCompletion_4") @Nullable UIView.Block_animateKeyframesWithDurationDelayOptionsAnimationsCompletion_4 completion);

    @Generated
    @Selector("animateWithDuration:animations:")
    public static native void animateWithDurationAnimations(
            double duration,
            @ObjCBlock(name = "call_animateWithDurationAnimations") @NotNull UIView.Block_animateWithDurationAnimations animations);

    @Generated
    @Selector("animateWithDuration:animations:completion:")
    public static native void animateWithDurationAnimationsCompletion(
            double duration,
            @ObjCBlock(name = "call_animateWithDurationAnimationsCompletion_1") @NotNull UIView.Block_animateWithDurationAnimationsCompletion_1 animations,
            @ObjCBlock(name = "call_animateWithDurationAnimationsCompletion_2") @Nullable UIView.Block_animateWithDurationAnimationsCompletion_2 completion);

    @Generated
    @Selector("animateWithDuration:delay:options:animations:completion:")
    public static native void animateWithDurationDelayOptionsAnimationsCompletion(
            double duration,
            double delay,
            @NUInt long options,
            @ObjCBlock(name = "call_animateWithDurationDelayOptionsAnimationsCompletion_3") @NotNull UIView.Block_animateWithDurationDelayOptionsAnimationsCompletion_3 animations,
            @ObjCBlock(name = "call_animateWithDurationDelayOptionsAnimationsCompletion_4") @Nullable UIView.Block_animateWithDurationDelayOptionsAnimationsCompletion_4 completion);

    @Generated
    @Selector("animateWithDuration:delay:usingSpringWithDamping:initialSpringVelocity:options:animations:completion:")
    public static native void animateWithDurationDelayUsingSpringWithDampingInitialSpringVelocityOptionsAnimationsCompletion(
            double duration,
            double delay,
            @NFloat double dampingRatio,
            @NFloat double velocity,
            @NUInt long options,
            @ObjCBlock(name = "call_animateWithDurationDelayUsingSpringWithDampingInitialSpringVelocityOptionsAnimationsCompletion_5") @NotNull UIView.Block_animateWithDurationDelayUsingSpringWithDampingInitialSpringVelocityOptionsAnimationsCompletion_5 animations,
            @ObjCBlock(name = "call_animateWithDurationDelayUsingSpringWithDampingInitialSpringVelocityOptionsAnimationsCompletion_6") @Nullable UIView.Block_animateWithDurationDelayUsingSpringWithDampingInitialSpringVelocityOptionsAnimationsCompletion_6 completion);

    @Generated
    @Selector("animateWithSpringDuration:bounce:initialSpringVelocity:delay:options:animations:completion:")
    public static native void animateWithSpringDurationBounceInitialSpringVelocityDelayOptionsAnimationsCompletion(
            double duration,
            @NFloat double bounce,
            @NFloat double velocity,
            double delay,
            @NUInt long options,
            @ObjCBlock(name = "call_animateWithSpringDurationBounceInitialSpringVelocityDelayOptionsAnimationsCompletion_5") @NotNull UIView.Block_animateWithSpringDurationBounceInitialSpringVelocityDelayOptionsAnimationsCompletion_5 animations,
            @ObjCBlock(name = "call_animateWithSpringDurationBounceInitialSpringVelocityDelayOptionsAnimationsCompletion_6") @Nullable UIView.Block_animateWithSpringDurationBounceInitialSpringVelocityDelayOptionsAnimationsCompletion_6 completion);

    @Generated
    @Selector("appearance")
    @NotNull
    public static native BarLineChartViewBase appearance();

    @Generated
    @ProtocolClassMethod("appearance")
    @NotNull
    public BarLineChartViewBase _appearance() {
        return appearance();
    }

    @Generated
    @Selector("appearanceForTraitCollection:")
    @NotNull
    public static native BarLineChartViewBase appearanceForTraitCollection(@NotNull UITraitCollection trait);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollection")
    @NotNull
    public BarLineChartViewBase _appearanceForTraitCollection(@NotNull UITraitCollection trait) {
        return appearanceForTraitCollection(trait);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceForTraitCollection:whenContainedIn:")
    @NotNull
    public static native BarLineChartViewBase appearanceForTraitCollectionWhenContainedIn(
            @NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedIn")
    @NotNull
    public BarLineChartViewBase _appearanceForTraitCollectionWhenContainedIn(@NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs) {
        return appearanceForTraitCollectionWhenContainedIn(trait, ContainerClass, varargs);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceWhenContainedIn:")
    @NotNull
    public static native BarLineChartViewBase appearanceWhenContainedIn(
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceWhenContainedIn")
    @NotNull
    public BarLineChartViewBase _appearanceWhenContainedIn(
            @Nullable Class ContainerClass, Object... varargs) {
        return appearanceWhenContainedIn(ContainerClass, varargs);
    }

    @Generated
    @Selector("areAnimationsEnabled")
    public static native boolean areAnimationsEnabled();

    /**
     * flag that indicates if auto scaling on the y axis is enabled.
     * if yes, the y axis automatically adjusts to the min and max y values of the current x axis range whenever the viewport changes
     */
    @Generated
    @Selector("autoScaleMinMaxEnabled")
    public native boolean autoScaleMinMaxEnabled();

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Deprecated
    @Selector("beginAnimations:context:")
    public static native void beginAnimationsContext(@Nullable String animationID, @Nullable VoidPtr context);

    @Generated
    @Selector("borderColor")
    @NotNull
    public native UIColor borderColor();

    @Generated
    @Selector("borderLineWidth")
    public native double borderLineWidth();

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
    @Selector("centerOffsets")
    @ByValue
    public native CGPoint centerOffsets();

    /**
     * This will move the center of the current viewport to the specified x-value and y-value animated.
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("centerViewToAnimatedWithXValue:yValue:axis:duration:")
    public native void centerViewToAnimatedWithXValueYValueAxisDuration(double xValue, double yValue, long axis,
            double duration);

    /**
     * This will move the center of the current viewport to the specified x-value and y-value animated.
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("centerViewToAnimatedWithXValue:yValue:axis:duration:easing:")
    public native void centerViewToAnimatedWithXValueYValueAxisDurationEasing(
            double xValue,
            double yValue,
            long axis,
            double duration,
            @ObjCBlock(name = "call_centerViewToAnimatedWithXValueYValueAxisDurationEasing") @Nullable Block_centerViewToAnimatedWithXValueYValueAxisDurationEasing easing);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_centerViewToAnimatedWithXValueYValueAxisDurationEasing {
        @Generated
        double call_centerViewToAnimatedWithXValueYValueAxisDurationEasing(double arg0, double arg1);
    }

    /**
     * This will move the center of the current viewport to the specified x-value and y-value animated.
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("centerViewToAnimatedWithXValue:yValue:axis:duration:easingOption:")
    public native void centerViewToAnimatedWithXValueYValueAxisDurationEasingOption(double xValue, double yValue,
            long axis, double duration, long easingOption);

    /**
     * This will move the center of the current viewport to the specified x-value and y-value.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis - which axis should be used as a reference for the y-axis
     */
    @Generated
    @Selector("centerViewToXValue:yValue:axis:")
    public native void centerViewToXValueYValueAxis(double xValue, double yValue, long axis);

    @Generated
    @Selector("chartXMax")
    public native double chartXMax();

    @Generated
    @Selector("chartXMin")
    public native double chartXMin();

    @Generated
    @Selector("chartYMax")
    public native double chartYMax();

    @Generated
    @Selector("chartYMin")
    public native double chartYMin();

    @Generated
    @Selector("classFallbacksForKeyedArchiver")
    @NotNull
    public static native NSArray<String> classFallbacksForKeyedArchiver();

    @Generated
    @Selector("classForKeyedUnarchiver")
    @NotNull
    public static native Class classForKeyedUnarchiver();

    @Generated
    @Selector("clearTextInputContextIdentifier:")
    public static native void clearTextInputContextIdentifier(@NotNull String identifier);

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     */
    @Generated
    @Selector("clipDataToContentEnabled")
    public native boolean clipDataToContentEnabled();

    /**
     * When enabled, the values will be clipped to contentRect, otherwise they can bleed outside the content rect.
     */
    @Generated
    @Selector("clipValuesToContentEnabled")
    public native boolean clipValuesToContentEnabled();

    @Generated
    @Deprecated
    @Selector("commitAnimations")
    public static native void commitAnimations();

    @Generated
    @Selector("data")
    @Nullable
    public native ChartData data();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * flag that indicates if double tap zoom is enabled or not
     */
    @Generated
    @Selector("doubleTapToZoomEnabled")
    public native boolean doubleTapToZoomEnabled();

    /**
     * is dragging enabled? (moving the chart with the finger) for the chart (this does not affect scaling).
     */
    @Generated
    @Selector("dragEnabled")
    public native boolean dragEnabled();

    /**
     * is dragging on the X axis enabled?
     */
    @Generated
    @Selector("dragXEnabled")
    public native boolean dragXEnabled();

    /**
     * is dragging on the Y axis enabled?
     */
    @Generated
    @Selector("dragYEnabled")
    public native boolean dragYEnabled();

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     */
    @Generated
    @Selector("drawBordersEnabled")
    public native boolean drawBordersEnabled();

    /**
     * flag indicating if the grid background should be drawn or not
     */
    @Generated
    @Selector("drawGridBackgroundEnabled")
    public native boolean drawGridBackgroundEnabled();

    @Generated
    @Selector("drawRect:")
    public native void drawRect(@ByValue CGRect rect);

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it’s bounds.
     */
    @Generated
    @Selector("fitScreen")
    public native void fitScreen();

    @Generated
    @IsOptional
    @Selector("gestureRecognizer:shouldBeRequiredToFailByGestureRecognizer:")
    public native boolean gestureRecognizerShouldBeRequiredToFailByGestureRecognizer(
            @NotNull UIGestureRecognizer gestureRecognizer, @NotNull UIGestureRecognizer otherGestureRecognizer);

    @Generated
    @IsOptional
    @Selector("gestureRecognizer:shouldReceiveEvent:")
    public native boolean gestureRecognizerShouldReceiveEvent(@NotNull UIGestureRecognizer gestureRecognizer,
            @NotNull UIEvent event);

    @Generated
    @IsOptional
    @Selector("gestureRecognizer:shouldReceivePress:")
    public native boolean gestureRecognizerShouldReceivePress(@NotNull UIGestureRecognizer gestureRecognizer,
            @NotNull UIPress press);

    @Generated
    @IsOptional
    @Selector("gestureRecognizer:shouldReceiveTouch:")
    public native boolean gestureRecognizerShouldReceiveTouch(@NotNull UIGestureRecognizer gestureRecognizer,
            @NotNull UITouch touch);

    @Generated
    @Selector("gestureRecognizer:shouldRecognizeSimultaneouslyWithGestureRecognizer:")
    public native boolean gestureRecognizerShouldRecognizeSimultaneouslyWithGestureRecognizer(
            @NotNull UIGestureRecognizer gestureRecognizer, @NotNull UIGestureRecognizer otherGestureRecognizer);

    @Generated
    @IsOptional
    @Selector("gestureRecognizer:shouldRequireFailureOfGestureRecognizer:")
    public native boolean gestureRecognizerShouldRequireFailureOfGestureRecognizer(
            @NotNull UIGestureRecognizer gestureRecognizer, @NotNull UIGestureRecognizer otherGestureRecognizer);

    @Generated
    @Selector("gestureRecognizerShouldBegin:")
    public native boolean gestureRecognizerShouldBegin(@NotNull UIGestureRecognizer gestureRecognizer);

    /**
     * returns:
     * The y-axis object to the corresponding AxisDependency. In the
     * horizontal bar-chart, LEFT == top, RIGHT == BOTTOM
     */
    @Generated
    @Selector("getAxis:")
    @NotNull
    public native ChartYAxis getAxis(long axis);

    /**
     * returns:
     * The range of the specified axis.
     */
    @Generated
    @Selector("getAxisRangeWithAxis:")
    public native double getAxisRangeWithAxis(long axis);

    /**
     * returns:
     * The DataSet object displayed at the touched position of the chart
     */
    @Generated
    @Selector("getDataSetByTouchPointWithPoint:")
    @MappedReturn(ObjCObjectMapper.class)
    @Nullable
    public native IBarLineScatterCandleBubbleChartDataSet getDataSetByTouchPointWithPoint(@ByValue CGPoint pt);

    /**
     * returns:
     * The Entry object displayed at the touched position of the chart
     */
    @Generated
    @Selector("getEntryByTouchPointWithPoint:")
    public native ChartDataEntry getEntryByTouchPointWithPoint(@ByValue CGPoint pt);

    /**
     * returns:
     * The position (in pixels) the provided Entry has inside the chart view
     */
    @Generated
    @Selector("getPositionWithEntry:axis:")
    @ByValue
    public native CGPoint getPositionWithEntryAxis(@NotNull ChartDataEntry e, long axis);

    @Generated
    @Selector("getTransformerForAxis:")
    @NotNull
    public native ChartTransformer getTransformerForAxis(long axis);

    /**
     * Zero (0.0) means there’s no maximum width
     * <em>default</em>: 0.0 (no maximum specified)
     * 
     * returns:
     * The (custom) maximum width of the specified Y axis.
     */
    @Generated
    @Selector("getYAxisMaxWidth:")
    public native double getYAxisMaxWidth(long axis);

    /**
     * <em>default</em>: 0.0
     * 
     * returns:
     * The (custom) minimum width of the specified Y axis.
     */
    @Generated
    @Selector("getYAxisMinWidth:")
    public native double getYAxisMinWidth(long axis);

    /**
     * <ul>
     *   <li>
     *     Returns the width of the specified y axis.
     *   </li>
     * </ul>
     */
    @Generated
    @Selector("getYAxisWidth:")
    public native double getYAxisWidth(long axis);

    /**
     * the color for the background of the chart-drawing area (everything behind the grid lines).
     */
    @Generated
    @Selector("gridBackgroundColor")
    @NotNull
    public native UIColor gridBackgroundColor();

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
    @Selector("highestVisibleX")
    public native double highestVisibleX();

    /**
     * flag that indicates if highlighting per dragging over a fully zoomed out chart is enabled
     */
    @Generated
    @Selector("highlightPerDragEnabled")
    public native boolean highlightPerDragEnabled();

    @Generated
    @Selector("inheritedAnimationDuration")
    public static native double inheritedAnimationDuration();

    @Generated
    @Selector("init")
    public native BarLineChartViewBase init();

    @Generated
    @Selector("initWithCoder:")
    public native BarLineChartViewBase initWithCoder(@NotNull NSCoder aDecoder);

    @Generated
    @Selector("initWithFrame:")
    public native BarLineChartViewBase initWithFrame(@ByValue CGRect frame);

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
     * <code>true</code> if either the left or the right or both axes are inverted.
     */
    @Generated
    @Selector("isAnyAxisInverted")
    public native boolean isAnyAxisInverted();

    /**
     * <em>default</em>: false
     * <code>true</code> if auto scaling on the y axis is enabled.
     */
    @Generated
    @Selector("isAutoScaleMinMaxEnabled")
    public native boolean isAutoScaleMinMaxEnabled();

    /**
     * <em>default</em>: true
     * <code>true</code> if zooming via double-tap is enabled <code>false</code> ifnot.
     */
    @Generated
    @Selector("isDoubleTapToZoomEnabled")
    public native boolean isDoubleTapToZoomEnabled();

    /**
     * is dragging enabled? (moving the chart with the finger) for the chart (this does not affect scaling).
     */
    @Generated
    @Selector("isDragEnabled")
    public native boolean isDragEnabled();

    /**
     * <em>default</em>: false
     * <code>true</code> if drawing the borders rectangle is enabled, <code>false</code> ifnot.
     */
    @Generated
    @Selector("isDrawBordersEnabled")
    public native boolean isDrawBordersEnabled();

    /**
     * <em>default</em>: true
     * <code>true</code> if drawing the grid background is enabled, <code>false</code> ifnot.
     */
    @Generated
    @Selector("isDrawGridBackgroundEnabled")
    public native boolean isDrawGridBackgroundEnabled();

    /**
     * if the chart is fully zoomed out, return true
     */
    @Generated
    @Selector("isFullyZoomedOut")
    public native boolean isFullyZoomedOut();

    /**
     * If set to true, highlighting per dragging over a fully zoomed out chart is enabled
     * You might want to disable this when using inside a <code>NSUIScrollView</code>
     * <em>default</em>: true
     */
    @Generated
    @Selector("isHighlightPerDragEnabled")
    public native boolean isHighlightPerDragEnabled();

    @Generated
    @Selector("isInvertedWithAxis:")
    public native boolean isInvertedWithAxis(long axis);

    /**
     * <em>default</em>: false
     * <code>true</code> if pinch-zoom is enabled, <code>false</code> ifnot
     */
    @Generated
    @Selector("isPinchZoomEnabled")
    public native boolean isPinchZoomEnabled();

    @Generated
    @Selector("isScaleXEnabled")
    public native boolean isScaleXEnabled();

    @Generated
    @Selector("isScaleYEnabled")
    public native boolean isScaleYEnabled();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    /**
     * Sets whether the chart should keep its position (zoom / scroll) after a rotation (orientation change)
     * <em>default</em>: false
     */
    @Generated
    @Selector("keepPositionOnRotation")
    public native boolean keepPositionOnRotation();

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Selector("layerClass")
    @NotNull
    public static native Class layerClass();

    /**
     * The left y-axis object. In the horizontal bar-chart, this is the
     * top axis.
     */
    @Generated
    @Selector("leftAxis")
    @NotNull
    public native ChartYAxis leftAxis();

    /**
     * The left Y axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of YAxisRenderer
     */
    @Generated
    @Selector("leftYAxisRenderer")
    @NotNull
    public native ChartYAxisRenderer leftYAxisRenderer();

    @Generated
    @Selector("lowestVisibleX")
    public native double lowestVisibleX();

    @Generated
    @Selector("maxHighlightDistance")
    public native double maxHighlightDistance();

    @Generated
    @Selector("maxVisibleCount")
    public native long maxVisibleCount();

    /**
     * Sets the minimum offset (padding) around the chart, defaults to 10
     */
    @Generated
    @Selector("minOffset")
    public native double minOffset();

    @Generated
    @Selector("modifyAnimationsWithRepeatCount:autoreverses:animations:")
    public static native void modifyAnimationsWithRepeatCountAutoreversesAnimations(
            @NFloat double count,
            boolean autoreverses,
            @ObjCBlock(name = "call_modifyAnimationsWithRepeatCountAutoreversesAnimations") @NotNull UIView.Block_modifyAnimationsWithRepeatCountAutoreversesAnimations animations);

    /**
     * This will move the left side of the current viewport to the specified x-position and center the viewport to the specified y-position animated.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("moveViewToAnimatedWithXValue:yValue:axis:duration:")
    public native void moveViewToAnimatedWithXValueYValueAxisDuration(double xValue, double yValue, long axis,
            double duration);

    /**
     * This will move the left side of the current viewport to the specified x-position and center the viewport to the specified y-position animated.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("moveViewToAnimatedWithXValue:yValue:axis:duration:easing:")
    public native void moveViewToAnimatedWithXValueYValueAxisDurationEasing(
            double xValue,
            double yValue,
            long axis,
            double duration,
            @ObjCBlock(name = "call_moveViewToAnimatedWithXValueYValueAxisDurationEasing") @Nullable Block_moveViewToAnimatedWithXValueYValueAxisDurationEasing easing);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_moveViewToAnimatedWithXValueYValueAxisDurationEasing {
        @Generated
        double call_moveViewToAnimatedWithXValueYValueAxisDurationEasing(double arg0, double arg1);
    }

    /**
     * This will move the left side of the current viewport to the specified x-position and center the viewport to the specified y-position animated.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("moveViewToAnimatedWithXValue:yValue:axis:duration:easingOption:")
    public native void moveViewToAnimatedWithXValueYValueAxisDurationEasingOption(double xValue, double yValue,
            long axis, double duration, long easingOption);

    /**
     * Moves the left side of the current viewport to the specified x-value.
     * This also refreshes the chart by calling setNeedsDisplay().
     */
    @Generated
    @Selector("moveViewToX:")
    public native void moveViewToX(double xValue);

    /**
     * This will move the left side of the current viewport to the specified x-value on the x-axis, and center the viewport to the specified y-value on the y-axis.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis - which axis should be used as a reference for the y-axis
     */
    @Generated
    @Selector("moveViewToXValue:yValue:axis:")
    public native void moveViewToXValueYValueAxis(double xValue, double yValue, long axis);

    /**
     * Centers the viewport to the specified y-value on the y-axis.
     * This also refreshes the chart by calling setNeedsDisplay().
     * \param yValue
     * 
     * \param axis - which axis should be used as a reference for the y-axis
     */
    @Generated
    @Selector("moveViewToY:axis:")
    public native void moveViewToYAxis(double yValue, long axis);

    @Generated
    @Owned
    @Selector("new")
    public static native BarLineChartViewBase new_objc();

    @Generated
    @Selector("notifyDataSetChanged")
    public native void notifyDataSetChanged();

    @Generated
    @Selector("observeValueForKeyPath:ofObject:change:context:")
    public native void observeValueForKeyPathOfObjectChangeContext(@Nullable String keyPath,
            @Mapped(ObjCObjectMapper.class) @Nullable Object object, @Nullable NSDictionary<String, ?> change,
            @Nullable VoidPtr context);

    @Generated
    @Selector("performSystemAnimation:onViews:options:animations:completion:")
    public static native void performSystemAnimationOnViewsOptionsAnimationsCompletion(
            @NUInt long animation,
            @NotNull NSArray<? extends UIView> views,
            @NUInt long options,
            @ObjCBlock(name = "call_performSystemAnimationOnViewsOptionsAnimationsCompletion_3") @Nullable UIView.Block_performSystemAnimationOnViewsOptionsAnimationsCompletion_3 parallelAnimations,
            @ObjCBlock(name = "call_performSystemAnimationOnViewsOptionsAnimationsCompletion_4") @Nullable UIView.Block_performSystemAnimationOnViewsOptionsAnimationsCompletion_4 completion);

    @Generated
    @Selector("performWithoutAnimation:")
    public static native void performWithoutAnimation(
            @ObjCBlock(name = "call_performWithoutAnimation") @NotNull UIView.Block_performWithoutAnimation actionsWithoutAnimation);

    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis can be scaled simultaneously with 2 fingers, if false, x and y axis can be scaled separately
     */
    @Generated
    @Selector("pinchZoomEnabled")
    public native boolean pinchZoomEnabled();

    /**
     * Transforms the given chart values into pixels. This is the opposite
     * method to <code>valueForTouchPoint(...)</code>.
     */
    @Generated
    @Selector("pixelForValuesWithX:y:axis:")
    @ByValue
    public native CGPoint pixelForValuesWithXYAxis(double x, double y, long axis);

    @Generated
    @Selector("requiresConstraintBasedLayout")
    public static native boolean requiresConstraintBasedLayout();

    /**
     * Resets all custom offsets set via <code>setViewPortOffsets(...)</code> method. Allows the chart to again calculate all offsets automatically.
     */
    @Generated
    @Selector("resetViewPortOffsets")
    public native void resetViewPortOffsets();

    /**
     * Zooms out to original size.
     */
    @Generated
    @Selector("resetZoom")
    public native void resetZoom();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    /**
     * The right y-axis object. In the horizontal bar-chart, this is the
     * bottom axis.
     */
    @Generated
    @Selector("rightAxis")
    @NotNull
    public native ChartYAxis rightAxis();

    /**
     * The right Y axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of YAxisRenderer
     */
    @Generated
    @Selector("rightYAxisRenderer")
    @NotNull
    public native ChartYAxisRenderer rightYAxisRenderer();

    /**
     * The current x-scale factor
     */
    @Generated
    @Selector("scaleX")
    public native double scaleX();

    @Generated
    @Selector("scaleXEnabled")
    public native boolean scaleXEnabled();

    /**
     * The current y-scale factor
     */
    @Generated
    @Selector("scaleY")
    public native double scaleY();

    @Generated
    @Selector("scaleYEnabled")
    public native boolean scaleYEnabled();

    @Generated
    @Deprecated
    @Selector("setAnimationBeginsFromCurrentState:")
    public static native void setAnimationBeginsFromCurrentState(boolean fromCurrentState);

    @Generated
    @Deprecated
    @Selector("setAnimationCurve:")
    public static native void setAnimationCurve(@NInt long curve);

    @Generated
    @Deprecated
    @Selector("setAnimationDelay:")
    public static native void setAnimationDelay(double delay);

    @Generated
    @Deprecated
    @Selector("setAnimationDelegate:")
    public static native void setAnimationDelegate(@Mapped(ObjCObjectMapper.class) @Nullable Object delegate);

    @Generated
    @Deprecated
    @Selector("setAnimationDidStopSelector:")
    public static native void setAnimationDidStopSelector(@Nullable SEL selector);

    @Generated
    @Deprecated
    @Selector("setAnimationDuration:")
    public static native void setAnimationDuration_static(double duration);

    @Generated
    @Deprecated
    @Selector("setAnimationRepeatAutoreverses:")
    public static native void setAnimationRepeatAutoreverses(boolean repeatAutoreverses);

    @Generated
    @Deprecated
    @Selector("setAnimationRepeatCount:")
    public static native void setAnimationRepeatCount_static(float repeatCount);

    @Generated
    @Deprecated
    @Selector("setAnimationStartDate:")
    public static native void setAnimationStartDate(@NotNull NSDate startDate);

    @Generated
    @Deprecated
    @Selector("setAnimationTransition:forView:cache:")
    public static native void setAnimationTransitionForViewCache(@NInt long transition, @NotNull UIView view,
            boolean cache);

    @Generated
    @Deprecated
    @Selector("setAnimationWillStartSelector:")
    public static native void setAnimationWillStartSelector(@Nullable SEL selector);

    @Generated
    @Selector("setAnimationsEnabled:")
    public static native void setAnimationsEnabled(boolean enabled);

    /**
     * flag that indicates if auto scaling on the y axis is enabled.
     * if yes, the y axis automatically adjusts to the min and max y values of the current x axis range whenever the viewport changes
     */
    @Generated
    @Selector("setAutoScaleMinMaxEnabled:")
    public native void setAutoScaleMinMaxEnabled(boolean value);

    @Generated
    @Selector("setBorderColor:")
    public native void setBorderColor(@NotNull UIColor value);

    @Generated
    @Selector("setBorderLineWidth:")
    public native void setBorderLineWidth(double value);

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     */
    @Generated
    @Selector("setClipDataToContentEnabled:")
    public native void setClipDataToContentEnabled(boolean value);

    /**
     * When enabled, the values will be clipped to contentRect, otherwise they can bleed outside the content rect.
     */
    @Generated
    @Selector("setClipValuesToContentEnabled:")
    public native void setClipValuesToContentEnabled(boolean value);

    /**
     * flag that indicates if double tap zoom is enabled or not
     */
    @Generated
    @Selector("setDoubleTapToZoomEnabled:")
    public native void setDoubleTapToZoomEnabled(boolean value);

    /**
     * is dragging enabled? (moving the chart with the finger) for the chart (this does not affect scaling).
     */
    @Generated
    @Selector("setDragEnabled:")
    public native void setDragEnabled(boolean value);

    /**
     * Set an offset in dp that allows the user to drag the chart over it’s
     * bounds on the x-axis.
     */
    @Generated
    @Selector("setDragOffsetX:")
    public native void setDragOffsetX(double offset);

    /**
     * Set an offset in dp that allows the user to drag the chart over it’s
     * bounds on the y-axis.
     */
    @Generated
    @Selector("setDragOffsetY:")
    public native void setDragOffsetY(double offset);

    /**
     * is dragging on the X axis enabled?
     */
    @Generated
    @Selector("setDragXEnabled:")
    public native void setDragXEnabled(boolean value);

    /**
     * is dragging on the Y axis enabled?
     */
    @Generated
    @Selector("setDragYEnabled:")
    public native void setDragYEnabled(boolean value);

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     */
    @Generated
    @Selector("setDrawBordersEnabled:")
    public native void setDrawBordersEnabled(boolean value);

    /**
     * flag indicating if the grid background should be drawn or not
     */
    @Generated
    @Selector("setDrawGridBackgroundEnabled:")
    public native void setDrawGridBackgroundEnabled(boolean value);

    /**
     * the color for the background of the chart-drawing area (everything behind the grid lines).
     */
    @Generated
    @Selector("setGridBackgroundColor:")
    public native void setGridBackgroundColor(@NotNull UIColor value);

    /**
     * flag that indicates if highlighting per dragging over a fully zoomed out chart is enabled
     */
    @Generated
    @Selector("setHighlightPerDragEnabled:")
    public native void setHighlightPerDragEnabled(boolean value);

    /**
     * Sets whether the chart should keep its position (zoom / scroll) after a rotation (orientation change)
     * <em>default</em>: false
     */
    @Generated
    @Selector("setKeepPositionOnRotation:")
    public native void setKeepPositionOnRotation(boolean value);

    /**
     * The left Y axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of YAxisRenderer
     */
    @Generated
    @Selector("setLeftYAxisRenderer:")
    public native void setLeftYAxisRenderer(@NotNull ChartYAxisRenderer value);

    /**
     * the number of maximum visible drawn values on the chart only active when <code>drawValuesEnabled</code> is enabled
     */
    @Generated
    @Selector("setMaxVisibleCount:")
    public native void setMaxVisibleCount(long value);

    /**
     * Sets the minimum offset (padding) around the chart, defaults to 10
     */
    @Generated
    @Selector("setMinOffset:")
    public native void setMinOffset(double value);

    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis can be scaled simultaneously with 2 fingers, if false, x and y axis can be scaled separately
     */
    @Generated
    @Selector("setPinchZoomEnabled:")
    public native void setPinchZoomEnabled(boolean value);

    /**
     * The right Y axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of YAxisRenderer
     */
    @Generated
    @Selector("setRightYAxisRenderer:")
    public native void setRightYAxisRenderer(@NotNull ChartYAxisRenderer value);

    /**
     * is scaling enabled? (zooming in and out by gesture) for the chart (this does not affect dragging).
     */
    @Generated
    @Selector("setScaleEnabled:")
    public native void setScaleEnabled(boolean enabled);

    /**
     * Sets the minimum scale value to which can be zoomed out. 1 = fitScreen
     */
    @Generated
    @Selector("setScaleMinima:scaleY:")
    public native void setScaleMinimaScaleY(double scaleX, double scaleY);

    @Generated
    @Selector("setScaleXEnabled:")
    public native void setScaleXEnabled(boolean value);

    @Generated
    @Selector("setScaleYEnabled:")
    public native void setScaleYEnabled(boolean value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    /**
     * Sets custom offsets for the current <code>ChartViewPort</code> (the offsets on the sides of the actual chart window). Setting this will prevent the chart from automatically calculating it’s offsets. Use <code>resetViewPortOffsets()</code> to undo this.
     * ONLY USE THIS WHEN YOU KNOW WHAT YOU ARE DOING, else use <code>setExtraOffsets(...)</code>.
     */
    @Generated
    @Selector("setViewPortOffsetsWithLeft:top:right:bottom:")
    public native void setViewPortOffsetsWithLeftTopRightBottom(double left, double top, double right, double bottom);

    /**
     * Sets the size of the area (range on the x-axis) that should be maximum visible at once (no further zooming out allowed).
     * If this is e.g. set to 10, no more than a range of 10 values on the x-axis can be viewed at once without scrolling.
     * If you call this method, chart must have data or it has no effect.
     */
    @Generated
    @Selector("setVisibleXRangeMaximum:")
    public native void setVisibleXRangeMaximum(double maxXRange);

    /**
     * Sets the size of the area (range on the x-axis) that should be minimum visible at once (no further zooming in allowed).
     * If this is e.g. set to 10, no less than a range of 10 values on the x-axis can be viewed at once without scrolling.
     * If you call this method, chart must have data or it has no effect.
     */
    @Generated
    @Selector("setVisibleXRangeMinimum:")
    public native void setVisibleXRangeMinimum(double minXRange);

    /**
     * Limits the maximum and minimum value count that can be visible by pinching and zooming.
     * e.g. minRange=10, maxRange=100 no less than 10 values and no more that 100 values can be viewed
     * at once without scrolling.
     * If you call this method, chart must have data or it has no effect.
     */
    @Generated
    @Selector("setVisibleXRangeWithMinXRange:maxXRange:")
    public native void setVisibleXRangeWithMinXRangeMaxXRange(double minXRange, double maxXRange);

    /**
     * Sets the size of the area (range on the y-axis) that should be maximum visible at once.
     * \param yRange
     * 
     * \param axis - the axis for which this limit should apply
     */
    @Generated
    @Selector("setVisibleYRangeMaximum:axis:")
    public native void setVisibleYRangeMaximumAxis(double maxYRange, long axis);

    /**
     * Sets the size of the area (range on the y-axis) that should be minimum visible at once, no further zooming in possible.
     * \param yRange
     * 
     * \param axis - the axis for which this limit should apply
     */
    @Generated
    @Selector("setVisibleYRangeMinimum:axis:")
    public native void setVisibleYRangeMinimumAxis(double minYRange, long axis);

    /**
     * Limits the maximum and minimum y range that can be visible by pinching and zooming.
     * \param minYRange
     * 
     * \param maxYRange
     * 
     * \param axis
     */
    @Generated
    @Selector("setVisibleYRangeWithMinYRange:maxYRange:axis:")
    public native void setVisibleYRangeWithMinYRangeMaxYRangeAxis(double minYRange, double maxYRange, long axis);

    /**
     * The X axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of XAxisRenderer
     */
    @Generated
    @Selector("setXAxisRenderer:")
    public native void setXAxisRenderer(@NotNull ChartXAxisRenderer value);

    /**
     * Sets a maximum width to the specified y axis.
     * Zero (0.0) means there’s no maximum width
     */
    @Generated
    @Selector("setYAxisMaxWidth:width:")
    public native void setYAxisMaxWidthWidth(long axis, double width);

    /**
     * Sets a minimum width to the specified y axis.
     */
    @Generated
    @Selector("setYAxisMinWidth:width:")
    public native void setYAxisMinWidthWidth(long axis, double width);

    @Generated
    @Selector("stopDeceleration")
    public native void stopDeceleration();

    @Generated
    @Selector("superclass")
    public static native Class superclass_static();

    @Generated
    @Selector("transitionFromView:toView:duration:options:completion:")
    public static native void transitionFromViewToViewDurationOptionsCompletion(
            @NotNull UIView fromView,
            @NotNull UIView toView,
            double duration,
            @NUInt long options,
            @ObjCBlock(name = "call_transitionFromViewToViewDurationOptionsCompletion") @Nullable UIView.Block_transitionFromViewToViewDurationOptionsCompletion completion);

    @Generated
    @Selector("transitionWithView:duration:options:animations:completion:")
    public static native void transitionWithViewDurationOptionsAnimationsCompletion(
            @NotNull UIView view,
            double duration,
            @NUInt long options,
            @ObjCBlock(name = "call_transitionWithViewDurationOptionsAnimationsCompletion_3") @Nullable UIView.Block_transitionWithViewDurationOptionsAnimationsCompletion_3 animations,
            @ObjCBlock(name = "call_transitionWithViewDurationOptionsAnimationsCompletion_4") @Nullable UIView.Block_transitionWithViewDurationOptionsAnimationsCompletion_4 completion);

    @Generated
    @Deprecated
    @Selector("useStoredAccessor")
    public static native boolean useStoredAccessor();

    @Generated
    @Selector("userInterfaceLayoutDirectionForSemanticContentAttribute:")
    @NInt
    public static native long userInterfaceLayoutDirectionForSemanticContentAttribute(@NInt long attribute);

    @Generated
    @Selector("userInterfaceLayoutDirectionForSemanticContentAttribute:relativeToLayoutDirection:")
    @NInt
    public static native long userInterfaceLayoutDirectionForSemanticContentAttributeRelativeToLayoutDirection(
            @NInt long semanticContentAttribute, @NInt long layoutDirection);

    /**
     * returns:
     * The x and y values in the chart at the given touch point
     * (encapsulated in a <code>CGPoint</code>). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * <code>getPixelsForValues(...)</code>.
     */
    @Generated
    @Selector("valueForTouchPointWithPoint:axis:")
    @ByValue
    public native CGPoint valueForTouchPointWithPointAxis(@ByValue CGPoint pt, long axis);

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    @Generated
    @Selector("visibleXRange")
    public native double visibleXRange();

    /**
     * The X axis renderer. This is a read-write property so you can set your own custom renderer here.
     * <em>default</em>: An instance of XAxisRenderer
     */
    @Generated
    @Selector("xAxisRenderer")
    @NotNull
    public native ChartXAxisRenderer xAxisRenderer();

    @Generated
    @Selector("xRange")
    public native double xRange();

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     * \param scaleX
     * 
     * \param scaleY
     * 
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("zoomAndCenterViewAnimatedWithScaleX:scaleY:xValue:yValue:axis:duration:")
    public native void zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDuration(double scaleX, double scaleY,
            double xValue, double yValue, long axis, double duration);

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     * \param scaleX
     * 
     * \param scaleY
     * 
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("zoomAndCenterViewAnimatedWithScaleX:scaleY:xValue:yValue:axis:duration:easing:")
    public native void zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasing(
            double scaleX,
            double scaleY,
            double xValue,
            double yValue,
            long axis,
            double duration,
            @ObjCBlock(name = "call_zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasing") @Nullable Block_zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasing easing);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasing {
        @Generated
        double call_zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasing(double arg0, double arg1);
    }

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     * \param scaleX
     * 
     * \param scaleY
     * 
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis which axis should be used as a reference for the y-axis
     * 
     * \param duration the duration of the animation in seconds
     * 
     * \param easing
     */
    @Generated
    @Selector("zoomAndCenterViewAnimatedWithScaleX:scaleY:xValue:yValue:axis:duration:easingOption:")
    public native void zoomAndCenterViewAnimatedWithScaleXScaleYXValueYValueAxisDurationEasingOption(double scaleX,
            double scaleY, double xValue, double yValue, long axis, double duration, long easingOption);

    /**
     * Zooms in by 1.4, into the charts center.
     */
    @Generated
    @Selector("zoomIn")
    public native void zoomIn();

    /**
     * Zooms out by 0.7, from the charts center.
     */
    @Generated
    @Selector("zoomOut")
    public native void zoomOut();

    /**
     * Zooms to the center of the chart with the given scale factor.
     * \param scaleX if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param scaleY if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis
     */
    @Generated
    @Selector("zoomToCenterWithScaleX:scaleY:")
    public native void zoomToCenterWithScaleXScaleY(double scaleX, double scaleY);

    /**
     * Zooms in or out by the given scale factor. x and y are the coordinates
     * (in pixels) of the zoom center.
     * \param scaleX if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param scaleY if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param x
     * 
     * \param y
     */
    @Generated
    @Selector("zoomWithScaleX:scaleY:x:y:")
    public native void zoomWithScaleXScaleYXY(double scaleX, double scaleY, double x, double y);

    /**
     * Zooms in or out by the given scale factor.
     * x and y are the values (<em>not pixels</em>) of the zoom center.
     * \param scaleX if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param scaleY if < 1 –> zoom out, if > 1 –> zoom in
     * 
     * \param xValue
     * 
     * \param yValue
     * 
     * \param axis
     */
    @Generated
    @Selector("zoomWithScaleX:scaleY:xValue:yValue:axis:")
    public native void zoomWithScaleXScaleYXValueYValueAxis(double scaleX, double scaleY, double xValue, double yValue,
            long axis);

    @Generated
    @Selector("appearanceForTraitCollection:whenContainedInInstancesOfClasses:")
    @NotNull
    public static native BarLineChartViewBase appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedInInstancesOfClasses")
    @NotNull
    public BarLineChartViewBase _appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes) {
        return appearanceForTraitCollectionWhenContainedInInstancesOfClasses(trait, containerTypes);
    }

    @Generated
    @Selector("appearanceWhenContainedInInstancesOfClasses:")
    @NotNull
    public static native BarLineChartViewBase appearanceWhenContainedInInstancesOfClasses(
            @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceWhenContainedInInstancesOfClasses")
    @NotNull
    public BarLineChartViewBase _appearanceWhenContainedInInstancesOfClasses(
            @NotNull NSArray<? extends Class> containerTypes) {
        return appearanceWhenContainedInInstancesOfClasses(containerTypes);
    }
}