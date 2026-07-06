# Sequence Diagram — Sistem Gamifikasi Pembelajaran PAUD

Diagram berikut disusun berdasarkan implementasi **React Frontend** (`gamifikasiFE`) dan **Spring Boot Backend** (`gamifikasi`).

## File Draw.io

Buka di [draw.io](https://app.diagrams.net/) atau VS Code (extension Draw.io Integration):

| File | Proses |
|------|--------|
| `drawio/sequence/SD_01_Login_Admin.drawio` | Sequence Diagram Login Admin |
| `drawio/sequence/SD_02_Logout_Admin.drawio` | Sequence Diagram Logout Admin |
| `drawio/sequence/SD_03_Melihat_Dashboard.drawio` | Sequence Diagram Melihat Dashboard |
| `drawio/sequence/SD_04_Mengelola_Daftar_Siswa.drawio` | Sequence Diagram Mengelola Daftar Siswa |
| `drawio/sequence/SD_05_Mengelola_Daftar_Tema.drawio` | Sequence Diagram Mengelola Daftar Tema |
| `drawio/sequence/SD_06_Mengelola_Soal_Harian.drawio` | Sequence Diagram Mengelola Daftar Soal Harian |
| `drawio/sequence/SD_07_Memulai_Soal_Harian.drawio` | Sequence Diagram Memulai Soal Harian |
| `drawio/sequence/SD_08_Review_Hasil_Belajar.drawio` | Sequence Diagram Review Hasil Belajar |
| `drawio/sequence/SD_Gamifikasi_PAUD_All.drawio` | **Semua diagram (multi-page)** |

Regenerate: `python docs/generate_sequence_drawio.py`

---

## 3.2.1.1 Proses Bisnis Login Admin

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant Auth as AuthController
    participant UserSvc as UserService
    participant AuthMgr as AuthenticationManager
    participant JWT as JwtUtil
    participant DB as Database

    Admin->>FE: Buka halaman /login
    Admin->>FE: Masukkan username & password
    Admin->>FE: Klik "Ayo Masuk!"
    FE->>Auth: POST /api/auth/login {username, password}
    Auth->>UserSvc: login(username, password)
    UserSvc->>AuthMgr: authenticate(credentials)
    AuthMgr->>DB: Validasi user
    DB-->>AuthMgr: Data user valid
    UserSvc->>DB: findByUsername(username)
    DB-->>UserSvc: User entity
    UserSvc->>JWT: generateToken(user)
    JWT-->>UserSvc: JWT token
    UserSvc-->>Auth: LoginResponse {token, username, role}
    Auth-->>FE: 200 OK + token
    FE->>FE: Simpan token ke localStorage
    FE->>FE: Redirect ke /dashboard
    FE-->>Admin: Tampilkan Dashboard Admin
```

---

## 3.2.1.2 Proses Bisnis Logout Admin

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant AuthCtx as AuthContext

    Admin->>FE: Klik "Keluar" pada Sidebar
    FE->>AuthCtx: logout()
    AuthCtx->>AuthCtx: Hapus token dari localStorage
    AuthCtx->>AuthCtx: Set state token = null
    FE->>FE: Navigate ke /login
    FE-->>Admin: Tampilkan halaman Login

    Note over FE,AuthCtx: Logout bersifat client-side.<br/>Backend menggunakan JWT stateless<br/>tanpa endpoint logout.
```

---

## 3.2.1.3 Proses Bisnis Melihat Dashboard

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant Dash as DashboardController
    participant DashSvc as DashboardService
    participant ScoreSvc as StudentTopicScoreService
    participant DB as Database

    Admin->>FE: Buka halaman /dashboard
  par Ambil statistik
        FE->>Dash: GET /api/dashboard/stats
        Dash->>DashSvc: getStats()
        DashSvc->>DB: Hitung total siswa, soal, tema
        DB-->>DashSvc: Jumlah data
        DashSvc-->>Dash: DashboardStatsDto
        Dash-->>FE: {totalStudents, totalSoal, totalTopics}
    and Peringkat total poin
        FE->>Dash: GET /api/dashboard/students/by-total-points
        Dash->>DashSvc: getStudentsByTotalPoints()
        DashSvc->>DB: Agregasi skor & bintang per siswa
        DB-->>DashSvc: Data peringkat
        DashSvc-->>Dash: List StudentRankDto
        Dash-->>FE: Daftar peringkat siswa
    and Peringkat per tema
        FE->>Dash: GET /api/dashboard/students/by-topic
        Dash->>DashSvc: getStudentsByTopic()
        DashSvc->>ScoreSvc: buildScore() per tema
        ScoreSvc->>DB: Ambil skor per siswa per tema
        DB-->>ScoreSvc: Data skor
        ScoreSvc-->>DashSvc: TopicScoreGroup
        DashSvc-->>Dash: List per tema
        Dash-->>FE: Peringkat siswa per tema
    end
    FE-->>Admin: Tampilkan kartu statistik & tabel peringkat
```

---

## 3.2.1.4 Proses Bisnis Mengelola Daftar Siswa

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant Stu as StudentController
    participant StuSvc as StudentService
    participant DB as Database

    Admin->>FE: Buka halaman /admin/siswa
    FE->>Stu: GET /api/students
    Stu->>StuSvc: getAllStudents()
    StuSvc->>DB: SELECT Student + agregasi poin/bintang
    DB-->>StuSvc: List siswa
    StuSvc-->>Stu: List StudentDto
    Stu-->>FE: Data daftar siswa
    FE-->>Admin: Tampilkan tabel daftar siswa

    alt Tambah Siswa
        Admin->>FE: Isi form & klik Simpan
        FE->>Stu: POST /api/students (FormData: name, group, avatar)
        Stu->>StuSvc: createStudent()
        StuSvc->>DB: INSERT Student
        DB-->>StuSvc: Siswa baru
        StuSvc-->>Stu: StudentDto
        Stu-->>FE: 201 Created
        FE->>Stu: GET /api/students (refresh)
        Stu-->>FE: Data terbaru
    else Edit Siswa
        Admin->>FE: Ubah data & klik Simpan Perubahan
        FE->>Stu: PUT /api/students/{id} (FormData)
        Stu->>StuSvc: updateStudent()
        StuSvc->>DB: UPDATE Student
        DB-->>StuSvc: Data diperbarui
        Stu-->>FE: 200 OK
        FE->>Stu: GET /api/students (refresh)
    else Hapus Siswa
        Admin->>FE: Klik Hapus & konfirmasi
        FE->>Stu: DELETE /api/students/{id}
        Stu->>StuSvc: deleteStudent()
        StuSvc->>DB: DELETE Student + jawaban/skor terkait
        DB-->>StuSvc: Berhasil dihapus
        Stu-->>FE: 204 No Content
        FE->>Stu: GET /api/students (refresh)
    end
    FE-->>Admin: Tampilkan daftar siswa terbaru
```

---

## 3.2.1.5 Proses Bisnis Mengelola Daftar Tema

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant Top as TopicController
    participant TopSvc as TopicService
    participant DB as Database

    Admin->>FE: Buka halaman /admin/tema
    FE->>Top: GET /api/topics
    Top->>TopSvc: getAllTopics()
    TopSvc->>DB: SELECT Tema
    DB-->>TopSvc: List tema
    TopSvc-->>Top: List TopicDto
    Top-->>FE: Data daftar tema
    FE-->>Admin: Tampilkan kartu tema

    alt Tambah Tema
        Admin->>FE: Isi form & klik Simpan
        FE->>Top: POST /api/topics (FormData)
        Top->>TopSvc: createTopic()
        TopSvc->>DB: INSERT Tema
        DB-->>TopSvc: Tema baru
        Top-->>FE: 201 Created
    else Edit Tema
        Admin->>FE: Ubah data & klik Simpan
        FE->>Top: PUT /api/topics/{id} (FormData)
        Top->>TopSvc: updateTopic()
        TopSvc->>DB: UPDATE Tema
        Top-->>FE: 200 OK
    else Aktivasi / Nonaktivasi
        Admin->>FE: Toggle status Aktif
        FE->>Top: PATCH /api/topics/{id}/activate atau /deactivate
        Top->>TopSvc: setActive(id, true/false)
        TopSvc->>DB: UPDATE Tema.isActive
        Top-->>FE: 200 OK
    else Hapus Tema
        Admin->>FE: Klik Hapus & konfirmasi
        FE->>Top: DELETE /api/topics/{id}
        Top->>TopSvc: deleteTopic()
        TopSvc->>DB: DELETE Tema
        Top-->>FE: 204 No Content
    end
    FE->>Top: GET /api/topics (refresh)
    Top-->>FE: Data terbaru
    FE-->>Admin: Tampilkan daftar tema terbaru
```

---

## 3.2.1.6 Proses Bisnis Mengelola Daftar Soal Harian

```mermaid
sequenceDiagram
    actor Admin as Admin (Guru)
    participant FE as Frontend (React)
    participant Top as TopicController
    participant Q as QuestionsController
    participant Opt as QuestionOptionsController
    participant Match as MatchingRelationController
    participant Jig as JigsawPuzzleController
    participant QSvc as QuestionsService
    participant DB as Database

    Admin->>FE: Buka /admin/soal
    FE->>Top: GET /api/topics
    Top-->>FE: Daftar tema
    Admin->>FE: Pilih tema → Kelola Soal
    FE->>Top: GET /api/topics/{topicId}
    FE->>Q: GET /api/questions/topic/{topicId}
    FE->>Q: GET /api/questions/topic/{topicId}/learning-dates
    Q->>QSvc: getQuestionsByTopicId() + getAvailabilityByTopic()
    QSvc->>DB: SELECT Questions per tanggal
    DB-->>QSvc: Data soal & ketersediaan
    Q-->>FE: Kalender & daftar tanggal
    FE-->>Admin: Tampilkan kalender & soal per tanggal

    Admin->>FE: Pilih tanggal belajar
    FE->>Q: GET /api/questions/topic/{topicId}/date/{date}
    FE->>Q: GET /api/questions/topic/{topicId}/date/{date}/availability
    Q-->>FE: Daftar soal hari tersebut
    FE-->>Admin: Tampilkan daftar soal harian

    alt Tambah Soal
        Admin->>FE: Isi form Tambah Soal Baru
        FE->>Q: POST /api/questions (FormData)
        Q->>QSvc: createQuestion()
        QSvc->>DB: INSERT Questions
        Q-->>FE: Soal baru
        Admin->>FE: Kelola Opsi Soal
        FE->>Opt: POST /api/question-options (pilihan ganda/sorting)
        Opt->>DB: INSERT opsi_soal
        opt Tambah pasangan matching
            FE->>Match: POST /api/matching-relations
            Match->>DB: INSERT relasi_matching
        end
        opt Tambah puzzle
            FE->>Jig: POST /api/jigsaw/puzzles + pieces
            Jig->>DB: INSERT jigsaw_puzzle & jigsaw_piece
        end
    else Aktifkan Soal Harian
        Admin->>FE: Toggle ketersediaan tanggal
        FE->>Q: POST /api/questions/topic/{topicId}/set-available
        Q->>QSvc: setAvailabilityByTopicAndDate()
        QSvc->>DB: UPDATE Questions.isAvailable
        Q-->>FE: Status diperbarui
    else Duplikat / Ubah Tanggal
        Admin->>FE: Pilih duplikat atau ubah tanggal
        FE->>Q: GET soal tanggal sumber
        FE->>Q: POST /api/questions/{id}/duplicate atau PUT /api/questions/{id}
        Q->>QSvc: duplicateQuestion() / updateQuestion()
        QSvc->>DB: Salin/perbarui soal & relasi
        Q-->>FE: Berhasil
    else Hapus Soal
        Admin->>FE: Klik hapus soal
        FE->>Q: DELETE /api/questions/{id}
        Q->>QSvc: deleteQuestion()
        QSvc->>DB: DELETE Questions + opsi terkait
        Q-->>FE: 204 No Content
    end
    FE-->>Admin: Tampilkan daftar soal terbaru
```

---

## 3.2.1.7 Proses Bisnis Memulai Soal Harian

```mermaid
sequenceDiagram
    actor Siswa as Siswa
    participant FE as Frontend (React)
    participant Stu as StudentController
    participant Top as TopicController
    participant Q as QuestionsController
    participant Quiz as QuizController
    participant Timer as QuizTimerController
    participant Jig as JigsawPuzzleController
    participant QuizSvc as QuizService
    participant DB as Database

    Siswa->>FE: Klik Mulai pada beranda
    FE->>Stu: GET /api/students
    Stu-->>FE: Daftar siswa
    Siswa->>FE: Pilih profil siswa
    FE->>Stu: GET /api/students/{studentId}
    FE->>Top: GET /api/topics
    FE->>Quiz: GET /api/quiz/scores/students/{sid}/topics/{tid}
    FE-->>Siswa: Tampilkan daftar tema

    Siswa->>FE: Pilih tema & Lanjutkan
    FE->>Top: GET /api/topics/{topicId}
    FE->>Q: GET /api/questions/topic/{topicId}/student/{studentId}
    Q->>DB: Soal per tanggal + status SELESAI
    Q-->>FE: Peta belajar (level per tanggal)
    FE-->>Siswa: Tampilkan Peta Belajar

    Siswa->>FE: Klik Kerjakan pada level/tanggal
    FE->>Top: GET /api/topics/{topicId}
    FE->>Quiz: GET /api/quiz/topics/{topicId}/date/{date}/questions
    FE->>Q: GET /api/questions/topic/{topicId}/date/{date}
    Quiz->>QuizSvc: getQuestionsByTopicAndDate()
    QuizSvc->>DB: SELECT soal tersedia (isAvailable=true)
    Quiz-->>FE: Daftar soal + opsi
    FE-->>Siswa: Tampilkan soal ke-1

    loop Setiap soal
        opt Soal berbatas waktu
            FE->>Timer: GET /api/quiz/timer/{sid}/{tid}/{qid}
            Timer->>DB: SELECT question_timer_session
            Timer-->>FE: remainingSeconds
            FE->>Timer: POST /api/quiz/timer (simpan sisa waktu)
            Timer->>DB: UPSERT question_timer_session
        end
        Siswa->>FE: Jawab soal & klik Selanjutnya
        alt Tipe QUIZ / MATCH / SORTING / DRAG_AND_DROP
            FE->>Quiz: POST /api/quiz/submit/answer
            Quiz->>QuizSvc: submitSingleAnswer() → gradeAnswer()
            QuizSvc->>DB: INSERT/UPDATE student_answer
            QuizSvc-->>Quiz: {correct, earnedScore}
            Quiz-->>FE: Hasil penilaian
        else Tipe PUZZLE
            FE->>Jig: POST /api/jigsaw/progress (opsional)
            FE->>Jig: POST /api/jigsaw/submit
            Jig->>DB: Simpan jawaban puzzle
            Jig-->>FE: {isCorrect, earnedScore}
        end
        FE->>Timer: DELETE /api/quiz/timer/{sid}/{tid}/{qid}
        FE-->>Siswa: Tampilkan umpan balik (Berhasil/Gagal)
    end

    Siswa->>FE: Selesai soal terakhir
    FE->>Timer: DELETE /api/quiz/timer/{sid}/{tid}
    FE->>Quiz: POST /api/quiz/finish {studentId, topicId, learningDate, correctCount}
    Quiz->>QuizSvc: finishQuiz()
    QuizSvc->>DB: UPDATE student_day_score
    QuizSvc-->>Quiz: {starsEarned, totalStars, totalScore}
    Quiz-->>FE: Ringkasan hasil
    FE-->>Siswa: Tampilkan halaman hasil belajar
```

---

## 3.2.1.8 Proses Bisnis Review Hasil Belajar

```mermaid
sequenceDiagram
    actor Siswa as Siswa
    participant FE as Frontend (React)
    participant Top as TopicController
    participant Quiz as QuizController
    participant Jig as JigsawPuzzleController
    participant QuizSvc as QuizService
    participant DB as Database

    Siswa->>FE: Buka review dari Peta Belajar atau setelah selesai quiz
    par Ambil jawaban siswa
        FE->>Quiz: GET /api/quiz/students/{sid}/topics/{tid}/answers?date={date}
        Quiz->>QuizSvc: getStudentAnswersDetailForTopic()
        QuizSvc->>DB: SELECT student_answer + Questions
        DB-->>QuizSvc: Riwayat jawaban
        QuizSvc-->>Quiz: Detail jawaban per soal
        Quiz-->>FE: {questionId, correct, earnedScore, submittedAnswer, options}
    and Ambil kunci jawaban
        FE->>Top: GET /api/topics/{topicId}
        Top-->>FE: Info tema
        FE->>Quiz: GET /api/quiz/topics/{topicId}/date/{date}/questions/answer
        Quiz->>QuizSvc: getQuestionsWithAnswersByTopicAndDate()
        QuizSvc->>DB: SELECT soal + opsi benar + pasangan
        QuizSvc-->>Quiz: Kunci jawaban
        Quiz-->>FE: Data kunci jawaban
    end
    FE->>FE: Gabungkan jawaban siswa & kunci jawaban
    FE-->>Siswa: Tampilkan soal ke-1 beserta status benar/salah

    loop Navigasi antar soal
        Siswa->>FE: Klik Berikutnya / Sebelumnya
        opt Soal tipe PUZZLE
            FE->>Jig: GET /api/jigsaw/students/{sid}/questions/{qid}/review
            Jig->>DB: Ambil penempatan keping siswa
            Jig-->>FE: Data review puzzle
            FE->>Jig: GET /api/jigsaw/questions/{qid}/puzzle
            Jig-->>FE: Konfigurasi puzzle
        end
        FE-->>Siswa: Tampilkan soal + jawaban siswa + poin
    end

    FE->>FE: Hitung ringkasan (benar, salah, total skor, persentase)
    FE-->>Siswa: Tampilkan ringkasan capaian belajar
```
