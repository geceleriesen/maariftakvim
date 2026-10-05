package com.geceleriesen.maariftakvim.domain

import java.time.LocalDate

data class DailyInfo(
    val girlNames: String,
    val boyNames: String,
    val menu: String,
    val riddle: String,
    val riddleAnswer: String,
    val joke: String
)

/**
 * Her gun icin deterministik (ayni gun = ayni icerik) arka yaprak icerigi uretir.
 * data.json'daki gunluk kayitlar bunlarin ustune yazar.
 */
object DailyContent {

    enum class Season { KIS, ILKBAHAR, YAZ, SONBAHAR }

    fun seasonOf(date: LocalDate): Season = when (date.monthValue) {
        12, 1, 2 -> Season.KIS
        3, 4, 5 -> Season.ILKBAHAR
        6, 7, 8 -> Season.YAZ
        else -> Season.SONBAHAR
    }

    private val menus: Map<Season, List<String>> = mapOf(
        Season.KIS to listOf(
            "Kuru fasulye, pirinç pilavı, turşu",
            "Mercimek çorbası, etli nohut, bulgur pilavı",
            "Karnabahar yemeği, makarna, yoğurt",
            "Ispanak yemeği, bulgur pilavı, yoğurt",
            "Zeytinyağlı pırasa, mantı, sarımsaklı yoğurt",
            "Tavuk haşlama, şehriyeli pilav, ayran",
            "Lahana sarması, yoğurt, ayran",
            "Etli kereviz, pirinç pilavı, cacık",
            "Ezogelin çorbası, kıymalı börek, ayran",
            "Karnıyarık, pirinç pilavı, cacık",
            "Etli türlü, bulgur pilavı, turşu",
            "Sebzeli güveç, makarna, yoğurt"
        ),
        Season.ILKBAHAR to listOf(
            "Zeytinyağlı enginar, dereotlu pilav, cacık",
            "Taze bakla, mercimek köftesi, ayran",
            "Semizotu yemeği, bulgur pilavı, yoğurt",
            "Kıymalı ıspanak, makarna, cacık",
            "Nohutlu pilav, tavuk sote, mevsim salata",
            "Yayla çorbası, patatesli köfte, pilav",
            "Pazı sarması, yoğurt, ayran",
            "Zeytinyağlı bezelye, şehriyeli pilav, cacık",
            "Mücver, yoğurt, bulgur pilavı",
            "Tavuklu pilav, cacık, turşu",
            "Kabak çiçeği dolması, yoğurt, ayran",
            "Fırında tavuk, patates, mevsim salata"
        ),
        Season.YAZ to listOf(
            "Taze fasulye, pirinç pilavı, cacık",
            "Patlıcan musakka, bulgur pilavı, yoğurt",
            "Etli bamya, şehriyeli pilav, cacık",
            "Kabak mücver, yoğurt, domates salatası",
            "Izgara köfte, közlenmiş biber, cacık",
            "Karnıyarık, pirinç pilavı, cacık",
            "İmam bayıldı, bulgur pilavı, ayran",
            "Zeytinyağlı barbunya, pilav, yoğurt",
            "Domates çorbası, biber dolması, cacık",
            "Etli türlü, pilav, ayran",
            "Patlıcan kebabı, çoban salata, pilav",
            "Menemen, çoban salata, ayran"
        ),
        Season.SONBAHAR to listOf(
            "Nohut yemeği, pirinç pilavı, turşu",
            "Kabak yemeği, bulgur pilavı, yoğurt",
            "Mantı, sarımsaklı yoğurt, ayran",
            "Mercimek çorbası, kıymalı pide, ayran",
            "Etli ıspanak, pilav, cacık",
            "Karnabahar kızartması, bulgur pilavı, yoğurt",
            "Biber dolması, yoğurt, ayran",
            "Tavuk güveç, pirinç pilavı, salata",
            "Etli pırasa, bulgur pilavı, yoğurt",
            "Lahana sarması, yoğurt, ayran",
            "Sebzeli köfte, makarna, cacık",
            "Tas kebabı, pirinç pilavı, çoban salata"
        )
    )

    private val girlNames = listOf(
        "Ayşe", "Fatma", "Zeynep", "Elif", "Merve", "Büşra", "Esra", "Hatice", "Emine", "Meryem",
        "Selin", "Derya", "Ceren", "Burcu", "Gizem", "Pınar", "Seda", "Sevgi", "Aylin", "Nur",
        "Yasemin", "Özlem", "Dilek", "Gül", "Sibel", "Hülya", "Nazlı", "Defne", "Melis", "İpek",
        "Ece", "Deniz", "Naz", "Sude", "Azra", "Asya", "Duru", "Eylül", "Yağmur", "Irmak",
        "Damla", "Aslı", "Buse", "Cansu", "Gamze", "Tuğba", "Sena", "Rabia", "Zehra", "Hande",
        "Leyla", "Nehir", "Beren", "Elçin", "Gonca", "Kübra", "Nilay", "Pelin", "Simge", "Songül",
        "Tülay", "Ülkü", "Vildan", "Yıldız", "Zübeyde", "Canan", "Figen", "Güler", "Havva", "Kader"
    )

    private val boyNames = listOf(
        "Mehmet", "Mustafa", "Ahmet", "Ali", "Hüseyin", "Hasan", "İbrahim", "İsmail", "Yusuf", "Murat",
        "Ömer", "Emre", "Burak", "Can", "Cem", "Kerem", "Efe", "Arda", "Eren", "Kaan",
        "Barış", "Onur", "Serkan", "Volkan", "Tolga", "Okan", "Uğur", "Gökhan", "Hakan", "Selim",
        "Kemal", "Orhan", "Osman", "Fatih", "Enes", "Yiğit", "Alp", "Berk", "Deniz", "Ege",
        "Furkan", "Halil", "İlker", "Levent", "Mert", "Necati", "Ozan", "Polat", "Rıza", "Sinan",
        "Taner", "Umut", "Veli", "Yılmaz", "Zafer", "Bora", "Cüneyt", "Doğan", "Erdem", "Ferhat",
        "Gürkan", "Haluk", "Kadir", "Metin", "Nihat", "Oğuz", "Recep", "Suat", "Tarık", "Yasin"
    )

    private val riddles = listOf(
        "Ben giderim o gider, ben dururum o durur." to "Gölge",
        "Dişleri var ısırmaz, saçlarımı düzeltir." to "Tarak",
        "Dört ayağı var yürümez, sırtında yük taşır." to "Masa",
        "Gökten iner, toprağı doyurur, ağacı yeşertir." to "Yağmur",
        "Gündüz görünmez, gece parlar, yıldızlarla gezer." to "Ay",
        "Her gün doğar, her akşam batar, dünyayı ısıtır." to "Güneş",
        "Evini sırtında taşır, boynuzları var ama koç değil." to "Salyangoz",
        "Yatağı var uyumaz, ağzı var yemez." to "Nehir",
        "Kuyruğu var uçar ama kuş değil, ipi çekilince yükselir." to "Uçurtma",
        "Her sabah kapıya gelir, içi haber doludur." to "Gazete",
        "Yazın herkes ister, sıcakta hemen erir." to "Dondurma",
        "Kırılmadan yenmez, içinde beyaz ve sarı var." to "Yumurta",
        "Ne gözü var görür ne kulağı var duyar, herkes ona bakıp saçını düzeltir." to "Ayna",
        "Tavandan sarkar, kanatları döner ama uçmaz." to "Tavan vantilatörü",
        "Suyun üstünde yüzer, rüzgâr onu iter, yelkeni vardır." to "Yelkenli",
        "Geceleri parlar ama lamba değil, yüzlercesi gökte dizilidir." to "Yıldız"
    )

    private val jokes = listOf(
        "Nasreddin Hoca bir gün göle bir kaşık yoğurt mayası çalmış. Görenler 'Hocam ne yapıyorsun?' diye sormuş. Hoca: 'Göl yoğurt olur mu bilmem ama tutarsa ne âlâ!' demiş.",
        "Hoca bir düğüne eski kıyafetle gidince kimse yüzüne bakmamış. Eve gidip kürkünü giyip dönmüş, baş köşeye oturtulmuş. Çorbayı kürkün koluna dökerek 'Ye kürküm ye, davet sana!' demiş.",
        "Hoca komşusundan bir kazan ödünç almış, geri verirken içine küçük bir kazan koymuş: 'Kazanınız doğurdu.' Sonra kazanı bir daha vermemiş: 'Kazanınız öldü.' Komşu şaşırınca Hoca: 'Doğurduğuna inandın da öldüğüne mi inanmıyorsun?' demiş.",
        "Hoca ağaca çıkmış, oturduğu dalı kesiyormuş. Yoldan geçen biri 'Düşeceksin' demiş. Düşünce Hoca adamın peşinden koşmuş: 'Madem geleceği biliyorsun, ben ne zaman öleceğim?'",
        "Hoca bir gece kuyuda ayın yansımasını görmüş. 'Ay kuyuya düşmüş!' diye ip atmış. İp kopunca sırtüstü yere düşmüş, gökteki ayı görünce 'Çok şükür, ay yerine döndü. Ben biraz yoruldum ama olsun' demiş.",
        "Hoca eşeğine ters binmiş. Görenler sorunca: 'Eşek nereye giderse gitsin, ben kimseye arkamı dönmek istemem' demiş.",
        "Hocaya sormuşlar: 'Kaç yaşındasın?' 'Kırk.' 'Ama üç yıl önce de kırk demiştin.' Hoca: 'Ben sözümden dönmem!' demiş.",
        "Hoca'ya misafir gelmiş, çorba ikram etmiş. Ertesi gün misafirin misafiri gelmiş, Hoca çorbaya su katmış. Bir gün sonra 'misafirin misafirinin misafiri' gelince tabağa sadece su koyup: 'Buyur, çorbanın çorbasının çorbası!' demiş."
    )

    fun forDate(date: LocalDate): DailyInfo {
        val doy = date.dayOfYear
        val seasonMenus = menus.getValue(seasonOf(date))
        val menu = seasonMenus[doy % seasonMenus.size]

        val girl = pick(girlNames, doy, 3)
        val boy = pick(boyNames, doy, 3)

        val useRiddle = doy % 2 == 1
        val index = doy / 2
        return if (useRiddle) {
            val r = riddles[index % riddles.size]
            DailyInfo(girl, boy, menu, r.first, r.second, "")
        } else {
            DailyInfo(girl, boy, menu, "", "", jokes[index % jokes.size])
        }
    }

    private fun pick(list: List<String>, dayOfYear: Int, count: Int): String {
        val start = (dayOfYear * count) % list.size
        return (0 until count).joinToString(", ") { list[(start + it) % list.size] }
    }
}
