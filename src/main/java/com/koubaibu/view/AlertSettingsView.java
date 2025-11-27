package com.koubaibu.view;

import com.koubaibu.dto.AlertHistoryResponse;
import com.koubaibu.dto.AlertSettingsRequest;
import com.koubaibu.entity.AlertHistory;
import com.koubaibu.entity.AlertSettings;
import com.koubaibu.service.AlertService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * アラート設定画面
 * Alert Settings View
 */
@Route(value = "alerts", layout = MainLayout.class)
@PageTitle("アラート設定 | 購買部")
public class AlertSettingsView extends VerticalLayout {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AlertService alertService;
    private final IntegerField thresholdField;
    private final TextField operatorField;
    private final Grid<AlertHistory> historyGrid;

    public AlertSettingsView(AlertService alertService) {
        this.alertService = alertService;

        addClassName("alert-settings-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // ヘッダー
        H2 title = new H2("アラート設定");

        // 戻るボタン
        Button backButton = new Button("在庫管理へ戻る", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(InventoryView.class)));

        HorizontalLayout toolbar = new HorizontalLayout(backButton);

        // 閾値設定フォーム
        H3 settingsTitle = new H3("閾値設定");
        
        AlertSettings currentSettings = alertService.getAlertSettings();
        
        thresholdField = new IntegerField("アラート閾値");
        thresholdField.setValue(currentSettings.getThreshold());
        thresholdField.setMin(0);
        thresholdField.setMax(999);
        thresholdField.setStepButtonsVisible(true);
        thresholdField.setHelperText("在庫数がこの値以下になるとアラートが送信されます（0〜999）");

        operatorField = new TextField("操作担当者名");
        operatorField.setRequired(true);
        operatorField.setHelperText("設定変更の担当者名を入力してください");

        Button saveButton = new Button("設定を保存", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveSettings());

        FormLayout settingsForm = new FormLayout(thresholdField, operatorField);
        settingsForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout settingsSection = new VerticalLayout(settingsTitle, settingsForm, saveButton);
        settingsSection.setSpacing(true);
        settingsSection.setPadding(false);

        // アラート履歴セクション
        H3 historyTitle = new H3("アラート履歴");
        
        historyGrid = createHistoryGrid();

        Button refreshButton = new Button("更新", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> updateHistoryGrid());

        HorizontalLayout historyToolbar = new HorizontalLayout(historyTitle, refreshButton);
        historyToolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        VerticalLayout historySection = new VerticalLayout(historyToolbar, historyGrid);
        historySection.setSizeFull();
        historySection.setPadding(false);

        add(title, toolbar, settingsSection, historySection);
        updateHistoryGrid();
    }

    private Grid<AlertHistory> createHistoryGrid() {
        Grid<AlertHistory> grid = new Grid<>(AlertHistory.class, false);
        grid.setHeight("400px");

        grid.addColumn(alertHistory -> alertHistory.getAlertTime().format(DATETIME_FORMATTER))
            .setHeader("発生日時")
            .setWidth("180px")
            .setFlexGrow(0);

        grid.addColumn(alertHistory -> alertHistory.getProduct().getName())
            .setHeader("商品名")
            .setAutoWidth(true);

        grid.addColumn(AlertHistory::getMessage)
            .setHeader("メッセージ")
            .setAutoWidth(true);

        grid.addComponentColumn(alertHistory -> createStatusBadge(alertHistory.getStatus()))
            .setHeader("ステータス")
            .setWidth("120px")
            .setFlexGrow(0);

        grid.addColumn(AlertHistory::getOperatorName)
            .setHeader("操作者")
            .setWidth("120px")
            .setFlexGrow(0);

        return grid;
    }

    private Span createStatusBadge(AlertHistory.Status status) {
        Span badge = new Span(status.name());
        badge.getElement().getThemeList().add("badge");
        
        switch (status) {
            case SENT:
                badge.getElement().getThemeList().add("success");
                break;
            case FAILED:
                badge.getElement().getThemeList().add("error");
                break;
            case SUPPRESSED:
                badge.getElement().getThemeList().add("contrast");
                break;
        }
        
        return badge;
    }

    private void saveSettings() {
        Integer threshold = thresholdField.getValue();
        String operator = operatorField.getValue();

        if (threshold == null || threshold < 0 || threshold > 999) {
            Notification.show("閾値は0〜999の範囲で入力してください", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (operator == null || operator.trim().isEmpty()) {
            Notification.show("操作担当者名を入力してください", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            AlertSettingsRequest request = new AlertSettingsRequest(threshold, operator.trim());
            String requestId = UUID.randomUUID().toString();
            
            alertService.updateAlertSettings(request, requestId);
            
            Notification.show("アラート設定を更新しました", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            operatorField.clear();

        } catch (Exception e) {
            Notification.show("設定の更新に失敗しました: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateHistoryGrid() {
        Page<AlertHistory> historyPage = alertService.getAlertHistory(PageRequest.of(0, 100));
        historyGrid.setItems(historyPage.getContent());
    }
}
