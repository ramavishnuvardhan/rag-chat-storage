package com.rag.chat.config;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import com.rag.chat.config.model.ApiKeyAuth;

import jakarta.servlet.http.HttpServletRequest;

//impo
@Component
public class ApiKeyAuthExtractor {

 @Value("${RAG_API_KEY}")
 private String apiKey;

 public Optional<Authentication> extract(HttpServletRequest request) {
     String providedKey = request.getHeader("ApiKey");
     if (providedKey == null || !providedKey.equals(apiKey))
         return Optional.empty();

     return Optional.of(new ApiKeyAuth(providedKey, AuthorityUtils.NO_AUTHORITIES));
 }

}
