# Dépendances à ajouter dans CHAQUE microservice (les 9)

Ces manifests supposent que `/actuator/prometheus` est disponible. Ce
n'est pas le cas actuellement : `spring-boot-starter-actuator` est bien
présent dans tes `pom.xml`, mais pas le pont vers Prometheus. Sans cet
ajout, Prometheus obtiendra une erreur 404 sur chaque service.

## 1. Métriques Prometheus

À ajouter dans le `pom.xml` de CHAQUE service (les 9) :

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Et dans `application.yml` de chaque service, exposer l'endpoint :

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, prometheus
  endpoint:
    health:
      probes:
        enabled: true
```

## 2. Traçabilité distribuée (équivalent de Spring Sleuth)

**Note importante** : le document dont tu t'es inspiré mentionne *Spring
Sleuth*, mais ce projet est **abandonné** (déprécié) depuis Spring Boot
3.x — remplacé par **Micrometer Tracing**. Comme SEN-PRIX utilise Spring
Boot 3.3.4, c'est Micrometer Tracing qu'il faut utiliser, pas Sleuth :

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

Cela permet de générer un `traceId` unique propagé automatiquement à
travers les appels REST entre microservices (via les en-têtes HTTP),
visible dans les logs envoyés à Loki.

## 3. Rebuild nécessaire

Après ajout de ces dépendances, il faut reconstruire les images Docker
de chaque service (`docker build`) avant de les redéployer sur le
cluster Kubernetes local.
