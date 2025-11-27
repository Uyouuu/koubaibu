package com.koubaibu.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * メインレイアウト
 * Main Layout with navigation
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("購買部在庫管理");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        Nav nav = new Nav();
        nav.addClassNames(LumoUtility.Padding.SMALL);

        VerticalLayout navLinks = new VerticalLayout();
        navLinks.setSpacing(false);
        navLinks.setPadding(false);

        // 在庫管理リンク
        RouterLink inventoryLink = createNavLink("在庫管理", VaadinIcon.STORAGE, InventoryView.class);
        navLinks.add(inventoryLink);

        // 商品管理リンク
        RouterLink productLink = createNavLink("商品管理", VaadinIcon.PACKAGE, ProductManagementView.class);
        navLinks.add(productLink);

        // アラート設定リンク
        RouterLink alertLink = createNavLink("アラート設定", VaadinIcon.BELL, AlertSettingsView.class);
        navLinks.add(alertLink);

        nav.add(navLinks);
        addToDrawer(nav);
    }

    private RouterLink createNavLink(String text, VaadinIcon iconType, Class<?> viewClass) {
        Icon icon = new Icon(iconType);
        icon.addClassNames(LumoUtility.Margin.Right.SMALL);
        
        Span label = new Span(text);
        
        HorizontalLayout content = new HorizontalLayout(icon, label);
        content.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.addClassNames(
            LumoUtility.Padding.Vertical.SMALL,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        RouterLink link = new RouterLink();
        link.add(content);
        link.setRoute((Class<? extends com.vaadin.flow.component.Component>) viewClass);
        link.addClassNames(
            LumoUtility.Display.BLOCK,
            LumoUtility.TextColor.BODY
        );
        
        return link;
    }
}
