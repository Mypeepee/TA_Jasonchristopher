package com.perpustakaan.util;

public class UserSession {
    private static UserSession instance;

    private int userId;
    private String username;
    private String namaLengkap;
    private String role;

    private UserSession(int userId, String username, String namaLengkap, String role) {
        this.userId = userId;
        this.username = username;
        this.namaLengkap = namaLengkap;
        this.role = role;
    }

    public static void setSession(int userId, String username, String namaLengkap, String role) {
        instance = new UserSession(userId, username, namaLengkap, role);
    }

    public static UserSession getSession() {
        return instance;
    }

    public String getNamaLengkap() { return namaLengkap; }
    public String getRole() { return role; }

    public static void cleanSession() {
        instance = null;
    }
}