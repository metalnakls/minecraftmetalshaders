// Copyright 2026 mcopt-shade contributors. Apache-2.0.
import java.nio.*;
import java.nio.file.*;
import mcopt.metal.shade.*;
public class Fixtures {
 public static void main(String[] args)throws Exception {
  Path out=Path.of(args[0]);Files.createDirectories(out);System.setProperty("mcopt.shade.user.bloom","200");
  Files.writeString(out.resolve("post.metal"),ShadeMsl.post());Files.writeString(out.resolve("lut.metal"),ShadeMsl.lut());
  for(int angle:new int[]{0,65,85}){
   ShadeFrame f=new ShadeFrame();double[]d=new double[3];ShadeSky.celestial(Math.toRadians(angle),d);f.sunDir.set((float)d[0],(float)d[1],(float)d[2]);f.moonDir.set(f.sunDir).negate();f.eyeSkyLight=15;f.minAmbient=.0125f;f.width=f.height=1024;f.updateAtmosphere(0,true);ShadeLook l=new ShadeLook();l.update(f,0,.016f);
   ByteBuffer b=ByteBuffer.allocateDirect(ShadeFrame.SIZE+ShadeLook.SIZE+96).order(ByteOrder.LITTLE_ENDIAN);f.pack(b.slice(0,ShadeFrame.SIZE));l.pack(b.slice(ShadeFrame.SIZE,ShadeLook.SIZE),f);int p=ShadeFrame.SIZE+ShadeLook.SIZE;b.putFloat(p,1);b.putFloat(p+4,1);b.putFloat(p+64,2);b.putFloat(p+68,4);b.position(p+72);l.putHdrCurve(b,4);b.putFloat(p+92,1);byte[]bytes=new byte[b.capacity()];b.get(0,bytes);Files.write(out.resolve(angle+".bin"),bytes);
  }
 }
}
