/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *  You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.distribution.repository;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.db.categories.UnitTests;
import org.openlmis.distribution.domain.ChildCoverageLineItem;
import org.openlmis.distribution.domain.OpenedVialLineItem;
import org.openlmis.distribution.domain.VaccinationChildCoverage;
import org.openlmis.distribution.repository.mapper.VaccinationCoverageMapper;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTests.class)
public class VaccinationCoverageRepositoryTest {

  @Mock
  private VaccinationCoverageMapper mapper;

  @InjectMocks
  private VaccinationCoverageRepository repository;

  @Test
  public void shouldSaveChildCoverage() throws Exception {
    ChildCoverageLineItem childCoverageLineItem = new ChildCoverageLineItem();
    OpenedVialLineItem openedVialLineItem = new OpenedVialLineItem();
    VaccinationChildCoverage vaccinationChildCoverage = new VaccinationChildCoverage();
    vaccinationChildCoverage.setChildCoverageLineItems(asList(childCoverageLineItem));
    vaccinationChildCoverage.setOpenedVialLineItems(asList(openedVialLineItem));

    repository.saveChildCoverage(vaccinationChildCoverage);

    verify(mapper).insertChildCoverageLineItem(childCoverageLineItem);
    verify(mapper).insertOpenedVialLineItem(openedVialLineItem);
  }

  @Test
  public void shouldUpdateChildCoverageIfIdExists() throws Exception {
    ChildCoverageLineItem childCoverageLineItem = new ChildCoverageLineItem();
    childCoverageLineItem.setId(12345L);
    OpenedVialLineItem openedVialLineItem = new OpenedVialLineItem();
    openedVialLineItem.setId(2345L);
    VaccinationChildCoverage vaccinationChildCoverage = new VaccinationChildCoverage();
    vaccinationChildCoverage.setChildCoverageLineItems(asList(childCoverageLineItem));
    vaccinationChildCoverage.setOpenedVialLineItems(asList(openedVialLineItem));

    repository.saveChildCoverage(vaccinationChildCoverage);

    verify(mapper).updateChildCoverageLineItem(childCoverageLineItem);
    verify(mapper).updateOpenedVialLineItem(openedVialLineItem);
  }
}
