package io.dropwizard.primer.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "DYNAMIC", value = DynamicTokenDetails.class),
        @JsonSubTypes.Type(name = "STATIC", value = StaticTokenDetails.class)

})
public abstract class PrimerTokenDetails {

    private TokenType type;

    private String subject;

    private String role;

    public enum TokenType {
        DYNAMIC, STATIC
    }
}
