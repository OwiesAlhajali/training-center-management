# 📱 شرح استخدام Postman مع التطبيق

## 🚀 الخطوة 1: تحميل Postman

1. اذهب إلى: https://www.postman.com/downloads/
2. حمّل النسخة المناسبة لجهازك (Windows, Mac, Linux)
3. فتح Postman بعد التثبيت

---

## 📝 الخطوة 2: إنشاء مستخدم جديد (التسجيل)

### الخطوات:
1. **اضغط على زر `+` أو `New`** في Postman
2. اختر **HTTP Request**

### الإعدادات:

| الحقل | القيمة |
|------|--------|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/users` |

### التبويب Headers:
```
Content-Type: application/json
```

### التبويب Body:
1. اختر **raw**
2. اختر **JSON** من القائمة على اليمين
3. انسخ البيانات التالية:

```json
{
  "username": "ahmed",
  "email": "ahmed@example.com",
  "password": "password123",
  "userType": "STUDENT",
  "contactInfo": "555-1234"
}
```

### اضغط **Send**

### النتيجة (Status 201):
```json
{
  "id": 1,
  "username": "ahmed",
  "email": "ahmed@example.com",
  "userType": "STUDENT",
  "contactInfo": "555-1234"
}
```

---

## 🔓 الخطوة 3: عرض البيانات (بدون توثيق)

### المتطلبات:
- **Method:** GET
- **URL:** `http://localhost:8080/api/training-sessions`
- **Authorization:** لا تحتاج
- **Body:** لا تحتاج

### اضغط **Send**

### النتيجة:
```json
[
  {
    "id": 1,
    "price": 199.99,
    "availableSeats": 30,
    "courseName": "Java Programming",
    "instituteName": "Tech Academy",
    ...
  }
]
```

---

## 🔐 الخطوة 4: إنشاء دورة تدريبية (محمية - تحتاج توثيق)

### الإعدادات الأساسية:

| الحقل | القيمة |
|------|--------|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/training-sessions` |

### ✅ التبويب **Authorization**:

1. اضغط على تبويب **Authorization**
2. من القائمة **Type** اختر **Basic Auth**
3. أدخل:
   - **Username:** `ahmed`
   - **Password:** `password123`

### التبويب Headers:
```
Content-Type: application/json
```

### التبويب Body:
اختر **raw** و **JSON** وأدخل:

```json
{
  "price": 299.99,
  "availableSeats": 25,
  "minSeats": 10,
  "numberOfLectures": 12,
  "requiredEquipment": "Laptop",
  "duration": "3 months",
  "status": "ACTIVE",
  "courseId": 1,
  "classroomId": 1,
  "teacherId": 1
}
```

### اضغط **Send**

### النتيجة (Status 201):
```json
{
  "id": 2,
  "price": 299.99,
  "availableSeats": 25,
  ...
}
```

---

## ✏️ الخطوة 5: تحديث دورة تدريبية

### الإعدادات:

| الحقل | القيمة |
|------|--------|
| **Method** | PUT |
| **URL** | `http://localhost:8080/api/training-sessions/1` |

### ✅ التبويب **Authorization**:
- **Type:** Basic Auth
- **Username:** `ahmed`
- **Password:** `password123`

### التبويب Body:

```json
{
  "price": 349.99,
  "availableSeats": 20,
  "minSeats": 10,
  "numberOfLectures": 12,
  "requiredEquipment": "Laptop + Monitor",
  "duration": "3 months",
  "status": "ACTIVE",
  "courseId": 1,
  "classroomId": 1,
  "teacherId": 1
}
```

### اضغط **Send**

---

## 🗑️ الخطوة 6: حذف دورة تدريبية

### الإعدادات:

| الحقل | القيمة |
|------|--------|
| **Method** | DELETE |
| **URL** | `http://localhost:8080/api/training-sessions/1` |

### ✅ التبويب **Authorization**:
- **Type:** Basic Auth
- **Username:** `ahmed`
- **Password:** `password123`

### اضغط **Send**

### النتيجة (Status 204):
لا توجد نتيجة - هذا يعني تم الحذف بنجاح

---

## 🔑 الخطوة 7: تسجيل الدخول (اختياري)

### الإعدادات:

| الحقل | القيمة |
|------|--------|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/auth/login` |

### التبويب Headers:
```
Content-Type: application/json
```

### التبويب Body:

```json
{
  "username": "ahmed",
  "password": "password123"
}
```

### اضغط **Send**

### النتيجة:
```json
{
  "id": 1,
  "username": "ahmed",
  "email": "ahmed@example.com",
  "userType": "STUDENT",
  "message": "Login successful"
}
```

---

## 📚 جدول الـ Endpoints

| العملية | الطريقة | الـ URL | توثيق مطلوب | 
|--------|--------|--------|-----------|
| عرض جميع الدورات | GET | `/api/training-sessions` | ❌ لا |
| عرض دورة واحدة | GET | `/api/training-sessions/1` | ❌ لا |
| إنشاء دورة | POST | `/api/training-sessions` | ✅ نعم |
| تحديث دورة | PUT | `/api/training-sessions/1` | ✅ نعم |
| حذف دورة | DELETE | `/api/training-sessions/1` | ✅ نعم |
| تسجيل دخول | POST | `/api/auth/login` | ❌ لا |
| إنشاء مستخدم | POST | `/api/users` | ❌ لا |
| عرض الفئات | GET | `/api/categories` | ❌ لا |

---

## ⚠️ الأخطاء الشائعة وحلولها

### ❌ خطأ: 401 Unauthorized

**السبب:** لم تدخل بيانات المستخدم

**الحل:**
1. اذهب إلى **Authorization**
2. اختر **Basic Auth**
3. أدخل Username و Password

---

### ❌ خطأ: User not found

**السبب:** كلمة المرور خاطئة أو المستخدم غير موجود

**الحل:**
1. تأكد من اسم المستخدم (username)
2. تأكد من كلمة المرور (password)
3. أنشئ مستخدم جديد إذا لم يكن موجود

---

### ❌ خطأ: 403 Forbidden

**السبب:** ليس لديك صلاحية

**الحل:**
- استخدم مستخدم له الصلاحيات المطلوبة

---

### ❌ خطأ: Connection refused

**السبب:** السيرفر غير مشغل

**الحل:**
1. شغّل التطبيق من IDE
2. تأكد أن السيرفر يعمل على `localhost:8080`

---

## 💾 حفظ الـ Requests

### لحفظ كل requests في مكان واحد:

1. اضغط على **New** → **Collection**
2. اكتب الاسم: `Training Center API`
3. لكل request:
   - اضغط **Save**
   - اختر Collection
   - اكتب اسم Request مثل: `Create Training Session`

---

## 🎯 مثال كامل: إنشاء دورة من الصفر

### الخطوة 1: إنشاء مستخدم
```
POST http://localhost:8080/api/users
Body:
{
  "username": "teacher1",
  "email": "teacher@example.com",
  "password": "pass123",
  "userType": "TEACHER",
  "contactInfo": "555-9999"
}
```

### الخطوة 2: إنشاء دورة باستخدام اسم المستخدم وكلمة المرور
```
POST http://localhost:8080/api/training-sessions
Authorization: Basic Auth
Username: teacher1
Password: pass123

Body:
{
  "price": 399.99,
  "availableSeats": 35,
  "minSeats": 15,
  "numberOfLectures": 20,
  "requiredEquipment": "Projector, Whiteboard",
  "duration": "4 months",
  "status": "ACTIVE",
  "courseId": 1,
  "classroomId": 1,
  "teacherId": 1
}
```

### الخطوة 3: عرض الدورة (بدون توثيق)
```
GET http://localhost:8080/api/training-sessions/2
```

---

## 📱 نصائح مهمة

✅ **استخدم Collections** لتنظيم requests
✅ **اكتب comments** لوصف كل request
✅ **استخدم Environment Variables** للـ URL (إذا كنت تستخدم URLs مختلفة)
✅ **اختبر جميع الحالات** (نجاح، فشل، أخطاء)

---

## 🔗 الروابط المهمة

- Postman Documentation: https://learning.postman.com/
- API Testing: https://learning.postman.com/docs/sending-requests/requests/

---

**الآن أنت جاهز لاستخدام Postman مع التطبيق! 🎉**

