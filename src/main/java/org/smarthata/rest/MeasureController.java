package org.smarthata.rest;

import org.smarthata.model.Measure;
import org.smarthata.service.MeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices/{deviceId}/measures")
public class MeasureController {

    private final MeasureService measureService;

    @Autowired
    public MeasureController(MeasureService measureService) {
        this.measureService = measureService;
    }

    @GetMapping
    public List<Measure> measures(@PathVariable Integer deviceId) {
        return measureService.findTopByDevice(deviceId);
    }

    @PostMapping
    public List<Measure> savePost(@PathVariable Integer deviceId, @RequestParam Map<String, String> params) {
        return measureService.save(deviceId, params);
    }

}
