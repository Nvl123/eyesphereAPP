# Shimmer Effect untuk tv_description

## Deskripsi
Shimmer effect telah ditambahkan pada `tv_description` untuk memberikan feedback visual selama proses translasi. Ini akan menghilangkan jeda yang terasa kosong dan memberikan pengalaman pengguna yang lebih baik.

## Fitur yang Ditambahkan

### 1. **Shimmer Layout**
- File: `app/src/main/res/layout/shimmer_description.xml`
- Berisi 3 baris shimmer dengan panjang yang berbeda untuk simulasi text description
- Menggunakan `ShimmerFrameLayout` dari Facebook Shimmer library

### 2. **Shimmer Background**
- File: `app/src/main/res/drawable/shimmer_background.xml`
- Background abu-abu dengan corner radius untuk shimmer effect

### 3. **ShimmerHelper Utility**
- File: `app/src/main/java/com/dicoding/eyesphere_nav/utils/ShimmerHelper.kt`
- Utility class untuk mengelola shimmer effect
- Fungsi untuk show/hide shimmer dan update description dengan shimmer

### 4. **Updated Adapter**
- File: `app/src/main/java/com/dicoding/eyesphere_nav/ui/panduan/PanduanVerticalAdapter.kt`
- Menambahkan shimmer effect pada ListViewHolder
- Fungsi untuk mengelola shimmer effect

### 5. **Updated Layout**
- File: `app/src/main/res/layout/item_vertical.xml`
- Menambahkan include untuk shimmer description layout
- Shimmer container dengan visibility "gone" secara default

## Cara Kerja

### 1. **Shimmer Display**
```kotlin
// Tampilkan shimmer
holder.showShimmer()

// Sembunyikan shimmer
holder.hideShimmer()
```

### 2. **Update Description dengan Shimmer**
```kotlin
// Update description dengan shimmer effect
holder.updateDescriptionWithShimmer("Text baru")

// Update dengan callback
holder.updateDescriptionWithShimmer("Text baru") {
    // Callback setelah shimmer selesai
    Log.d("Shimmer", "Selesai")
}
```

### 3. **ShimmerHelper Functions**
```kotlin
// Show shimmer
ShimmerHelper.showDescriptionShimmer(tvDescription, shimmerContainer)

// Hide shimmer
ShimmerHelper.hideDescriptionShimmer(tvDescription, shimmerContainer)

// Update dengan shimmer
ShimmerHelper.updateDescriptionWithShimmer(
    tvDescription, 
    shimmerContainer, 
    "Text baru",
    1500 // delay dalam ms
)
```

## Dependencies

### Shimmer Library
```gradle
implementation("com.facebook.shimmer:shimmer:0.5.0")
```

## Demo Shimmer Effect

Di `PanduanFragment`, ada demo shimmer effect yang akan berjalan setelah 3 detik:

```kotlin
private fun setupShimmerDemo(adapter: PanduanVerticalAdapter, recyclerView: RecyclerView) {
    recyclerView.postDelayed({
        val holder = recyclerView.findViewHolderForAdapterPosition(0) as? PanduanVerticalAdapter.ListViewHolder
        
        holder?.let { viewHolder ->
            viewHolder.updateDescriptionWithShimmer(
                "Panduan lengkap yang telah diterjemahkan ke bahasa Indonesia..."
            ) {
                Log.d("PanduanFragment", "Shimmer demo selesai")
            }
        }
    }, 3000)
}
```

## Customization

### 1. **Shimmer Duration**
Ubah delay shimmer di `ShimmerHelper.updateDescriptionWithShimmer()`:
```kotlin
ShimmerHelper.updateDescriptionWithShimmer(
    tvDescription, 
    shimmerContainer, 
    "Text baru",
    2000 // 2 detik delay
)
```

### 2. **Shimmer Colors**
Ubah warna shimmer di `shimmer_background.xml`:
```xml
<solid android:color="#E0E0E0" /> <!-- Warna abu-abu -->
```

### 3. **Shimmer Layout**
Modifikasi `shimmer_description.xml` untuk mengubah:
- Jumlah baris shimmer
- Panjang baris
- Spacing antar baris

## Integration dengan Translation System

Shimmer effect dapat diintegrasikan dengan sistem translasi yang ada:

```kotlin
// Saat mulai translasi
holder.showShimmer()

// Lakukan translasi
TranslationHelper.translateServerResponseAsync(context, originalText) { translatedText ->
    // Update UI dengan hasil translasi
    requireActivity().runOnUiThread {
        holder.tvDescription.text = translatedText
        holder.hideShimmer()
    }
}
```

## Testing

1. **Build dan install aplikasi**
2. **Buka halaman Panduan**
3. **Tunggu 3 detik** - shimmer effect akan muncul pada item pertama
4. **Verifikasi shimmer effect** berjalan dengan smooth
5. **Verifikasi text baru** muncul setelah shimmer selesai

## Troubleshooting

### Shimmer Tidak Muncul
- Pastikan dependency shimmer library sudah ditambahkan
- Check apakah `shimmer_description_container` ada di layout
- Verifikasi `ShimmerFrameLayout` import sudah benar

### Shimmer Tidak Berhenti
- Pastikan `stopShimmer()` dipanggil
- Check apakah ada error di callback
- Verifikasi delay time tidak terlalu lama

## Kesimpulan

Shimmer effect telah berhasil ditambahkan untuk memberikan feedback visual yang lebih baik selama proses translasi pada `tv_description`. Pengguna sekarang akan melihat animasi loading yang smooth daripada jeda kosong, sehingga pengalaman menggunakan aplikasi menjadi lebih profesional dan user-friendly.
