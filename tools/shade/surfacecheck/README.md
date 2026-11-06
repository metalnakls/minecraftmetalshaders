# Rain and lava checks

After `:metal:build`, with JDK 25 and Metal access:

```sh
JAVA_HOME=/path/to/jdk-25 GRADLE_USER_HOME="$PWD/.gradle-cache" \
 MTL_DEBUG_LAYER=1 MTL_SHADER_VALIDATION=1 \
 python3 tools/shade/surfacecheck/run.py /tmp/surfacecheck /path/to/baseline.jar
```

Runs the actual rain shader against the baseline rain source, with the same production tone and exposure code. Checks full and budget lighting, light/heavy rain, night, lightning, unchanged snow/media/dimension fallbacks, transparent texels and finite bounded output. Compiles the patched weather fragment and all 22 terrain libraries, including PBR, Nether, dry and water variants.

The lava probe checks exact still/flow sprite classification, finite self-emission and a smooth heat response across 256 samples. Current atlas rectangles are packed independently of the water module each frame; zero bounds during loading disable lava classification. An existing glowing-blocks multiplier of zero keeps the general terrain path.

Optional `rain.rgba32f` and `lava.rgba32f` inputs (RGBA float32, unmodified texture samples) produce corresponding offscreen output buffers for inspection. These fixtures are not in-game screenshots. No game is launched; no profile settings are changed.
