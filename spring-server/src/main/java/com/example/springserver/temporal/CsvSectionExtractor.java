package com.example.springserver.temporal;

import org.springframework.stereotype.Component;

@Component
public class CsvSectionExtractor {

    public String extractSection(String csv, String sectionHeader) {
        String[] lines = csv.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            if (line.trim().equals(sectionHeader)) {
                inSection = true;
                continue;
            }
            if (inSection) {
                if (line.isBlank()) {
                    break;
                }
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
