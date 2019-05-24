package com.islotwin.multichat.service;

import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ColorService {

    private final Random random = new Random();
    private final Float saturation = 0.3f;
    private final Float brightness = 0.9f;

    public String generateColor() {
        val hue = random.nextFloat();
        val color = java.awt.Color.getHSBColor(hue, saturation, brightness);
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
