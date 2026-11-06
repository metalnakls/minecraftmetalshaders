# Softer shadow tones and cloud weather

After building `:metal:build` with JDK 25, run:

```sh
JAVA_HOME=/path/to/jdk-25 GRADLE_USER_HOME="$PWD/.gradle-cache" \
  MTL_DEBUG_LAYER=1 MTL_SHADER_VALIDATION=1 \
  python3 tools/shade/weathercheck/run.py /tmp/weathercheck /path/to/baseline.jar
```

The baseline is the pre-change mod JAR. No game is launched and no settings are edited.

`WeatherCheck` samples the production weather code across 16 seeds and 20 days, including negative time and front boundaries. It requires meaningful fully clear periods, gradual changes, bounded dry-weather coverage, and storm coverage. These percentages describe the sampled weather targets, not a guarantee for a particular session.

`ToneFixtures` packs actual uniforms in three looks at night, sunset and noon. `tonecheck.m` runs the production SDR and full-scene HDR curves on dark texture-like steps, mid-gray and highlights. It checks monotonicity, finite output, reduced dark-patch contrast and a stable mid-gray anchor. This caught the old HDR black-floor reversal near zero.

Four cloud previews run the actual weather, density, lighting, temporal resolve and composite passes. A forced clear front verifies zero scattered light and full transmission in every march pixel, and full transmission throughout the 256×256 cloud-shadow map. Preview PNGs are offscreen fixtures, not screenshots from the user's world.
