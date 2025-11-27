package com.koubaibu.view;

import com.koubaibu.dto.StockChangeRequest;
import com.koubaibu.entity.Product;
import com.koubaibu.service.AlertService;
import com.koubaibu.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.UUID;

/**
 * 在庫管理画面
 * Inventory Management View
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("在庫管理 | 購買部")
public class InventoryView extends VerticalLayout {

    private final ProductService productService;
    private final AlertService alertService;
    private final Grid<Product> grid;
    private final TextField searchField;

    public InventoryView(ProductService productService, AlertService alertService) {
        this.productService = productService;
        this.alertService = alertService;

        addClassName("inventory-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // ヘッダー
        H2 title = new H2("在庫管理");
        
        // 検索フィールド
        searchField = new TextField();
        searchField.setPlaceholder("商品名で検索...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateGrid());

        // ナビゲーションボタン
        Button productManagementButton = new Button("商品管理", new Icon(VaadinIcon.PACKAGE));
        productManagementButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(ProductManagementView.class)));
        
        Button alertSettingsButton = new Button("アラート設定", new Icon(VaadinIcon.BELL));
        alertSettingsButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(AlertSettingsView.class)));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, productManagementButton, alertSettingsButton);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        // 在庫グリッド
        grid = createGrid();

        add(title, toolbar, grid);
        updateGrid();
    }

    private Grid<Product> createGrid() {
        Grid<Product> grid = new Grid<>(Product.class, false);
        grid.setSizeFull();
        
        grid.addColumn(Product::getId)
            .setHeader("ID")
            .setWidth("80px")
            .setFlexGrow(0);
        
        grid.addColumn(Product::getName)
            .setHeader("商品名")
            .setAutoWidth(true);
        
        grid.addColumn(product -> String.format("¥%.0f", product.getPrice()))
            .setHeader("価格")
            .setWidth("120px")
            .setFlexGrow(0);
        
        grid.addComponentColumn(product -> createStockDisplay(product))
            .setHeader("在庫数")
            .setWidth("250px")
            .setFlexGrow(0);
        
        return grid;
    }

    private HorizontalLayout createStockDisplay(Product product) {
        int threshold = alertService.getAlertSettings().getThreshold();
        
        // 減少ボタン
        Button decreaseButton = new Button(new Icon(VaadinIcon.MINUS));
        decreaseButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        decreaseButton.setEnabled(product.getStockQuantity() > 0);
        decreaseButton.addClickListener(e -> showStockChangeDialog(product, false));

        // 在庫数表示
        Span stockSpan = new Span(String.valueOf(product.getStockQuantity()));
        stockSpan.getStyle().set("min-width", "40px");
        stockSpan.getStyle().set("text-align", "center");
        
        // 閾値以下の場合は警告色
        if (product.getStockQuantity() <= threshold) {
            stockSpan.getStyle().set("color", "var(--lumo-error-color)");
            stockSpan.getStyle().set("font-weight", "bold");
        }

        // 増加ボタン
        Button increaseButton = new Button(new Icon(VaadinIcon.PLUS));
        increaseButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        increaseButton.addClickListener(e -> showStockChangeDialog(product, true));

        HorizontalLayout layout = new HorizontalLayout(decreaseButton, stockSpan, increaseButton);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setSpacing(true);
        
        return layout;
    }

    private void showStockChangeDialog(Product product, boolean isIncrease) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isIncrease ? "在庫追加" : "在庫削減");
        
        TextField deltaField = new TextField("数量");
        deltaField.setValue("1");
        deltaField.setPattern("[0-9]+");
        deltaField.setHelperText("1〜1000の範囲で入力");
        
        TextField operatorField = new TextField("操作担当者名");
        operatorField.setRequired(true);
        operatorField.setHelperText("1〜64文字で入力");
        
        VerticalLayout content = new VerticalLayout(deltaField, operatorField);
        content.setSpacing(true);
        dialog.add(content);
        
        Button confirmButton = new Button(isIncrease ? "追加" : "削減", e -> {
            try {
                int delta = Integer.parseInt(deltaField.getValue());
                String operator = operatorField.getValue();
                
                if (operator == null || operator.trim().isEmpty()) {
                    Notification.show("操作担当者名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                
                if (delta < 1 || delta > 1000) {
                    Notification.show("数量は1〜1000の範囲で入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                
                StockChangeRequest request = new StockChangeRequest(delta, operator.trim());
                String requestId = UUID.randomUUID().toString();
                
                if (isIncrease) {
                    productService.increaseStock(product.getId(), request, requestId);
                    Notification.show("在庫を追加しました", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    productService.decreaseStock(product.getId(), request, requestId);
                    Notification.show("在庫を削減しました", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                
                dialog.close();
                updateGrid();
                
            } catch (NumberFormatException ex) {
                Notification.show("数量は数字で入力してください", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("キャンセル", e -> dialog.close());
        
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    private void updateGrid() {
        String keyword = searchField.getValue();
        List<Product> products;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword, null).getContent();
        } else {
            products = productService.getAllProducts();
        }
        
        grid.setItems(products);
    }
}
