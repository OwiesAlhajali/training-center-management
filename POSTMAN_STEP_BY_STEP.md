# 🎬 شرح خطوة بخطوة مع Postman

## سيناريو كامل: إنشاء ومعالجة دورة تدريبية

---

## 📍 المرحلة 1: إنشاء مستخدم (بدون توثيق)

### الخطوة 1.1:
افتح Postman وانقر على **New** → **HTTP Request**

### الخطوة 1.2:
اختر **POST** من القائمة العلوية اليسار

### الخطوة 1.3:
أدخل الـ URL:
```
http://localhost:8080/api/users
```

### الخطوة 1.4:
اذهب إلى التبويب **Headers** وتأكد من:
```
Content-Type: application/json
```

### الخطوة 1.5:
اذهب إلى التبويب **Body**
- اختر **raw**
- اختر **JSON** من القائمة على اليمين

### الخطوة 1.6:
انسخ هذا في Body:
```json
{
  "username": "ahmed",
  "email": "ahmed@example.com",
  "password": "password123",
  "userType": "STUDENT",
  "contactInfo": "555-1234"
}
```

### الخطوة 1.7:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 201 Created

Response:
{
  "id": 1,
  "username": "ahmed",
  "email": "ahmed@example.com",
  "userType": "STUDENT",
  "contactInfo": "555-1234"
}
```

---

## 📍 المرحلة 2: عرض جميع الدورات (بدون توثيق)

### الخطوة 2.1:
اضغط **+** لـ New Request

### الخطوة 2.2:
اختر **GET**

### الخطوة 2.3:
أدخل الـ URL:
```
http://localhost:8080/api/training-sessions
```

### الخطوة 2.4:
اذهب إلى **Authorization**
- اختر **No Auth** (أو اتركه كما هو)

### الخطوة 2.5:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 200 OK

Response:
[]
(قائمة فارغة الآن - لأننا لم نُنشئ دورات بعد)
```

---

## 📍 المرحلة 3: إنشاء دورة تدريبية (محمية - تحتاج توثيق)

### الخطوة 3.1:
اضغط **+** لـ New Request

### الخطوة 3.2:
اختر **POST**

### الخطوة 3.3:
أدخل الـ URL:
```
http://localhost:8080/api/training-sessions
```

### ⚠️ الخطوة 3.4: **الأهم** - إضافة التوثيق
1. اذهب إلى التبويب **Authorization**
2. من القائمة **Type**، اختر **Basic Auth**
3. في **Username**: اكتب `ahmed`
4. في **Password**: اكتب `password123`

```
┌─────────────────────────────────────┐
│ Authorization                       │
├─────────────────────────────────────┤
│ Type: ▼ Basic Auth                  │
│                                     │
│ Username: [ahmed        ]          │
│ Password: [password123 ]           │
│                                     │
│ ✓ Show password                    │
└─────────────────────────────────────┘
```

### الخطوة 3.5:
اذهب إلى **Headers** وتأكد:
```
Content-Type: application/json
```

### الخطوة 3.6:
اذهب إلى **Body**
- اختر **raw**
- اختر **JSON**

### الخطوة 3.7:
انسخ هذا في Body:
```json
{
  "price": 299.99,
  "availableSeats": 25,
  "minSeats": 10,
  "numberOfLectures": 12,
  "requiredEquipment": "Laptop, Projector",
  "duration": "3 months",
  "status": "ACTIVE",
  "courseId": 1,
  "classroomId": 1,
  "teacherId": 1
}
```

### الخطوة 3.8:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 201 Created

Response:
{
  "id": 1,
  "price": 299.99,
  "availableSeats": 25,
  "minSeats": 10,
  "numberOfLectures": 12,
  "requiredEquipment": "Laptop, Projector",
  "duration": "3 months",
  "status": "ACTIVE",
  "courseName": "Java Programming",
  "instituteName": "Tech Academy",
  ...
}
```

---

## 📍 المرحلة 4: تحديث الدورة (محمية)

### الخطوة 4.1:
اضغط **+** لـ New Request

### الخطوة 4.2:
اختر **PUT**

### الخطوة 4.3:
أدخل الـ URL (لاحظ الـ /1 في النهاية):
```
http://localhost:8080/api/training-sessions/1
```

### الخطوة 4.4:
اذهب إلى **Authorization**
- **Type:** Basic Auth
- **Username:** ahmed
- **Password:** password123

### الخطوة 4.5:
اذهب إلى **Body**
- اختر **raw** و **JSON**

### الخطوة 4.6:
انسخ هذا (لاحظ السعر تغيّر):
```json
{
  "price": 399.99,
  "availableSeats": 20,
  "minSeats": 10,
  "numberOfLectures": 12,
  "requiredEquipment": "Laptop, Projector, Monitor",
  "duration": "4 months",
  "status": "ACTIVE",
  "courseId": 1,
  "classroomId": 1,
  "teacherId": 1
}
```

### الخطوة 4.7:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 200 OK

Response:
{
  "id": 1,
  "price": 399.99,  ← تغيّر!
  "availableSeats": 20,
  ...
}
```

---

## 📍 المرحلة 5: حذف الدورة (محمية)

### الخطوة 5.1:
اضغط **+** لـ New Request

### الخطوة 5.2:
اختر **DELETE**

### الخطوة 5.3:
أدخل الـ URL:
```
http://localhost:8080/api/training-sessions/1
```

### الخطوة 5.4:
اذهب إلى **Authorization**
- **Type:** Basic Auth
- **Username:** ahmed
- **Password:** password123

### الخطوة 5.5:
**لا تحتاج Body** للـ DELETE

### الخطوة 5.6:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 204 No Content

Response: (فارغ - وهذا صحيح!)
```

---

## 📍 المرحلة 6: التحقق من الحذف

### الخطوة 6.1:
عد إلى **GET** Request من المرحلة 2

### الخطوة 6.2:
اضغط **Send**

### النتيجة المتوقعة:
```
Status: 200 OK

Response:
[]
(قائمة فارغة مرة أخرى - الدورة حُذفت!)
```

---

## 🔴 ماذا لو حصل خطأ؟

### ❌ خطأ 401 Unauthorized

**الشاشة:**
```
Status: 401 Unauthorized

Response:
{
  "error": "Unauthorized",
  "message": "..."
}
```

**الحل:**
1. تأكد من التبويب **Authorization**
2. تأكد أن **Type** = **Basic Auth**
3. تأكد من **Username** و **Password**
4. اضغط **Send** مرة أخرى

---

### ❌ خطأ 400 Bad Request

**الشاشة:**
```
Status: 400 Bad Request

Response:
{
  "error": "Invalid request body",
  "message": "..."
}
```

**الحل:**
1. تأكد من **Body** بصيغة JSON صحيحة
2. تأكد من جميع الحقول المطلوبة
3. تأكد من أنواع البيانات صحيحة (أرقام، نصوص، إلخ)

---

### ❌ خطأ 404 Not Found

**الشاشة:**
```
Status: 404 Not Found

Response:
{
  "error": "Not found",
  "message": "..."
}
```

**الحل:**
1. تأكد من الـ URL صحيح
2. تأكد من الـ ID موجود
3. تأكد من السيرفر مشغل

---

### ❌ خطأ Connection refused

**الشاشة:**
```
Error: connect ECONNREFUSED 127.0.0.1:8080
```

**الحل:**
1. **السيرفر غير مشغل**
2. شغّل التطبيق من IDE
3. انتظر قليلاً حتى يبدأ التطبيق
4. جرّب مرة أخرى

---

## 💡 نصائح مهمة

### 1️⃣ استخدم المتغيرات (Environment Variables)
```
1. اضغط على Gear icon (في الزاوية العلوية)
2. اختر "Manage Environments"
3. أنشئ environment جديد
4. أضف متغير: BASE_URL = http://localhost:8080
5. استخدم {{BASE_URL}} في الـ URLs
```

### 2️⃣ احفظ الـ Requests
```
1. اضغط Save بعد كل request
2. اختر Collection أو أنشئ واحدة جديدة
3. أدخل اسم واضح للـ request
```

### 3️⃣ استخدم Pre-request Script
```
1. اذهب إلى Pre-request Script
2. أضف كود JavaScript إذا لزم الأمر
```

### 4️⃣ استخدم Tests
```
1. اذهب إلى Tests
2. أضف اختبارات للـ Response
3. مثال:
   pm.test("Status code is 201", function () {
       pm.response.to.have.status(201);
   });
```

---

## 📋 ملخص سريع

| المرحلة | الطريقة | URL | توثيق | Body |
|--------|--------|-----|-------|------|
| 1. إنشاء مستخدم | POST | /api/users | ❌ | ✅ JSON |
| 2. عرض دورات | GET | /api/training-sessions | ❌ | ❌ |
| 3. إنشاء دورة | POST | /api/training-sessions | ✅ | ✅ JSON |
| 4. تحديث دورة | PUT | /api/training-sessions/1 | ✅ | ✅ JSON |
| 5. حذف دورة | DELETE | /api/training-sessions/1 | ✅ | ❌ |

---

## 🎯 تمرين عملي

جرّب هذا التسلسل:

1. ✅ أنشئ 3 مستخدمين مختلفين
2. ✅ أنشئ 5 دورات تدريبية
3. ✅ حدّث سعر دورة واحدة
4. ✅ احذف دورة واحدة
5. ✅ تحقق من أن الحذف نجح

---

**مبروك! أنت الآن خبير في استخدام Postman! 🎉**


