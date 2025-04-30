# MinStrøm — Update Branch

> **Note:** This repository contains only the latest update for the existing MinStrøm Android app, which is already live.

---

## What’s new

- **Navigation & MVVM Enhancements**: Introduced Jetpack Compose Navigation and shared `DeviceViewModel` across screens for clear state flow.
- **Edit & Delete Actions**: Users can now edit device entries or remove them via a trash icon, directly from the Plan screen.
- **Code Organization**: Refactored into an MVVM-friendly package layout; separated UI, ViewModel, and data/API concerns.
- **Improved UI Spacing**: Adjusted spacing in both Plan and AddDevice screens for better visual balance.

---

## Project Layout

```
com.example.minstrm
├── data
│   └── APIConnection.kt         # LLM image parsing + DeviceInfo DTO
│
├── model
│   └── Device.kt                # Pure data class representing a device
│
├── viewmodel
│   └── DeviceViewModel.kt       # StateFlow-based VM with add/update/delete logic
│
├── ui
│   ├── navigation
│   │   └── AppNavigation.kt     # Compose NavHost wiring Plan ↔ Add/Edit
│   └── screens
│       ├── AddDeviceScreen.kt   # Stateless Composable for smart add form
│       └── PlanScreen.kt        # Stateless Composable for device list + controls
│
└── MainActivity.kt              # Hosts Compose and initializes the ViewModel
```  

---

## Integration Instructions

1. **Pull the Update Branch** into your existing MinStrøm project.  
2. **Migrate Files**: Place the updated `.kt` files into the matching packages (`data`,`model`,`viewmodel`,`ui/navigation`,`ui/screens`).  
3. **Sync Dependencies**: Ensure you have:
   - `androidx.navigation:navigation-compose`
   - `androidx.lifecycle:lifecycle-viewmodel-compose`
   - `io.coil-kt:coil-compose`
   - `com.squareup.okhttp3:okhttp`
   - and other important dependencies - check build.gradle app  
4. **API Key**: Confirm your `OPENAI_API_KEY` is in `local.properties`.  
5. **Rebuild & Test**: Verify the updated navigation flow, edit/delete actions, and image-based device addition.

---

## Testing & Release

- Run on an emulator or device (API 21+).  
- Validate:
  - **Smart Add** form population from images.  
  - **Editing**: Fields pre-fill when tapping “Rediger enhed.”  
  - **Deletion**: Trash icon removes the entry immediately.  
  - **Navigation**: Back-stack behavior returns correctly after save/cancel.

Once verified, merge into your production branch and publish an incremental version.


