# Mimari

Uygulama tek modüllü bir Android projesidir (Kotlin, minSdk 26). Takvim yaprağı **tek bir çizim
motoruyla** çizilir; aynı kod tam ekranda, kilit ekranı resminde ve (ileride) widget resminde
kullanılır.

## Paketler (`com.geceleriesen.maariftakvim`)

| Paket | Görev |
|---|---|
| `domain` | Saf Kotlin, Android'siz: `TurkishCalendar` (Miladi/Hicri/Rumi), `DayLength` (gün uzunluğu), `DailyContent` (menü, isim, bilmece, fıkra). Birim testlidir. |
| `data` | `CalendarRepository` (günün tüm içeriğini birleştirir), `Settings`, `AgendaRepository`, `LocationHelper` ve `CityResolver`. |
| `network` | `WeatherPrayerService` (Aladhan + Open-Meteo, aylık disk önbelleği), `CitySearch` (şehir arama). |
| `ui/page` | `CalendarPageRenderer` (Canvas ile yaprağı çizer), `CalendarPageView`, `FontLoader`. |
| `ui` | `MainActivity` (tam ekran, çift dokununca yaprak çevirir, uzun basınca ayarlar), `SettingsActivity`. |
| `ui/wallpaper` | Kilit ekranı resmi üretme (`WallpaperJob`), gece alarmı ve açılış alıcısı. |
| `ui/widget` | Jetpack Glance ana ekran widget'ı. |

## Veri akışı
1. `CalendarRepository.getDay()` tarih motorunu, `DailyContent`'i ve `assets/data.json`
   kaydını birleştirir. `data.json`'daki alanlar hesaplanan içeriğin üstüne yazar.
2. `WeatherPrayerService` şehir başına hava durumunu ve ayın namaz vakitlerini getirir. Aylık
   vakitler `filesDir` altında saklanır, böylece internetsiz de çalışır.
3. `CalendarPageRenderer` bu verilerle yaprağı çizer. Çizim 1000 birim genişlikli sanal bir
   koordinat sisteminde yapılır ve ekran oranına göre ölçeklenir. Ekran uzunsa (20:9 gibi)
   sanal yükseklik 1600'ün üstüne çıkar ve fazlalık ön yüzde başlığa, saatlere ve vakit
   kutularına dağıtılır; arka yaprak şeridi sabit boyutta alta yaslanır. Kısa/geniş ekranlarda
   yüksekliğe göre küçülüp yatayda ortalanır. Kilit ekranında üstte sistem saati için ekran
   yüksekliğinin %20'si boş bırakılır (`WallpaperJob.TOP_INSET_RATIO`).

## Testler ve CI
- Birim testler `domain` katmanını kapsar (`app/src/test`).
- `.github/workflows/build.yml` her `push`'ta `testDebugUnitTest assembleDebug` çalıştırır ve
  APK'yı artifact olarak yükler.
- Çizim motoru `android.graphics` dışında hiçbir şeye bağlı olmadığı için masaüstünde de
  çizdirilip görsel olarak kontrol edilebilir.

## Bilinen sınırlar
- Widget (Glance) özel font ve doku desteklemez; yeni tasarıma uyarlanması planlanıyor.
- Kilit ekranı resmi bazı telefon markalarında tema sistemi tarafından ezilebilir.
- Hicri tarih Umm al-Qura'dan gelir, Diyanet takvimiyle bir gün farklı olabilir.
