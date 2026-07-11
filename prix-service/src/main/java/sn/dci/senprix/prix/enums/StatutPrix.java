package sn.dci.senprix.prix.enums;

/**
 * Statut de modération d'un relevé de prix.
 * Un prix est VALIDE par défaut ; il peut être marqué SUSPECT
 * automatiquement (variation anormale détectée) ou par un agent
 * de collecte, puis REJETE après vérification par un ADMIN.
 */
public enum StatutPrix {
    VALIDE,
    SUSPECT,
    REJETE
}
