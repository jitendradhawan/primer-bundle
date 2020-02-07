/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dropwizard.primer.auth.filter;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.dropwizard.primer.auth.AuthFilter;
import io.dropwizard.primer.auth.AuthType;
import io.dropwizard.primer.auth.PrimerAuthorizationRegistry;
import io.dropwizard.primer.core.PrimerError;
import io.dropwizard.primer.exception.PrimerException;
import io.dropwizard.primer.model.PrimerBundleConfiguration;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletionException;


/**
 * @author phaneesh
 */
@Slf4j
@Provider
@Priority(Priorities.AUTHENTICATION)
@Singleton
public class PrimerAuthConfigFilter extends AuthFilter {

  private final SecretKeySpec secretKeySpec;

  private final GCMParameterSpec ivParameterSpec;

  @Builder
  public PrimerAuthConfigFilter(final PrimerBundleConfiguration configuration, final ObjectMapper objectMapper,
                                final SecretKeySpec secretKeySpec, final GCMParameterSpec ivParameterSpec) {
    super(AuthType.CONFIG, configuration, objectMapper);
    this.secretKeySpec = secretKeySpec;
    this.ivParameterSpec = ivParameterSpec;
  }

  @Override
  @Metered(name = "primer")
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // Do not proceed further with Auth if its disabled or whitelisted
    if (!isEnabled() || isWhitelisted(requestContext))
      return;

    Optional<String> token = getToken(requestContext);
    if (!token.isPresent()) {
      requestContext.abortWith(
          Response.status(configuration.getAbsentTokenStatus())
              .entity(objectMapper.writeValueAsBytes(PrimerError.builder().errorCode("PR000").message("Bad request")
                  .build())).build()
      );
    } else {
      try {
        final String decryptedToken = tokenDecrypt(token.get());
        JsonWebToken webToken = authorize(requestContext, decryptedToken, this.authType);
        //Stamp authorization headers for downstream services which can
        // use this to stop token forgery & misuse
        stampHeaders(requestContext, webToken);
      } catch (UncheckedExecutionException e) {
        if (e.getCause() instanceof CompletionException) {
          handleException(e.getCause().getCause(), requestContext, token.get());
        } else {
          handleException(e.getCause(), requestContext, token.get());
        }
      } catch (Exception e) {
        if (e.getCause() instanceof PrimerException) {
          handleException(e.getCause(), requestContext, token.get());
        } else {
          handleException(e, requestContext, token.get());
        }
      }
    }
  }

  private boolean isEnabled() {
    return configuration.isEnabled()
        && configuration.getAuthTypesEnabled().getOrDefault(AuthType.CONFIG, false);
  }

  private boolean isWhitelisted(ContainerRequestContext requestContext) {
    //Short circuit for all white listed urls
    return PrimerAuthorizationRegistry.isWhilisted(requestContext.getUriInfo().getPath());
  }

  public String tokenDecrypt(final String token) {
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(token)));
    } catch (Exception e) {
      return token;
    }
  }
}
