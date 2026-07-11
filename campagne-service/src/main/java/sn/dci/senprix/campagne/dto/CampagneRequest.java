package sn.dci.senprix.campagne.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO reçu en entrée lors de la création ou modification d'une campagne.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampagneRequest {

    @NotBlank(message = "Le nom de la campagne est obligatoire")
    @Size(max = 200)
    private String nom;

    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;
}
