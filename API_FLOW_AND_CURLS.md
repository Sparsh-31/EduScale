# EduScale Backend API — Flow & cURL Examples

Base URL: **http://localhost:8082** (ensure MongoDB is running and the Spring Boot app is started).

---

## 1. Intended flow (for demo)

1. **Create parent** → parent gets an `id`.
2. **Create child** (with parent’s `id` and optional `gradeId`) → child gets an `id`.
3. **List children** for that parent.
4. **Browse curriculum hierarchy**: Curriculums → Grades → Subjects → Chapters → Learning objectives.
5. **Get learning objectives for a child** (based on child’s assigned grade).
6. **Start a session** for that child and an objective → get `activityIds`.
7. **Get activities** by those IDs.
8. **Record activity progress** and **parent dashboard** / **child progress** as needed.

---

## 2. User APIs

### Create parent
```bash
curl -X POST "http://localhost:8082/api/v1/users/parents" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Parent Demo\",\"age\":35,\"password\":\"demo123\"}"
```
**Response:** `201` with body `{ "id": "...", "parentId": null, "name": "Parent Demo", "age": 35, "grade": null, "role": "PARENT" }`.  
Use `id` as `parentId` when creating children.

### Parent login (demo-only)
```bash
curl -X POST "http://localhost:8082/api/v1/users/parents/login" \
  -H "Content-Type: application/json" \
  -d "{\"parentId\":\"PARENT_ID_HERE\",\"password\":\"demo123\"}"
```

### Create child (with parent, grade, and password)
```bash
curl -X POST "http://localhost:8082/api/v1/users/children" \
  -H "Content-Type: application/json" \
  -d "{\"parentId\":\"PARENT_ID_HERE\",\"name\":\"Aarav\",\"age\":5,\"gradeId\":\"GRADE_LKG\",\"password\":\"demo123\"}"
```
**Response:** `201` with child object. Use `gradeId` so the child sees learning objectives for that grade (e.g. `GRADE_LKG`).

### List children for a parent
```bash
curl -X GET "http://localhost:8082/api/v1/users?parentId=PARENT_ID_HERE" \
  -H "Content-Type: application/json"
```

### Get one user
```bash
curl -X GET "http://localhost:8082/api/v1/users/USER_ID_HERE" \
  -H "Content-Type: application/json"
```

### Update user (e.g. assign/change grade for child)
```bash
curl -X PUT "http://localhost:8082/api/v1/users/CHILD_ID_HERE" \
  -H "Content-Type: application/json" \
  -d "{\"gradeId\":\"GRADE_LKG\"}"
```

---

## 3. Curriculum hierarchy APIs

### List curriculums
```bash
curl -X GET "http://localhost:8082/api/v1/curriculums" \
  -H "Content-Type: application/json"
```
**Seeded:** e.g. `CURR001` (Early Learning Curriculum).

### List grades for a curriculum
```bash
curl -X GET "http://localhost:8082/api/v1/curriculums/CURR001/grades" \
  -H "Content-Type: application/json"
```
**Seeded:** e.g. `GRADE_LKG`, `GRADE_UKG`.

### List subjects for a grade
```bash
curl -X GET "http://localhost:8082/api/v1/grades/GRADE_LKG/subjects" \
  -H "Content-Type: application/json"
```
**Seeded:** e.g. Mathematics, English.

### List chapters for a subject
```bash
curl -X GET "http://localhost:8082/api/v1/subjects/SUB_MATH/chapters" \
  -H "Content-Type: application/json"
```
**Seeded:** e.g. Shapes, Numbers.

### List learning objectives for a chapter
```bash
curl -X GET "http://localhost:8082/api/v1/chapters/CH_SHAPES/objectives" \
  -H "Content-Type: application/json"
```

### Get learning objectives for a child (by child’s grade)
```bash
curl -X GET "http://localhost:8082/api/v1/children/CHILD_ID_HERE/learning-objectives" \
  -H "Content-Type: application/json"
```
Returns all learning objectives for chapters under the child’s assigned grade. If the child has no `gradeId`, returns `[]`.

---

## 4. Session & activity APIs (existing)

### Start session
```bash
curl -X POST "http://localhost:8082/api/v1/sessions/start?userId=CHILD_ID_HERE&objectiveId=OBJ_IDENTIFY_SQUARE" \
  -H "Content-Type: application/json"
```
**Response:** session with `id` and `activityIds`. Use `id` in “Get session” and “Record activity progress”.

### Get session
```bash
curl -X GET "http://localhost:8082/api/v1/sessions/SESSION_ID_HERE" \
  -H "Content-Type: application/json"
```

### Get activities by IDs
```bash
curl -X GET "http://localhost:8082/api/v1/activities?ids=ACT_OBS_SQUARE&ids=ACT_SEL_TAP_SQUARE&ids=ACT_MATCH_SHAPES&ids=ACT_SEL_SQUARE_QUIZ" \
  -H "Content-Type: application/json"
```

### Record activity progress
```bash
curl -X POST "http://localhost:8082/api/v1/progress/activity" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"CHILD_ID\",\"sessionId\":\"SESSION_ID\",\"activityId\":\"ACT_OBS_SQUARE\",\"correct\":true,\"attempts\":1,\"responseTimeSeconds\":8.5,\"hintUsageCount\":0,\"difficultyLevel\":\"EASY\"}"
```

### Recompute objective progress
```bash
curl -X POST "http://localhost:8082/api/v1/progress/objective/CHILD_ID_HERE/OBJ_IDENTIFY_SQUARE/recompute" \
  -H "Content-Type: application/json"
```

---

## 5. Parent dashboard APIs (existing)

### Parent dashboard
```bash
curl -X GET "http://localhost:8082/api/v1/parent/PARENT_ID_HERE/dashboard" \
  -H "Content-Type: application/json"
```

### Child progress
```bash
curl -X GET "http://localhost:8082/api/v1/parent/child/CHILD_ID_HERE/progress" \
  -H "Content-Type: application/json"
```

---

## 6. Full demo sequence (copy-paste order)

Replace `PARENT_ID_HERE` and `CHILD_ID_HERE` with IDs from the create responses.

```bash
# 1) Create parent
curl -s -X POST "http://localhost:8082/api/v1/users/parents" -H "Content-Type: application/json" -d "{\"name\":\"Parent Demo\",\"age\":35,\"password\":\"demo123\"}"

# 2) Create child (use parent id from step 1 as parentId)
curl -s -X POST "http://localhost:8082/api/v1/users/children" -H "Content-Type: application/json" -d "{\"parentId\":\"PARENT_ID_HERE\",\"name\":\"Aarav\",\"age\":5,\"gradeId\":\"GRADE_LKG\",\"password\":\"demo123\"}"

# 3) List children
curl -s -X GET "http://localhost:8082/api/v1/users?parentId=PARENT_ID_HERE"

# 4) Curriculums → grades → subjects → chapters → objectives
curl -s -X GET "http://localhost:8082/api/v1/curriculums"
curl -s -X GET "http://localhost:8082/api/v1/curriculums/CURR001/grades"
curl -s -X GET "http://localhost:8082/api/v1/grades/GRADE_LKG/subjects"
curl -s -X GET "http://localhost:8082/api/v1/subjects/SUB_MATH/chapters"
curl -s -X GET "http://localhost:8082/api/v1/chapters/CH_SHAPES/objectives"

# 5) Learning objectives for this child (by grade)
curl -s -X GET "http://localhost:8082/api/v1/children/CHILD_ID_HERE/learning-objectives"

# 6) Start session for child + objective
curl -s -X POST "http://localhost:8082/api/v1/sessions/start?userId=CHILD_ID_HERE&objectiveId=OBJ_IDENTIFY_SQUARE"

# 7) Get activities (use session id and activityIds from step 6)
curl -s -X GET "http://localhost:8082/api/v1/activities?ids=ACT_OBS_SQUARE&ids=ACT_SEL_TAP_SQUARE&ids=ACT_MATCH_SHAPES&ids=ACT_SEL_SQUARE_QUIZ"

# 8) Parent dashboard & child progress
curl -s -X GET "http://localhost:8082/api/v1/parent/PARENT_ID_HERE/dashboard"
curl -s -X GET "http://localhost:8082/api/v1/parent/child/CHILD_ID_HERE/progress"
```

---

## 7. Seeded IDs (after bootstrap)

| Type        | Id(s) |
|------------|--------|
| Curriculum | `CURR001` |
| Grades     | `GRADE_LKG`, `GRADE_UKG` |
| Subjects   | `SUB_MATH`, `SUB_ENGLISH` |
| Chapters   | `CH_SHAPES`, `CH_NUMBERS`, `CH_ALPHABETS` |
| Objectives | `OBJ_IDENTIFY_SQUARE`, `OBJ_COUNT_ONE_TO_TEN` |

Use these in the hierarchy and session/activity calls above.
