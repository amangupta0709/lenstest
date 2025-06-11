package com.db.lenstest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagStats {
    private String tag;
    private final StatusCounter stats = new StatusCounter();
}
