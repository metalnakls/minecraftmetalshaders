// Copyright 2026 mcopt-shade contributors. Apache-2.0.
kernel void sunInput(texture2d<half,access::write> dst [[texture(0)]],texture2d<half> lut [[texture(1)]],texture2d<half> mw [[texture(2)]],sampler ls [[sampler(13)]],constant ShadeFrame& f [[buffer(16)]],constant ShadeLook& L [[buffer(17)]],uint2 p [[thread_position_in_grid]]){
 float2 uv=(float2(p)+.5)/float2(dst.get_width(),dst.get_height());float3 sd=f.sunDirWorld.xyz;
 float3 right=normalize(cross(float3(0,0,1),sd)),up=normalize(cross(sd,right));float scale=tan(0.6108652382);float3 dir=normalize(sd+right*((uv.x*2-1)*scale)+up*((1-uv.y*2)*scale));
 float3 c=shadeSkyRadiance2(dir,lut,ls,f,L,mw,2*scale/dst.get_height())*f.time.z;dst.write(half4(half3(c),1),p);
}
kernel void sunOutput(device float4* dst [[buffer(1)]],constant ShadeFinalParams& P [[buffer(0)]],constant ShadeLook& L [[buffer(17)]],texture2d<half> scene [[texture(0)]],texture2d<half> bloom [[texture(1)]],texture2d<half> nearBloom [[texture(2)]],uint2 p [[thread_position_in_grid]]){
 uint2 size=uint2(scene.get_width(),scene.get_height());float2 uv=(float2(p)+.5)/float2(size);float3 b0=float3(nearBloom.sample(shadeLinearClamp,uv).rgb);float3 b=(float3(bloom.sample(shadeLinearClamp,uv).rgb)+b0*P.q.x)*P.p.x;
 float3 c=shadeBloomComposite(float3(scene.read(p).rgb),b,b0,L.bloom.x,L.user2.z);dst[p.y*size.x+p.x]=float4(shadeFullHdr(c,L,P),1);
}
