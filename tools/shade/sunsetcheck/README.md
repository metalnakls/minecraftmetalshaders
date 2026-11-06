# Sunset colour regression

Runs the production Java atmosphere and look uniforms, Metal atmosphere, final sky LUT, SDR/HDR display transforms and diffuse lighting without launching Minecraft. Requires a built `:metal:build`, JDK 25, cached Gradle dependencies, and access to a Metal GPU.

```sh
export JAVA_HOME='/path/to/jdk-25/Contents/Home'
export GRADLE_USER_HOME="$PWD/.gradle-cache"
MTL_DEBUG_LAYER=1 MTL_SHADER_VALIDATION=1 \
  python3 tools/shade/sunsetcheck/run.py /tmp/sunset-after
```

Set `BASELINE_JAR` to test an older build; the affected 1.2.6 build should fail the green-band assertions. Then pass that output directory as a second argument when testing the new build:

```sh
BASELINE_JAR=/path/to/old-metal.jar \
  python3 tools/shade/sunsetcheck/run.py /tmp/sunset-before
python3 tools/shade/sunsetcheck/run.py /tmp/sunset-after /tmp/sunset-before
```

The sweep covers 4,186 sky directions at 13 sun elevations in each of three presets, for evening, morning, rain and the optional legacy sky. Each GPU sample contains six float4 values: raw atmosphere, SDR atmosphere, HDR atmosphere, a neutral diffuse surface, SDR final LUT, HDR final LUT. The old JAR adapter exposes the original LUT fragment body as a callable helper without changing its grading arithmetic. Current builds use the same helper as the actual LUT fragment.

Assertions check finite nonnegative output, CPU/GPU agreement, and a maximum green excess of 2% for the sun at 0–25° in the current sky. Green excess is `max(G / max(R, B) - 1, 0)` in linear output; this is a diagnostic for sky colours, not a general material-saturation metric. The legacy sky is checked for validity and CPU/GPU parity, not the new colour constraint. With a baseline, the test also checks atmosphere luminance preservation, unchanged noon/night output within floating-point tolerance, and no increase in blue dominance on neutral evening surfaces. A warmer surface becoming less orange does not count as blue dominance.

These are synthetic GPU measurements, not in-game biome screenshots. The diffuse samples sweep normal direction and shadow visibility; they do not include textures, screen-space bounce, bloom, exposure history or the display's HDR calibration. Twilight below the horizon is checked for valid output and parity, but is outside the 0–25° hue assertion.

## 2026-10-29 source changes

The remaining green peak existed in the RGB atmosphere before display mapping. The directional sky grade and strong sunset white balance could recreate or amplify it. An atmosphere-only fix still failed the HDR test. Reducing white-balance warming required a matching ambient-fill adjustment to avoid stronger blue shadows.

Reference: [Merlin1809's Eclipse](https://github.com/Merlin1809/Eclipse-Shader), revision `e89e6c6eda248ae946480bd131db1b2a0a0f5c9e`. Its [settings](https://github.com/Merlin1809/Eclipse-Shader/blob/e89e6c6eda248ae946480bd131db1b2a0a0f5c9e/shaders/lib/settings.glsl) use 6500 K white balance, 6300 K sunlight and neutral ambient tint. The direction here is warm direct light with restrained overall grading and gentler ambient colour. This is a tuning of the existing renderer, not a port or a verified visual match to Eclipse.

Every changed rendering value:

| Value | Before | After |
| --- | --- | --- |
| White-balance keyframe at sun elevation 0° | +40 mired | +6 mired |
| White-balance keyframe at 5° | +42 mired | +6 mired |
| White-balance keyframe at 12° | +32 mired | +6 mired |
| Evening white-balance bonus | `8 × lowSun` | `8 × lowSun × (1 − smoothstep(-4°, 0°, elevation))` |
| Clear sunset ambient desaturation target | 0.30 | 0.65, blending into the existing daytime 0.80 |
| Additional ambient-fill weight | none | `smoothstep(-0.07, 0, sunY) × (1 − smoothstep(0.32, 0.50, sunY)) × (1 − rain) × (1 − day)` |
| Atmospheric green-peak correction | none for low-chroma sky | subtract `max(G − 0.96 × max(R, B), 0) × transition` |
| Hue correction weight | none | `(1 − smoothstep(0.32, 0.50, sunY)) × smoothstep(-0.16, -0.02, sunY)` |
| Correction luminance normalization | none | original luma / corrected luma, denominator floor `1e-8` |

The hue correction runs after weather blending in both CPU and GPU atmosphere, then after the LUT's directional grade. It preserves luminance and does not process material albedo. `sunY` is sine of the sun elevation. Existing saturation, contrast, tone curve/inverse, cloud density/softness, vignette and user settings are retained. No render pass, texture sample or raymarch step was added; a full-game performance comparison was not run.

Measured against installed JAR SHA-256 `22f9da0e3213f0f6d72c27064d65ffeb6431973821e36f85d8914d0f70426a24`:

| Clear evening sweep, three presets | Before | After |
| --- | --- | --- |
| Maximum raw atmospheric green excess | 10.41% | 0.00% |
| Maximum final LUT green excess, SDR | 4.79% | 0.52% |
| Maximum final LUT green excess, HDR at 4× headroom | 10.23% | 1.45% |

CPU/GPU atmosphere agreement is within 0.124% across all fixtures. Atmosphere luminance differs by at most 0.00002% from the baseline. Noon and night output differ by at most 0.000053 linear units. Existing HDR/compositor checks also pass, including 90 curve configurations.

Offscreen before/after renders and raw measurements are retained locally in `dist/sunset-review/`. Cloud previews use the existing cloud harness with a synthetic flat ground plane; no game window was opened.
