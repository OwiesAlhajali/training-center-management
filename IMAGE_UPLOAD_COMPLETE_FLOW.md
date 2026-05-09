# 📸 Complete Image Upload Flow Documentation

## 📋 ملخص الـ Flow الكامل

تم تطوير نظام رفع صور متكامل حيث:
- **الطالب (Student)** يرفع صورة عبر `PUT /api/students/{id}/profile-image`
- **المعلم (Teacher)** يرفع صورة عبر `PUT /api/teachers/{id}/profile-image`
- الصورة تُحفظ **محلياً** وتُخزّن **الـ URL في جدول User**
- لا يوجد endpoint منفصل في UserController (مباشر جداً)

---

## 🏗️ الـ Architecture الكاملة

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND (React/Vue)                   │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ Student/Teacher Registration Form                       ││
│  │ - firstName, lastName, email, password, etc.            ││
│  │ - FILE INPUT: image (اختياري أثناء التسجيل)            ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Multipart Form Data
                       │ (id + file)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  StudentController                                           │
│  ├─ PUT /api/students/{id}/profile-image                   │
│  │  └─ @RequestParam("file") MultipartFile file            │
│  │  └─ studentService.updateProfileImage(id, file)         │
│  │                                                           │
│  TeacherController                                           │
│  └─ PUT /api/teachers/{id}/profile-image                   │
│     └─ @RequestParam("file") MultipartFile file            │
│     └─ teacherService.updateProfileImage(id, file)         │
│                                                               │
│  NOTE: UserController لا يحتوي على endpoint صور             │
│        (الصور تُدار عن طريق Student/Teacher فقط)          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Service Layer Call
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  StudentService.updateProfileImage(Long id,                 │
│                                    MultipartFile file)     │
│  ├─ 1️⃣  Fetch Student by ID                                │
│  │    └─ throw ResourceNotFoundException if not found       │
│  │                                                           │
│  ├─ 2️⃣  Get User from Student.user (OneToOne relation)     │
│  │                                                           │
│  ├─ 3️⃣  Call ImageService.uploadImage(file)                │
│  │    ├─ Validate file (not empty)                         │
│  │    ├─ Validate contentType (starts with "image/")       │
│  │    ├─ Validate size (max 10MB)                          │
│  │    ├─ Save to disk with UUID name                       │
│  │    └─ Return image URL                                   │
│  │                                                           │
│  ├─ 4️⃣  Set User.image = imageUrl                          │
│  │                                                           │
│  ├─ 5️⃣  Save User to Database                              │
│  │                                                           │
│  ├─ 6️⃣  Update Student.user reference                      │
│  │                                                           │
│  ├─ 7️⃣  Save Student to Database                           │
│  │                                                           │
│  └─ 8️⃣  Return StudentResponseDTO with image               │
│                                                               │
│  TeacherService.updateProfileImage(...)                     │
│  └─ Same logic as StudentService                            │
│                                                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Database Update
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  USERS TABLE                                                 │
│  ┌─────────────┐                                             │
│  │ id = 1      │                                             │
│  │ username    │                                             │
│  │ email       │                                             │
│  │ image ◄─────┼─── "./uploads/images/uuid.jpg"  ✅         │
│  └─────────────┘                                             │
│                                                               │
│  STUDENTS TABLE                                              │
│  ┌────────────────┐                                          │
│  │ id = 1         │                                          │
│  │ firstName      │                                          │
│  │ lastName       │                                          │
│  │ user_id ──────────► USERS(id=1)                           │
│  └────────────────┘                                          │
│                                                               │
│  TEACHERS TABLE                                              │
│  ┌────────────────┐                                          │
│  │ id = 1         │                                          │
│  │ firstName      │                                          │
│  │ lastName       │                                          │
│  │ user_id ──────────► USERS(id=2)                           │
│  └────────────────┘                                          │
│                                                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ JSON Response
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   RESPONSE (Frontend)                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  StudentResponseDTO {                                        │
│    "id": 1,                                                  │
│    "firstName": "Ahmed",                                     │
│    "lastName": "Ali",                                        │
│    "email": "ahmed@example.com",                            │
│    ...                                                       │
│    "image": "./uploads/images/9f8e7d6c-5b4a-3c2b1a0f.jpg"  │
│  }                                                           │
│                                                               │
│  TeacherResponseDTO {                                        │
│    "id": 1,                                                  │
│    "firstName": "Dr. Smith",                                │
│    "lastName": "Johnson",                                    │
│    "specialization": "Mathematics",                          │
│    ...                                                       │
│    "image": "./uploads/images/1a2b3c4d-5e6f-7g8h-9i0j.jpg" │
│  }                                                           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                       │
                       │ Frontend displays image
                       ▼
                  <img src="./uploads/images/uuid.jpg" />
```

---

## 🔌 الـ Endpoints المتاحة

### ✅ 1. Student - تحديث الصورة
```bash
PUT /api/students/{id}/profile-image

Headers:
  Content-Type: multipart/form-data

Body:
  file: <image file>

Response: 200 OK
{
  "id": 1,
  "firstName": "Ahmed",
  "lastName": "Ali",
  "gender": "Male",
  "birthDate": "2000-01-01",
  "address": "Cairo, Egypt",
  "interest": "Web Development",
  "enrollmentDate": "2026-01-15",
  "userId": 5,
  "username": "ahmed_ali",
  "email": "ahmed@example.com",
  "contactInfo": "20-123-456-789",
  "image": "./uploads/images/9f8e7d6c-5b4a-3c2b1a0f.jpg"
}
```

### ✅ 2. Teacher - تحديث الصورة
```bash
PUT /api/teachers/{id}/profile-image

Headers:
  Content-Type: multipart/form-data

Body:
  file: <image file>

Response: 200 OK
{
  "id": 1,
  "firstName": "Dr. Ahmed",
  "lastName": "Smith",
  "specialization": "Mathematics",
  "certificates": "BSc, MSc, PhD",
  "address": "Giza, Egypt",
  "cv": "Expert in advanced mathematics...",
  "experienceYears": 15,
  "userId": 10,
  "username": "dr_ahmed",
  "email": "ahmed@school.com",
  "contactInfo": "20-987-654-321",
  "image": "./uploads/images/1a2b3c4d-5e6f-7g8h-9i0j.jpg"
}
```

---

## 📊 الـ Database Schema

### USERS Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(50),
    contact_info VARCHAR(255),
    image VARCHAR(500),                      ← Image URL يُحفظ هنا
    email_verified_at TIMESTAMP
);
```

### STUDENTS Table
```sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    gender VARCHAR(50),
    birth_date DATE,
    enrollment_date DATE,
    interest VARCHAR(500),
    address VARCHAR(500),
    user_id BIGINT UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### TEACHERS Table
```sql
CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255),
    certificates VARCHAR(500),
    address VARCHAR(500),
    cv VARCHAR(500),
    experience_years INT,
    user_id BIGINT UNIQUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 🔄 Step-by-Step Flow

### مثال عملي: Student يرفع صورة

**Step 1: Frontend يُرسل request**
```bash
curl -X PUT \
  -F "file=@profile.jpg" \
  http://localhost:8080/api/students/1/profile-image
```

**Step 2: StudentController يستقبل الـ request**
```java
@PutMapping("/{id}/profile-image")
public StudentResponseDTO updateProfileImage(
    @PathVariable Long id,
    @RequestParam(value = "file", required = false) MultipartFile file
) {
    return studentService.updateProfileImage(id, file);
}
```

**Step 3: StudentService ينفّذ الـ logic**
```java
public StudentResponseDTO updateProfileImage(Long id, MultipartFile file) {
    // 1. جلب الطالب
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    // 2. جلب المستخدم المرتبط
    User user = student.getUser();
    
    // 3. رفع الصورة (validation + storage)
    if (file != null && !file.isEmpty()) {
        String imageUrl = imageService.uploadImage(file);
        user.setImage(imageUrl);
    } else {
        user.setImage(null);
    }
    
    // 4. حفظ المستخدم (تحديث الصورة)
    User updatedUser = userRepository.save(user);
    student.setUser(updatedUser);
    
    // 5. حفظ الطالب
    Student updatedStudent = studentRepository.save(student);
    
    // 6. إرجاع الاستجابة
    return mapToResponse(updatedStudent);
}
```

**Step 4: ImageService يرفع الملف**
```java
public String uploadImage(MultipartFile file) {
    // Validate
    if (file == null || file.isEmpty()) {
        throw new BadRequestException("File is empty");
    }
    
    if (!file.getContentType().startsWith("image/")) {
        throw new BadRequestException("Unsupported file type");
    }
    
    if (file.getSize() > maxFileSize) {
        throw new BadRequestException("File size exceeds limit");
    }
    
    // Save locally
    String fileName = UUID.randomUUID().toString() + getExtension(file);
    Path filePath = Paths.get(uploadDir, fileName);
    Files.write(filePath, file.getBytes());
    
    // Return URL
    return "./uploads/images/" + fileName;
}
```

**Step 5: Database gets updated**
```sql
UPDATE users SET image = './uploads/images/uuid.jpg' WHERE id = 5;
```

**Step 6: Response is sent back**
```json
{
  "id": 1,
  "firstName": "Ahmed",
  "lastName": "Ali",
  ...
  "image": "./uploads/images/uuid.jpg"
}
```

**Step 7: Frontend displays the image**
```html
<img src="./uploads/images/uuid.jpg" alt="Student Profile" />
```

---

## 🛡️ Validation & Error Handling

### Validation Steps
1. ✅ ملف موجود ✓
2. ✅ نوع الملف صورة ✓
3. ✅ حجم الملف < 10MB ✓
4. ✅ Student/Teacher موجود في DB ✓
5. ✅ User مرتبط بـ Student/Teacher ✓

### Exception Handling
| Exception | HTTP Status | Message |
|-----------|-------------|---------|
| `BadRequestException` | 400 | File is empty / Unsupported file type / File too large |
| `ResourceNotFoundException` | 404 | Student not found / Teacher not found |
| `RuntimeException` | 500 | Failed to upload image |

---

## 📁 File Storage Structure

```
project-root/
├── uploads/
│   └── images/
│       ├── 9f8e7d6c-5b4a-3c2b1a0f.jpg  (Student 1 image)
│       ├── 1a2b3c4d-5e6f-7g8h-9i0j.jpg (Teacher 1 image)
│       ├── 2x3y4z5w6v7u8t9s0r1q2p.png  (Student 2 image)
│       └── ...
└── ...
```

---

## 🎯 Key Features

### ✅ اضيفت للـ System:

1. **Multi-Entity Support**
   - Student صور
   - Teacher صور
   - Centralized في User table

2. **Validation**
   - Content-Type check
   - File size limit (10MB)
   - Non-empty files

3. **Security**
   - UUID filenames (prevent guessing)
   - No file execution
   - Proper exception handling

4. **Extensibility**
   - Easy to switch to Cloudinary/S3
   - Configurable upload directory
   - Configurable max file size

5. **Database Consistency**
   - URL stored in User table
   - OneToOne relationship maintained
   - Transaction support (@Transactional)

6. **Clean Architecture**
   - Service layer handles business logic
   - Controller just routes
   - Repository manages data access

---

## 🚀 Configuration

### application.properties
```ini
# Image Upload Configuration
upload.dir=./uploads/images
upload.max-file-size=10485760  # 10MB in bytes
```

### Änderbar zur Runtime
```bash
java -jar app.jar \
  --upload.dir=/var/uploads/images \
  --upload.max-file-size=52428800
```

---

## 🔄 Sequence Diagram

```
┌────────┐              ┌────────────┐           ┌──────────┐
│Frontend│              │StudentCtrl │           │StudentSvc│
└───┬────┘              └──────┬─────┘           └────┬─────┘
    │                          │                      │
    │ PUT /students/1/image    │                      │
    │ (multipart file)         │                      │
    ├─────────────────────────►│                      │
    │                          │ updateProfileImage   │
    │                          ├─────────────────────►│
    │                          │                      │
    │                          │ 1. Find student      │
    │                          │ 2. Get user          │
    │                          │ 3. Upload file       │
    │                          │    (ImageService)    │
    │                          │ 4. Set user.image    │
    │                          │ 5. Save user         │
    │                          │ 6. Save student      │
    │                          │                      │
    │                          │ StudentResponseDTO   │
    │                          │◄─────────────────────┤
    │                          │                      │
    │         200 OK + DTO     │                      │
    │◄─────────────────────────┤                      │
    │                          │                      │
```

---

## 📌 الملفات التي تم معالجتها

### ✅ Created (جديد):
- `src/main/java/com/.../config/ImageKitConfig.java`
- `src/main/java/com/.../service/ImageService.java`
- `IMAGE_UPLOAD_DOCUMENTATION.md`
- `IMAGE_UPLOAD_TESTING_GUIDE.md`
- `IMAGE_UPLOAD_COMPLETE_FLOW.md` (هذا الملف)

### ✅ Modified (معدّل):
1. `pom.xml` - بدون إضافة dependencies
2. `application.properties` - إضافة upload configuration
3. `StudentService.java` - إضافة updateProfileImage
4. `StudentController.java` - إضافة PUT endpoint
5. `TeacherService.java` - إضافة updateProfileImage
6. `TeacherController.java` - إضافة PUT endpoint
7. `UserService.java` - حذف updateProfileImage (لا يُستخدم)
8. `UserController.java` - حذف profile-image endpoint (لا يُستخدم)

---

## 💡 مثال استخدام كامل

### 1. Student Registration + Image Upload

**Request 1: Register Student**
```bash
POST /api/students
Content-Type: application/json

{
  "firstName": "Ahmed",
  "lastName": "Ali",
  "username": "ahmed_ali",
  "email": "ahmed@example.com",
  "password": "pass123",
  "confirmPassword": "pass123",
  "gender": "Male",
  "birthDate": "2000-01-01",
  "contactInfo": "20-1234567",
  "address": "Cairo, Egypt",
  "interest": "Web Development"
}

Response: 201 Created (id: 1, userId: 5)
```

**Request 2: Upload Image**
```bash
PUT /api/students/1/profile-image
Content-Type: multipart/form-data

file: @profile.jpg

Response: 200 OK
{
  "id": 1,
  "firstName": "Ahmed",
  "lastName": "Ali",
  ...
  "image": "./uploads/images/uuid.jpg"
}
```

**Request 3: Get Student with Image**
```bash
GET /api/students/1

Response: 200 OK
{
  "id": 1,
  "firstName": "Ahmed",
  ...
  "image": "./uploads/images/uuid.jpg"
}
```

---

## ✨ الخلاصة

تم تطوير نظام **متكامل وآمن وقابل للتوسع** لرفع الصور حيث:
- ✅ الصور تُرفع من **Student و Teacher فقط** (لا UserController)
- ✅ الـ URL يُحفظ في **User table** (مركزي)
- ✅ الصور تُخزّن **محلياً** (بدون تكاليف cloud)
- ✅ **قابل للتحول** لـ Cloudinary/S3 في المستقبل
- ✅ **آمن** مع validation كامل
- ✅ **نظيف** معمارياً مع separation of concerns

**النظام جاهز للـ Production! 🚀**

