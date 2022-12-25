package org.smarthata.rest;

import org.smarthata.service.device.HeatingDevice;
import org.smarthata.service.device.Room;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping("/heating")
public class HeatingController {

    final HeatingDevice heatingDevice;

    public HeatingController(HeatingDevice heatingDevice) {
        this.heatingDevice = heatingDevice;
    }

    @GetMapping("/floor-temp")
    public Map<String, Double> getFloorTemp() {
        return singletonMap("value", heatingDevice.getExpectedTemp(Room.FLOOR));
    }

    @PostMapping("/floor-temp")
    public void setFloorTemp(@RequestParam Double floorTemp) {
        heatingDevice.setExpectedTemp(Room.FLOOR, floorTemp);
    }

    @GetMapping("/floor-pomp")
    public Map<String, Integer> getFloorPomp() {
        return singletonMap("value", heatingDevice.getFloorPomp(Room.FLOOR));
    }

    @PostMapping("/floor-pomp")
    public void setFloorPomp(@RequestParam String floorPomp, @RequestParam(required = false) String in) {
        heatingDevice.setFloorPomp(Room.FLOOR, floorPomp);
    }

}
