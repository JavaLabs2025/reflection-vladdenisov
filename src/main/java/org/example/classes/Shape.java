package org.example.classes;

import org.example.generator.Generatable;

@Generatable()
public interface Shape {
    double getArea();
    double getPerimeter();
}