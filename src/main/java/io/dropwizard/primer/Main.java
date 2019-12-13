package io.dropwizard.primer;

import io.dropwizard.primer.model.DynamicTokenDetails;
import io.dropwizard.primer.model.PrimerTokenDetails;
import io.dropwizard.primer.model.StaticTokenDetails;

public class Main {

    public static void main(String[] args) {
        DynamicTokenDetails dynamicTokenDetails = DynamicTokenDetails.dynamicTokenBuilder()
                .id("123")
                .name("ABCD")
                .subject("SUBJECT")
                .role("ROLE")
                .build();

        DynamicTokenDetails dynamicTokenDetails1 = DynamicTokenDetails.dynamicTokenBuilder()
                .id("123")
                .name("ABCD")
                .subject("SUBJECT")
                .role("ROLE")
                .build();

        StaticTokenDetails staticTokenDetails = StaticTokenDetails.staticTokenBuilder()
                .role("ROLE")
                .subject("SUBJECT")
                .build();

        boolean equals = compare(dynamicTokenDetails, dynamicTokenDetails1);
        System.out.println(equals);
    }

    private static boolean compare(PrimerTokenDetails primerTokenDetails1, PrimerTokenDetails primerTokenDetails2) {
        return primerTokenDetails1.equals(primerTokenDetails2);
    }
}
