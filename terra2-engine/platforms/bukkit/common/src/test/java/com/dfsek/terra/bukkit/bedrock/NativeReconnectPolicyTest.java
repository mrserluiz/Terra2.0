package com.dfsek.terra.bukkit.bedrock;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class NativeReconnectPolicyTest {
 @Test void temporaryFailuresBackOffButNeverPermanentlyPauseAConnection(){
  var p=new NativeReconnectPolicy();var id=UUID.randomUUID();long tick=0;
  assertTrue(p.ready(id,tick));
  for(int attempt=0;attempt<20;attempt++){
   p.failed(id,tick,"not spawned yet");assertFalse(p.ready(id,tick));
   tick+=30;assertTrue(p.ready(id,tick));
  }
  p.clear(id);assertEquals("AUTO_READY",p.status(id));assertTrue(p.ready(id,0));
 }
 @Test void retriesAreIsolatedAndDisconnectClearsThem(){
  var p=new NativeReconnectPolicy();var first=UUID.randomUUID();var second=UUID.randomUUID();
  p.failed(first,10,"registry not ready");assertFalse(p.ready(first,10));assertTrue(p.ready(second,10));
  p.clear();assertTrue(p.ready(first,10));
 }
}
