package io.jooby.internal.mvc;

import io.jooby.Context;
import io.jooby.Err;
import io.jooby.QueryString;
import io.jooby.Reified;
import io.jooby.Route;
import io.jooby.Value;
import io.jooby.annotations.GET;
import io.jooby.annotations.POST;
import io.jooby.annotations.Path;
import io.jooby.annotations.PathParam;
import io.jooby.annotations.QueryParam;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QPoint {
}

class Poc {

  @POST
  @Path(("/body/json"))
  public String[] getIt(String body) {
    String[] result = new String[1];
    result[0] = body;
    return result;
  }

}

class MvcHandler implements Route.Handler {

  private Provider<Poc> provider;

  public MvcHandler(Provider<Poc> provider) {
    this.provider = provider;
  }

  private String tryParam0(Context ctx, String desc) {
    try {
      return ctx.body(String.class);
    } catch (Err.Provisioning x) {
      throw x;
    } catch (Exception x) {
      throw new Err.Provisioning(desc, x);
    }
  }

  @Nonnull @Override public Object apply(@Nonnull Context ctx) throws Exception {
    Poc target = provider.get();
    return target.getIt(tryParam0(ctx, "body: java.lang.String"));
  }
}

public class MvcHandlerASM {

  @Test
  public void compare() throws IOException, NoSuchMethodException, ClassNotFoundException {
    // Lio/jooby/Throwing$Supplier<Lio/jooby/mvc/NoTopLevelPath;>;
    //    ASMifier.main(new String[] {"-debug",MvcHandler.class.getName()});

    Method handler = Poc.class.getDeclaredMethod("getIt", String.class);
    MvcCompiler writer = new MvcCompiler();
    Class runtime = writer.compileClass(mvc(handler));

    System.out.println("Loaded: " + runtime);
    //    byte[] asm = writer.toByteCode(classname, handler);

    assertEquals(asmifier(new ClassReader(MvcHandler.class.getName())),
        asmifier(new ClassReader(writer.compile(mvc(handler)))));

  }

  private MvcMethod mvc(Method handler) {
    MvcMethod result = new MvcMethod();
    result.method = handler;
    return result;
  }

  private String asmifier(ClassReader reader) throws IOException {
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    Printer printer = new ASMifier();
    TraceClassVisitor traceClassVisitor =
        new TraceClassVisitor(null, printer, new PrintWriter(buff));

    reader.accept(traceClassVisitor, ClassReader.SKIP_DEBUG);

    return new String(buff.toByteArray());
  }

  private String listOf(org.objectweb.asm.Type owner) {
    StringBuilder signature = new StringBuilder(
        org.objectweb.asm.Type.getType(List.class).getDescriptor());
    signature.insert(signature.length() - 1, "<" + owner.getDescriptor() + ">");
    return signature.toString();
  }
}