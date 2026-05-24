package com.sgr9.url_shortener.dto;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClickEventDTO {
    private LocalDate clickDate;
    private Long count;
}