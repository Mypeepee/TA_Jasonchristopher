package com.perpustakaan;

public class Book {
    private int id;
    private String judul;
    private String penulis;
    private String penerbit;
    private int stok;
    private String kategori;
    private String gambarUrl; // Field Baru

    // Constructor Diupdate (Nambah parameter gambarUrl)
    public Book(int id, String judul, String penulis, String penerbit, int stok, String kategori, String gambarUrl) {
        this.id = id;
        this.judul = judul;
        this.penulis = penulis;
        this.penerbit = penerbit;
        this.stok = stok;
        this.kategori = kategori;
        this.gambarUrl = gambarUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getPenulis() { return penulis; }
    public String getPenerbit() { return penerbit; }
    public int getStok() { return stok; }
    public String getKategori() { return kategori; }

    // Getter Baru
    public String getGambarUrl() { return gambarUrl; }
}