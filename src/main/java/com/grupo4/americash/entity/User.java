package com.grupo4.americash.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import java.util.Set;

    @Entity
    @Table(name = "users")
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String email;

        @Column(unique = true, nullable = false)
        private String username;

        @Column(nullable = false)
        private String password;

        @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
        @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
        @Enumerated(EnumType.STRING)
        private Set<Role> roles;
    }