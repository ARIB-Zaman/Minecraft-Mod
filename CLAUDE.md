# watermelonmod — project context for Claude Code

A Fabric mod for Minecraft **26.2** (Java **25**), built around Signals & Systems / DSP concepts as
actual gameplay: a boss fight built on frequency-spectrum analysis, image-processing goggles with a
real GPU FFT, and (new) a blur/deconvolution zone called **the Veil**.

## Build

```
.\gradlew.bat runClient   # launch a dev client with the mod loaded (Windows)
./gradlew runClient       # same, Mac/Linux
.\gradlew.bat build       # compile + jar
```

Needs **JDK 25** on PATH (`java -version` should say 25). The project currently lives at:
`D:\Slides 2-2\220-Project\Project\Minecraft-Mod\Minecraft-Mod` (Windows, PowerShell).

## Branches

- `main` — the primary line.
- `mahdiat` — a teammate's branch, currently the one being worked on. It independently added:
  - **High-pass Goggles** (`HighPassGogglesItem`, `FilterMode.GAUSSIAN_HIGH_PASS` in
    `GpuFftProcessor`) — a Gaussian high-pass filter goggle, sibling to the existing low-pass and
    band-pass goggles.
  - **Sonar** (`client.sonar.SonarController` / `SonarHud` / `SonarState`, plus `SonarGogglesItem`)
    — a separate echolocation system, registered in `WatermelonModClient`, independent of the FFT
    goggles pipeline.
  - `mahdiat` was merged with `main` (a `git merge origin/main`) partway through this work; that
    merge is done and the branch builds clean.

## Existing architecture (read this before touching goggles/FFT code)

- `goggles/` + `item/custom/*GogglesItem.java` — every pair of goggles is a `GogglesItem` wrapping a
  `GogglesPipeline` (named DSP parameters with min/max/default). Settings are stored per-ItemStack via
  `GOGGLES_SETTINGS` data component (`GogglesSettingsService`).
- `client/fft/GpuFftProcessor.java` — the whole GPU FFT pipeline lives here. Runs once per frame from
  `GameRendererFftMixin`, **after** the world/vanilla post-chain, **before** the GUI. Per-frame order:
  pack → forward FFT (radix-2 butterfly passes) → [Veil degrade] → capture spectrum → [Veil
  deconvolve] → [other goggles' frequency filter] → reorder → inverse FFT → unpack → composite →
  spectrum overlay.
- `client/fft/FftFullscreenPasses.java` — low-level shader-pass runner; owns two uniform buffers,
  `FftConfig` (generic 4-float `Params`) and `VeilConfig` (Veil-specific kernel/restore floats).
- `shaders/fft/*.fsh` — GLSL 330. `pack_rg`/`pack_b`/`reorder`/`unpack`/`spectrum` all take FFT size
  (`log2(width)`, `log2(height)`) as parameters now, so the FFT can run at two resolutions
  (`FftQuality.HIGH` = 1024×512, `FftQuality.LOW` = 512×256).
- `menu/WorkbenchMenu.java` + `client/workbench/WorkbenchScreen.java` — the Goggles Workbench GUI.
  Supports up to **6 sliders across 2 pages** (`WorkbenchMenu.SLIDER_COUNT = 6`), driven purely by a
  goggles item's `GogglesPipeline` parameter list — adding a new goggles type with ≤6 parameters
  needs no GUI code changes.

## The Veil (new feature, in progress)

**Concept:** the world can be degraded by a blur `H` (Gaussian / Motion / Defocus) plus noise,
`G = H·F + N` — the standard image-restoration model. Veil Goggles let the player estimate the blur
and pick a restoration filter (Inverse / Pseudo-inverse / Wiener) to undo it. This is deliberately a
*different* DSP concept from the existing Radiation Warden boss fight (which teaches spectrum
*analysis*); the Veil teaches *systems* — identifying and inverting a convolution.

Planned follow-ups (not yet built): a spatial-sampling/aliasing zone ("Lattice Fields") and a
temporal-sampling/strobe zone ("Strobe Hollow"), tying into the boss fight's reward economy. Full
design notes exist in conversation history but not yet as a repo doc — ask the user if a written
design doc should be added under `docs/`.

### Files

| File | Purpose |
|---|---|
| `client/veil/VeilKernel.java` | One blur model: type (Gaussian/Motion/Defocus), size, angle, noise σ |
| `client/veil/VeilClientState.java` | Currently-active blur, set only by `/veil` right now |
| `client/veil/VeilCommands.java` | `/veil gaussian\|motion\|defocus\|off`, `/veil quality high\|low\|auto` — **client-only test command**, not real in-world content yet |
| `client/fft/FftQuality.java` | HIGH (1024×512) / LOW (512×256) |
| `item/custom/VeilGogglesItem.java` | The goggles: 6 params (kernel type/size/angle estimate, restoration mode/ε/K) |
| `shaders/fft/veil_degrade.fsh` | Applies `H` and noise to the forward spectrum (`G = H·F + N`) |
| `shaders/fft/deconvolve.fsh` | Inverse (gain-capped to avoid NaN/screen blackout) / pseudo-inverse / Wiener restoration |

### Status
- Builds clean on `mahdiat` (merged with High-pass + Sonar without losing either).
- Tested in-game: `/veil motion 40 30` blurs the world; Veil Goggles set to Motion/40px/30°/Wiener on
  the workbench restore it correctly; spectrum overlay (now square, was 2:1 before — fixed so stripe
  angles read correctly) shows the blur's fingerprint.
- **Known non-bug:** setting Restoration to **Inverse** on a scene with little/no real degradation
  produces full-screen rainbow static — this is *intentional*, demonstrating why the naive inverse
  filter explodes wherever the assumed blur response is near zero. Not a bug if this happens; it's
  the point of that mode. If static appears unexpectedly, check the goggles' Restoration mode first.
- 51 FPS measured on integrated Intel graphics (i5-13420H) with the Veil active — acceptable but not
  great; automatic quality-lowering (drops to LOW FFT resolution after 5s under 25 FPS) exists to
  protect weaker GPUs. Not yet measured on the laptop's RTX 2050 dedicated GPU.
- The `/veil` command and `VeilClientState` are a **test harness only** — there's no real in-world
  trigger (a "Veil zone" block/biome) yet. That's the next real chunk of work if continuing this
  feature.

## Working conventions (please follow)

- **This user wants git left alone.** Do not `git commit`, `git push`, or take any git action beyond
  what's needed to inspect state (`status`, `diff`, `log`) unless explicitly asked. All work should
  land as plain file edits on disk; the user commits manually when ready. (This preference came from
  a bad experience with a prior cloud session pushing without asking — respect it strictly.)
- The user is a CSE student at BUET; explanations of DSP/Signals concepts embedded in gameplay design
  are welcome and should be pedagogically clear, not just implementation notes.
- No crafting recipes exist for any mod item yet — everything is obtained via the Ingredients creative
  tab or `/give`.
