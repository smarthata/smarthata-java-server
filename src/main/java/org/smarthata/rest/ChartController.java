package org.smarthata.rest;

import org.smarthata.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices/{deviceId}")
public class ChartController {

    private final ChartService chartService;

    @Autowired
    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/chart")
    public List<List> getChartData(@PathVariable Integer deviceId,
                                   @RequestParam(defaultValue = "2") int hours,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int points
                                   ) {
        return chartService.getChartData(deviceId, hours, page, points);
    }

}
