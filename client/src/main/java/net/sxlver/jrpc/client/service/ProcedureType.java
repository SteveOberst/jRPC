package net.sxlver.jrpc.client.service;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public interface ProcedureType {

    boolean checkPrerequisites(final Method method);

    @NotNull Procedure parse(final ServiceDefinition service, final Method method);
}
