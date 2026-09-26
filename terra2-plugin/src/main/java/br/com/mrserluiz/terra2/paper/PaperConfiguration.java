package br.com.mrserluiz.terra2.paper;

import br.com.mrserluiz.terra2.core.GenerationSafetySettings;
import br.com.mrserluiz.terra2.core.WorldDefinition;
import java.util.List;

public record PaperConfiguration(GenerationSafetySettings safety, List<WorldDefinition> worlds) {
    public PaperConfiguration {
        worlds = List.copyOf(worlds);
    }
}
