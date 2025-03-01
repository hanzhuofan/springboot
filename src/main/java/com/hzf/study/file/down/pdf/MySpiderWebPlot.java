package com.hzf.study.file.down.pdf;

import lombok.Data;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;

/**
 * @Author zhuofan.han
 * @Date 2020/12/10 10:09
 */
@Data
public class MySpiderWebPlot extends SpiderWebPlot {
    private static final long serialVersionUID = 4005814203754627127L;
    private int ticks = DEFAULT_TICKS;
    private static final int DEFAULT_TICKS = 5;
    private NumberFormat format = NumberFormat.getInstance();
    private static final double PERPENDICULAR = 90;
    private static final double TICK_SCALE = 0.015;
    private int valueLabelGap = DEFAULT_GAP;
    private static final int DEFAULT_GAP = 10;
    private static final double THRESHOLD = 15;
    /**
     * 画环
     */
    private boolean drawRing = false;
    /**
     * 最大值
     */
    private double max = 1;
    private boolean webFilled = true;

    MySpiderWebPlot(CategoryDataset createCategoryDataset) {
        super(createCategoryDataset);
    }

    /**
     * 画图，支持添加圆环
     *
     * @param g2          the graphics device.
     * @param area        the area within which the plot should be drawn.
     * @param anchor      the anchor point (<code>null</code> permitted).
     * @param parentState the state from the parent plot, if there is one.
     * @param info        collects info about the drawing.
     */
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);
        if (info != null) {
            info.setPlotArea(area);
            info.setDataArea(area);
        }
        drawBackground(g2, area);
        drawOutline(g2, area);
        Shape savedClip = g2.getClip();
        g2.clip(area);
        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));
        if (!DatasetUtils.isEmptyOrNull(this.getDataset())) {
            int seriesCount = 0, catCount = 0;
            if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
                seriesCount = this.getDataset().getRowCount();
                catCount = this.getDataset().getColumnCount();
            } else {
                seriesCount = this.getDataset().getColumnCount();
                catCount = this.getDataset().getRowCount();
            }
            // ensure we have a maximum value to use on the axes
            if (this.getMaxValue() == DEFAULT_MAX_VALUE) {
                calculateMaxValue(seriesCount, catCount);
            }
            // Next, setup the plot area
            // adjust the plot area by the interior spacing value
            double gapHorizontal = area.getWidth() * getInteriorGap();
            double gapVertical = area.getHeight() * getInteriorGap();
            double X = area.getX() + gapHorizontal / 2;
            double Y = area.getY() + gapVertical / 2;
            double W = area.getWidth() - gapHorizontal;
            double H = area.getHeight() - gapVertical;
            double headW = area.getWidth() * this.headPercent;
            double headH = area.getHeight() * this.headPercent;
            // make the chart area a square
            double min = Math.min(W, H) / 2;
            X = (X + X + W) / 2 - min;
            Y = (Y + Y + H) / 2 - min;
            W = 2 * min;
            H = 2 * min;
            Point2D centre = new Point2D.Double(X + W / 2, Y + H / 2);
            Rectangle2D radarArea = new Rectangle2D.Double(X, Y, W, H);
            // draw the axis and category label
            for (int cat = 0; cat < catCount; cat++) {
                double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);
                //如果只有两个分类，设置固定角度
                if (catCount == 2 && cat == 1) {
                    angle = 0;
                }
                Point2D endPoint = getWebPoint(radarArea, angle, 1);
                // 1 = end of axis
                Line2D line = new Line2D.Double(centre, endPoint);
                g2.setPaint(this.getAxisLinePaint());
                g2.setStroke(this.getAxisLineStroke());
                g2.setPaint(Color.lightGray);
                g2.draw(line);
                drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
            }
            if (this.isDrawRing()) {
                //画环
                //以90度为轴心，计算各个圆环的x、y坐标
                Point2D topPoint = getWebPoint(radarArea, 90, 1);
                //轴心顶点圆的半径
                double topPointR = centre.getY() - topPoint.getY();
                //每个刻度的半径长
                double step = topPointR / this.getTicks();
                for (int p = this.getTicks(); p >= 1; p--) {
                    double r = p * step;
                    double upperLeftX = centre.getX() - r;
                    double upperLeftY = centre.getY() - r;
                    double d = 2 * r;
                    Ellipse2D ring = new Ellipse2D.Double(upperLeftX, upperLeftY, d, d);
                    g2.setPaint(Color.lightGray);
                    g2.draw(ring);
                }
            } else {
                //画多边形
                for (int p = this.getTicks(); p >= 1; p--) {
                    for (int i = 0; i < catCount; i++) {
                        //以90度为轴心，计算各个圆环的x、y坐标
                        Point2D topPoint1 = getWebPoint(radarArea, 90 - (360 * i / catCount), 0.2 * p);
                        Point2D topPoint2 = getWebPoint(radarArea, 90 - (360 * (i + 1) / catCount), 0.2 * p);

                        Line2D ring = new Line2D.Double(topPoint1, topPoint2);
                        g2.setPaint(Color.lightGray);
                        g2.draw(ring);
                    }
                }
            }
            // Now actually plot each of the series polygons..
            for (int series = 0; series < seriesCount; series++) {
                this.setSeriesPaint(new Color(253, 186, 64));
                this.setSeriesOutlinePaint(new Color(253, 186, 64));
                this.setSeriesOutlineStroke(new BasicStroke(8, 0, 1, 2, null, 0));
                drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW);
            }
        } else {
            drawNoDataMessage(g2, area);
        }
        g2.setClip(savedClip);
        g2.setComposite(originalComposite);
        drawOutline(g2, area);
    }

    @Override
    protected void drawRadarPoly(Graphics2D g2,
                                 Rectangle2D plotArea,
                                 Point2D centre,
                                 PlotRenderingInfo info,
                                 int series, int catCount,
                                 double headH, double headW) {

        Polygon polygon = new Polygon();

        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        // plot the data...
        for (int cat = 0; cat < catCount; cat++) {

            Number dataValue = getPlotValue(series, cat);

            if (dataValue != null) {
                double value = dataValue.doubleValue();

                if (value >= 0) { // draw the polygon series...

                    // Finds our starting angle from the centre for this axis

                    double angle = getStartAngle()
                            + (getDirection().getFactor() * cat * 360 / catCount);

                    // The following angle calc will ensure there isn't a top
                    // vertical axis - this may be useful if you don't want any
                    // given criteria to 'appear' move important than the
                    // others..
                    //  + (getDirection().getFactor()
                    //        * (cat + 0.5) * 360 / catCount);

                    // find the point at the appropriate distance end point
                    // along the axis/angle identified above and add it to the
                    // polygon

                    Point2D point = getWebPoint(plotArea, angle,
                            value / this.getMaxValue());
                    polygon.addPoint((int) point.getX(), (int) point.getY());

                    // put an elipse at the point being plotted..

                    Paint paint = getSeriesPaint(series);
                    Paint outlinePaint = getSeriesOutlinePaint(series);
                    Stroke outlineStroke = getSeriesOutlineStroke(series);

                    Ellipse2D head = new Ellipse2D.Double(point.getX()
                            - headW / 2, point.getY() - headH / 2, headW,
                            headH);
                    g2.setPaint(paint);
//                    g2.fill(head);
                    g2.setStroke(new BasicStroke(2, 0, 1, 2, null, 0));
                    g2.setPaint(Color.RED);
                    g2.draw(head);

                    if (entities != null) {
                        int row, col;
                        if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
                            row = series;
                            col = cat;
                        } else {
                            row = cat;
                            col = series;
                        }
                        String tip = null;
//                        if (this.toolTipGenerator != null) {
//                            tip = this.toolTipGenerator.generateToolTip(
//                                    this.dataset, row, col);
//                        }

                        String url = null;
//                        if (this.urlGenerator != null) {
//                            url = this.urlGenerator.generateURL(this.dataset,
//                                    row, col);
//                        }

                        Shape area = new Rectangle(
                                (int) (point.getX() - headW),
                                (int) (point.getY() - headH),
                                (int) (headW * 2), (int) (headH * 2));
                        CategoryItemEntity entity = new CategoryItemEntity(
                                area, tip, url, this.getDataset(),
                                this.getDataset().getRowKey(row),
                                this.getDataset().getColumnKey(col));
                        entities.add(entity);
                    }

                }
            }
        }
        // Plot the polygon

        Paint paint = getSeriesPaint(series);
        g2.setPaint(paint);
        g2.setStroke(new BasicStroke(8, 0, 1, 2, null, 0));
        g2.draw(polygon);

        // Lastly, fill the web polygon if this is required

        if (this.webFilled) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    0.1f));
            g2.setPaint(new Color(70, 130, 180));
            g2.fill(polygon);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));
        }
    }

    /**
     * 获取分类的最大值
     *
     * @param seriesCount the number of series
     * @param catCount    the number of categories
     */
    private void calculateMaxValue(int seriesCount, int catCount) {
        double v = 0;
        Number nV = null;
        for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
            for (int catIndex = 0; catIndex < catCount; catIndex++) {
                nV = getPlotValue(seriesIndex, catIndex);
                if (nV != null) {
                    v = nV.doubleValue();
                    if (v > this.getMaxValue()) {
                        this.setMaxValue(v);
                    }
                }
            }
        }
        this.setMaxValue(max);
    }

    public static void main(String args[]) {
        //在SWING中显示
//        JFrame jf = new JFrame();
//        jf.add(erstelleSpinnenDiagramm());
//        jf.pack();
//        jf.setVisible(true);
        //将JFreeChart保存为图片存在文件路径中
        saveAsFile(createDataset(), "C:\\tmp\\logo.png", 1920, 1080);
    }

    public static JPanel erstelleSpinnenDiagramm() {
        JFreeChart jfreechart = createChart(createDataset());
        ChartPanel chartpanel = new ChartPanel(jfreechart);
        return chartpanel;
    }


    public static void saveAsFile(DefaultCategoryDataset dataset, String outputPath,
                                  int weight, int height) {
        FileOutputStream out = null;
        try {
            File outFile = new File(outputPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(outputPath);

            // 保存为PNG
            JFreeChart chart = createChart(dataset);
            ChartUtils.writeChartAsJPEG(out, 0.5f, chart, weight, height);
            // 保存为JPEG
//            ChartUtils.writeChartAsJPEG(out, createChart(), weight, height);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    public static JFreeChart createChart(DefaultCategoryDataset dataset) {
//        MySpiderWebPlot spiderwebplot = new MySpiderWebPlot(createDataset());
//        JFreeChart jfreechart = new JFreeChart("前三个季度水果销售报告", TextTitle.DEFAULT_FONT, spiderwebplot, false);
//        LegendTitle legendtitle = new LegendTitle(spiderwebplot);
//        legendtitle.setPosition(RectangleEdge.BOTTOM);
//        jfreechart.addSubtitle(legendtitle);
//        return jfreechart;
        MySpiderWebPlot spiderWebPlot = new MySpiderWebPlot(dataset);
        spiderWebPlot.setOutlineVisible(false);
        spiderWebPlot.setBaseSeriesOutlinePaint(Color.WHITE);
        spiderWebPlot.setOutlinePaint(Color.WHITE);
        spiderWebPlot.setSeriesOutlinePaint(Color.WHITE);
        spiderWebPlot.setBackgroundPaint(Color.WHITE);
        spiderWebPlot.setLabelFont(new Font("黑体", Font.BOLD, 48));
        spiderWebPlot.setLabelPaint(Color.gray);
        spiderWebPlot.setAxisLineStroke(new BasicStroke(8, 0, 1, 2, null, 0));
        JFreeChart jFreeChart = new JFreeChart(spiderWebPlot);
        jFreeChart.setBorderVisible(false);
        jFreeChart.setElementHinting(false);
        jFreeChart.setSubtitles(Collections.emptyList());
        return jFreeChart;
    }

    public static DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String group1 = "苹果 ";

        dataset.addValue(1, group1, "合格率评估项1");
        dataset.addValue(1, group1, "合格率评估项2");
        dataset.addValue(1, group1, "合格率评估项3");
        dataset.addValue(0.8, group1, "合格率评估项4");
        dataset.addValue(0.33, group1, "合格率评估项5");
        dataset.addValue(0.5, group1, "合格率评估项6");
        dataset.addValue(0.2, group1, "合格率评估项7");
        return dataset;
    }
}
