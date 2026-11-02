package net.yura.domination.android;

import java.lang.reflect.Field;
import java.text.ChoiceFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.yura.android.AndroidMeApp;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.flashgui.DominationMain;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import net.yura.domination.engine.core.StatType;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.layout.XULLoader;

public class StatsActivity extends Activity {

    static Map<Integer, String> icons = new HashMap();
    static {
        // from: https://en.wikipedia.org/wiki/Religious_and_political_symbols_in_Unicode
        // TODO maybe should check with Paint.hasGlyph
        boolean unicode7 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        icons.put(ColorUtil.RED, unicode7 ? "\u0fd6" : "\u534d");
        icons.put(ColorUtil.BLUE, unicode7 ? "\u262f" : "\u25d0"); // "\ud83d\udd04" = blue refresh emoji
        icons.put(ColorUtil.YELLOW, unicode7 ? "\u262c" : "\ud83d\udd31");
        icons.put(ColorUtil.CYAN, "\u2721"); // works on old android
        icons.put(ColorUtil.GREEN, unicode7 ? "\u262a" : "\u2768\u200E\u066d"); // "\ud83c\udf18" = emoji moon "\u2605" = unicode star
        icons.put(ColorUtil.MAGENTA, "\u271d"); // works on old android
    }

    private ResourceBundle resb;
    
    List<Player> getPlayersStats() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        if (dmain == null) {
            return Collections.emptyList();
        }
        // TODO we may have just been started, and so the Risk object is not created yet, and even if it has, the autosave is still not loaded
        // so do not show anything for now, and the user can go back to the main activity and then re-launch the stats if they need to
        Risk r = dmain.risk;
        if (r == null) {
            return Collections.emptyList();
        }
        // if we open the stats activity at the same time as closing the game, avoid throwing a error
        RiskGame game = r.getGame();
        return game == null ? Collections.<Player>emptyList() : (List<Player>)game.getPlayersStats();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            resb = TranslationBundle.getBundle();
            setTitle(resb.getString("swing.tab.statistics"));
            String graph = getIntent().getData() == null ? null : getIntent().getData().getQueryParameter("graph");
            showGraph(graph != null ? StatType.valueOf(graph) : StatType.COUNTRIES);
        }
        catch (Throwable th) {
            // it does not make any sense, but we seem to be getting crashes here
            Logger.getLogger(StatsActivity.class.getName()).log(Level.WARNING, "crash in StatsActivity onCreate", th);
        }

        // hack to always show the overflow menu, as users are not finding it
        // from https://stackoverflow.com/a/11438245/15542109
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Throwable ignored) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        for (StatType statType : StatType.values()) {
            menu.add(android.view.Menu.NONE, statType.ordinal(), android.view.Menu.NONE,
                    resb.getString("swing.toolbar." + statType.getName()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        int id = item.getItemId();
        showGraph(StatType.fromOrdinal(id));
        return true;
    }

    public void showGraph(StatType statType) {
        setTitle(resb.getString("swing.tab.statistics") + " - "
                + resb.getString("swing.toolbar." + statType.getName()));

        //GraphicalView gview = ChartFactory.getLineChartView(this, getDataset(statType), getRenderer());

        XYChart chart = new LineChart(getDataset(statType), getRenderer()) {
            @Override
            protected void drawChartValuesText(Canvas canvas, XYSeries series, XYSeriesRenderer renderer, Paint paint, List<Float> points, int seriesIndex, int startIndex) {
                // the default for some strange reason seems to be STROKE for text
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                super.drawChartValuesText(canvas, series, renderer, paint, points, seriesIndex, startIndex);
            }
        };
        GraphicalView gview = new GraphicalView(this, chart);

        // hack: this extra parentLayout is needed to fix edge-to-edge mode
        FrameLayout parentLayout = new FrameLayout(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // needed for edge-to-edge mode (api-35+)
            parentLayout.setFitsSystemWindows(true);
        }
        parentLayout.addView(gview);
        setContentView(parentLayout);
    }

    private XYMultipleSeriesRenderer getRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setLegendTextSize(XULLoader.adjustSizeToDensity((int)renderer.getLegendTextSize()));
        renderer.setLabelsTextSize(XULLoader.adjustSizeToDensity((int)renderer.getLabelsTextSize()));

        // is not smart, starts showing labels like 0.5
        //renderer.setXLabels(25);
        //renderer.setYLabels(25);

        // make sure legend does not overlap labels
        int[] margins = renderer.getMargins();
        renderer.setMargins(new int[] {
                XULLoader.adjustSizeToDensity(margins[0]),
                XULLoader.adjustSizeToDensity(margins[1]),
                XULLoader.adjustSizeToDensity(margins[2]),
                XULLoader.adjustSizeToDensity(margins[3])
        });

        List<Player> players = getPlayersStats();

        for (Player p : players) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setShowLegendItem(true);
            r.setColor( p.getColor() );

            final String icon = getIcon(p);
            if (icon != null) {
                r.setDisplayChartValues(true);
                r.setChartValuesFormat(new ChoiceFormat(new double[]{0}, new String[]{icon}));
                r.setChartValuesTextSize(XULLoader.adjustSizeToDensity((int)r.getChartValuesTextSize()));
            }

            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

    /**
     * @see net.yura.domination.mobile.PicturePanel#getIconForColor(int)
     */
    private static String getIcon(Player p) {
        return DominationMain.getBoolean("color_blind",false) ? icons.get(p.getColor()) : null;
    }

    public XYMultipleSeriesDataset getDataset(StatType statType) {

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        
        List<Player> players = getPlayersStats();

        //draw each player graph.
        for (int c = 0; c < players.size(); c++) {
            Player p = players.get(c);
            double playerOffset = (c / 100.0);

            String icon = getIcon(p);
            CategorySeries series = new CategorySeries((icon == null ? "" : icon) + MiniUtil.getStatsLabel(statType, p));

            double[] PointToDraw = p.getStatistics(statType);

            double newPoint=0;

            series.add( newPoint + playerOffset ); // everything starts from 0

            for (double aPointToDraw : PointToDraw) {

                if (statType.isSummable()) {
                    newPoint += aPointToDraw;
                }
                else {
                    newPoint = aPointToDraw;
                }

                series.add( newPoint + playerOffset );
            }

            dataset.addSeries(toXYSeries(series));
        }

        return dataset;
    }

    /**
     * this fixes the default implementation to handle NaN correctly, as otherwise nothing gets drawn
     * @see CategorySeries#toXYSeries()
     */
    public XYSeries toXYSeries(CategorySeries series) {
        XYSeries xySeries = new XYSeries(series.getTitle());
        for (int i = 0; i < series.getItemCount(); i++) {
            double value = series.getValue(i);
            if (!Double.isNaN(value)) { // Double.isFinite was added in android-24
                xySeries.add(i + 1, value);
            }
        }
        return xySeries;
    }
}
