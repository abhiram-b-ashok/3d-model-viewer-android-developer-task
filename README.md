# Multi-Model 3D Viewer (Filament + Compose)

This is a high-performance Android application that allows users to view and interact with multiple 3D models simultaneously within a flexible, "windowed" UI. It leverages the **Google Filament** rendering engine for high-fidelity 3D graphics and **Jetpack Compose** for a responsive, modern user interface.

## Key Features

- **Multi-Model Support**: Load multiple 3D models (`.glb` assets) onto the screen at once.
- **Windowed Interface**: Each model lives in a draggable and resizable "container".
- **Dual Interaction Modes**:
  - **Normal Mode**: Drag to move the model window; pinch to resize the window.
  - **Interact Mode**: Drag to rotate the 3D model; pinch to zoom in/out on the asset.
- **Optimized for Performance**:
  - **Shared Rendering Engine**: A single Filament `Engine` instance is shared across all 3D views to minimize memory footprint and initialization overhead.
  - **Reactive Rendering**: Implements "dirty rendering" logic using `Choreographer`, ensuring the GPU only works when the scene actually changes.
  - **Mobile-First Optimizations**: Disables expensive post-processing (SSAO, MSAA, etc.) and utilizes dynamic resolution scaling to maintain high frame rates on varied hardware.

## Architecture Overview

### 3D Rendering (Filament)
- **`FilamentEngineHolder`**: A singleton that manages the lifecycle of the Filament `Engine`, `AssetLoader`, and `MaterialLoader`.
- **`ModelSurfaceView`**: A custom `SurfaceView` that bridges Android's view system with Filament. It handles the 3D `Scene`, `Camera`, and `Renderer` for each individual model instance.

### UI & Layout (Compose)
- **`ModelContainer`**: A Composable that provides the "chrome" for each 3D model, including the title bar, interaction toggle, and close button. It manages its own position and size state.
- **`MainActivity`**: The main entry point that hosts the model scene and a catalog for adding new assets.

## Technologies Used

- **Kotlin**: Primary programming language.
- **Jetpack Compose**: Modern toolkit for building native UI.
- **Google Filament**: Real-time physically based rendering engine.
- **gltfio**: Filament's utility library for loading glTF 2.0 files.

## Getting Started

1.  Clone the repository.
2.  Open the project in **Android Studio (Ladybug or newer)**.
3.  Sync Gradle dependencies.
4.  Run the `app` module on a physical device or an emulator with OpenGL ES 3.0+ support.

## Performance Notes

This app is designed to handle 5+ active 3D models simultaneously. To maintain performance:
- All models share the same ubershader bundle.
- Shadow casting is disabled by default.
- Dynamic resolution scaling kicks in if the frame time exceeds the 16ms budget.
