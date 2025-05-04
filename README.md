# MinStrøm — Update Branch

## What’s new

* **AI-Powered Device Addition**: Users can now take a picture or select one from the gallery. AI will analyze the image and automatically populate all required device information in the form.
* **Navigation & MVVM Enhancements**: Introduced Jetpack Compose Navigation and shared `DeviceViewModel` across screens for clear state flow.
* **Edit & Delete Actions**: Users can update device entries by tapping “Rediger enhed” or remove them via a trash icon directly from the Plan screen.
* **Improved UI Spacing**: Adjusted spacing in both Plan and AddDevice screens for better visual balance.

---

## Project Layout

```
com.example.minstrm
├── aiAPI
│   └── APIConnection.kt         # LLM image parsing + APIConnection DTO
│
├── model
│   └── Device.kt                # Pure data class representing a device
│
├── viewmodel
│   └── DeviceViewModel.kt       # StateFlow-based VM with add/update/delete logic
│
├── ui
│   ├── navigation
│   │   └── AppNavigation.kt     # Compose NavHost wiring Plan
│   └── screens
│       ├── AddDeviceScreen.kt   # Stateless Composable for smart add form
│       └── PlanScreen.kt        # Stateless Composable for planløg and device list 
│
└── MainActivity.kt              # Hosts Compose and initializes the ViewModel
```

---

## Integration Instructions

1. **Pull the Update Branch** into your existing MinStrøm project.
2. **Migrate Files**: Place the updated `.kt` files into the matching packages (`data`, `model`, `viewmodel`, `ui/navigation`, `ui/screens`).
3. **Sync Dependencies**: Ensure you have the following in your `build.gradle` app:

   * `androidx.navigation:navigation-compose`
   * `androidx.lifecycle:lifecycle-viewmodel-compose`
   * `io.coil-kt:coil-compose`
   * `com.squareup.okhttp3:okhttp`
   * *Optional:* `com.openai:openai-android-sdk` (or your chosen OpenAI client) for AI image analysis
4. **Permissions**: Add camera and read‑external‑storage permissions in your `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   ```
5. **API Key**: Confirm your `OPENAI_API_KEY` is set in `local.properties`:

   ```properties
   OPENAI_API_KEY=your_api_key_here
   ```
6. **Rebuild & Test**: Verify the updated flow:

   * **AI Add**: Take or choose a photo and watch AI populate the form.
   * **Editing**: Fields pre-fill when tapping “Rediger enhed.”
   * **Deletion**: Trash icon removes the entry immediately.
   * **Navigation**: Back-stack behavior returns correctly after save/cancel.

---

## Testing & Release

* Run on an emulator or device (API 21+).
* Validate:

  * **Smart Add**: AI correctly parses images and fills in device details.
  * **Editing**: Updates reflect in the Plan screen immediately.
  * **Deletion**: Entries are removed without delay.
  * **Navigation**: Smooth transitions and proper back-stack handling.

Once verified, merge into your production branch and publish an incremental version.")
