package org.vivias.domain.casierie;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.vivias.domain.casierie.Suma.Moneda;

/**
 * Value Object Custom Data Type (slide 25, T2.C4: @Convert..@Converter):
 * Moneda se persista prin codul ISO 4217 (ex: "RON"), nu prin ordinalul enum-ului,
 * astfel ca reordonarea constantelor din enum nu corupe datele salvate.
 */
@Converter
public class MonedaConverter implements AttributeConverter<Moneda, String> {

	@Override
	public String convertToDatabaseColumn(Moneda moneda) {
		return moneda == null ? null : moneda.name();
	}

	@Override
	public Moneda convertToEntityAttribute(String codIso) {
		return codIso == null ? null : Moneda.valueOf(codIso);
	}
}
