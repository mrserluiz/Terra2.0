package com.dfsek.terra.bukkit.bedrock;
import java.util.*;
/** Main-thread retry clock in one-second bridge ticks; only explicit stop pauses a player. */
public final class NativeReconnectPolicy {
    private record Failure(int attempts,long next,String reason) {}
    private final Map<UUID,Failure> failures=new HashMap<>();
    public boolean ready(UUID player,long tick){var f=failures.get(player);return f==null||tick>=f.next();}
    public void failed(UUID player,long tick,String reason){
        var prior=failures.get(player);int attempts=prior==null?1:Math.min(6,prior.attempts()+1);
        long delay=Math.min(30,1L<<(attempts-1));failures.put(player,new Failure(attempts,tick+delay,reason));
    }
    public String status(UUID player){var f=failures.get(player);return f==null?"AUTO_READY":"AUTO_RETRY attempts="+f.attempts()+" reason="+f.reason();}
    public void clear(UUID player){failures.remove(player);}
    public void clear(){failures.clear();}
}
