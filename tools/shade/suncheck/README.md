# Solar profile

Build `:metal:build` with JDK 25, then run `python3 tools/shade/suncheck/run.py /tmp/sun-check` with `JAVA_HOME` set. The GPU preview uses the production sky, six bloom levels at the user's 200% bloom setting, halation and full-scene HDR curve at four times SDR white. It renders a 70-degree view at noon, low sun and sunset; PNGs normalize HDR by four for ordinary image viewers.

`SUNCHECK_BASELINE=<previous fixture directory>` checks the visible HDR radial falloff against recorded metrics: at least 35% less maximum adjacent-pixel drop, with peak brightness within 5%. This checks the displayed profile rather than only smoothness of the raw radiance curve. It does not capture Minecraft or reproduce display-specific HDR presentation.

The solar aureole uses positive narrow and broad scattering lobes (g=0.85 / 0.65, weights 0.35 / 1.2). This removes the old clamped difference’s finite halo boundary. The cloud preview now calls the production daytime sky function rather than its obsolete hard-disc approximation. Cloud previews use the SDR display curve without bloom; the separate suncheck covers production HDR and bloom. Neither is an in-game or perceptual-match assertion.
