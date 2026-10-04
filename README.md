# 📅 Büyük Saatli Maarif Takvimi (Android Widget & Application)

Nostaljik Büyük Saatli Maarif Takvimi estetiğini modern Android teknolojileriyle (Jetpack Glance, Kotlin, WorkManager) birleştiren açık kaynaklı bir Android widget ve kilit ekranı uygulaması.

## 🌟 Özellikler

### 🖼️ Ön Yüz (Nostaljik Tasarım)
- **Nostaljik Estetik:** Gazete/daktilo tonlarında tipografi ve yıldızlı çerçeve motifi.
- **Çoklu Takvim:** Miladi, Hicri ve Rumi takvim göstergeleri.
- **Çift Şehirli Simetrik Sütunlar:**
  - **Sol Sütun (Dinamik):** Otomatik GPS konumu (Anlık derece, hava durumu, lokal namaz vakitleri, lokal analog saat).
  - **Sağ Sütun (Sabit):** Kullanıcının seçtiği 2. şehir (Anlık derece, namaz vakitleri, analog saat).
- **Kişisel Ajanda Entegrasyonu:** Google Calendar / Cihaz ajandasından sıradaki etkinlik göstergesi (`[AJANDA]`).
- **Halk Takvimi & Vecize:** Fırtına, cemre, hamsin günleri notları ve günün sözü.

### 🔄 Arka Yüz (Yaprak Çevirme Animasyonu)
- **İsim Önerileri:** Bugün doğan kız ve erkek çocuk isimleri.
- **Günün Yemeği:** Günlük menü ve tarif önerisi.
- **Günün Fıkrası / Bilmecesi:** Cevabıyla birlikte eğlenceli içerikler.
- **Tarihte Bugün:** Detaylı tarihi olaylar listesi.

### ⚙️ Teknik & Arka Plan
- **Kilit Ekranı / AOD (Always On Display) & Screen Saver:** Kilit ekranında ve stant modunda çalışma.
- **Otomatik Arka Plan Servisi:** Gece 00:00'da otomatik yaprak yenileme, 1 saattelik hava durumu senkronizasyonu.
- **%100 Offline Çalışma:** İnternet kesilse dahi lokal JSON veritabanı sayesinde kesintisiz kullanım.
