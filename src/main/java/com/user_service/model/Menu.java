package com.user_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "menu", schema = "code")
@Getter
@Setter
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "menu_key", nullable = false)
    private String menuKey;
    @Column(name = "menu_label", nullable = false)
    private String menuLabel;
    @Column(name = "menu_order", nullable = false)
    private Integer menuOrder;
    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "role_id", nullable = false)
    private Long roleId;
}