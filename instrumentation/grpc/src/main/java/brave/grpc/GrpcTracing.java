package brave.grpc;

import brave.ErrorParser;
import brave.Tracing;
import com.google.auto.value.AutoValue;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;

@AutoValue
public abstract class GrpcTracing {
  public static GrpcTracing create(Tracing tracing) {
    return newBuilder(tracing).build();
  }

  public static Builder newBuilder(Tracing tracing) {
    ErrorParser errorParser = tracing.errorParser();
    return new AutoValue_GrpcTracing.Builder().tracing(tracing)
        // override to re-use any custom error parser from the tracing component
        .clientParser(new GrpcClientParser() {
          @Override protected ErrorParser errorParser() {
            return errorParser;
          }
        })
        .serverParser(new GrpcServerParser() {
          @Override protected ErrorParser errorParser() {
            return errorParser;
          }
        });
  }

  abstract Tracing tracing();

  abstract GrpcClientParser clientParser();

  abstract GrpcServerParser serverParser();

  public abstract Builder toBuilder();

  /** This interceptor traces outbound calls */
  public final ClientInterceptor newClientInterceptor() {
    return new TracingClientInterceptor(this);
  }

  /** This interceptor traces inbound calls */
  public ServerInterceptor newServerInterceptor() {
    return new TracingServerInterceptor(this);
  }

  @AutoValue.Builder public static abstract class Builder {
    abstract Builder tracing(Tracing tracing);

    public abstract Builder clientParser(GrpcClientParser clientParser);

    public abstract Builder serverParser(GrpcServerParser serverParser);

    public abstract GrpcTracing build();

    Builder() {
    }
  }

  GrpcTracing() { // intentionally hidden constructor
  }
}
