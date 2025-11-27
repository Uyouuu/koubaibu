package com.koubaibu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 研究室購買部在庫管理システム - メインアプリケーション
 * Laboratory Purchasing Department Inventory Management System
 */
@SpringBootApplication
@EnableScheduling
public class KoubaibuApplication {

    public static void main(String[] args) {
        SpringApplication.run(KoubaibuApplication.class, args);
    }
}
