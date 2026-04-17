# تحليل شامل لأخطاء المنطق في مشروع Training Center Management

تاريخ التحليل: 2026-04-14

---

## 📋 جدول الأخطاء المكتشفة

| # | الملف | نوع الخطأ | الخطورة | الوصف |
|---|------|----------|--------|-------|
| 1 | GradeService.java | منطق خاطئ | عالية | عدم التحقق من تسجيل الطالب قبل إضافة الدرجة |
| 2 | EnrollmentService.java | منطق خاطئ | عالية | عدم حفظ قاعدة البيانات بعد تقليل المقاعد |
| 3 | StudentService.java | منطق خاطئ | متوسطة | عدم حفظ تحديث بيانات الطالب في updateStudent |
| 4 | CourseService.java | منطق خاطئ | متوسطة | إمكانية إعادة تعيين Category إلى null |
| 5 | CourseRating.java | تسمية خاطئة | متوسطة | استخدام أسماء أعمدة غير متسقة (Student_id vs course_id) |
| 6 | PaymentService.java | منطق خاطئ | عالية | عدم التحقق من وجود enrollment قبل الدفع |
| 7 | LoginController.java | أمان | عالية | رمي استثناء واحد لسيناريوهات مختلفة (مستخدم غير موجود وكلمة مرور خاطئة) |
| 8 | AttendanceService.java | منطق خاطئ | متوسطة | الفشل في حفظ الجلسة بعد تحديث المقاعد |
| 9 | Grade entity | مفقود | عالية | عدم وجود حقل created_at أو updated_at |
| 10 | StudentRepository.java | منطق خاطئ | متوسطة | استخدام findByUser قد يرجع null بدلاً من Optional |
| 11 | Enrollment entity | منطق خاطئ | متوسطة | عدم وجود حقل يتتبع حالة الالتحاق (status) |
| 12 | GradeController.java | خطأ في الاسم | منخفضة | اسم الدالة creatGrade بدلاً من createGrade |

---

## 🔴 الأخطاء ذات الأولوية العالية

### 1. **GradeService - عدم التحقق من تسجيل الطالب** ⚠️

**الملف:** `GradeService.java` (سطر 30-48)

**المشكلة:**
```java
public GradeResponseDTO createGrade (GradeRequestDTO request) {
    Student student = getStudentOrThrow(request.getStudentId());
    Quiz quiz = getQuizOrThrow(request.getQuizId());
    validateScore(request.getScore(), quiz.getMaxScore());
    
    // ❌ المشكلة: لا يتم التحقق من أن الطالب مسجل في الجلسة التدريبية
    Grade grade = gradeRepository.findByStudentIdAndQuizId(...)
            .orElse(new Grade());
    
    grade.setScore(request.getScore());
    grade.setStudent(student);
    grade.setQuiz(quiz);
    Grade savedGrade = gradeRepository.save(grade);
}
```

**السبب:**
- يجب التحقق من أن الطالب مسجل بالفعل في الجلسة التدريبية التي ينتمي إليها الاختبار
- النظام يسمح بإضافة درجة لطالب لم يلتحق بالدورة

**الحل المقترح:**
```java
public GradeResponseDTO createGrade (GradeRequestDTO request) {
    Student student = getStudentOrThrow(request.getStudentId());
    Quiz quiz = getQuizOrThrow(request.getQuizId());
    validateScore(request.getScore(), quiz.getMaxScore());
    
    // ✅ إضافة التحقق
    if (!enrollmentRepository.existsByStudentAndTrainingSession(
            student, quiz.getTrainingSession())) {
        throw new BadRequestException(
            "Student is not enrolled in this training session");
    }
    
    Grade grade = gradeRepository.findByStudentIdAndQuizId(...)
            .orElse(new Grade());
    grade.setScore(request.getScore());
    grade.setStudent(student);
    grade.setQuiz(quiz);
    return mapToResponse(gradeRepository.save(grade));
}
```

---

### 2. **EnrollmentService - عدم حفظ قاعدة البيانات** ⚠️

**الملف:** `EnrollmentService.java` (سطر 29-65)

**المشكلة:**
```java
@Transactional
public EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO request) {
    // ... التحقق من الطالب والجلسة ...
    
    if (session.getAvailableSeats() <= 0) {
        throw new IllegalStateException("No available seats");
    }
    
    Enrollment enrollment = new Enrollment();
    enrollment.setStudent(student);
    enrollment.setTrainingSession(session);
    
    // ❌ المشكلة: تحديث session لكن عدم حفظه
    session.setAvailableSeats(session.getAvailableSeats() - 1);
    // لا يوجد trainingSessionRepository.save(session)
    
    try {
        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToDTO(saved);
    } catch (DataIntegrityViolationException ex) {
        throw new DuplicateResourceException("Student already enrolled (race condition)");
    }
}
```

**السبب:**
- تقليل عدد المقاعد لا يتم حفظه في قاعدة البيانات
- عند التحديث التالي للجلسة، سيتم فقدان هذا التغيير
- يؤدي لعدم تناسق البيانات

**الحل المقترح:**
```java
// في EnrollmentService، أضف dependency
private final TrainingSessionRepository trainingSessionRepository;

@Transactional
public EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO request) {
    // ...
    session.setAvailableSeats(session.getAvailableSeats() - 1);
    trainingSessionRepository.save(session); // ✅ حفظ التغييرات
    
    try {
        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToDTO(saved);
    } catch (DataIntegrityViolationException ex) {
        // إذا فشل الالتحاق، استرجع المقعد
        session.setAvailableSeats(session.getAvailableSeats() + 1);
        trainingSessionRepository.save(session);
        throw new DuplicateResourceException("Student already enrolled");
    }
}
```

---

### 3. **PaymentService - عدم التحقق من الالتحاق** ⚠️

**الملف:** `PaymentService.java` (سطر 32-83)

**المشكلة:**
```java
@Transactional
public String initiatePayment(Long sessionId) throws StripeException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();
    
    Student student = studentRepository.findByUser(user);
    TrainingSession session = trainingSessionRepository.findById(sessionId)...
    
    // ❌ المشكلة: لا يتحقق من أن الطالب مسجل في الجلسة
    Optional<Payment> existingPayment = paymentRepository
        .findByStudentAndTrainingSessionAndStatus(student, session, PaymentStatus.PENDING);
    
    // يمكن للطالب إجراء دفع للدورة التي لم يسجل فيها
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(session.getPrice().multiply(new java.math.BigDecimal(100)).longValue())
        ...
}
```

**السبب:**
- يجب التحقق من أن الطالب مسجل بالفعل في الجلسة قبل السماح بالدفع
- النظام يسمح بدفع لدورة لم يسجل الطالب فيها

**الحل المقترح:**
```java
@Transactional
public String initiatePayment(Long sessionId) throws StripeException {
    // ...
    Student student = studentRepository.findByUser(user);
    TrainingSession session = trainingSessionRepository.findById(sessionId)...
    
    // ✅ التحقق من الالتحاق
    if (!enrollmentRepository.existsByStudentAndTrainingSession(student, session)) {
        throw new BadRequestException(
            "You must be enrolled in this training session to make payment");
    }
    
    Optional<Payment> existingPayment = paymentRepository
        .findByStudentAndTrainingSessionAndStatus(student, session, PaymentStatus.PENDING);
    // ...
}
```

---

### 4. **LoginController - تسرب معلومات الأمان** 🔐

**الملف:** `LoginController.java` (سطر 27-44)

**المشكلة:**
```java
@PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new ResourceNotFoundException("Invalid credentials");
    }
    // ...
}
```

**السبب:**
- الخطأ في "User not found" يختلف عن "Invalid credentials"
- هذا يسمح للمهاجمين بمعرفة أي أسماء المستخدمين موجودة في النظام
- خطأ أمان معروف (User Enumeration Attack)

**الحل المقترح:**
```java
@PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
    try {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid username or password");
        }
        
        // ✅ رسالة موحدة لا تكشف إن كان اسم المستخدم موجود أم لا
        LoginResponseDTO response = LoginResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .message("Login successful")
                .build();
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        throw new ResourceNotFoundException("Invalid username or password");
    }
}
```

---

## 🟡 الأخطاء ذات الأولوية المتوسطة

### 5. **StudentService - عدم حفظ التحديثات**

**الملف:** `StudentService.java` (سطر 100-113)

**المشكلة:**
```java
public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
    Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    student.setFirstName(request.getFirstName());
    student.setLastName(request.getLastName());
    student.setGender(request.getGender());
    student.setBirthDate(request.getBirthDate());
    student.setAddress(request.getAddress());
    student.setInterest(request.getInterest());
    
    // ❌ المشكلة: لا يوجد studentRepository.save(student)
    return mapToResponse(student);
}
```

**السبب:**
- التحديثات لن تُحفظ في قاعدة البيانات
- سيتم إرجاع البيانات المحدثة في الذاكرة فقط دون حفظها

**الحل:**
```java
public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
    Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    student.setFirstName(request.getFirstName());
    student.setLastName(request.getLastName());
    student.setGender(request.getGender());
    student.setBirthDate(request.getBirthDate());
    student.setAddress(request.getAddress());
    student.setInterest(request.getInterest());
    
    Student updatedStudent = studentRepository.save(student); // ✅ حفظ
    return mapToResponse(updatedStudent);
}
```

---

### 6. **CourseService - إمكانية إعادة تعيين Category إلى null**

**الملف:** `CourseService.java` (سطر 85-106)

**المشكلة:**
```java
@Transactional
public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    
    course.setName(dto.getName());
    course.setDescription(dto.getDescription());
    course.setRequirements(dto.getRequirements());
    course.setHours(dto.getHours());
    
    if (dto.getCategoryId() != null) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        course.setCategory(category); 
    } else {
        course.setCategory(null); // ❌ مشكلة: تعيين null قد ينتهك constraint
    }
    
    return mapToResponse(courseRepository.save(course));
}
```

**السبب:**
- الـ Course لديه `@Column(name = "category_id", nullable = false)`
- تعيين null سيرمي استثناء DataIntegrityViolationException
- يجب عدم السماح بتحديث الـ category إلى null

**الحل:**
```java
@Transactional
public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    
    course.setName(dto.getName());
    course.setDescription(dto.getDescription());
    course.setRequirements(dto.getRequirements());
    course.setHours(dto.getHours());
    
    // ✅ التحقق قبل التحديث فقط
    if (dto.getCategoryId() != null) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        course.setCategory(category); 
    }
    // إذا كان null، لا نحدث الفئة (نحتفظ بالقيمة الحالية)
    
    return mapToResponse(courseRepository.save(course));
}
```

---

### 7. **CourseRating - عدم تناسق أسماء الأعمدة**

**الملف:** `CourseRating.java` (سطر 13-39)

**المشكلة:**
```java
@Table (name="Course_Rating", uniqueConstraints =
    {@UniqueConstraint (columnNames = {"course_id","Student_id"})
    }
)
public class CourseRating {
    // ...
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name ="course_id",nullable = false)
    private Course course ;
    
    // ❌ المشكلة: اسم العمود "Student_id" بحرف كبير
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name ="Student_id",nullable = false)
    private Student student ;
}
```

**السبب:**
- عدم اتساق في أسماء الأعمدة (course_id vs Student_id)
- قد يسبب مشاكل في الاستعلامات والـ conventions

**الحل:**
```java
@Table (name="course_ratings", uniqueConstraints =
    {@UniqueConstraint (columnNames = {"course_id","student_id"})
    }
)
public class CourseRating {
    // ...
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course ;
    
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "student_id", nullable = false) // ✅ اسم موحد
    private Student student ;
}
```

---

### 8. **StudentRepository - المؤشر البروتوكولي البطيء**

**الملف:** `StudentRepository.java` (سطر 9)

**المشكلة:**
```java
Student findByUser(User user); // ❌ قد يرجع null
```

**السبب:**
- الدالة قد ترجع null بدلاً من Optional
- في PaymentService سطر 39: `Student student = studentRepository.findByUser(user);`
  ثم مباشرة سطر 40: `if (student == null)` - تحقق من null
- هذا ليس آمن النوع (type-safe)

**الحل:**
```java
// في StudentRepository
Optional<Student> findByUser(User user);

// في PaymentService
Student student = studentRepository.findByUser(user)
    .orElseThrow(() -> new ResourceNotFoundException("Student not found for user"));
```

---

### 9. **Enrollment - حقل Status مفقود**

**الملف:** `Enrollment.java`

**المشكلة:**
```java
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "training_session_id"})
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TrainingSession trainingSession;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ❌ مفقود:
    // - status (ACTIVE, COMPLETED, WITHDRAWN, etc.)
    // - updated_at
}
```

**السبب:**
- لا طريقة لتتبع حالة الالتحاق (هل الطالب نشط، انسحب، أكمل الدورة؟)
- لا نستطيع معرفة متى تم آخر تحديث للالتحاق

**الحل:**
```java
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "training_session_id"})
})
public class Enrollment {
    // ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

// و إضافة enum جديد
public enum EnrollmentStatus {
    ACTIVE, COMPLETED, WITHDRAWN, SUSPENDED
}
```

---

### 10. **Grade - حقول التتبع مفقودة**

**الملف:** `Grade.java`

**المشكلة:**
```java
@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "quiz_id"})
})
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "score", nullable = false)
    private BigDecimal score;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Quiz quiz;
    
    // ❌ مفقود: created_at, updated_at
}
```

**السبب:**
- لا يمكن تتبع متى تم إضافة الدرجة أو تحديثها
- مهم للتدقيق والتحليل الإحصائي

**الحل:**
```java
@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "quiz_id"})
})
public class Grade {
    // ...
    @Column(name = "score", nullable = false)
    private BigDecimal score;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Quiz quiz;
}
```

---

## 🟢 الأخطاء ذات الأولوية المنخفضة

### 11. **GradeController - خطأ في اسم الدالة**

**الملف:** `GradeController.java` (سطر 23)

**المشكلة:**
```java
@PostMapping("/grades")
public ResponseEntity<GradeResponseDTO> creatGrade( // ❌ creatGrade
    @Valid @RequestBody GradeRequestDTO request
) {
    GradeResponseDTO response = gradeService.createGrade(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
}
```

**الحل:**
```java
@PostMapping("/grades")
public ResponseEntity<GradeResponseDTO> createGrade( // ✅ createGrade
    @Valid @RequestBody GradeRequestDTO request
) {
    GradeResponseDTO response = gradeService.createGrade(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
}
```

---

## 📊 ملخص الأخطاء

### حسب النوع:
- **أخطاء منطقية**: 8 أخطاء
- **أخطاء أمان**: 1 خطأ
- **أخطاء تسمية**: 1 خطأ
- **أخطاء تصميم**: 2 خطأ

### حسب الخطورة:
- **عالية**: 4 أخطاء
- **متوسطة**: 6 أخطاء
- **منخفضة**: 2 أخطأ

---

## ✅ التوصيات العامة

1. **إضافة اختبارات وحدة (Unit Tests)**
   - اختبر كل عملية Enrollment و Payment
   - تحقق من تحديثات قاعدة البيانات

2. **استخدام Validation annotations**
   - أضف `@NotNull` على الحقول الإلزامية
   - تحقق من الأرقام السالبة

3. **تحسين معالجة الأخطاء**
   - استخدم استثناءات مخصصة
   - اختبر جميع السيناريوهات

4. **الأمان**
   - أضف JWT للمصادقة (الحالية ليست آمنة)
   - تجنب تسرب معلومات المستخدمين

5. **التسجيل (Logging)**
   - أضف logs للعمليات الحساسة
   - تتبع التغييرات في الالتحاقات والدفعات

---

**نهاية التقرير**

