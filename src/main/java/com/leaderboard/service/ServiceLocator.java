package com.leaderboard.service;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceLocator pattern implementation to decouple service providers from consumers
 * and manage singletons/instances dynamically.
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new HashMap<>();

    public static synchronized <T> void register(Class<T> serviceClass, T implementation) {
        if (serviceClass == null || implementation == null) {
            return;
        }
        services.put(serviceClass, implementation);
    }

    public static synchronized <T> T get(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service " + serviceClass.getName() + " has not been registered in ServiceLocator!");
        }
        return serviceClass.cast(service);
    }
}
