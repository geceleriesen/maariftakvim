# Büyük Saatli Maarif Takvimi

Eskiden evlerde duvarda asılı duran büyük saatli maarif takviminin Android uygulaması.
Saman kâğıdı tonlarında bir yaprak: Miladi, Hicri ve Rumi tarih, iki şehrin saati, havası ve
namaz vakitleri, halk takvimi notları ve yaprağın arkasında günün menüsü, isimleri, bilmecesi.

> **Durum:** Aktif geliştirme aşamasında. Kod derleniyor ve birim testleri geçiyor. Gerçek
> cihazda yalnızca Poco (HyperOS) üzerinde denendi; diğer markalarda, özellikle kilit ekranı
> davranışı değişebilir.

## Kurulum (Android)

1. Telefonda şu bağlantıyı aç ve indir:
   **https://github.com/geceleriesen/maariftakvim/releases/latest/download/MaarifTakvimi.apk**
2. İndirilen dosyaya dokun. "Bilinmeyen kaynaklardan yükleme" izni isterse **İzin ver**'e bas.
   Google "zararlı olabilir" uyarısı verirse **Yine de yükle**'yi seç: uygulama Play Store'da değil,
   imzalı bir sürüm de henüz yok, bu yüzden uyarı normal.
3. Uygulamayı aç. İlk açılışta **Kilit ekranına koy**'a bas, açılan ekranda **Uygula**'ya basıp
   **kilit ekranını** seç (Poco'da seçenek "Ana ekran ve kilit ekranı" olarak çıkar). Analog saatler
   kilit ekranında gerçekten çalışır.
4. Şehirleri değiştirmek için takvim ekranında **uzun bas** → Ayarlar.

Poco/Xiaomi'de saat takılırsa: Ayarlar → Uygulamalar → Maarif Takvimi → **Pil tasarrufu: Kısıtlama yok**
ve **Otomatik başlat** açık olsun.

## Neler var

**Takvim sayfası (tam ekran)**
- Miladi, Hicri ve Rumi tarih. Hicri tarih Umm al-Qura takviminden hesaplanır ve Diyanet
  takviminden bir gün sapabilir.
- İki şehir: ad yazarak aranır. İstenirse 1. şehir telefonun konumundan otomatik belirlenir.
- Her şehir için canlı analog saat (şehrin saat diliminde), hava durumu ve ikon.
- Namaz vakitleri (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı) ve gün uzunluğunun
  uzama/kısalma miktarı.
- Halk takvimi notu, vecize ve "tarihte bugün" satırı (`assets/data.json`).
- İsteğe bağlı ajanda satırı: telefon takvimindeki bugünün sıradaki etkinliği.

**Arka yaprak** (ekrana dokununca yaprak çevrilir)
- Günün menüsü, bugün doğanlara isim önerileri, günün bilmecesi (cevabıyla) ya da fıkrası,
  tarihte bugün.

**Kilit ekranı** (isteğe bağlı, Ayarlar'dan)

- **Canlı kilit ekranı:** takvim yaprağı canlı duvar kağıdı olarak kilit ekranında görünür.
  Analog saatlerin akrep, yelkovan ve kırmızı saniye kolu gerçekten çalışır. Ekran kapalıyken
  hiçbir şey çizilmez. Ajanda gizlilik için kilit ekranına eklenmez.
- **Ana ekran resmi:** Bazı telefonlarda (örneğin Poco) canlı duvar kağıdı kilit ve ana ekrana
  birlikte uygulanır. Ayarlar'dan bir resim seçilirse telefon kilitliyken takvim, kilit
  açıkken bu resim görünür. Telefonun kendi duvar kağıdı ayarlarına dokunulmaz.
- **Statik seçenek:** Yaprak, her gece yenilenen sabit bir resim olarak da ayarlanabilir
  (analog saat bu modda donuk kalır). Canlı mod açıkken statik yenileme devre dışıdır.
- Bazı telefon markaları kilit ekranı duvar kağıdını kendi tema sistemleriyle yönettiği için
  bu özellikler her cihazda aynı çalışmayabilir.
- Kilit ekranında dokunma alınamadığı için yaprağın arkası yalnızca uygulamada görülür.

**Ana ekran widget'ı**
- Basit bir sürüm var. Yeni tasarıma uyarlanması yol haritasında.

## Ayarlar
Takvim ekranında **uzun basınca** ayarlar açılır: şehirler, otomatik konum, ajanda,
ekranın açık kalması ve kilit ekranı seçenekleri.

## Yol haritası

- Widget'ı yeni tasarıma ve farklı boyutlara uyarlama, otomatik yenileme
- Kilit ekranında ön ve arka yüzün sırayla gösterilmesi
- Kilit ekranında sayfanın üst boşluğunun ayarlanabilir olması (şimdilik ekran yüksekliğinin %32'si)
- Farklı markalarda (Samsung, Pixel vb.) deneme ve uyumluluk notları
- Sabit imzalı sürüm (şimdi her derleme farklı debug imzası taşır), gizlilik politikası ve yayın
- Daha fazla günlük içerik (halk takvimi, vecize, tarihte bugün)

## Veri kaynakları ve gizlilik
- **Namaz vakitleri:** [Aladhan API](https://aladhan.com/prayer-times-api), hesaplama yöntemi 13
  (Diyanet, deneysel). Vakitler hesaplanmıştır, resmî imsakiyeden birkaç dakika sapabilir.
  Aylık indirilip cihazda saklanır, internet yokken son indirilen ay kullanılır.
- **Hava durumu ve şehir arama:** [Open-Meteo](https://open-meteo.com/). Şehir koordinatları
  bu servislere gönderilir. Otomatik konum açıksa telefonun son bilinen konumu kullanılır.
- **Ajanda:** Telefonun takviminden yerelde okunur, cihazdan dışarı gönderilmez.
- Uygulama kullanıcı hesabı, reklam veya analiz aracı içermez.

## Günlük içerik
Menüler, isimler, bilmeceler ve fıkralar bu depoda kendi yazılmış metinlerdir. `data.json`
dosyasına eklenen alıntı, tarihte bugün ve benzeri metinlerin kaynağını ve telif durumunu
kontrol edin; başkasının telifli metnini eklemeyin.

## Derleme

- GitHub Actions `main`'e her `push`'ta derleyip testleri çalıştırır ve APK'yı **Releases**
  altındaki "son sürüm"e koyar. Sabit indirme bağlantısı:
  `https://github.com/geceleriesen/maariftakvim/releases/latest/download/MaarifTakvimi.apk`
- APK şimdilik debug imzalıdır. Her derlemenin imzası farklı olduğu için yeni sürümü kurmadan
  önce eskisini silmek gerekir; ayarlar sıfırlanır.
- Yerelde: Android Studio ile açıp derleyin (JDK 17, Android SDK 34).

## Yazı tipi
Büyük gün numarası [Anton](https://github.com/googlefonts/AntonFont) yazı tipiyle çizilir,
SIL Open Font License 1.1 ile lisanslıdır (`app/src/main/assets/fonts/OFL.txt`).

## Lisans
Henüz seçilmedi. Bir lisans eklenene kadar varsayılan olarak tüm hakları saklıdır.
