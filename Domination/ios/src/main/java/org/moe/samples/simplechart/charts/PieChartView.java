package org.moe.samples.simplechart.charts;


import apple.NSObject;
import apple.corefoundation.struct.CGPoint;
import apple.corefoundation.struct.CGRect;
import apple.foundation.NSArray;
import apple.foundation.NSAttributedString;
import apple.foundation.NSCoder;
import apple.foundation.NSDate;
import apple.foundation.NSMethodSignature;
import apple.foundation.NSNumber;
import apple.foundation.NSSet;
import apple.uikit.UIColor;
import apple.uikit.UIFont;
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
 * View that represents a pie chart. Draws cake like slices.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCClassName("_TtC6Charts12PieChartView")
@ObjCClassBinding
public class PieChartView extends PieRadarChartViewBase {
    static {
        NatJ.register();
    }

    @Generated
    protected PieChartView(Pointer peer) {
        super(peer);
    }

    /**
     * returns:
     * The absolute angles of the different chart slices (where the
     * slices end)
     */
    @Generated
    @Selector("absoluteAngles")
    @NotNull
    public native NSArray<? extends NSNumber> absoluteAngles();

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
    public static native PieChartView alloc();

    @Generated
    @Owned
    @Selector("allocWithZone:")
    public static native PieChartView allocWithZone(VoidPtr zone);

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
    public static native PieChartView appearance();

    @Generated
    @ProtocolClassMethod("appearance")
    @NotNull
    public PieChartView _appearance() {
        return appearance();
    }

    @Generated
    @Selector("appearanceForTraitCollection:")
    @NotNull
    public static native PieChartView appearanceForTraitCollection(@NotNull UITraitCollection trait);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollection")
    @NotNull
    public PieChartView _appearanceForTraitCollection(@NotNull UITraitCollection trait) {
        return appearanceForTraitCollection(trait);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceForTraitCollection:whenContainedIn:")
    @NotNull
    public static native PieChartView appearanceForTraitCollectionWhenContainedIn(@NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedIn")
    @NotNull
    public PieChartView _appearanceForTraitCollectionWhenContainedIn(@NotNull UITraitCollection trait,
            @Nullable Class ContainerClass, Object... varargs) {
        return appearanceForTraitCollectionWhenContainedIn(trait, ContainerClass, varargs);
    }

    @Generated
    @Variadic()
    @Deprecated
    @Selector("appearanceWhenContainedIn:")
    @NotNull
    public static native PieChartView appearanceWhenContainedIn(
            @Nullable Class ContainerClass, Object... varargs);

    @Generated
    @Deprecated
    @ProtocolClassMethod("appearanceWhenContainedIn")
    @NotNull
    public PieChartView _appearanceWhenContainedIn(
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

    /**
     * the text that is displayed in the center of the pie-chart
     */
    @Generated
    @Selector("centerAttributedText")
    @Nullable
    public native NSAttributedString centerAttributedText();

    /**
     * The center of the circlebox
     */
    @Generated
    @Selector("centerCircleBox")
    @ByValue
    public native CGPoint centerCircleBox();

    /**
     * the text that is displayed in the center of the pie-chart
     */
    @Generated
    @Selector("centerText")
    @Nullable
    public native String centerText();

    /**
     * Sets the offset the center text should have from it’s original position in dp. Default x = 0, y = 0
     */
    @Generated
    @Selector("centerTextOffset")
    @ByValue
    public native CGPoint centerTextOffset();

    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie hole
     */
    @Generated
    @Selector("centerTextRadiusPercent")
    public native double centerTextRadiusPercent();

    /**
     * The circlebox, the boundingbox of the pie-chart slices
     */
    @Generated
    @Selector("circleBox")
    @ByValue
    public native CGRect circleBox();

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

    /**
     * returns:
     * The index of the DataSet this x-index belongs to.
     */
    @Generated
    @Selector("dataSetIndexForIndex:")
    public native long dataSetIndexForIndex(double xValue);

    @Generated
    @Selector("debugDescription")
    public static native String debugDescription_static();

    @Generated
    @Selector("description")
    public static native String description_static();

    /**
     * returns:
     * The distance of a certain point on the chart to the center of the chart.
     */
    @Generated
    @Selector("distanceToCenterWithX:y:")
    public native double distanceToCenterWithXY(double x, double y);

    /**
     * returns:
     * An integer array of all the different angles the chart slices
     * have the angles in the returned array determine how much space (of 360°)
     * each slice takes
     */
    @Generated
    @Selector("drawAngles")
    @NotNull
    public native NSArray<? extends NSNumber> drawAngles();

    /**
     * <code>true</code> if drawing the center text is enabled
     */
    @Generated
    @Selector("drawCenterTextEnabled")
    public native boolean drawCenterTextEnabled();

    /**
     * Set this to true to draw the enrty labels into the pie slices
     */
    @Generated
    @Selector("drawEntryLabelsEnabled")
    public native boolean drawEntryLabelsEnabled();

    /**
     * <code>true</code> if the hole in the center of the pie-chart is set to be visible, <code>false</code> ifnot
     */
    @Generated
    @Selector("drawHoleEnabled")
    public native boolean drawHoleEnabled();

    @Generated
    @Selector("drawRect:")
    public native void drawRect(@ByValue CGRect rect);

    /**
     * if true, the hole will see-through to the inner tips of the slices
     * <em>default</em>: <code>false</code>
     */
    @Generated
    @Selector("drawSlicesUnderHoleEnabled")
    public native boolean drawSlicesUnderHoleEnabled();

    /**
     * The color the entry labels are drawn with.
     */
    @Generated
    @Selector("entryLabelColor")
    @Nullable
    public native UIColor entryLabelColor();

    /**
     * The font the entry labels are drawn with.
     */
    @Generated
    @Selector("entryLabelFont")
    @Nullable
    public native UIFont entryLabelFont();

    @Generated
    @Selector("getMarkerPositionWithHighlight:")
    @ByValue
    public native CGPoint getMarkerPositionWithHighlight(@NotNull ChartHighlight highlight);

    @Generated
    @Selector("hash")
    @NUInt
    public static native long hash_static();

    /**
     * The color for the hole that is drawn in the center of the PieChart (if enabled).
     * note:
     * Use holeTransparent with holeColor = nil to make the hole transparent.*
     */
    @Generated
    @Selector("holeColor")
    @Nullable
    public native UIColor holeColor();

    /**
     * the radius of the hole in the center of the piechart in percent of the maximum radius (max = the radius of the whole chart)
     * <em>default</em>: 0.5 (50%) (half the pie)
     */
    @Generated
    @Selector("holeRadiusPercent")
    public native double holeRadiusPercent();

    @Generated
    @Selector("indexForAngle:")
    public native long indexForAngle(double angle);

    @Generated
    @Selector("inheritedAnimationDuration")
    public static native double inheritedAnimationDuration();

    @Generated
    @Selector("init")
    public native PieChartView init();

    @Generated
    @Selector("initWithCoder:")
    public native PieChartView initWithCoder(@NotNull NSCoder aDecoder);

    @Generated
    @Selector("initWithFrame:")
    public native PieChartView initWithFrame(@ByValue CGRect frame);

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
     * <code>true</code> if drawing the center text is enabled
     */
    @Generated
    @Selector("isDrawCenterTextEnabled")
    public native boolean isDrawCenterTextEnabled();

    /**
     * <code>true</code> if drawing entry labels is enabled, <code>false</code> ifnot
     */
    @Generated
    @Selector("isDrawEntryLabelsEnabled")
    public native boolean isDrawEntryLabelsEnabled();

    /**
     * <code>true</code> if the hole in the center of the pie-chart is set to be visible, <code>false</code> ifnot
     */
    @Generated
    @Selector("isDrawHoleEnabled")
    public native boolean isDrawHoleEnabled();

    /**
     * <code>true</code> if the inner tips of the slices are visible behind the hole, <code>false</code> if not.
     */
    @Generated
    @Selector("isDrawSlicesUnderHoleEnabled")
    public native boolean isDrawSlicesUnderHoleEnabled();

    @Generated
    @Selector("isSubclassOfClass:")
    public static native boolean isSubclassOfClass(Class aClass);

    /**
     * <code>true</code> if drawing x-values is enabled, <code>false</code> ifnot
     */
    @Generated
    @Selector("isUsePercentValuesEnabled")
    public native boolean isUsePercentValuesEnabled();

    @Generated
    @Selector("keyPathsForValuesAffectingValueForKey:")
    @NotNull
    public static native NSSet<String> keyPathsForValuesAffectingValueForKey(@NotNull String key);

    @Generated
    @Selector("layerClass")
    @NotNull
    public static native Class layerClass();

    /**
     * The max angle that is used for calculating the pie-circle.
     * 360 means it’s a full pie-chart, 180 results in a half-pie-chart.
     * <em>default</em>: 360.0
     */
    @Generated
    @Selector("maxAngle")
    public native double maxAngle();

    @Generated
    @Selector("modifyAnimationsWithRepeatCount:autoreverses:animations:")
    public static native void modifyAnimationsWithRepeatCountAutoreversesAnimations(
            @NFloat double count,
            boolean autoreverses,
            @ObjCBlock(name = "call_modifyAnimationsWithRepeatCountAutoreversesAnimations") @NotNull UIView.Block_modifyAnimationsWithRepeatCountAutoreversesAnimations animations);

    /**
     * Checks if the given index is set to be highlighted.
     */
    @Generated
    @Selector("needsHighlightWithIndex:")
    public native boolean needsHighlightWithIndex(long index);

    @Generated
    @Owned
    @Selector("new")
    public static native PieChartView new_objc();

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

    @Generated
    @Selector("radius")
    public native double radius();

    @Generated
    @Selector("requiresConstraintBasedLayout")
    public static native boolean requiresConstraintBasedLayout();

    @Generated
    @Selector("resolveClassMethod:")
    public static native boolean resolveClassMethod(SEL sel);

    @Generated
    @Selector("resolveInstanceMethod:")
    public static native boolean resolveInstanceMethod(SEL sel);

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
     * the text that is displayed in the center of the pie-chart
     */
    @Generated
    @Selector("setCenterAttributedText:")
    public native void setCenterAttributedText(@Nullable NSAttributedString value);

    /**
     * the text that is displayed in the center of the pie-chart
     */
    @Generated
    @Selector("setCenterText:")
    public native void setCenterText(@Nullable String value);

    /**
     * Sets the offset the center text should have from it’s original position in dp. Default x = 0, y = 0
     */
    @Generated
    @Selector("setCenterTextOffset:")
    public native void setCenterTextOffset(@ByValue CGPoint value);

    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie hole
     */
    @Generated
    @Selector("setCenterTextRadiusPercent:")
    public native void setCenterTextRadiusPercent(double value);

    /**
     * <code>true</code> if drawing the center text is enabled
     */
    @Generated
    @Selector("setDrawCenterTextEnabled:")
    public native void setDrawCenterTextEnabled(boolean value);

    /**
     * Set this to true to draw the enrty labels into the pie slices
     */
    @Generated
    @Selector("setDrawEntryLabelsEnabled:")
    public native void setDrawEntryLabelsEnabled(boolean value);

    /**
     * <code>true</code> if the hole in the center of the pie-chart is set to be visible, <code>false</code> ifnot
     */
    @Generated
    @Selector("setDrawHoleEnabled:")
    public native void setDrawHoleEnabled(boolean value);

    /**
     * if true, the hole will see-through to the inner tips of the slices
     * <em>default</em>: <code>false</code>
     */
    @Generated
    @Selector("setDrawSlicesUnderHoleEnabled:")
    public native void setDrawSlicesUnderHoleEnabled(boolean value);

    /**
     * The color the entry labels are drawn with.
     */
    @Generated
    @Selector("setEntryLabelColor:")
    public native void setEntryLabelColor(@Nullable UIColor value);

    /**
     * The font the entry labels are drawn with.
     */
    @Generated
    @Selector("setEntryLabelFont:")
    public native void setEntryLabelFont(@Nullable UIFont value);

    /**
     * The color for the hole that is drawn in the center of the PieChart (if enabled).
     * note:
     * Use holeTransparent with holeColor = nil to make the hole transparent.*
     */
    @Generated
    @Selector("setHoleColor:")
    public native void setHoleColor(@Nullable UIColor value);

    /**
     * the radius of the hole in the center of the piechart in percent of the maximum radius (max = the radius of the whole chart)
     * <em>default</em>: 0.5 (50%) (half the pie)
     */
    @Generated
    @Selector("setHoleRadiusPercent:")
    public native void setHoleRadiusPercent(double value);

    /**
     * The max angle that is used for calculating the pie-circle.
     * 360 means it’s a full pie-chart, 180 results in a half-pie-chart.
     * <em>default</em>: 360.0
     */
    @Generated
    @Selector("setMaxAngle:")
    public native void setMaxAngle(double value);

    /**
     * The color that the transparent-circle should have.
     * <em>default</em>: <code>nil</code>
     */
    @Generated
    @Selector("setTransparentCircleColor:")
    public native void setTransparentCircleColor(@Nullable UIColor value);

    /**
     * the radius of the transparent circle that is drawn next to the hole in the piechart in percent of the maximum radius (max = the radius of the whole chart)
     * <em>default</em>: 0.55 (55%) -> means 5% larger than the center-hole by default
     */
    @Generated
    @Selector("setTransparentCircleRadiusPercent:")
    public native void setTransparentCircleRadiusPercent(double value);

    /**
     * If this is enabled, values inside the PieChart are drawn in percent and not with their original value. Values provided for the ValueFormatter to format are then provided in percent.
     */
    @Generated
    @Selector("setUsePercentValuesEnabled:")
    public native void setUsePercentValuesEnabled(boolean value);

    @Generated
    @Selector("setVersion:")
    public static native void setVersion(@NInt long aVersion);

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

    /**
     * The color that the transparent-circle should have.
     * <em>default</em>: <code>nil</code>
     */
    @Generated
    @Selector("transparentCircleColor")
    @Nullable
    public native UIColor transparentCircleColor();

    /**
     * the radius of the transparent circle that is drawn next to the hole in the piechart in percent of the maximum radius (max = the radius of the whole chart)
     * <em>default</em>: 0.55 (55%) -> means 5% larger than the center-hole by default
     */
    @Generated
    @Selector("transparentCircleRadiusPercent")
    public native double transparentCircleRadiusPercent();

    /**
     * If this is enabled, values inside the PieChart are drawn in percent and not with their original value. Values provided for the ValueFormatter to format are then provided in percent.
     */
    @Generated
    @Selector("usePercentValuesEnabled")
    public native boolean usePercentValuesEnabled();

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

    /**
     * This will throw an exception, PieChart has no XAxis object.
     */
    @Generated
    @Selector("xAxis")
    @NotNull
    public native ChartXAxis xAxis();

    @Generated
    @Selector("appearanceForTraitCollection:whenContainedInInstancesOfClasses:")
    @NotNull
    public static native PieChartView appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceForTraitCollectionWhenContainedInInstancesOfClasses")
    @NotNull
    public PieChartView _appearanceForTraitCollectionWhenContainedInInstancesOfClasses(
            @NotNull UITraitCollection trait, @NotNull NSArray<? extends Class> containerTypes) {
        return appearanceForTraitCollectionWhenContainedInInstancesOfClasses(trait, containerTypes);
    }

    @Generated
    @Selector("appearanceWhenContainedInInstancesOfClasses:")
    @NotNull
    public static native PieChartView appearanceWhenContainedInInstancesOfClasses(
            @NotNull NSArray<? extends Class> containerTypes);

    @Generated
    @ProtocolClassMethod("appearanceWhenContainedInInstancesOfClasses")
    @NotNull
    public PieChartView _appearanceWhenContainedInInstancesOfClasses(@NotNull NSArray<? extends Class> containerTypes) {
        return appearanceWhenContainedInInstancesOfClasses(containerTypes);
    }
}