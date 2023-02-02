package io.lakefs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class LakeFSInterceptor implements Interceptor {
    private Set<String> publicMethods;

    public LakeFSInterceptor() {
        publicMethods = Arrays.stream(LakeFSFileSystem.class.getDeclaredMethods())
                .map(method -> method.getName())
                .collect(Collectors.toSet());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String methodName = "unknown";
        for (StackTraceElement stackTrace : Thread.currentThread().getStackTrace()) {
            if (stackTrace.getClassName().equals(LakeFSFileSystem.class.getName())
                    && publicMethods.contains(stackTrace.getMethodName())) {
                methodName = stackTrace.getMethodName();
                break;
            }
        }
        Request request = original.newBuilder()
                .header("X-Lakefs-Client",
                        "lakefs-hadoopfs/" + getClass().getPackage().getImplementationVersion() + "/"
                                + Thread.currentThread().getId() + "/" + methodName)
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
