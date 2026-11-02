package org.moe.samples.simplechart.charts.protocol;


import org.jetbrains.annotations.NotNull;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Library;
import org.moe.natj.general.ann.Mapped;
import org.moe.natj.general.ann.Runtime;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCProtocolName;
import org.moe.natj.objc.ann.Selector;
import org.moe.natj.objc.map.ObjCObjectMapper;

/**
 * Protocol for providing a custom logic to where the filling line of a LineDataSet should end. This of course only works if setFillEnabled(…) is set to true.
 */
@Generated
@Library("Charts")
@Runtime(ObjCRuntime.class)
@ObjCProtocolName("IChartFillFormatter")
public interface IChartFillFormatter {
    /**
     * returns:
     * The vertical (y-axis) position where the filled-line of the LineDataSet should end.
     */
    @Generated
    @Selector("getFillLinePositionWithDataSet:dataProvider:")
    double getFillLinePositionWithDataSetDataProvider(
            @Mapped(ObjCObjectMapper.class) @NotNull ILineChartDataSet dataSet,
            @Mapped(ObjCObjectMapper.class) @NotNull LineChartDataProvider dataProvider);
}