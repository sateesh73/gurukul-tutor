package com.tutor.gurukul.company.internal;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity that represents a Company in the application domain.
 *
 * <p>Mapping and behavior:
 * - Annotated with {@link Entity} so JPA will map instances to a database table.
 * - {@link Table} is present (with no explicit name) which means the default table name
 *   (typically the entity class name) will be used by the JPA provider.
 *
 * <p>Boilerplate generation:
 * - Lombok's {@link Data} generates getters, setters, {@code toString()}, {@code equals()} and
 *   {@code hashCode()} implementations.
 * - {@link NoArgsConstructor} generates a no-argument constructor required by many frameworks.
 * - {@link AllArgsConstructor} generates a constructor with one parameter for each field.
 *
 * <p>Important notes / recommendations:
 * - The primary key field {@code companyId} is annotated with
 *   {@link Id} and {@link GeneratedValue} using {@link GenerationType#IDENTITY}. Most JPA providers
 *   emit numeric values (e.g. {@code Long}) for IDENTITY generation. This field is declared as
 *   {@code String} which may cause a type mismatch at runtime. If you intend numeric auto-generated
 *   IDs, change the type to {@code Long} (or the appropriate numeric type). If you need string-based
 *   IDs, consider using a UUID generation strategy or manually assigning the ID.
 * - The class is package-private (no {@code public} modifier). While JPA entities do not strictly
 *   require a public class, making the entity {@code public} is conventional and avoids visibility
 *   issues when other packages or frameworks need to access the type.
 *
 * Example usage (conceptual):
 * - Persist a new company:
 *   Company c = new Company(null, "Acme Inc", "info@acme.example");
 *   entityManager.persist(c);
 *
 * - After persist, {@code companyId} will be populated by the JPA provider (subject to the ID type
 *   being compatible with the generation strategy).
 */
@Entity
@Table(name = "t_company", schema = "gurukul")
@Data
@AllArgsConstructor
@Builder
class Company {
    /**
     * Primary key of the company entity.
     *
     * <p>Annotated with {@link Id} so JPA recognizes this field as the entity identifier.
     * {@link GeneratedValue} with {@link GenerationType#IDENTITY} delegates ID generation to the
     * database (auto-increment behavior in many RDBMS).
     *
     * <p>Value type: {@code String} in this source. See class-level note about a potential mismatch
     * between {@code GenerationType.IDENTITY} and a non-numeric field type.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id", nullable = false, updatable = false)
    private String companyId;

    /**
     * The human-readable name of the company.
     *
     * <p>By default this will be mapped to a column named "companyName". If you require constraints
     * (e.g. non-null, length limits, unique) or a different column name, add an appropriate
     * {@link Column} annotation (for example {@code @Column(nullable = false, length = 100)}).
     */
    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    /**
     * Contact email for the company.
     *
     * <p>No validation is performed by JPA at this level. Consider adding application-level
     * validation (e.g. Bean Validation {@code @Email} / {@code @NotBlank}) to ensure correct values.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Human-readable description or summary of the company.
     *
     * <p>Persistence / mapping notes:
     * - Currently mapped to a default column named "description". If you expect long text, consider
     *   annotating with {@code @Column(length = ...)} or {@code @Lob} to allow larger values:
     *   {@code @Column(length = 2000)} or {@code @Lob}.
     *
     * <p>Validation suggestions:
     * - Use Bean Validation to enforce length or presence when appropriate, e.g.
     *   {@code @Size(max = 2000)} or {@code @NotBlank} depending on business rules.
     *
     * <p>Example:
     * - "A remote-first edtech company that builds interactive tutoring tools."
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Public website URL for the company.
     *
     * <p>Persistence / mapping notes:
     * - Defaults to a column named "websiteUrl". If you prefer snake_case or a constrained length,
     *   annotate with {@code @Column(name = "website_url", length = 255)}.
     *
     * <p>Validation suggestions:
     * - Validate as a URL using a validator such as Hibernate Validator's {@code @URL} or a
     *   {@code @Pattern} that enforces a valid URL format. Also consider {@code @Size(max = 255)}.
     *
     * <p>Example:
     * - "https://www.example.com" or "http://company.example.org"
     */
    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    /**
     * URL pointing to the company's logo (CDN / static file path) or a relative path used by the UI.
     *
     * <p>Persistence / mapping notes:
     * - Consider limiting length (e.g. {@code @Column(length = 512)}) to match expected URL lengths.
     *
     * <p>Validation suggestions:
     * - Enforce URL format with {@code @URL} or {@code @Pattern} if the field must contain a valid URI.
     * - Allow null/empty if a logo is optional; otherwise use {@code @NotBlank}.
     *
     * <p>Example:
     * - "https://cdn.example.com/logos/acme-logo.png" or "/static/logos/acme.png"
     */
    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    /**
     * No-argument constructor required by JPA and generated by Lombok's {@link NoArgsConstructor}.
     *
     * <p>JPA requires a no-arg constructor (which can be {@code protected} or {@code public}) to
     * instantiate entities via reflection. Lombok generates this constructor automatically, so no
     * manual implementation is needed.
     */
    public Company() {
    }
}