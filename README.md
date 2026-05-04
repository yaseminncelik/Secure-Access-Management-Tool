# Güvenli Erişim ve URL Yönetim Sistemi (Secure Access System)

Bu proje, bir kurum içerisindeki kullanıcıların belirli URL'lere erişim yetkilerini merkezi olarak yönetmek ve test etmek amacıyla geliştirilmiştir. Projenin ana odağı, Nesneye Dayalı Programlama (NDP) prensiplerini gerçek bir senaryo üzerinde uygulamaktır.

## Projenin Amacı
Sistem, adminlerin kullanıcılar için "izinli URL listesi" (whitelist) oluşturmasına ve bu kuralların bir simülatör üzerinden test edilmesine olanak sağlar. Kullanıcılar sadece kendilerine tanımlanan adreslere erişebilirler.

## Kullanılan Teknolojiler
- **Backend:** Java, Spring Boot, Spring Security (JWT Tabanlı Kimlik Doğrulama)
- **Frontend:** HTML5, CSS3, JavaScript (Fetch API)
- **Veritabanı:** H2 Database (Bellek içi çalışan test veritabanı)

## Temel Çalışma Mantığı
1. **Kimlik Doğrulama:** Kullanıcılar e-posta ve şifreleri ile sisteme giriş yapar. Sistem bir JWT token üretir.
2. **Rol Yönetimi:** İki temel rol vardır: `ADMIN` (Kullanıcı ve URL yönetimi yapar) ve `USER` (Kendi erişimlerini görüntüler).
3. **Erişim Denetimi:** Her istekte kullanıcının token'ı kontrol edilir ve gitmek istediği URL'in kendi listesinde olup olmadığı denetlenir.
4. **Politika Simülatörü:** Adminler, herhangi bir kullanıcının belirli bir URL'e erişip erişemeyeceğini "Policy Simulator" sayfasından test edebilir.

## Uygulanan NDP Prensipleri
Kod içerisinde aşağıdaki temel prensipler vurgulanmıştır:
- **Encapsulation (Kapsülleme):** Entity ve DTO sınıflarında verilerin private fieldlar ile korunması.
- **Abstraction (Soyutlama):** Service katmanında Interface kullanımıyla iş mantığının gizlenmesi.
- **Polymorphism (Çok Biçimlilik):** Interface implementasyonları ve metot ezme (override) işlemleri.
- **Inheritance (Kalıtım):** Ortak özelliklerin (id, tarih vb.) Base sınıflardan aktarılması.
