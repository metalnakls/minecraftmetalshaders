// Copyright 2026 mcopt-shade contributors. Apache-2.0.
package mcopt.metal.shade.light;
import java.nio.file.*;
import mcopt.metal.shade.*;

/** Exercise the actual settings screen mapping and the live reader used to pack dynamic lights. */
public class Settings {
 public static void main(String[] args) throws Exception {
  Path out=Path.of(args[0]);
  ShadeOption light=ShadeOptions.all().stream().filter(o->o.key.equals("mcopt.shade.dynLights.strength")).findFirst().orElseThrow();
  for(int percent:new int[]{20,100,400}) {
   light.set(percent);float actual=DynLightConfig.strength();
   System.out.printf("held light %d%% -> %.4f%n",percent,actual);
   if(Math.abs(actual-percent/100f)>1e-6)throw new AssertionError("held-light slider stores incorrect strength");
   if(light.pos()!=percent)throw new AssertionError("slider round trip");
   ShadeSettings.save(out);
   if(percent!=100&&!Files.readString(out.resolve("config/mcopt.properties")).contains("mcopt.shade.dynLights.strength="+System.getProperty(light.key)))throw new AssertionError("saved strength");
  }
  light.reset();ShadeSettings.save(out);
  if(DynLightConfig.strength()!=1f||Files.readString(out.resolve("config/mcopt.properties")).contains("mcopt.shade.dynLights.strength="))throw new AssertionError("reset to actual 100%");
  System.out.println("PASS live strength, slider round trip, saved values and reset");
 }
}
