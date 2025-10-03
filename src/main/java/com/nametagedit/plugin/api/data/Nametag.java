package com.nametagedit.plugin.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple POJO for a player's nametag (prefix & suffix).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nametag {
    private String prefix;
    private String suffix;
}