package org.vivias.domain.services;

import java.util.Collection;
import java.util.List;

import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;

public interface IConsolidatingRegistruCasaSumarViewDomainService {

	RegistruCasaSumarView getRegistruCasaSumarViewDataOf(RegistruCasa registru);

	List<RegistruCasaSumarView> getRegistruCasaSumarViewDataOf(Collection<RegistruCasa> registre);
}
