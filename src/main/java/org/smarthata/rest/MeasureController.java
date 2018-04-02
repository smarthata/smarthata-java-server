package org.smarthata.rest;

import org.smarthata.model.Measure;
import org.smarthata.service.MeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices/{mac}/measures")
public class MeasureController {

    private final MeasureService measureService;

    @Autowired
    public MeasureController(MeasureService measureService) {
        this.measureService = measureService;
    }

    @GetMapping
    public List<Measure> saveGet(@PathVariable String mac, @RequestParam Map<String, String> params) {
        return measureService.save(mac, params);
    }

    @PostMapping
    public List<Measure> savePost(@PathVariable String mac, @RequestBody Map<String, String> params) {
        return measureService.save(mac, params);
    }

}
