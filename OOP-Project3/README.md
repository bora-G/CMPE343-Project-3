# Local Greengrocer Project

A JavaFX + JDBC + MySQL application for managing a local greengrocer business.

**CMPE343 Project 3 - Group 17**

---

## 📋 İçindekiler

- [Gereksinimler](#-gereksinimler)
- [Kurulum](#-kurulum)
- [Veritabanı Kurulumu](#-veritabanı-kurulumu)
- [Projeyi Çalıştırma](#-projeyi-çalıştırma)
- [Giriş Bilgileri](#-giriş-bilgileri)
- [Özellikler](#-özellikler)
- [Proje Yapısı](#-proje-yapısı)
- [Sorun Giderme](#-sorun-giderme)

---

## 🔧 Gereksinimler

Projeyi çalıştırmak için aşağıdaki yazılımların kurulu olması gerekmektedir:

### 1. Java Development Kit (JDK)
- **Versiyon**: JDK 11 veya üzeri (JDK 17 önerilir)
- **İndirme**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) veya [OpenJDK](https://adoptium.net/)
- **Kontrol**: Komut satırında `java -version` komutunu çalıştırarak kontrol edebilirsiniz

### 2. Apache Maven
- **Versiyon**: 3.6.0 veya üzeri
- **İndirme**: [Maven Download](https://maven.apache.org/download.cgi)
- **Kurulum**: 
  - Windows: ZIP dosyasını indirip çıkarın, `bin` klasörünü sistem PATH'ine ekleyin
  - Alternatif: [Maven Installation Guide](https://maven.apache.org/install.html)
- **Kontrol**: Komut satırında `mvn -version` komutunu çalıştırarak kontrol edebilirsiniz

### 3. MySQL Server
- **Versiyon**: MySQL 5.7 veya üzeri (MySQL 8.0 önerilir)
- **İndirme**: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
- **Kurulum**: 
  - Windows: MySQL Installer'ı kullanarak kurun
  - Kurulum sırasında root şifresini belirleyin (varsayılan: `1234`)
- **Kontrol**: MySQL servisinin çalıştığından emin olun

### 4. MySQL Workbench (Opsiyonel - Önerilir)
- **İndirme**: [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)
- Veritabanı yönetimi ve SQL dosyası import işlemleri için kullanılabilir

---

## 🚀 Kurulum

### Adım 1: Projeyi İndirin

Projeyi bilgisayarınıza indirin veya klonlayın:
```bash
git clone <repository-url>
cd OOP-Project3
```

### Adım 2: Maven Bağımlılıklarını İndirin

Proje klasöründe aşağıdaki komutu çalıştırın:
```bash
mvn clean install
```

Bu komut tüm bağımlılıkları (JavaFX, MySQL Connector, vb.) otomatik olarak indirecektir.

**Not**: İnternet bağlantısı gereklidir. İlk çalıştırmada bağımlılıklar indirileceği için biraz zaman alabilir.

### Adım 3: Veritabanı Bağlantı Ayarlarını Kontrol Edin

`src/main/java/com/group17/greengrocer/util/DatabaseAdapter.java` dosyasını açın ve MySQL şifrenizi kontrol edin:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234"; // MySQL şifrenizi buraya yazın
```

**Önemli**: Eğer MySQL root şifreniz `1234` değilse, bu dosyada şifreyi güncelleyin.

---

## 💾 Veritabanı Kurulumu

### Yöntem 1: Otomatik Kurulum (Windows - Önerilir)

1. `setup-database.bat` dosyasına çift tıklayın
2. Script otomatik olarak:
   - Veritabanını oluşturacak
   - Tabloları oluşturacak
   - Örnek verileri yükleyecek

**Not**: MySQL şifreniz `1234` değilse, `setup-database.bat` dosyasını düzenleyip şifrenizi güncelleyin.

### Yöntem 2: MySQL Workbench ile Kurulum (En Kolay)

Detaylı adımlar için `IMPORT_WITH_WORKBENCH.md` dosyasına bakın.

**Kısa Özet:**
1. MySQL Workbench'i açın
2. MySQL sunucunuza bağlanın (root / şifreniz)
3. **Server** → **Data Import** menüsüne gidin
4. **"Import from Self-Contained File"** seçeneğini işaretleyin
5. `database/schema.sql` dosyasını seçin
6. **Default Target Schema** olarak `greengrocer_db` seçin (yoksa oluşturun)
7. **Start Import** butonuna tıklayın

### Yöntem 3: Komut Satırı ile Kurulum

1. MySQL komut satırını açın:
   ```bash
   mysql -u root -p
   ```

2. Şifrenizi girin

3. Veritabanını oluşturun:
   ```sql
   CREATE DATABASE IF NOT EXISTS greengrocer_db;
   USE greengrocer_db;
   ```

4. SQL dosyasını import edin:
   ```sql
   source database/schema.sql;
   ```
   
   Veya Windows'ta:
   ```bash
   mysql -u root -p greengrocer_db < database\schema.sql
   ```

### Veritabanı Kurulumunu Doğrulama

MySQL Workbench veya komut satırında:
```sql
USE greengrocer_db;
SHOW TABLES;
```

Şu tabloları görmelisiniz:
- `UserInfo`
- `ProductInfo`
- `OrderInfo`
- `OrderItem`
- `Coupon`
- `CarrierRating`
- `Message`

Örnek verileri kontrol etmek için:
```sql
SELECT * FROM UserInfo LIMIT 5;
SELECT * FROM ProductInfo LIMIT 5;
```

---

## ▶️ Projeyi Çalıştırma

### Yöntem 1: Maven ile Çalıştırma (Önerilir)

Komut satırında proje klasöründe:
```bash
mvn clean javafx:run
```

### Yöntem 2: Windows Batch Script ile

`run.bat` dosyasına çift tıklayın veya komut satırında:
```bash
run.bat
```

### Yöntem 3: IDE ile Çalıştırma

#### IntelliJ IDEA
1. Projeyi **File** → **Open** ile açın
2. Maven projesi olarak import edin
3. **Run** → **Edit Configurations**
4. **+** butonuna tıklayıp **Maven** seçin
5. **Command line**: `clean javafx:run` yazın
6. **Apply** ve **Run**

Alternatif olarak, `Main.java` dosyasını açıp sağ tıklayarak **Run 'Main.main()'** seçebilirsiniz (JavaFX modül ayarları gerekebilir).

#### Eclipse
1. Projeyi **File** → **Import** → **Existing Maven Projects** ile açın
2. **Run** → **Run Configurations**
3. **Maven Build** oluşturun
4. **Goals**: `clean javafx:run`
5. **Run**

#### VS Code
1. Java Extension Pack'i yükleyin
2. Projeyi açın
3. Terminal'de `mvn clean javafx:run` komutunu çalıştırın

---

## 🔐 Giriş Bilgileri

Uygulama örnek kullanıcılarla birlikte gelir:

### Müşteri (Customer)
- **Kullanıcı Adı**: `customer1`
- **Şifre**: `customer123`

### Kurye (Carrier)
- **Kullanıcı Adı**: `carrier1`
- **Şifre**: `carrier123`

### Sahip (Owner)
- **Kullanıcı Adı**: `owner1`
- **Şifre**: `owner123`

**Not**: Veritabanında daha fazla örnek kullanıcı bulunmaktadır (`customer2`, `customer3`, `carrier2`, vb.)

---

## ✨ Özellikler

### Müşteri Özellikleri
- ✅ Ürünleri türe göre gruplandırılmış şekilde görüntüleme (TitledPane ile)
- ✅ Ürünleri alfabetik sıralama
- ✅ Sadece stokta olan ürünleri gösterme
- ✅ Ürün arama (büyük/küçük harf duyarsız)
- ✅ Sepete kg cinsinden ürün ekleme
- ✅ Girdi doğrulama (negatif, sıfır, sayısal olmayan değerleri engelleme)
- ✅ Sepette aynı ürünleri birleştirme
- ✅ Sepeti ayrı pencerede görüntüleme
- ✅ Sipariş tamamlama:
  - Teslimat zamanı doğrulama (48 saat içinde)
  - Doğru toplam maliyet hesaplama
  - Eşik kuralı (eşik altında fiyat ikiye katlanır)
- ✅ Fatura oluşturma ve kaydetme

### Kurye Özellikleri
- ✅ Mevcut siparişleri görüntüleme
- ✅ Atanmış siparişleri görüntüleme
- ✅ Tamamlanmış siparişleri görüntüleme
- ✅ Sipariş seçme (aynı siparişin birden fazla kuryeye atanmasını önleme)
- ✅ Siparişleri tamamlandı olarak işaretleme

### Sahip Özellikleri
- ✅ Ürün ekleme/güncelleme/silme
- ✅ Ürün stoğunu güncelleme
- ✅ Kurye yönetimi (işe alma/çıkarma)
- ✅ Raporlar:
  - Kar raporu
  - Teslim edilen siparişler raporu
  - Kurye performans raporu (grafiklerle)

---

## 📁 Proje Yapısı

```
OOP-Project3/
├── database/
│   └── schema.sql              # Veritabanı şeması ve örnek veriler
├── src/
│   └── main/
│       ├── java/
│       │   └── com/group17/greengrocer/
│       │       ├── app/        # Ana uygulama giriş noktası
│       │       ├── controller/ # JavaFX controller'ları
│       │       ├── model/      # POJO/entity sınıfları
│       │       ├── repository/ # Veritabanı erişim katmanı
│       │       ├── service/    # İş mantığı katmanı
│       │       └── util/       # Yardımcı sınıflar
│       └── resources/
│           ├── css/           # Stil dosyaları
│           ├── fxml/          # FXML görünüm dosyaları
│           └── images/        # Görsel kaynaklar
├── lib/                        # Harici JAR dosyaları
│   ├── mysql-connector-j-8.2.0.jar
│   └── pdfbox-2.0.29.jar
├── pom.xml                     # Maven yapılandırma dosyası
├── run.bat                     # Windows çalıştırma scripti
├── setup-database.bat          # Veritabanı kurulum scripti
├── DATABASE_SETUP.md           # Veritabanı kurulum dokümantasyonu
├── IMPORT_WITH_WORKBENCH.md    # MySQL Workbench ile import rehberi
└── README.md                   # Bu dosya
```

---

## 🏗️ Mimari

Proje **MVC (Model-View-Controller)** mimarisini takip eder:

- **Model**: Entity sınıfları (User, Product, Order, OrderItem)
- **View**: FXML dosyaları ile tanımlanan UI
- **Controller**: Kullanıcı etkileşimlerini yöneten JavaFX controller'ları

### Katman Ayrımı

- **Repository Katmanı**: Tüm veritabanı erişimlerini yönetir (JDBC/SQL)
- **Service Katmanı**: İş mantığını içerir ve iş kurallarını uygular
- **Controller Katmanı**: UI olaylarını yönetir ve servislere delege eder
- **Controller'larda SQL Yok**: Tüm veritabanı işlemleri repository'lerde
- **Repository'lerde İş Mantığı Yok**: İş kuralları servislerde uygulanır

---

## 📜 İş Kuralları

1. **Eşik Fiyatlandırması**: Sipariş edilen miktar ürün eşiğinin altındaysa, kg başına fiyat ikiye katlanır.
2. **Teslimat Zamanı**: Teslimat, sipariş verilmesinden itibaren 48 saat içinde planlanmalıdır.
3. **Stok Doğrulama**: Stok = 0 olan ürünler müşterilere gösterilmez.
4. **Sipariş Atama**: Bir siparişe sadece bir kurye atanabilir (transaction tabanlı).

---

## 🔧 Sorun Giderme

### Veritabanı Bağlantı Sorunları

**Hata**: `Access denied for user 'root'@'localhost'`
- MySQL şifrenizi kontrol edin
- `DatabaseAdapter.java` dosyasındaki şifrenin doğru olduğundan emin olun
- MySQL servisinin çalıştığını kontrol edin

**Hata**: `Unknown database 'greengrocer_db'`
- Veritabanını oluşturun: `CREATE DATABASE greengrocer_db;`
- `setup-database.bat` scriptini çalıştırın

**Hata**: `Table doesn't exist`
- `database/schema.sql` dosyasını import edin
- MySQL Workbench veya komut satırı ile import işlemini tekrarlayın

### JavaFX Sorunları

**Hata**: `Error: JavaFX runtime components are missing`
- Maven bağımlılıklarının indirildiğinden emin olun: `mvn clean install`
- `mvn javafx:run` komutunu kullanın (IDE yerine)

**Hata**: `Module javafx.controls not found`
- `pom.xml` dosyasının doğru olduğundan emin olun
- Maven projesini yeniden yükleyin (IDE'de)

### Derleme Sorunları

**Hata**: `Package does not exist` veya `Cannot find symbol`
- Maven bağımlılıklarını indirin: `mvn clean install`
- IDE'de Maven projesini yeniden import edin
- `target` klasörünü silip tekrar derleyin: `mvn clean compile`

**Hata**: `FXML file not found`
- `src/main/resources/fxml/` klasöründeki FXML dosyalarının varlığını kontrol edin
- Dosya yollarının doğru olduğundan emin olun

### Maven Sorunları

**Hata**: `'mvn' is not recognized as an internal or external command`
- Maven'ın kurulu olduğunu kontrol edin: `mvn -version`
- Maven'ı sistem PATH'ine ekleyin
- IDE'de Maven plugin'inin yüklü olduğundan emin olun

**Hata**: `Could not resolve dependencies`
- İnternet bağlantınızı kontrol edin
- Maven repository erişimini kontrol edin
- Proxy ayarlarını kontrol edin (gerekirse)

### Diğer Sorunlar

**Uygulama açılmıyor**
- Java versiyonunu kontrol edin: `java -version` (JDK 11+ olmalı)
- MySQL servisinin çalıştığını kontrol edin
- Veritabanı bağlantı ayarlarını kontrol edin

**Fatura oluşturulamıyor**
- `invoices` klasörünün yazılabilir olduğundan emin olun
- Disk alanını kontrol edin

---

## 📚 Kullanılan Teknolojiler

- **Java**: JDK 11+
- **JavaFX**: 17.0.2 (GUI framework)
- **MySQL**: 5.7+ (Veritabanı)
- **JDBC**: MySQL Connector/J 8.0.33
- **Maven**: Bağımlılık yönetimi ve build aracı
- **Apache PDFBox**: 2.0.29 (Fatura oluşturma için)

---

## 📝 Notlar

- Fatura oluşturma basitleştirilmiştir (yol veritabanında saklanır). Üretim sisteminde gerçek PDF dosyaları oluşturulur.
- Uygulama, DatabaseAdapter ve Session yönetimi için singleton pattern kullanır.
- Tüm veritabanı işlemleri SQL injection'ı önlemek için PreparedStatement kullanır.
- Girdi doğrulama hem UI hem de servis katmanında yapılır.

---

## 👥 Katkıda Bulunanlar

**Group 17 - CMPE343 Project 3**

---

## 📄 Lisans

Bu proje eğitim amaçlı oluşturulmuştur (CMPE343 Project 3).

---

## 📞 Destek

Sorun yaşarsanız:
1. Bu README dosyasındaki **Sorun Giderme** bölümüne bakın
2. `DATABASE_SETUP.md` ve `IMPORT_WITH_WORKBENCH.md` dosyalarını kontrol edin
3. Proje yapısını ve kod yorumlarını inceleyin

---

**Son Güncelleme**: 2024
