package org.smarthata.rest;

import org.smarthata.service.device.LightService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping("/light")
public class LightController {

    final LightService lightService;

    public LightController(LightService lightService) {
        this.lightService = lightService;
    }

    @GetMapping
    public Map<String, Boolean> getLight(@RequestParam String room) {
        return singletonMap("value", lightService.getLight(room));
    }

    @PostMapping
    public void setLight(@RequestParam String room, @RequestParam String action) {
        lightService.setLight(room, action);
    }


}
