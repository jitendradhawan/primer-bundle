package io.dropwizard.primer.model;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
public class StaticTokenDetails extends PrimerTokenDetails {

    @Builder(builderMethodName = "staticTokenBuilder")
    public StaticTokenDetails(String subject, String role) {
        super(TokenType.STATIC, subject, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticTokenDetails)) return false;
        StaticTokenDetails that = (StaticTokenDetails) o;
        return Objects.equals(getSubject(), that.getSubject())
                && Objects.equals(getRole(), that.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubject(), getRole());
    }
}
