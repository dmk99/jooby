package io.jooby;

import io.jooby.internal.InMemorySessionStore;
import io.jooby.internal.RequestSessionStore;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Function;

public class SessionOptions {
  private static final SecureRandom secure = new SecureRandom();

  private Function<Context, String> idGenerator = SessionOptions::defaultIdGenerator;

  private Cookie cookie = new Cookie("jooby.sid")
      .setMaxAge(-1)
      .setHttpOnly(true)
      .setPath("/");

  private SessionStore store = new InMemorySessionStore();

  public @Nonnull Cookie getCookie() {
    return cookie.clone();
  }

  public @Nonnull SessionOptions setCookie(@Nonnull Cookie cookie) {
    this.cookie = cookie;
    return this;
  }

  public SessionStore getStore() {
    return new RequestSessionStore(store);
  }

  public void setStore(SessionStore store) {
    this.store = store;
  }

  public Function<Context, String> getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(Function<Context, String> idGenerator) {
    this.idGenerator = idGenerator;
  }

  public @Nonnull String generateId(Context ctx) {
    return idGenerator.apply(ctx);
  }

  private static String defaultIdGenerator(Context ctx) {
    byte[] bytes = new byte[30];
    secure.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

}
