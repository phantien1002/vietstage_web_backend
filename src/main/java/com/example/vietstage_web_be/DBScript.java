package com.example.vietstage_web_be;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBScript {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String user = "postgres.kzjdtnyxhnpqsfdprvrv";
        String pass = "1000Vietstage";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE learner_profiles ADD COLUMN IF NOT EXISTS total_points INTEGER DEFAULT 0");
            stmt.executeUpdate("ALTER TABLE learner_profiles ADD COLUMN IF NOT EXISTS total_stars INTEGER DEFAULT 0");
            System.out.println("Alter table success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}