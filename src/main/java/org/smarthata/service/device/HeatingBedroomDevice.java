package org.smarthata.service.device;

import org.springframework.stereotype.Service;

@Service
public class HeatingBedroomDevice {

    private int baseTemp = 22;


    public int getBaseTemp() {
        return baseTemp;
    }
}
