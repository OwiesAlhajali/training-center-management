# 🧪 Image Upload - Curl Examples & Testing Guide

## 🚀 الاختبار السريع

### 1️⃣ تحديث صورة المستخدم (User)
```bash
# تحديث صورة للمستخدم بـ ID = 1
curl -X PUT \
  -F "file=@/path/to/image.jpg" \
  http://localhost:8080/api/users/1/profile-image

# أو استخدام صورة من الإنترنت:
curl -X PUT \
  -F "file=@test-image.png" \
  http://localhost:8080/api/users/1/profile-image
```

### 2️⃣ تحديث صورة الطالب (Student)
```bash
# تحديث صورة للطالب بـ ID = 1
curl -X PUT \
  -F "file=@/path/to/student-photo.jpg" \
  http://localhost:8080/api/students/1/profile-image
```

### 3️⃣ تحديث صورة المعلم (Teacher)
```bash
# تحديث صورة للمعلم بـ ID = 1
curl -X PUT \
  -F "file=@/path/to/teacher-photo.jpg" \
  http://localhost:8080/api/teachers/1/profile-image
```

### 4️⃣ الحصول على بيانات المستخدم مع الصورة
```bash
# جلب بيانات المستخدم (تتضمن الصورة)
curl http://localhost:8080/api/users/1

# جلب بيانات الطالب (تتضمن الصورة)
curl http://localhost:8080/api/students/1

# جلب بيانات المعلم (تتضمن الصورة)
curl http://localhost:8080/api/teachers/1
```

---

## 🛠️ Shell Script للاختبار التلقائي

### for Linux/Mac:
```bash
#!/bin/bash

# متغيرات
USER_ID=1
STUDENT_ID=1
TEACHER_ID=1
IMAGE_PATH="./test-image.jpg"  # ضع مسار صورة حقيقية هنا
SERVER="http://localhost:8080"

echo "🧪 بدء اختبار Image Upload..."

# 1. تحديث صورة المستخدم
echo -e "\n✅ اختبار: تحديث صورة المستخدم"
curl -X PUT \
  -F "file=@$IMAGE_PATH" \
  "$SERVER/api/users/$USER_ID/profile-image" | jq .

# 2. تحديث صورة الطالب
echo -e "\n✅ اختبار: تحديث صورة الطالب"
curl -X PUT \
  -F "file=@$IMAGE_PATH" \
  "$SERVER/api/students/$STUDENT_ID/profile-image" | jq .

# 3. تحديث صورة المعلم
echo -e "\n✅ اختبار: تحديث صورة المعلم"
curl -X PUT \
  -F "file=@$IMAGE_PATH" \
  "$SERVER/api/teachers/$TEACHER_ID/profile-image" | jq .

# 4. جلب البيانات المحدثة
echo -e "\n✅ اختبار: جلب بيانات المستخدم المحدثة"
curl "$SERVER/api/users/$USER_ID" | jq .
```

### for Windows (PowerShell):
```powershell
# متغيرات
$USER_ID = 1
$STUDENT_ID = 1
$TEACHER_ID = 1
$IMAGE_PATH = "C:\path\to\test-image.jpg"  # ضع مسار صورة حقيقية هنا
$SERVER = "http://localhost:8080"

Write-Host "🧪 بدء اختبار Image Upload..."

# 1. تحديث صورة المستخدم
Write-Host "`n✅ اختبار: تحديث صورة المستخدم"
$form = @{
    file = Get-Item -Path $IMAGE_PATH
}
Invoke-WebRequest -Uri "$SERVER/api/users/$USER_ID/profile-image" `
    -Method PUT `
    -Form $form

# 2. تحديث صورة الطالب
Write-Host "`n✅ اختبار: تحديث صورة الطالب"
Invoke-WebRequest -Uri "$SERVER/api/students/$STUDENT_ID/profile-image" `
    -Method PUT `
    -Form $form

# 3. تحديث صورة المعلم
Write-Host "`n✅ اختبار: تحديث صورة المعلم"
Invoke-WebRequest -Uri "$SERVER/api/teachers/$TEACHER_ID/profile-image" `
    -Method PUT `
    -Form $form

# 4. جلب البيانات المحدثة
Write-Host "`n✅ اختبار: جلب بيانات المستخدم المحدثة"
Invoke-WebRequest -Uri "$SERVER/api/users/$USER_ID" -Method GET | ConvertTo-Json
```

---

## 📮 Postman Collection

### استيراد في Postman:

```json
{
  "info": {
    "name": "Image Upload",
    "description": "Collection for testing image upload endpoints",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "User - Update Profile Image",
      "request": {
        "method": "PUT",
        "header": {},
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": ""
            }
          ]
        },
        "url": {
          "raw": "http://localhost:8080/api/users/1/profile-image",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "users", "1", "profile-image"]
        }
      }
    },
    {
      "name": "Student - Update Profile Image",
      "request": {
        "method": "PUT",
        "header": {},
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": ""
            }
          ]
        },
        "url": {
          "raw": "http://localhost:8080/api/students/1/profile-image",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "students", "1", "profile-image"]
        }
      }
    },
    {
      "name": "Teacher - Update Profile Image",
      "request": {
        "method": "PUT",
        "header": {},
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": ""
            }
          ]
        },
        "url": {
          "raw": "http://localhost:8080/api/teachers/1/profile-image",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "teachers", "1", "profile-image"]
        }
      }
    },
    {
      "name": "Get User with Image",
      "request": {
        "method": "GET",
        "header": {},
        "url": {
          "raw": "http://localhost:8080/api/users/1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "users", "1"]
        }
      }
    },
    {
      "name": "Get Student with Image",
      "request": {
        "method": "GET",
        "header": {},
        "url": {
          "raw": "http://localhost:8080/api/students/1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "students", "1"]
        }
      }
    },
    {
      "name": "Get Teacher with Image",
      "request": {
        "method": "GET",
        "header": {},
        "url": {
          "raw": "http://localhost:8080/api/teachers/1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "teachers", "1"]
        }
      }
    }
  ]
}
```

---

## ❌ اختبار الأخطاء

### 1️⃣ محاولة رفع ملف ليس صورة:
```bash
# هذا سيرجع BadRequestException
curl -X PUT \
  -F "file=@document.pdf" \
  http://localhost:8080/api/users/1/profile-image

# Response:
# {
#   "error": "Unsupported file type. Please upload an image file."
# }
```

### 2️⃣ محاولة رفع ملف فارغ:
```bash
# هذا سيرجع BadRequestException
curl -X PUT \
  -F "file=@empty-file.jpg" \
  http://localhost:8080/api/users/1/profile-image

# Response:
# {
#   "error": "File is empty"
# }
```

### 3️⃣ محاولة تحديث مستخدم غير موجود:
```bash
# هذا سيرجع ResourceNotFoundException
curl -X PUT \
  -F "file=@image.jpg" \
  http://localhost:8080/api/users/9999/profile-image

# Response:
# {
#   "error": "User not found with id: 9999"
# }
```

---

## 📊 Response Examples

### ✅ نجاح (200 OK):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "userType": "STUDENT",
  "contactInfo": "123-456-7890",
  "image": "./uploads/images/a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6.jpg"
}
```

### ❌ خطأ 400 (Bad Request):
```json
{
  "error": "Unsupported file type. Please upload an image file."
}
```

### ❌ خطأ 404 (Not Found):
```json
{
  "error": "User not found with id: 999"
}
```

---

## 🔧 Troubleshooting

### المشكلة: "Failed to upload image"
**الحل**: تأكد من:
1. المجلد `uploads/images` موجود ولديك صلاحية كتابة
2. حجم الملف أقل من 10MB
3. الملف هو صورة حقيقية

### المشكلة: "File is empty"
**الحل**: تأكد من أن ملف الصورة غير فارغ

### المشكلة: "Unsupported file type"
**الحل**: استخدم ملف صورة بصيغة: JPEG, PNG, GIF, WebP

### المشكلة: "ResourceNotFoundException"
**الحل**: تأكد من أن ID المستخدم/الطالب/المعلم موجود في قاعدة البيانات

---

## 📌 ملاحظات

- ✓ الصور تُحفظ محلياً في `./uploads/images/`
- ✓ يمكن تغيير المسار في `application.properties`
- ✓ الحد الأقصى للملف 10MB (قابل للتعديل)
- ✓ الصور القديمة لا تُحذف تلقائياً (اختياري في المستقبل)

---

**استمتع بالاختبار! 🎉**

