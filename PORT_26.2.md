# NeoBiosphere 1.0.6 — unofficial port to Minecraft 26.2 (NeoForge 26.2.0.88)

This branch (`port-26.2-dum`) ports [NeoBiosphere](https://github.com/mlus-asuka/NeoBiosphere) by mlus-asuka to
Minecraft 26.2 for the Misty Dale modpack. It is **not** an official release and is not supported by the original
author. When an official 26.2 version appears, use that one.

License: GNU GPL v3 (unchanged, see `LICENSE`).

## What changed
- **A separate world type** instead of replacing the overworld: *Biospheres* (`neobiosphere:biosphere`, a chunk
  generator on top of `minecraft:overworld` with the `multi_noise` biome preset). Upstream overrides the overworld,
  amplified and large-biomes noise settings for every world; here existing worlds, the Nether, the End and other mods'
  dimensions are untouched — every mixin checks that the generator is the Biosphere one.
- Spheres keep the biomes and structures of whatever biome mods are installed (Terralith etc.); bridges between
  spheres, void outside. Carvers/features/data generators of the old approach were removed, their job is done by the
  generator.
- 26.2 API renames and the 26.2 world-preset format.

Built with plain `javac` against the 26.2 client (no Gradle update).
