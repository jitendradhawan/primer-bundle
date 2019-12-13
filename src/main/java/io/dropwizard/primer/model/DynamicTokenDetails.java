package io.dropwizard.primer.model;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
public class DynamicTokenDetails extends PrimerTokenDetails {

    private String id;

    private String name;

    @Builder(builderMethodName = "dynamicTokenBuilder")
    public DynamicTokenDetails(String id, String name, String subject, String role) {
        super(TokenType.DYNAMIC, subject, role);
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicTokenDetails)) return false;
        DynamicTokenDetails that = (DynamicTokenDetails) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(getSubject(), that.getSubject())
                && Objects.equals(getRole(), that.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, getSubject(), getRole());
    }
}
