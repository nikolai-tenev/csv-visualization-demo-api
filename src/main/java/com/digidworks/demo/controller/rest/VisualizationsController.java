package com.digidworks.demo.controller.rest;

import com.digidworks.demo.dto.DataPoint;
import com.digidworks.demo.dto.VisualizationDto;
import com.digidworks.demo.model.Dataset;
import com.digidworks.demo.model.User;
import com.digidworks.demo.model.Visualization;
import com.digidworks.demo.repository.DatasetRepository;
import com.digidworks.demo.repository.VisualizationRepository;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * CRUD controller for visualizations.
 */
@RestController
@RequestMapping("/api/visualizations")
@Secured("ROLE_USER")
public class VisualizationsController {

    @Autowired
    private VisualizationRepository visualizationRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    /**
     * Get a page of visualizations.
     *
     * @param pageable
     * @return
     */
    @GetMapping
    public Page<Visualization> allVisualizations(@AuthenticationPrincipal User user, @PageableDefault(size = Integer.MAX_VALUE, value = Integer.MAX_VALUE) Pageable pageable) {
        return visualizationRepository.findAllByUser(user, pageable);
    }

    /**
     * Get all visualizations for the dashboard.
     *
     * @return
     */
    @GetMapping("/dashboard")
    public List<Visualization> allDashboardVisualizations(@AuthenticationPrincipal User user) {
        return visualizationRepository.findAllByUserAndShowOnDashboard(user, true);
    }

    /**
     * Get a visualization by id.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Visualization singleVisualization(@AuthenticationPrincipal User user, @PathVariable String id) {
        return visualizationRepository.findByUserAndId(user, id).orElseThrow();
    }

    /**
     * Get the first row of a dataset by id.
     *
     * @param id
     * @return
     */
    @GetMapping("/dataset/{id}/header")
    public List<String> datasetHeaderForVisualization(@AuthenticationPrincipal User user, @PathVariable String id) {
        return datasetRepository.findByUserAndId(user, id).orElseThrow().getData().get(0);
    }

    /**
     * Get visualization's data prepared for charting.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}/chart/data")
    public List<DataPoint> chartDataForVisualization(@AuthenticationPrincipal User user, @PathVariable String id) {
        Visualization vis = visualizationRepository.findByUserAndId(user, id).orElseThrow();
        Dataset data = vis.getDataset();

        String xAxis = vis.getXAxis();
        String yAxis = vis.getYAxis();

        boolean xAxisAggr = vis.isXAxisAggregateSum() || vis.isXAxisAggregateAvg();
        boolean yAxisAggr = vis.isYAxisAggregateSum() || vis.isYAxisAggregateAvg();
        Map<String, DataPoint> xAggregator = new HashMap<>();
        Map<String, DataPoint> yAggregator = new HashMap<>();

        List<DataPoint> result = new LinkedList<>();
        int xIndex = -1;
        int yIndex = -1;

        Iterator<List<String>> it = data.getData().iterator();
        List<String> firstLine = it.next();

        for (int i = 0; i < firstLine.size(); i++) {
            String colName = firstLine.get(i);
            if (colName.equals(xAxis)) {
                xIndex = i;
            } else if (colName.equals(yAxis)) {
                yIndex = i;
            }
        }

        while (it.hasNext()) {
            List<String> currLine = it.next();
            String x = currLine.get(xIndex);
            String y = currLine.get(yIndex);
            DataPoint dataPoint;

            if (xAxisAggr) {
                dataPoint = xAggregator.computeIfAbsent(x, k -> {
                    DataPoint d = new DataPoint();
                    d.setX(k);
                    d.setY(0D);
                    return d;
                });

                double yParsed = NumberUtils.isParsable(y) ? NumberUtils.createDouble(y) : 0; //if data is parseable - add it, else - ignore it
                dataPoint.setY((double) dataPoint.getY() + yParsed);
                dataPoint.setTotalRowsCount(dataPoint.getTotalRowsCount() + 1);
            } else if (yAxisAggr) {
                dataPoint = yAggregator.computeIfAbsent(y, k -> {
                    DataPoint d = new DataPoint();
                    d.setY(k);
                    d.setX(0D);
                    return d;
                });

                double xParsed = NumberUtils.isParsable(x) ? NumberUtils.createDouble(x) : 0; //if data is parseable - add it, else - ignore it
                dataPoint.setX((double) dataPoint.getX() + xParsed);
                dataPoint.setTotalRowsCount(dataPoint.getTotalRowsCount() + 1);
            } else {
                dataPoint = new DataPoint();
                dataPoint.setX(x);
                dataPoint.setY(y);
                result.add(dataPoint);
            }
        }

        if (xAggregator.size() > 0) {
            result.addAll(xAggregator.values());

            if (vis.isXAxisAggregateAvg()) {
                result.forEach(dataPoint -> dataPoint.setY((double) dataPoint.getY() / dataPoint.getTotalRowsCount()));
            }
        } else if (yAggregator.size() > 0) {
            result.addAll(yAggregator.values());

            if (vis.isYAxisAggregateAvg()) {
                result.forEach(dataPoint -> dataPoint.setX((double) dataPoint.getX() / dataPoint.getTotalRowsCount()));
            }
        }

        return result;
    }

    /**
     * Create a visualization.
     *
     * @return
     */
    @PostMapping
    public Visualization createVisualization(@AuthenticationPrincipal User user, @RequestBody @Valid VisualizationDto dto) {
        return saveVisualization(user, dto, null);
    }

    /**
     * Update a visualization by id.
     *
     * @param id
     * @param dto
     * @return
     */
    @PutMapping("/{id}")
    public Visualization updateVisualization(@AuthenticationPrincipal User user, @PathVariable String id, @RequestBody @Valid VisualizationDto dto) {
        return saveVisualization(user, dto, id);
    }

    /**
     * Delete visualization by id.
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    public void deleteVisualization(@AuthenticationPrincipal User user, @PathVariable String id) {
        visualizationRepository.deleteByUserAndId(user, id);
    }

    /**
     * Fetches (or creates) a new Visualization object and makes the basic updates.
     *
     * @param user
     * @param dto
     * @param id
     * @return
     */
    private Visualization saveVisualization(User user, VisualizationDto dto, String id) {
        Visualization visualization = id != null ? visualizationRepository.findByUserAndId(user, id).orElseThrow() : new Visualization();
        Dataset dataset = datasetRepository.findByUserAndId(user, dto.getDataset()).orElseThrow();

        visualization.setName(dto.getName());
        visualization.setShowOnDashboard(dto.isShowOnDashboard());
        visualization.setXAxis(dto.getXAxis());
        visualization.setYAxis(dto.getYAxis());
        visualization.setXAxisAggregateAvg(dto.isXAxisAggregateAvg());
        visualization.setYAxisAggregateAvg(dto.isYAxisAggregateAvg());
        visualization.setXAxisAggregateSum(dto.isXAxisAggregateSum());
        visualization.setYAxisAggregateSum(dto.isYAxisAggregateSum());

        if (visualization.getCreatedAt() == null) {
            visualization.setCreatedAt(new Date());
        }
        visualization.setModifiedAt(new Date());
        visualization.setUser(user);
        visualization.setDataset(dataset);

        visualizationRepository.save(visualization);

        return visualization;
    }
}
