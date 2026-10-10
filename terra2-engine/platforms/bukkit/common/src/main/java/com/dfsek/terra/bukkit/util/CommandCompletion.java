package com.dfsek.terra.bukkit.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class CommandCompletion {
    private CommandCompletion() {}
    public static List<String> suggest(String command, String[] args, boolean permitted,
                                       Collection<String> worlds, Collection<String> community,
                                       Collection<String> converted, Collection<String> datapacks,
                                       Collection<String> inputs) {
        if(!permitted || args.length == 0) return List.of();
        Collection<String> options = List.of();
        String action = args[0].toLowerCase(Locale.ROOT);
        if(command.equalsIgnoreCase("terra2reportlog")) {
            if(args.length == 1) options = List.of("start", "stop", "status");
        } else if(command.equalsIgnoreCase("terra2")) {
            if(args.length == 1) options = List.of("help", "reload", "lis", "list", "unlock", "packs", "convert", "datapack", "loot", "biome", "structures", "locate");
            else if(args.length == 2) options = switch(action) {
                case "unlock" -> worlds;
                case "lis", "list" -> List.of("Cpack", "Tpack", "all");
                case "packs", "datapack" -> List.of("list", "inspect");
                case "loot" -> List.of("status");
                default -> List.of();
            };
            else if(args.length == 3) options = switch(action) {
                case "unlock" -> Stream.concat(community.stream(), converted.stream()).toList();
                case "packs" -> args[1].equalsIgnoreCase("inspect") ? converted : List.of();
                case "datapack" -> args[1].equalsIgnoreCase("inspect") ? datapacks : List.of();
                case "convert" -> inputs;
                default -> List.of();
            };
        }
        String prefix = args[args.length - 1];
        String lead = "";
        if(command.equalsIgnoreCase("terra2") && args.length == 3 && (action.equals("unlock") || action.equals("convert"))) {
            int last = prefix.lastIndexOf(';');
            if(last >= 0) { lead = prefix.substring(0, last + 1); prefix = prefix.substring(last + 1); }
        }
        String match = prefix.toLowerCase(Locale.ROOT), prepend = lead;
        return options.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(match))
            .filter(value -> !List.of(prepend.split(";", -1)).contains(value))
            .distinct().sorted(String.CASE_INSENSITIVE_ORDER).map(value -> prepend + value).toList();
    }
}
