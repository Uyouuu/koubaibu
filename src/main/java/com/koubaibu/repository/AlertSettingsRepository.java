package com.koubaibu.repository;

import com.koubaibu.entity.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * アラート設定リポジトリ
 * Alert Settings Repository
 */
@Repository
public interface AlertSettingsRepository extends JpaRepository<AlertSettings, Long> {

    /**
     * デフォルトのアラート設定を取得（ID=1を想定）
     */
    default AlertSettings getDefaultSettings() {
        return findById(1L).orElseGet(() -> {
            AlertSettings settings = new AlertSettings(2);
            return save(settings);
        });
    }
}
