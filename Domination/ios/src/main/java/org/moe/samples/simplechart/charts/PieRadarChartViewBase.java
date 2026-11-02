package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
import apple.foundation.NSArray;
import apple.foundation.NSCoder;
import apple.foundation.NSDate;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSSet;
import apple.uikit.UIEvent;
import apple.uikit.UITouch;
import apple.uikit.UITraitCollection;
import apple.uikit.UIView;
import apple.uikit.protocol.UIAppearanceContainer;
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
import org.moe.natj.general.ann.NFloat;
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
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.ProtocolClassMethod;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

/**
 * Base class of PieChartView and RadarChartView.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts21PieRadarChartViewBase")
@ObjCClassBinding
public class PieRadarChartViewBase extends ChartViewBase {
    static {
        NatJ.register();
    }

    @Generated
    protected PieRadarChartViewBase(Pointer peer) {
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
    public static native PieRadarChartViewBase alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native PieRadarChartViewBase allocWithZone(VoidPtr zone);

    /**
     * returns:
     * The angle relative to the chart center for the given point on the chart in degrees.
     * The angle is always between 0 and 360°, 0° is NORTH, 90° is EAST, …
     */
    @Generated
    @Selector("angleForPointWithX:y:")
    public native double angleForPointWithXY(double x, double y);

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
    public static native PieRadarChartViewBase appearance();

    @Generated
    @ProtocolClassMethod("appearance")
    @NotNull
    public PieRadarChartViewBase _appearance() {
        return appearance();
    }

    @Generated
    @Selector("appearanceForTraitCollection:")
    @NotNull
    public static native PieRadarChartViewBase appearanceForTraitCollection(@NotNull UITraitCollection trait);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollection")
    @NotNull
    public PieRadarChartViewBase _appearanceForTraitCollection(@NotNull UITraitCollection trait) {
        return appearanceForTraitCollection(trait);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceForTraitCollection:whenContainedIn:")
    @NotNull
    public static native PieRadarChartViewBase appearanceForTraitCollectionWhenContainedIn(
            @NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedIn")
    @NotNull
    public PieRadarChartViewBase _appearanceForTraitCollectionWhenContainedIn(@NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs) {
        return appearanceForTraitCollectionWhenContainedIn(trait, ContainerClass, varargs);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceWhenContainedIn:")
    @NotNull
    public static native PieRadarChartViewBase appearanceWhenContainedIn(
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceWhenContainedIn")
    @NotNull
    public PieRadarChartViewBase _appearanceWhenContainedIn(
            @Nullable Class ContainerClass, Object... varargs) {
        return appearanceWhenContainedIn(ContainerClass, varargs);
    }

    @Generated
    @Selector("areAnimationsEnabled")
    public static native boolean areAnimationsEnabled();

    @Generated
    @Selector("automaticallyNotifiesObserversForKey:")
    public static native boolean automaticallyNotifiesObserversForKey(@NotNull String key);

    @Generated
    @Deprecated
    @Selector("beginAnimations:context:")
    public static native void beginAnimationsContext(@Nullable String animationID, @Nullable VoidPtr context);

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

    @Generated
    @Deprecated
    @Selector("commitAnimations")
    public static native void commitAnimations();

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * The diameter of the pie- or radar-chart
     */
    @Generated
    @Selector("diameter")
    public native double diameter();

    /**
     * returns:
     * The distance of a certain point on the chart to the center of the chart.
     */
    @Generated
    @Selector("distanceToCenterWithX:y:")
    public native double distanceToCenterWithXY(double x, double y);

    /**
     * Calculates the position around a center point, depending on the distance
     * from the center, and the angle of the position around the center.
     */
    @Generated
    @Selector("getPositionWithCenter:dist:angle:")
    @ByValue
    public native CGPoint getPositionWithCenterDistAngle(@ByValue CGPoint center, double dist, double angle);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    /**
     * returns:
     * The xIndex for the given angle around the center of the chart.
     * -1 if not found / outofbounds.
     */
    @Generated
    @Selector("indexForAngle:")
    public native long indexForAngle(double angle);

    @Generated
    @Selector("inheritedAnimationDuration")
    public static native double inheritedAnimationDuration();

    @Generated
    @Selector("init")
    public native PieRadarChartViewBase init();

    @Generated
    @Selector("initWithCoder:")
    public native PieRadarChartViewBase initWithCoder(@NotNull NSCoder aDecoder);

    @Generated
    @Selector("initWithFrame:")
    public native PieRadarChartViewBase initWithFrame(@ByValue CGRect frame);

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
    @Selector("isRotationEnabled")
    public native boolean isRotationEnabled();

    /**
     * flag that indicates if rotation is done with two fingers or one.
     * when the chart is inside a scrollview, you need a two-finger rotation because a one-finger rotation eats up all touch events.
     * On iOS this will disable one-finger rotation.
     * On OSX this will keep two-finger multitouch rotation, and one-pointer mouse rotation.
     * <em>default</em>: false
     */
    @Generated
    @Selector("isRotationWithTwoFingers")
    public native boolean isRotationWithTwoFingers();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Selector("layerClass")
    @NotNull
    public static native Class layerClass();

    @Generated
    @Selector("maxVisibleCount")
    public native long maxVisibleCount();

    /**
     * Sets the minimum offset (padding) around the chart, defaults to 0.0
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

    @Generated
    @Owned
    @Selector("new")
    public static native PieRadarChartViewBase new_objc();

    @Generated
    @Selector("notifyDataSetChanged")
    public native void notifyDataSetChanged();

    @Generated
    @Selector("nsuiTouchesBegan:withEvent:")
    public native void nsuiTouchesBeganWithEvent(@NotNull NSSet<? extends UITouch> touches, @Nullable UIEvent event);

    @Generated
    @Selector("nsuiTouchesCancelled:withEvent:")
    public native void nsuiTouchesCancelledWithEvent(@Nullable NSSet<? extends UITouch> touches, @Nullable UIEvent event);

    @Generated
    @Selector("nsuiTouchesEnded:withEvent:")
    public native void nsuiTouchesEndedWithEvent(@NotNull NSSet<? extends UITouch> touches, @Nullable UIEvent event);

    @Generated
    @Selector("nsuiTouchesMoved:withEvent:")
    public native void nsuiTouchesMovedWithEvent(@NotNull NSSet<? extends UITouch> touches, @Nullable UIEvent event);

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
     * The radius of the chart in pixels.
     */
    @Generated
    @Selector("radius")
    public native double radius();

    /**
     * gets the raw version of the current rotation angle of the pie chart the returned value could be any value, negative or positive, outside of the 360 degrees.
     * this is used when working with rotation direction, mainly by gestures and animations.
     */
    @Generated
    @Selector("rawRotationAngle")
    public native double rawRotationAngle();

    @Generated
    @Selector("requiresConstraintBasedLayout")
    public static native boolean requiresConstraintBasedLayout();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

    /**
     * current rotation angle of the pie chart
     * <em>default</em>: 270 –> top (NORTH)
     * Will always return a normalized value, which will be between 0.0 < 360.0
     */
    @Generated
    @Selector("rotationAngle")
    public native double rotationAngle();

    /**
     * flag that indicates if rotation is enabled or not
     */
    @Generated
    @Selector("rotationEnabled")
    public native boolean rotationEnabled();

    /**
     * flag that indicates if rotation is done with two fingers or one.
     * when the chart is inside a scrollview, you need a two-finger rotation because a one-finger rotation eats up all touch events.
     * On iOS this will disable one-finger rotation.
     * On OSX this will keep two-finger multitouch rotation, and one-pointer mouse rotation.
     * <em>default</em>: false
     */
    @Generated
    @Selector("rotationWithTwoFingers")
    public native boolean rotationWithTwoFingers();

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
     * Sets the minimum offset (padding) around the chart, defaults to 0.0
     */
    @Generated
    @Selector("setMinOffset:")
    public native void setMinOffset(double value);

    /**
     * current rotation angle of the pie chart
     * <em>default</em>: 270 –> top (NORTH)
     * Will always return a normalized value, which will be between 0.0 < 360.0
     */
    @Generated
    @Selector("setRotationAngle:")
    public native void setRotationAngle(double value);

    /**
     * flag that indicates if rotation is enabled or not
     */
    @Generated
    @Selector("setRotationEnabled:")
    public native void setRotationEnabled(boolean value);

    /**
     * flag that indicates if rotation is done with two fingers or one.
     * when the chart is inside a scrollview, you need a two-finger rotation because a one-finger rotation eats up all touch events.
     * On iOS this will disable one-finger rotation.
     * On OSX this will keep two-finger multitouch rotation, and one-pointer mouse rotation.
     * <em>default</em>: false
     */
    @Generated
    @Selector("setRotationWithTwoFingers:")
    public native void setRotationWithTwoFingers(boolean value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

    @Generated
    @Selector("spinWithDuration:fromAngle:toAngle:")
    public native void spinWithDurationFromAngleToAngle(double duration, double fromAngle, double toAngle);

    /**
     * Applys a spin animation to the Chart.
     */
    @Generated
    @Selector("spinWithDuration:fromAngle:toAngle:easing:")
    public native void spinWithDurationFromAngleToAngleEasing(
            double duration,
            double fromAngle,
            double toAngle,
            @ObjCBlock(name = "call_spinWithDurationFromAngleToAngleEasing") @Nullable Block_spinWithDurationFromAngleToAngleEasing easing);

    @Runtime(ObjCRuntime.class)
    @Generated
    public interface Block_spinWithDurationFromAngleToAngleEasing {
        @Generated
        double call_spinWithDurationFromAngleToAngleEasing(double arg0, double arg1);
    }

    @Generated
    @Selector("spinWithDuration:fromAngle:toAngle:easingOption:")
    public native void spinWithDurationFromAngleToAngleEasingOption(double duration, double fromAngle, double toAngle,
            long easingOption);

    @Generated
    @Selector("stopDeceleration")
    public native void stopDeceleration();

    @Generated
    @Selector("stopSpinAnimation")
    public native void stopSpinAnimation();

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

    @Generated
    @Selector("version")
    @NInt
    public static native long version_static();

    @Generated
    @Selector("appearanceForTraitCollection:whenContainedInInstancesOfClasses:")
    @NotNull
    public static native PieRadarChartViewBase appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedInInstancesOfClasses")
    @NotNull
    public PieRadarChartViewBase _appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes) {
        return appearanceForTraitCollectionWhenContainedInInstancesOfClasses(trait, containerTypes);
    }

    @Generated
    @Selector("appearanceWhenContainedInInstancesOfClasses:")
    @NotNull
    public static native PieRadarChartViewBase appearanceWhenContainedInInstancesOfClasses(
            @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceWhenContainedInInstancesOfClasses")
    @NotNull
    public PieRadarChartViewBase _appearanceWhenContainedInInstancesOfClasses(
            @NotNull NSArray<? extends Class> containerTypes) {
        return appearanceWhenContainedInInstancesOfClasses(containerTypes);
    }
}