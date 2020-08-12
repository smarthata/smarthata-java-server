package org.smarthata.rest;

import org.smarthata.service.device.HeatingFloorDevice;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping("/heating")
public class HeatingController {

    final HeatingFloorDevice heatingFloorDevice;

    public HeatingController(HeatingFloorDevice heatingFloorDevice) {
        this.heatingFloorDevice = heatingFloorDevice;
    }

    @GetMapping("/floor-temp")
    public Map<String, Integer> getFloorTemp() {
        return singletonMap("value", heatingFloorDevice.getTemp());
    }

    @PostMapping("/floor-temp")
    public void setFloorTemp(@RequestParam Integer floorTemp) {
        heatingFloorDevice.setTemp(floorTemp);
    }

}
