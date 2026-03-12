package dev.proplayer919.chasmic.service;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.entities.CreatureTypeRegistry;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import dev.proplayer919.chasmic.items.ItemActionRegistry;
import dev.proplayer919.chasmic.location.LocationRegistry;
import dev.proplayer919.chasmic.player.friend.FriendManager;
import dev.proplayer919.chasmic.player.punishment.PunishmentManager;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Service container for dependency injection.
 */
@Getter
public class ServiceContainer {
    private static final List<Class<?>> MANAGED_SERVICES = List.of(
            FriendManager.class,
            PunishmentManager.class,
            ItemActionRegistry.class,
            CustomItemRegistry.class,
            AccessoryRegistry.class,
            CreatureTypeRegistry.class,
            LocationRegistry.class
    );

    private final MongoDBHandler mongoDBHandler;
    private final ItemActionRegistry itemActionRegistry;
    private final CustomItemRegistry customItemRegistry;
    private final AccessoryRegistry accessoryRegistry;
    private final CreatureTypeRegistry creatureTypeRegistry;
    private final LocationRegistry locationRegistry;
    private final PunishmentManager punishmentManager;
    private final FriendManager friendManager;

    public ServiceContainer(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = Objects.requireNonNull(mongoDBHandler, "mongoDBHandler");

        Map<Class<?>, Object> initialized = new HashMap<>();
        initialized.put(MongoDBHandler.class, mongoDBHandler);

        List<Class<?>> initOrder = resolveInitializationOrder(new HashSet<>(initialized.keySet()));
        for (Class<?> serviceClass : initOrder) {
            initialized.put(serviceClass, instantiateService(serviceClass, initialized));
        }

        this.friendManager = getService(initialized, FriendManager.class);
        this.punishmentManager = getService(initialized, PunishmentManager.class);
        this.itemActionRegistry = getService(initialized, ItemActionRegistry.class);
        this.customItemRegistry = getService(initialized, CustomItemRegistry.class);
        this.accessoryRegistry = getService(initialized, AccessoryRegistry.class);
        this.creatureTypeRegistry = getService(initialized, CreatureTypeRegistry.class);
        this.locationRegistry = getService(initialized, LocationRegistry.class);
    }

    private static List<Class<?>> resolveInitializationOrder(Set<Class<?>> providedDependencies) {
        Map<Class<?>, Integer> indegree = new HashMap<>();
        Map<Class<?>, List<Class<?>>> edges = new HashMap<>();

        for (Class<?> serviceClass : MANAGED_SERVICES) {
            indegree.put(serviceClass, 0);
            edges.put(serviceClass, new ArrayList<>());
        }

        Set<Class<?>> managedSet = new HashSet<>(MANAGED_SERVICES);

        for (Class<?> serviceClass : MANAGED_SERVICES) {
            for (Class<?> dependency : getDependencies(serviceClass)) {
                if (serviceClass.equals(dependency)) {
                    throw new ServiceInitializationException("Service '" + serviceClass.getSimpleName() + "' cannot depend on itself");
                }

                if (managedSet.contains(dependency)) {
                    edges.get(dependency).add(serviceClass);
                    indegree.put(serviceClass, indegree.get(serviceClass) + 1);
                } else if (!providedDependencies.contains(dependency)) {
                    throw new ServiceInitializationException(
                            "Unsatisfied dependency for service '" + serviceClass.getSimpleName() + "': " + dependency.getSimpleName()
                    );
                }
            }
        }

        ArrayDeque<Class<?>> queue = new ArrayDeque<>();
        MANAGED_SERVICES.stream()
                .filter(service -> indegree.get(service) == 0)
                .sorted(Comparator.comparing(Class::getName))
                .forEach(queue::add);

        List<Class<?>> ordered = new ArrayList<>();

        while (!queue.isEmpty()) {
            Class<?> current = queue.removeFirst();
            ordered.add(current);

            List<Class<?>> neighbors = new ArrayList<>(edges.get(current));
            neighbors.sort(Comparator.comparing(Class::getName));

            for (Class<?> neighbor : neighbors) {
                int nextDegree = indegree.get(neighbor) - 1;
                indegree.put(neighbor, nextDegree);
                if (nextDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (ordered.size() != MANAGED_SERVICES.size()) {
            List<String> unresolved = MANAGED_SERVICES.stream()
                    .filter(service -> indegree.get(service) > 0)
                    .map(Class::getSimpleName)
                    .sorted()
                    .toList();
            throw new ServiceInitializationException("Cycle detected in service dependencies: " + String.join(", ", unresolved));
        }

        return ordered;
    }

    private static List<Class<?>> getDependencies(Class<?> serviceClass) {
        ServiceDependencies dependencies = serviceClass.getAnnotation(ServiceDependencies.class);
        if (dependencies == null || dependencies.value().length == 0) {
            return List.of();
        }
        return List.of(dependencies.value());
    }

    private static Object instantiateService(Class<?> serviceClass, Map<Class<?>, Object> initialized) {
        Constructor<?>[] constructors = serviceClass.getConstructors();
        if (constructors.length != 1) {
            throw new ServiceInitializationException(
                    "Service '" + serviceClass.getSimpleName() + "' must have exactly one public constructor"
            );
        }

        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> dependencyType = parameterTypes[i];
            Object dependency = initialized.get(dependencyType);
            if (dependency == null) {
                throw new ServiceInitializationException(
                        "Cannot initialize service '" + serviceClass.getSimpleName() + "': missing constructor dependency "
                                + dependencyType.getSimpleName()
                );
            }
            args[i] = dependency;
        }

        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServiceInitializationException("Failed to initialize service '" + serviceClass.getSimpleName() + "'", e);
        }
    }

    private static <T> T getService(Map<Class<?>, Object> initialized, Class<T> type) {
        Object service = initialized.get(type);
        if (service == null) {
            throw new ServiceInitializationException("Service not initialized: " + type.getSimpleName());
        }
        return type.cast(service);
    }
}