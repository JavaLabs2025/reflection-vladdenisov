package org.example.classes;

import org.example.generator.Generatable;

@Generatable({Rectangle.class, Triangle.class})
public interface Shape {
    double getArea();
    double getPerimeter();
}