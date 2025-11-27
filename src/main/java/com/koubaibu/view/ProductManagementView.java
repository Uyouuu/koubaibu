package com.koubaibu.view;

import com.koubaibu.dto.ProductCreateRequest;
import com.koubaibu.dto.ProductUpdateRequest;
import com.koubaibu.entity.Product;
import com.koubaibu.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 商品管理画面
 * Product Management View
 */
@Route(value = "products", layout = MainLayout.class)
@PageTitle("商品管理 | 購買部")
public class ProductManagementView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid;

    public ProductManagementView(ProductService productService) {
        this.productService = productService;

        addClassName("product-management-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // ヘッダー
        H2 title = new H2("商品管理");

        // 戻るボタン
        Button backButton = new Button("在庫管理へ戻る", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(InventoryView.class)));

        // 新規追加ボタン
        Button addButton = new Button("新規商品追加", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> showAddDialog());

        HorizontalLayout toolbar = new HorizontalLayout(backButton, addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // 商品グリッド
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

        grid.addColumn(Product::getStockQuantity)
            .setHeader("在庫数")
            .setWidth("100px")
            .setFlexGrow(0);

        grid.addComponentColumn(product -> createActionButtons(product))
            .setHeader("操作")
            .setWidth("200px")
            .setFlexGrow(0);

        return grid;
    }

    private HorizontalLayout createActionButtons(Product product) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("編集");
        editButton.addClickListener(e -> showEditDialog(product));

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.setTooltipText("削除");
        deleteButton.addClickListener(e -> showDeleteConfirmDialog(product));

        HorizontalLayout layout = new HorizontalLayout(editButton, deleteButton);
        layout.setSpacing(true);
        return layout;
    }

    private void showAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("新規商品追加");
        dialog.setWidth("400px");

        TextField nameField = new TextField("商品名");
        nameField.setRequired(true);
        nameField.setWidthFull();

        NumberField priceField = new NumberField("価格");
        priceField.setRequired(true);
        priceField.setMin(0);
        priceField.setStep(1);
        priceField.setWidthFull();

        NumberField stockField = new NumberField("初期在庫数");
        stockField.setRequired(true);
        stockField.setMin(0);
        stockField.setStep(1);
        stockField.setValue(0.0);
        stockField.setWidthFull();

        TextField operatorField = new TextField("操作担当者名");
        operatorField.setRequired(true);
        operatorField.setWidthFull();

        FormLayout form = new FormLayout(nameField, priceField, stockField, operatorField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        dialog.add(form);

        Button saveButton = new Button("追加", e -> {
            try {
                String name = nameField.getValue();
                Double price = priceField.getValue();
                Double stock = stockField.getValue();
                String operator = operatorField.getValue();

                if (name == null || name.trim().isEmpty()) {
                    Notification.show("商品名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (price == null || price < 0) {
                    Notification.show("価格を正しく入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (stock == null || stock < 0) {
                    Notification.show("初期在庫数を正しく入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (operator == null || operator.trim().isEmpty()) {
                    Notification.show("操作担当者名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                ProductCreateRequest request = new ProductCreateRequest(
                    name.trim(),
                    BigDecimal.valueOf(price),
                    stock.intValue(),
                    operator.trim()
                );
                String requestId = UUID.randomUUID().toString();

                productService.createProduct(request, requestId);
                
                Notification.show("商品を追加しました", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                dialog.close();
                updateGrid();

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("キャンセル", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void showEditDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("商品編集");
        dialog.setWidth("400px");

        TextField nameField = new TextField("商品名");
        nameField.setValue(product.getName());
        nameField.setRequired(true);
        nameField.setWidthFull();

        NumberField priceField = new NumberField("価格");
        priceField.setValue(product.getPrice().doubleValue());
        priceField.setRequired(true);
        priceField.setMin(0);
        priceField.setStep(1);
        priceField.setWidthFull();

        Span stockInfo = new Span("現在の在庫数: " + product.getStockQuantity());

        TextField operatorField = new TextField("操作担当者名");
        operatorField.setRequired(true);
        operatorField.setWidthFull();

        FormLayout form = new FormLayout(nameField, priceField, stockInfo, operatorField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        dialog.add(form);

        Button saveButton = new Button("保存", e -> {
            try {
                String name = nameField.getValue();
                Double price = priceField.getValue();
                String operator = operatorField.getValue();

                if (name == null || name.trim().isEmpty()) {
                    Notification.show("商品名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (price == null || price < 0) {
                    Notification.show("価格を正しく入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (operator == null || operator.trim().isEmpty()) {
                    Notification.show("操作担当者名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                ProductUpdateRequest request = new ProductUpdateRequest(
                    name.trim(),
                    BigDecimal.valueOf(price),
                    operator.trim()
                );
                String requestId = UUID.randomUUID().toString();

                productService.updateProduct(product.getId(), request, requestId);
                
                Notification.show("商品を更新しました", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                dialog.close();
                updateGrid();

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("キャンセル", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void showDeleteConfirmDialog(Product product) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("商品削除の確認");
        dialog.setText(String.format(
            "以下の商品を削除しますか？\n\n" +
            "商品名: %s\n" +
            "価格: ¥%.0f\n" +
            "在庫数: %d",
            product.getName(),
            product.getPrice(),
            product.getStockQuantity()
        ));

        // 操作担当者名入力フィールド
        TextField operatorField = new TextField("操作担当者名");
        operatorField.setRequired(true);
        dialog.add(operatorField);

        dialog.setCancelable(true);
        dialog.setCancelText("キャンセル");

        dialog.setConfirmText("削除");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                String operator = operatorField.getValue();
                if (operator == null || operator.trim().isEmpty()) {
                    Notification.show("操作担当者名を入力してください", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                String requestId = UUID.randomUUID().toString();
                productService.deleteProduct(product.getId(), operator.trim(), requestId);
                
                Notification.show("商品を削除しました", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                updateGrid();

            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void updateGrid() {
        grid.setItems(productService.getAllProducts());
    }
}
