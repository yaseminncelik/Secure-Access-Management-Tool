# Secure Access Management System

**Katmanlı Mimari ile OOP Odaklı Java Spring Boot Projesi**

## 🎯 Proje Açıklaması

Bu proje, Object-Oriented Programming (OOP) prensiplerini uygulayan kapsamlı bir Access Management sistemidir. Spring Boot framework'ü kullanarak katmanlı mimari (Layered Architecture) yapısı oluşturulmuştur.

## 🏗️ Proje Mimarisi

```
secure-access-system/
├── src/main/java/com/secureaccess/
│   ├── entity/              # Entity katmanı (Veri modeli)
│   │   ├── BaseEntity.java  # Parent class (Inheritance)
│   │   ├── Role.java        # ENUM (Type Safety)
│   │   └── User.java        # User entity
│   ├── dto/                 # DTO katmanı (Data Transfer Object)
│   │   ├── UserRequestDTO.java
│   │   ├── UserResponseDTO.java
│   │   ├── LoginRequestDTO.java
│   │   └── LoginResponseDTO.java
│   ├── repository/          # Repository katmanı (Database işlemleri)
│   │   └── UserRepository.java
│   ├── service/             # Service katmanı (Business logic)
│   │   ├── UserService.java (Interface - Polymorphism)
│   │   └── UserServiceImpl.java (Implementation)
│   ├── controller/          # Controller katmanı (HTTP endpoints)
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── DashboardController.java
│   ├── exception/           # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── UserNotFoundException.java
│   │   ├── InvalidCredentialsException.java
│   │   └── UserAlreadyExistsException.java
│   ├── DataInitializer.java # Demo veri oluşturma
│   └── SecureAccessApplication.java
├── src/main/resources/
│   ├── templates/           # Thymeleaf sayfaları
│   │   ├── login.html
│   │   └── users.html
│   ├── static/             # Static dosyalar (CSS, JS)
│   └── application.properties
└── pom.xml
```

## 🧠 OOP Prensiplerinin Uygulanması

### 1. **Encapsulation (Kapsülleme)**
- **Private Fields**: Tüm entity field'ları private
- **Getter/Setter**: Lombok ile otomatik oluşturulur
- **Data Hiding**: Entity direkt dönmeyip DTO kullanılır
- Örnek: `UserResponseDTO` password alanını içermez

### 2. **Inheritance (Kalıtım)**
- **BaseEntity**: Tüm entity'lerin parent class'ı
  - `id`: Primary Key
  - `createdAt`: Oluşturulma tarihi
  - `updatedAt`: Güncellenme tarihi
- `User` entity `BaseEntity`'den türer
- Code reusability ve consistency sağlanır

### 3. **Polymorphism (Çok Biçimlilik)**
- **Interface & Implementation**:
  - `UserService`: Interface (sözleşme)
  - `UserServiceImpl`: Implementation (gerçekleme)
- Dependency injection ile kullanılır
- Service değiştirilse de controller kodu değişmez

### 4. **Abstraction (Soyutlama)**
- **Controller**: İş mantığı bilmez, sadece request/response
- **Service**: Business logic barındırır
- **Repository**: Database işlemlerini soyutlar
- `GlobalExceptionHandler`: Exception'lar merkezi yerden yönetilir

## 🔄 Katmanlı Mimari Akışı

```
HTTP Request
    ↓
Controller (Request alır, validate eder)
    ↓
Service (Business logic - CRUD, Login)
    ↓
Repository (Database işlemleri)
    ↓
Entity (Veri modeli)
    ↓
H2 Database (In-Memory)
    ↓
Repository (Sonuç döner)
    ↓
Service (Dönüştürmeler)
    ↓
DTO (Response hazırlanır)
    ↓
Controller (Response döner)
    ↓
HTTP Response (JSON)
```

## 📋 Bağımlılıklar

```xml
- Spring Boot 3.2.0
- Spring Web (REST API)
- Spring Data JPA (Database işlemleri)
- H2 Database (In-Memory)
- Lombok (Boilerplate code azaltma)
- Validation (Input validation)
- Thymeleaf (Frontend)
```

## 🚀 Kurulum & Çalıştırma

### Gereksinimler
- **Java 17+**
- **Maven 3.8+**

### Adımlar

1. **Projeyi Git'ten klon et** (veya zip dosyasını aç)
```bash
cd secure-access-system
```

2. **Maven build çalıştır**
```bash
mvn clean install
```

3. **Uygulamayı çalıştır**
```bash
mvn spring-boot:run
```

4. **Tarayıcıda aç**
- **Frontend**: http://localhost:8080/login
- **H2 Console**: http://localhost:8080/api/h2-console

## 🔐 Demo Credentials

```
Username: admin
Password: password

Username: user1
Password: password123

Username: user2
Password: password456
```

## 📡 API Endpoints

### Authentication
- **POST** `/api/auth/login` - Giriş yap
  ```json
  {
    "username": "admin",
    "password": "password"
  }
  ```

### Users
- **GET** `/api/users` - Tüm kullanıcıları listele
- **GET** `/api/users/{id}` - Kullanıcı detaylarını getir
- **POST** `/api/users` - Yeni kullanıcı oluştur
  ```json
  {
    "username": "newuser",
    "password": "password",
    "email": "newuser@example.com",
    "role": "USER"
  }
  ```
- **PUT** `/api/users/{id}` - Kullanıcı güncelle
- **DELETE** `/api/users/{id}` - Kullanıcı sil

## 🌐 Frontend Sayfaları

### 1. Login (`/login`)
- Username/Password girişi
- Form validation
- Hata/Başarı mesajları
- Session storage'a kullanıcı kaydı

### 2. Users (`/users`)
- Tüm kullanıcıları tablo içinde görüntüle
- Add New User butonu
- Edit/Delete işlemleri
- Filtreleme (Role, Status)
- Logout butonu

## 🛡️ Exception Handling

Tüm exception'lar `GlobalExceptionHandler` tarafından merkezi yerden yönetilir:

```
- UserNotFoundException (404)
- InvalidCredentialsException (401)
- UserAlreadyExistsException (409)
- ValidationException (400)
- Generic Exception (500)
```

## 📊 Veritabanı

### H2 In-Memory Database
- Uygulama restart → Veriler silinir
- H2 Console: http://localhost:8080/api/h2-console
- DDL otomatik oluşturulur (`spring.jpa.hibernate.ddl-auto=create-drop`)

### Tables
- **users**: User tablosu
  - id (PK, Auto-increment)
  - username (Unique, Not Null)
  - password (Not Null)
  - email (Unique, Not Null)
  - role (Enum: ADMIN, USER)
  - isActive (Boolean)
  - createdAt (Timestamp)
  - updatedAt (Timestamp)

## 🎓 OOP Prensipleri Öğrenme Noktaları

1. **Encapsulation**
   - Private field'lar, public getter/setter'lar
   - Entity'de password, DTO'da yok

2. **Inheritance**
   - BaseEntity sınıfı ve türeme mekanizması
   - @MappedSuperclass annotation'ı

3. **Polymorphism**
   - Interface (UserService) ve Implementation (UserServiceImpl)
   - Method override

4. **Abstraction**
   - Her katmanın sorumluluğu net
   - Interface'ler kullanılarak bağımlılık azaltılması

## 📝 Kod Kalitesi

- ✅ SOLID Prensipleri uygulanmış
- ✅ Clean Code yazılmış
- ✅ DTOs kullanarak Entity direkt döndürme önlenmiş
- ✅ Transaction management uygulanmış
- ✅ Input validation mevcudiyet
- ✅ Exception handling merkezi
- ✅ Lombok ile boilerplate azaltılmış
- ✅ Dosya yapısı düzenli

## 🔄 Request/Response Örneği

### Kullanıcı Oluştur
**Request:**
```http
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "password": "secure123",
  "email": "john@example.com",
  "role": "USER"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-04-27T10:30:00",
    "updatedAt": "2024-04-27T10:30:00"
  }
}
```

## 🐛 Troubleshooting

### Port zaten kullanımda
```bash
# Başka port kullan
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Maven dependency hatası
```bash
mvn clean dependency:resolve
```

### H2 Console erişim problemi
- URL doğru: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (boş bırak)
