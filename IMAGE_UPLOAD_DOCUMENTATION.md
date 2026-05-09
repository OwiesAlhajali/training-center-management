# 📸 Image Upload Flow Documentation

## ✅ ما تم تنفيذه بنجاح

تم إضافة Flow كامل لرفع الصور للمستخدمين (User/Student/Teacher) بطريقة بسيطة وآمنة.

---

## 🏗️ البنية المعمارية

### 1️⃣ **Config Layer**
- **`ImageKitConfig.java`**: يدير إعدادات التخزين المحلي
  - يقرأ مسار التخزين من `application.properties`
  - يتأكد من وجود المجلد
  - **ملاحظة**: يمكن تحديثها لاحقاً لـ Cloudinary أو AWS S3

### 2️⃣ **Service Layer**
- **`ImageService.java`**: خدمة الرفع المركزية
  - التحقق من نوع الملف (يجب أن يبدأ بـ `image/*`)
  - التحقق من حجم الملف (افتراضي 10MB)
  - حفظ الملف بـ UUID فريد
  - إرجاع مسار نسبي للصورة

- **`UserService.updateProfileImage()`**: تحديث صورة المستخدم
- **`StudentService.updateProfileImage()`**: تحديث صورة الطالب (عبر User)
- **`TeacherService.updateProfileImage()`**: تحديث صورة المعلم (عبر User)

### 3️⃣ **Controller Layer**
- **`UserController`**: 
  - `PUT /api/users/{id}/profile-image`
  
- **`StudentController`**: 
  - `PUT /api/students/{id}/profile-image`
  
- **`TeacherController`**: 
  - `PUT /api/teachers/{id}/profile-image`

---

## 📝 الـ Flow الكامل

```
Frontend (React/Vue/etc)
    ↓
    [Form: MultipartFile + id]
    ↓
Controller (PUT /api/users/{id}/profile-image)
    ↓
    [استخراج الملف والـ ID]
    ↓
Service (updateProfileImage)
    ↓
    ├─→ ImageService.uploadImage()
    │   ├─→ التحقق من الملف (نوع + حجم)
    │   ├─→ حفظ الملف محلياً
    │   └─→ إرجاع مسار الملف
    │
    ├─→ تحديث User.image
    ├─→ حفظ في Database
    └─→ إرجاع DTO مع الصورة
    ↓
Frontend
    ↓
    [عرض الصورة من المسار]
```

---

## 🔌 الـ Endpoints

### ✅ 1. تحديث صورة المستخدم
```bash
PUT /api/users/{id}/profile-image
Content-Type: multipart/form-data

Body:
  file: <image file>

Response:
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "userType": "STUDENT",
  "contactInfo": "123-456-7890",
  "image": "./uploads/images/uuid.jpg"  ← الصورة الجديدة
}
```

### ✅ 2. تحديث صورة الطالب
```bash
PUT /api/students/{id}/profile-image
Content-Type: multipart/form-data

Body:
  file: <image file>

Response:
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "gender": "Male",
  "birthDate": "2000-01-01",
  "address": "...",
  "interest": "...",
  "enrollmentDate": "2026-01-15",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "contactInfo": "123-456-7890",
  "image": "./uploads/images/uuid.jpg"  ← الصورة الجديدة
}
```

### ✅ 3. تحديث صورة المعلم
```bash
PUT /api/teachers/{id}/profile-image
Content-Type: multipart/form-data

Body:
  file: <image file>

Response:
{
  "id": 1,
  "firstName": "Ahmed",
  "lastName": "Smith",
  "specialization": "Mathematics",
  "certificates": "...",
  "address": "...",
  "cv": "...",
  "experienceYears": 10,
  "userId": 2,
  "username": "ahmed_smith",
  "email": "ahmed@example.com",
  "contactInfo": "987-654-3210",
  "image": "./uploads/images/uuid.jpg"  ← الصورة الجديدة
}
```

---

## 🛡️ Validation

### ✅ ما يتم التحقق منه:
1. ✓ الملف غير فارغ
2. ✓ نوع الملف يبدأ بـ `image/*` (jpeg, png, gif, webp, etc.)
3. ✓ حجم الملف لا يتجاوز 10MB (قابل للتغيير)
4. ✓ المستخدم موجود في قاعدة البيانات

### ❌ الاستثناءات:
- `BadRequestException`: ملف فارغ أو نوع خاطئ
- `ResourceNotFoundException`: المستخدم/الطالب/المعلم غير موجود
- `RuntimeException`: فشل الحفظ

---

## 🗂️ هيكل التخزين المحلي

```
project-root/
├── uploads/
│   └── images/
│       ├── uuid1.jpg
│       ├── uuid2.png
│       └── uuid3.gif
└── ...
```

---

## ⚙️ الإعدادات (application.properties)

```ini
# مسار التخزين (يمكن تحديده أثناء التشغيل)
upload.dir=./uploads/images

# الحد الأقصى لحجم الملف بـ bytes (10MB افتراضي)
upload.max-file-size=10485760
```

---

## 🚀 كيفية التوسع في المستقبل

### الانتقال إلى Cloudinary:
```java
@Service
@RequiredArgsConstructor
public class ImageService {
    private final Cloudinary cloudinary;
    
    public String uploadImage(MultipartFile file) {
        // رفع إلى Cloudinary بدلاً من محليّ
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ...);
        return (String) uploadResult.get("secure_url");
    }
}
```

### الانتقال إلى AWS S3:
```java
@Service
@RequiredArgsConstructor
public class ImageService {
    private final AmazonS3 s3Client;
    
    public String uploadImage(MultipartFile file) {
        // رفع إلى S3 بدلاً من محليّ
        s3Client.putObject(bucketName, key, file.getInputStream(), ...);
        return s3Url;
    }
}
```

---

## 📋 الملفات التي تم تعديلها/إنشاؤها

### ✅ الملفات الجديدة:
1. `src/main/java/com/trainingcenter/management/config/ImageKitConfig.java`
2. `src/main/java/com/trainingcenter/management/service/ImageService.java`

### ✅ الملفات المعدّلة:
1. `pom.xml` (لا توجد dependencies مضافة حالياً)
2. `application.properties` (إضافة upload configuration)
3. `UserService.java` (إضافة `updateProfileImage`)
4. `StudentService.java` (إضافة `updateProfileImage`)
5. `TeacherService.java` (إضافة `updateProfileImage`)
6. `UserController.java` (تفعيل الـ endpoint و إضافة `PUT /{id}/profile-image`)
7. `StudentController.java` (إضافة `PUT /{id}/profile-image`)
8. `TeacherController.java` (إضافة `PUT /{id}/profile-image`)

---

## ✨ الميزات الحالية

- ✅ رفع صور آمن مع التحقق
- ✅ تخزين محلي بدون تكاليف إضافية
- ✅ تطبيق موحد على Student و Teacher و User
- ✅ القابلية للتوسع (يمكن الانتقال لـ cloud storage بسهولة)
- ✅ دعم Multiple image formats
- ✅ 10MB maximum file size

---

## 🧪 الاختبار (Postman/Curl)

### استخدام Postman:
1. اختر `PUT` method
2. أدخل URL: `http://localhost:8080/api/users/1/profile-image`
3. في `Body` اختر `form-data`
4. أضف key `file` مع notype `File`
5. اختر صورة من جهازك
6. أرسل الطلب

---

## 🔒 الأمان

- ✓ التحقق من نوع الملف (يجب أن يكون صورة)
- ✓ التحقق من حجم الملف
- ✓ استخدام UUID لتجنب collisions
- ✓ حفظ آمن في النظام الملفات
- ✓ لا يتم الوثوق بـ filenames المرسلة من الـ frontend

---

## 📌 الخطوات التالية (اختيارية)

1. صورة للـ TrainingSession (اختياري)
2. حذف الصور القديمة عند التحديث
3. Middleware للتحقق من File Size
4. Caching للصور
5. التحول إلى Cloudinary/AWS S3 للـ Production

---

**تم الانتهاء بنجاح! ✨**

