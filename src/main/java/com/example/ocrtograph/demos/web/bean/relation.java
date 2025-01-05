package com.example.ocrtograph.demos.web.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class relation {
    private Integer id;

    private String label;

    private Integer from;

    private Integer to;
}
