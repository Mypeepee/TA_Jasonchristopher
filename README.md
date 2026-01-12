# 📚 Sistem Informasi Perpustakaan (Library Management System)

Aplikasi Desktop berbasis JavaFX untuk pengelolaan sirkulasi perpustakaan (peminjaman & pengembalian buku) dengan fitur multi-user (Admin & Anggota). Proyek ini dikembangkan sebagai Tugas Akhir Sistem Informasi Bisnis.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-2C3E50?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

---

## 🌟 Fitur Utama

Sistem ini memiliki dua role pengguna dengan hak akses berbeda:

### 👨‍💻 Admin (Petugas)
* **Authentication:** Login aman dengan validasi database.
* **Dashboard:** Statistik real-time (Total Buku, Total Peminjaman Aktif, Jumlah Anggota).
* **Manajemen Buku:** Melihat katalog buku dan status stok.
* **Monitoring:** Memantau status peminjaman dan data anggota.

### 👤 Member (Anggota)
* **Katalog Visual:** Browsing buku dengan tampilan Grid & detail gambar.
* **Peminjaman Mandiri:** Meminjam buku (otomatis mengurangi stok).
* **History & Pengembalian:** Melihat riwayat pinjam & melakukan pengembalian (otomatis menambah stok).
* **Validasi:** Tidak bisa meminjam jika stok habis.

---

## 🛠️ Tech Stack

* **Bahasa Pemrograman:** Java (JDK 17+)
* **GUI Framework:** JavaFX
* **Database:** MySQL
* **Testing:** JUnit 5 (Unit Testing)
* **Build Tool:** Maven
* **Design Pattern:** MVC (Model-View-Controller) / Separation of Concerns

---

## 🗄️ Database Schema (ERD)

Sistem menggunakan database `db_perpustakaan` dengan 4 tabel utama yang saling berelasi:

1.  **`users`**: Data login petugas.
2.  **`members`**: Data anggota perpustakaan.
3.  **`books`**: Katalog buku (termasuk stok & url gambar).
4.  **`loans`**: Tabel transaksi peminjaman (mencatat ID Member, ID Buku, Tgl Pinjam, Tgl Kembali).

<img width="353" height="622" alt="Screenshot 2026-01-12 at 1 11 59 PM" src="https://github.com/user-attachments/assets/1d492b32-7fd0-4eb5-80a4-d2226b70b645" />

---

2. Penjelasan Hubungan (Relasi Garis)
Garis putus-putus dengan simbol wajik (diamond) kecil itu punya arti "One-to-Many".

Members ➔ Loans (One-to-Many)

Baca: "Satu Anggota (members) bisa melakukan BANYAK transaksi peminjaman (loans)."

Logika: Budi (1 orang) bisa meminjam buku hari ini, besok pinjam lagi, minggu depan pinjam lagi. Semua tercatat di loans.

Books ➔ Loans (One-to-Many)

Baca: "Satu Judul Buku (books) bisa muncul di BANYAK riwayat peminjaman (loans)."

Logika: Buku "Harry Potter" bisa dipinjam oleh Budi bulan Januari, lalu dipinjam oleh Siti bulan Februari. Bukunya satu, tapi riwayat peminjamannya banyak.

Users ➔ Loans (One-to-Many)

Baca: "Satu Petugas (users) bisa melayani BANYAK transaksi (loans)."

Logika: Petugas Admin A bisa melayani 100 orang peminjam dalam sehari.

---

## 🚀 Cara Instalasi & Menjalankan

1.  **Clone Repository**
    ```bash
    git clone (https://github.com/Mypeepee/TA_Jasonchristopher)
    ```

2.  **Setup Database**
    * Buka XAMPP, start Apache & MySQL.
    * Buat database baru bernama `db_perpustakaan`.
    * Import file `database.sql` (sertakan file sql kamu di repo) ke dalam database tersebut.

3.  **Konfigurasi Koneksi**
    * Buka file `src/com/perpustakaan/util/DatabaseConnection.java`.
    * Pastikan username/password database sesuai dengan XAMPP kamu (Default: `root` / kosong).

4.  **Run Application**
    * Jalankan file `Main.java`.

---

## 🧪 Testing

Pengujian dilakukan menggunakan **JUnit 5** dan **Black Box Testing** untuk memastikan validasi logika bisnis:

| ID Test | Skenario | Hasil |
| :--- | :--- | :--- |
| UT-01 | Login Valid (Admin/Member) | ✅ PASS |
| UT-02 | Login Invalid | ✅ PASS |
| UT-03 | Peminjaman (Stok Ada) | ✅ PASS |
| UT-04 | Peminjaman (Stok Habis) | ✅ PASS |
| UT-05 | Pengembalian Buku | ✅ PASS |

---

## 🔐 Akun Demo (Untuk Pengujian)

Gunakan akun berikut untuk mencoba aplikasi:

| Role | Username / Email | Password |
| :--- | :--- | :--- |
| **Admin** | `petugas` | `12345` |
| **Member** | `ahmad@gmail.com` | `12345` |

---

## 📸 Screenshots

### 1. Halaman Login
<img width="794" height="594" alt="Screenshot 2026-01-12 at 1 06 27 PM" src="https://github.com/user-attachments/assets/36da2ff1-93fa-440d-a84b-aa342f9c0e00" />

### 2. Dashboard Admin
<img width="1093" height="692" alt="Screenshot 2026-01-12 at 1 06 44 PM" src="https://github.com/user-attachments/assets/a5d63a29-d104-4697-afe2-6f7e61127238" />

### 3. Katalog & Peminjaman (Member)
<img width="1427" height="757" alt="Screenshot 2026-01-12 at 1 07 09 PM" src="https://github.com/user-attachments/assets/33f3103e-2056-4f38-b210-8d8c29d8a640" />

---

**Developed by [jason christopher liendo] - Sistem Informasi Bisnis 2026**
