# 🕰️ Büyük Saatli Maarif Takvimi

[![Derleme](https://github.com/geceleriesen/maariftakvim/actions/workflows/build.yml/badge.svg)](https://github.com/geceleriesen/maariftakvim/actions/workflows/build.yml)
[![Lisans: PolyForm Noncommercial](https://img.shields.io/badge/lisans-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84)

Eskiden evlerde duvarda asılı duran **büyük saatli maarif takvimi**, şimdi telefonunda. 📅
Saman kâğıdı tonlarında bir yaprak: tarih, iki şehrin saati ve namaz vakitleri, arkasında da günün menüsü, isimleri, bilmecesi ya da fıkrası. Kilit ekranına koyabilirsin, saatler gerçekten çalışır. ⏰

> 🚧 **İlk sürüm.** Şimdilik yalnızca bir Poco telefonda (HyperOS) denendi. Başka markalarda, özellikle kilit ekranında, farklılıklar olabilir. Sorun görürsen [buradan yaz](https://github.com/geceleriesen/maariftakvim/issues).

## ⬇️ İndir

**[MaarifTakvimi.apk dosyasını indir](https://github.com/geceleriesen/maariftakvim/releases/latest/download/MaarifTakvimi.apk)**

## 📲 Kurulum

1. APK'yı telefonda indir ve üzerine dokun.
2. "Bilinmeyen kaynaklardan yükleme" izni isterse **İzin ver**'e bas. Google "zararlı olabilir" derse **Yine de yükle**'yi seç. Uygulama Play Store'da olmadığı için bu uyarı normal. 🙂
3. Uygulamayı aç, **Kilit ekranına koy**'a bas. Açılan ekranda **Uygula**'ya basıp kilit ekranını seç.
4. Şehirlerini değiştirmek için takvimde **uzun bas** → Ayarlar.

## ✨ Neler var

- 📆 **Üç takvim bir arada:** Miladi, Hicri ve Rumi tarih.
- 🌍 **İki şehir:** her biri için canlı analog saat, hava durumu ve namaz vakitleri. Şehri ad yazarak seçersin, istersen birincisi telefonun konumundan bulunur.
- 🔄 **Arka yaprak:** takvime **çift dokun**, yaprak çevrilir. Günün menüsü, bugün doğanlara isim önerileri, bilmece ya da fıkra. İçerik her gün değişir.
- 🔒 **Kilit ekranı:** yaprak kilit ekranında durur, akrep, yelkovan ve kırmızı saniye kolu çalışır.
- 🗓️ **Ajanda (isteğe bağlı):** telefon takvimindeki sıradaki etkinlik görünür. Kilit ekranında gösterilmez.
- 📱 **Her ekrana uyum:** uzun ve kısa ekranlarda yerleşim kendini ayarlar.

## ❓ Sık sorulanlar

**Güvenli mi?**
Kaynak kodu bu sayfada açık, istediğin gibi inceleyebilirsin. Uygulama hesap, reklam ya da takip aracı içermez. Aşağıdaki "Gizlilik" bölümüne bak.

**Güncelleme nasıl olur?**
Şimdilik her sürümün imzası farklı. Yeni sürümü kurmadan önce eskisini sil. Ayarların sıfırlanır.

**Kilit ekranında saat takılıyor ya da durmuş.**
Poco/Xiaomi'de: Ayarlar → Uygulamalar → Maarif Takvimi → **Pil tasarrufu: Kısıtlama yok** ve **Otomatik başlat** açık olsun.

**Kilit ekranında yaprağı çeviremiyorum.**
Kilit ekranında dokunma alınamıyor. Arka yaprağı uygulamayı açıp çift dokunarak görürsün. Ama alt kısımda arka yaprağın özeti kilit ekranında da görünür.

**Namaz vakitleri resmî imsakiyeyle tutmuyor.**
Vakitler hesaplanıyor (Diyanet yöntemi, deneysel), resmî imsakiyeden birkaç dakika sapabilir. Hicri tarih de Diyanet takviminden bir gün farklı olabilir. İbadet vakitleri için resmî kaynağa bakmanı öneririm.

**Ana ekran widget'ı var mı?**
Basit bir sürümü var, yeni tasarıma uyarlanacak.

## 🔐 Gizlilik

- 🕌 **Namaz vakitleri:** [Aladhan](https://aladhan.com/prayer-times-api) servisinden aylık indirilir, cihazda saklanır. İnternet yokken son indirilen ay kullanılır.
- ⛅ **Hava durumu ve şehir arama:** [Open-Meteo](https://open-meteo.com/). Şehrin koordinatları bu servislere gönderilir. Otomatik konum açıksa telefonun son bilinen konumu kullanılır.
- 🗓️ **Ajanda:** telefonun takviminden yalnızca cihazda okunur, dışarı gönderilmez.
- Konum ve takvim izinleri **isteğe bağlıdır**. Vermezsen ilgili özellik çalışmaz, uygulama çalışır.
- Hesap, reklam ve analiz aracı yoktur.

## 🛠️ Geliştirenler için

- Android Studio ile açıp derle (JDK 17, Android SDK 34, minimum Android 8.0).
- GitHub Actions her `push`'ta derler, testleri çalıştırır ve APK'yı **Releases** altına koyar.
- Kod yapısı için [ARCHITECTURE.md](ARCHITECTURE.md).
- Günlük içerik (menü, isim, bilmece, fıkra) bu depodaki kendi yazılmış metinlerdir. `assets/data.json`'a eklenen metinlerin telif durumunu kontrol et, başkasının telifli metnini ekleme.

## 🗺️ Sırada ne var

- Widget'ı yeni tasarıma uyarlamak
- Kilit ekranında ön ve arka yüzün sırayla gösterilmesi
- Samsung, Pixel gibi farklı markalarda deneme
- Sabit imzalı sürüm (güncellemelerde eskisini silmek gerekmesin)
- Daha fazla günlük içerik

## 📜 Lisans

Kaynak kod herkese açıktır ama **açık kaynak değildir**: [PolyForm Noncommercial 1.0.0](LICENSE).

- ✅ Kişisel kullanım, inceleme, değiştirme ve kâr amacı gütmeden paylaşma serbest (lisans metni ve bildirim satırı korunarak).
- ❌ Satmak, reklamlı ya da ücretli bir üründe kullanmak, başka bir adla pazarlamak gibi **ticari kullanım için izin gerekir**.

Anton yazı tipi (SIL OFL 1.1) ile AndroidX ve Glance kütüphaneleri (Apache 2.0) kendi lisanslarında kalır. Bu bir hukuki tavsiye değildir, bağlayıcı olan lisans metnidir.
