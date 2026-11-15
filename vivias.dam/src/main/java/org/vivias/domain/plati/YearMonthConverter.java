package org.vivias.domain.plati;

import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Custom Data Type: luna statului de plata se salveaza ca text "2026-11"
@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

	@Override
	public String convertToDatabaseColumn(YearMonth luna) {
		return luna == null ? null : luna.toString();
	}

	@Override
	public YearMonth convertToEntityAttribute(String text) {
		return text == null ? null : YearMonth.parse(text);
	}
}
