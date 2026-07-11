package sn.dci.senprix.alerte.enums;

/**
 * Niveau de gravité d'une alerte, déterminé automatiquement à la
 * création selon l'ampleur de l'écart entre le prix suspect et la
 * moyenne du marché :
 * - FAIBLE  : écart entre 50% et 100%
 * - MOYENNE : écart entre 100% et 200%
 * - ELEVEE  : écart supérieur à 200%
 */
public enum NiveauGravite {
    FAIBLE,
    MOYENNE,
    ELEVEE
}
